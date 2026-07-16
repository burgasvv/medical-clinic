package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.burgas.dao.DoctorEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AuthToken
import org.burgas.dto.DoctorRequest
import org.burgas.dto.DoctorServiceRequest
import org.burgas.service.DoctorService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureDoctorRouter() {

    val doctorService by inject<DoctorService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path() == "/api/v1/doctors/update") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept doctors by update"))
            val doctorRequest = call.receive<DoctorRequest>()

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val doctorEntity = DoctorEntity.findById(doctorRequest.id!!)!!.load(DoctorEntity::identity)
                if (
                    doctorEntity.identity.email == authToken.email ||
                    authToken.authority == Authority.ADMIN
                ) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept doctors by update")
                }
            }

        } else if (call.request.path() == "/api/v1/doctors/delete") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept doctors by delete"))
            val doctorId = UUID.fromString(call.parameters["doctorId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val doctorEntity = DoctorEntity.findById(doctorId)!!.load(DoctorEntity::identity)
                if (
                    doctorEntity.identity.email == authToken.email ||
                    authToken.authority == Authority.ADMIN
                ) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept doctors by delete")
                }
            }

        } else if (
            call.request.path() == "/api/v1/doctors/add-image" || call.request.path() == "/api/v1/doctors/remove-image"
        ) {
            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept doctors by add/remove image"))
            val doctorId = UUID.fromString(call.parameters["doctorId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val doctorEntity = DoctorEntity.findById(doctorId)!!.load(DoctorEntity::identity)
                if (doctorEntity.identity.email == authToken.email) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept doctors by add/remove image")
                }
            }

        } else if (
            call.request.path() == "/api/v1/doctors/add-service" || call.request.path() == "/api/v1/doctors/remove-service"
        ) {
            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept doctors by add/remove service"))
            val doctorServiceRequest = call.receive<DoctorServiceRequest>()

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val doctorEntity = DoctorEntity.findById(doctorServiceRequest.doctorId)!!.load(DoctorEntity::identity)
                if (doctorEntity.identity.email == authToken.email) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept doctors by add/remove service")
                }
            }
        }
    }

    routing {

        route("/api/v1/doctors") {

            get {
                call.respond(HttpStatusCode.OK, doctorService.findAll())
            }

            get("/by-id") {
                val doctorId = UUID.fromString(call.parameters["doctorId"])
                call.respond(HttpStatusCode.OK, doctorService.findById(doctorId))
            }

            authenticate("session-auth-admin") {

                post("/create") {
                    val doctorRequest = call.receive<DoctorRequest>()
                    doctorService.create(doctorRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate(
                "session-auth-admin", "session-auth-doctor",
                strategy = AuthenticationStrategy.FirstSuccessful
            ) {
                put("/update") {
                    val doctorRequest = call.receive<DoctorRequest>()
                    doctorService.update(doctorRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val doctorId = UUID.fromString(call.parameters["doctorId"])
                    doctorService.delete(doctorId)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("session-auth-doctor") {

                post("/add-image") {
                    val doctorId = UUID.fromString(call.parameters["doctorId"])
                    doctorService.addImage(doctorId, call.receiveMultipart(Long.MAX_VALUE))
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-image") {
                    val doctorId = UUID.fromString(call.parameters["doctorId"])
                    doctorService.removeImage(doctorId)
                    call.respond(HttpStatusCode.OK)
                }

                put("/add-service") {
                    val doctorServiceRequest = call.receive<DoctorServiceRequest>()
                    doctorService.addService(doctorServiceRequest)
                    call.respond(HttpStatusCode.OK)
                }

                put("/remove-service") {
                    val doctorServiceRequest = call.receive<DoctorServiceRequest>()
                    doctorService.removeService(doctorServiceRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}