package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.burgas.dao.DoctorEntity
import org.burgas.dao.ScheduleEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AuthToken
import org.burgas.dto.ScheduleRequest
import org.burgas.service.ScheduleService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureScheduleRouter() {

    val scheduleService by inject<ScheduleService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path() == "/api/v1/schedules/create") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept by create schedule"))
            val scheduleRequest = call.receive<ScheduleRequest>()

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val doctorEntity = DoctorEntity.findById(scheduleRequest.doctorId!!)!!.load(DoctorEntity::identity)
                if (doctorEntity.identity.email == authToken.email || authToken.authority == Authority.ADMIN) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept by create schedule")
                }
            }

        } else if (call.request.path() == "/api/v1/schedules/update") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept by update schedule"))
            val scheduleRequest = call.receive<ScheduleRequest>()

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val scheduleEntity = ScheduleEntity.findById(scheduleRequest.id!!)!!.load(ScheduleEntity::doctor)
                if (scheduleEntity.doctor.identity.email == authToken.email || authToken.authority == Authority.ADMIN) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept by update schedule")
                }
            }

        } else if (call.request.path() == "/api/v1/schedules/delete") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept by delete schedule"))
            val scheduleId = UUID.fromString(call.parameters["scheduleId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val scheduleEntity = ScheduleEntity.findById(scheduleId)!!.load(ScheduleEntity::doctor)
                if (scheduleEntity.doctor.identity.email == authToken.email || authToken.authority == Authority.ADMIN) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept by delete schedule")
                }
            }

        } else if (call.request.path() == "/api/v1/schedules/by-id") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept schedule by id"))
            val scheduleId = UUID.fromString(call.parameters["scheduleId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val scheduleEntity = ScheduleEntity.findById(scheduleId)!!
                    .load(ScheduleEntity::doctor, ScheduleEntity::appointment)
                when(authToken.authority) {
                    Authority.ADMIN -> {
                        proceed()
                    }
                    Authority.DOCTOR -> {
                        if (scheduleEntity.doctor.identity.email == authToken.email) {
                            proceed()
                        } else {
                            throw IllegalArgumentException("Not authorized intercept schedule by id and by doctor authority")
                        }
                    }
                    Authority.PATIENT -> {
                        if (scheduleEntity.appointment?.patient?.identity?.email == authToken.email) {
                            proceed()
                        } else {
                            throw IllegalArgumentException("Not authorized intercept schedule by id and by patient authority")
                        }
                    }
                    else -> throw IllegalArgumentException("Not authorized intercept schedule wrong authority")
                }
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/schedules") {

            authenticate(
                "session-auth-admin", "session-auth-doctor",
                strategy = AuthenticationStrategy.FirstSuccessful
            ) {
                post("/create") {
                    val scheduleRequest = call.receive<ScheduleRequest>()
                    scheduleService.create(scheduleRequest)
                    call.respond(HttpStatusCode.OK)
                }

                put("/update") {
                    val scheduleRequest = call.receive<ScheduleRequest>()
                    scheduleService.update(scheduleRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val scheduleId = UUID.fromString(call.parameters["scheduleId"])
                    scheduleService.delete(scheduleId)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate(
                "session-auth-admin", "session-auth-doctor", "session-auth-patient",
                strategy = AuthenticationStrategy.FirstSuccessful
            ) {
                get("/by-id") {
                    val scheduleId = UUID.fromString(call.parameters["scheduleId"])
                    call.respond(HttpStatusCode.OK, scheduleService.findById(scheduleId))
                }
            }
        }
    }
}