package org.burgas.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.burgas.dto.CategoryRequest
import org.burgas.service.CategoryService
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureCategoryRouter() {

    val categoryService by inject<CategoryService>()

    routing {

        route("/api/v1/categories") {

            get("/by-id") {
                val categoryId = UUID.fromString(call.parameters["categoryId"])
                call.respond(HttpStatusCode.OK, categoryService.findById(categoryId))
            }

            authenticate("session-auth-admin") {

                post("/create") {
                    val categoryRequest = call.receive<CategoryRequest>()
                    categoryService.create(categoryRequest)
                    call.respond(HttpStatusCode.OK)
                }

                put("/update") {
                    val categoryRequest = call.receive<CategoryRequest>()
                    categoryService.update(categoryRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val categoryId = UUID.fromString(call.parameters["categoryId"])
                    categoryService.delete(categoryId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}