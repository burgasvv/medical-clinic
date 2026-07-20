package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.burgas.dao.PaymentEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AuthToken
import org.burgas.dto.PaymentRequest
import org.burgas.service.PaymentService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configurePaymentRouter() {

    val paymentService by inject<PaymentService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path() == "/api/v1/payments/by-id") {

            val authToken = call.principal<AuthToken>()
                ?: throw IllegalArgumentException("Not authenticated intercept payment by id")
            val paymentId = UUID.fromString(call.parameters["paymentId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val paymentEntity = PaymentEntity.findById(paymentId)!!.load(PaymentEntity::appointment)

                when(authToken.authority) {
                    Authority.ADMIN -> proceed()
                    Authority.DOCTOR -> {
                        if (paymentEntity.appointment.schedule.doctor.identity.email == authToken.email) {
                            proceed()
                        } else {
                            throw IllegalArgumentException("Not authorized intercept payment by id, authority DOCTOR")
                        }
                    }
                    Authority.PATIENT -> {
                        if (paymentEntity.appointment.patient.identity.email == authToken.email) {
                            proceed()
                        } else {
                            throw IllegalArgumentException("Not authorized intercept payment by id, authority PATIENT")
                        }
                    }
                    else -> throw IllegalArgumentException("Not authorized intercept payment, wrong authority")
                }
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/payments") {

            authenticate(
                "session-auth-admin", "session-auth-doctor", "session-auth-patient",
                strategy = AuthenticationStrategy.FirstSuccessful
            ) {
                get("/by-id") {
                    val paymentId = UUID.fromString(call.parameters["paymentId"])
                    call.respond(HttpStatusCode.OK, paymentService.findById(paymentId))
                }
            }

            authenticate("session-auth-admin") {

                post("/create") {
                    val paymentRequest = call.receive<PaymentRequest>()
                    paymentService.create(paymentRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val paymentId = UUID.fromString(call.parameters["paymentId"])
                    paymentService.delete(paymentId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}