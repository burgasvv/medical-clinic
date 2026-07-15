package org.burgas.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.burgas.dao.PatientEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AuthToken
import org.burgas.dto.PatientRequest
import org.burgas.service.PatientService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configurePatientRouter() {

    val patientService by inject<PatientService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path() == "/api/v1/patients/by-id") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept by id"))
            val patientId = UUID.fromString(call.parameters["patientId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val patientEntity = PatientEntity.findById(patientId)!!.load(PatientEntity::identity)
                if (
                    patientEntity.identity.email == authToken.email ||
                    (authToken.authority == Authority.ADMIN || authToken.authority == Authority.DOCTOR)
                ) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept with parameter")
                }
            }

        } else if (call.request.path() == "/api/v1/patients/update") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept by update"))
            val patientRequest = call.receive<PatientRequest>()

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val patientEntity = PatientEntity.findById(patientRequest.id!!)!!.load(PatientEntity::identity)
                if (patientEntity.identity.email == authToken.email || authToken.authority == Authority.ADMIN) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept with parameter")
                }
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/patients") {

            authenticate(
                "session-auth-admin", "session-auth-doctor",
                strategy = AuthenticationStrategy.FirstSuccessful
            ) {
                get {
                    call.respond(HttpStatusCode.OK, patientService.findAll())
                }
            }

            authenticate("session-auth-admin", optional = true) {

                post("/create") {
                    val authToken = call.sessions.get<AuthToken>()
                    if (authToken != null && authToken.authority == Authority.DOCTOR)
                        throw IllegalArgumentException("Your authority is DOCTOR, you can't create PATIENT")
                    val patientRequest = call.receive<PatientRequest>()
                    patientService.create(patientRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("session-auth") {

                get("/by-id") {
                    val patientId = UUID.fromString(call.parameters["patientId"])
                    call.respond(HttpStatusCode.OK, patientService.findById(patientId))
                }
            }

            authenticate(
                "session-auth-admin", "session-auth-patient",
                strategy = AuthenticationStrategy.FirstSuccessful
            ) {
                put("/update") {
                    val patientRequest = call.receive<PatientRequest>()
                    patientService.update(patientRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("session-auth-admin") {

                delete("/delete") {
                    val patientId = UUID.fromString(call.parameters["patientId"])
                    patientService.delete(patientId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}