package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.burgas.dto.DepartmentRequest
import org.burgas.service.DepartmentService
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureDepartmentRouter() {

    val departmentService by inject<DepartmentService>()

    routing {

        route("/api/v1/departments") {

            get {
                call.respond(HttpStatusCode.OK, departmentService.findAll())
            }

            get("/by-id") {
                val departmentId = UUID.fromString(call.parameters["departmentId"])
                call.respond(HttpStatusCode.OK, departmentService.findById(departmentId))
            }

            authenticate("session-auth-admin") {

                post("/create") {
                    val departmentRequest = call.receive<DepartmentRequest>()
                    departmentService.create(departmentRequest)
                    call.respond(HttpStatusCode.OK)
                }

                put("/update") {
                    val departmentRequest = call.receive<DepartmentRequest>()
                    departmentService.update(departmentRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val departmentId = UUID.fromString(call.parameters["departmentId"])
                    departmentService.delete(departmentId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}