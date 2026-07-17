package org.burgas.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.burgas.dao.AppointmentEntity
import org.burgas.dao.PatientEntity
import org.burgas.dao.ScheduleEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AppointmentRequest
import org.burgas.dto.AuthToken
import org.burgas.service.AppointmentService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureAppointmentRouter() {

    val appointmentService by inject<AppointmentService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path() == "/api/v1/appointments/by-id" || call.request.path() == "/api/v1/appointments/delete") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept appointment by id/delete"))
            val appointmentId = UUID.fromString(call.parameters["appointmentId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {

                val appointmentEntity = AppointmentEntity.findById(appointmentId)!!
                    .load(AppointmentEntity::schedule, AppointmentEntity::patient)

                when (authToken.authority) {
                    Authority.ADMIN -> proceed()
                    Authority.DOCTOR -> {
                        if (appointmentEntity.schedule.doctor.identity.email == authToken.email) {
                            proceed()
                        } else {
                            throw IllegalArgumentException("Not authorized intercept appointment, authority DOCTOR, by id/delete")
                        }
                    }
                    Authority.PATIENT -> {
                        if (appointmentEntity.patient.identity.email == authToken.email) {
                            proceed()
                        } else {
                            throw IllegalArgumentException("Not authorized intercept appointment, authority PATIENT, by id/delete")
                        }
                    }
                    else -> throw IllegalArgumentException("Not authorized intercept appointment by id/delete, wrong authority")
                }
            }

        } else if (call.request.path() == "/api/v1/appointments/create") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept appointment by create"))
            val appointmentRequest = call.receive<AppointmentRequest>()

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {

                val scheduleEntity = ScheduleEntity.findById(appointmentRequest.scheduleId)!!
                    .load(ScheduleEntity::doctor)

                val patientEntity = PatientEntity.findById(appointmentRequest.patientId)!!
                    .load(PatientEntity::identity)

                when(authToken.authority) {
                    Authority.ADMIN -> proceed()
                    Authority.DOCTOR -> {
                        if (scheduleEntity.doctor.identity.email == authToken.email) {
                            proceed()
                        } else {
                            throw IllegalArgumentException("Not authorized intercept appointment, authority DOCTOR, by create")
                        }
                    }
                    Authority.PATIENT -> {
                        if (patientEntity.identity.email == authToken.email) {
                            proceed()
                        } else {
                            throw IllegalArgumentException("Not authorized intercept appointment, authority PATIENT, by create")
                        }
                    }
                    else -> throw IllegalArgumentException("Not authorized intercept appointment by create, wrong authority")
                }
            }

        } else if (
            call.request.path() == "/api/v1/appointments/add-document" ||
            call.request.path() == "/api/v1/appointments/remove-document" ||
            call.request.path() == "/api/v1/appointments/conclude"
        ) {
            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept appointment by docs/conclude"))
            val appointmentId = UUID.fromString(call.parameters["appointmentId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val appointmentEntity = AppointmentEntity.findById(appointmentId)!!.load(AppointmentEntity::schedule)

                if (appointmentEntity.schedule.doctor.identity.email == authToken.email) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept appointment by docs/conclude")
                }
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/appointments") {

            authenticate(
                "session-auth-admin", "session-auth-doctor", "session-auth-patient",
                strategy = AuthenticationStrategy.FirstSuccessful
            ) {
                get("/by-id") {
                    val appointmentId = UUID.fromString(call.parameters["appointmentId"])
                    call.respond(HttpStatusCode.OK, appointmentService.findById(appointmentId))
                }

                post("/create") {
                    val appointmentRequest = call.receive<AppointmentRequest>()
                    appointmentService.create(appointmentRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val appointmentId = UUID.fromString(call.parameters["appointmentId"])
                    appointmentService.delete(appointmentId)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("session-auth-doctor") {

                post("/add-document") {
                    val appointmentId = UUID.fromString(call.parameters["appointmentId"])
                    appointmentService.addDocument(appointmentId, call.receiveMultipart(Long.MAX_VALUE))
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-document") {
                    val appointmentId = UUID.fromString(call.parameters["appointmentId"])
                    appointmentService.removeDocument(appointmentId)
                    call.respond(HttpStatusCode.OK)
                }

                put("/conclude") {
                    val appointmentId = UUID.fromString(call.parameters["appointmentId"])
                    appointmentService.conclude(appointmentId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}