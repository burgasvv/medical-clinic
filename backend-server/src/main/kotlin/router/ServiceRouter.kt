package org.burgas.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.burgas.dto.ServiceRequest
import org.burgas.service.MedicalService
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureServiceRouter() {

    val medicalService by inject<MedicalService>()

    routing {

        route("/api/v1/services") {

            get {
                call.respond(HttpStatusCode.OK, medicalService.findAll())
            }

            get("/by-id") {
                val serviceId = UUID.fromString(call.parameters["serviceId"])
                call.respond(HttpStatusCode.OK, medicalService.findById(serviceId))
            }

            authenticate("session-auth-admin") {

                post("/create") {
                    val serviceRequest = call.receive<ServiceRequest>()
                    medicalService.create(serviceRequest)
                    call.respond(HttpStatusCode.OK)
                }

                put("/update") {
                    val serviceRequest = call.receive<ServiceRequest>()
                    medicalService.update(serviceRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val serviceId = UUID.fromString(call.parameters["serviceId"])
                    medicalService.delete(serviceId)
                    call.respond(HttpStatusCode.OK)
                }

                post("/create-by-document") {
                    medicalService.createByDocument(call.receiveMultipart(Long.MAX_VALUE))
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}