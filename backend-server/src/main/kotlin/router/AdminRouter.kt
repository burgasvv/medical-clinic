package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.burgas.dao.AdminEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AdminRequest
import org.burgas.dto.AuthToken
import org.burgas.service.AdminService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureAdminRouter() {

    val adminService by inject<AdminService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path() == "/api/v1/admins/by-id" || call.request.path() == "/api/v1/admins/delete") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept admins by id"))
            val adminId = UUID.fromString(call.parameters["adminId"])

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val adminEntity = AdminEntity.findById(adminId)!!.load(AdminEntity::identity)
                if (adminEntity.identity.email == authToken.email) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept admins by id")
                }
            }

        } else if (call.request.path() == "/api/v1/admins/update") {

            val authToken = (call.sessions.get(AuthToken::class)
                ?: throw IllegalArgumentException("Not authenticated intercept admins update"))
            val adminRequest = call.receive<AdminRequest>()

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val adminEntity = AdminEntity.findById(adminRequest.id!!)!!.load(AdminEntity::identity)
                if (adminEntity.identity.email == authToken.email) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept admins update")
                }
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/admins") {

            authenticate("session-auth-admin") {

                get {
                    call.respond(HttpStatusCode.OK, adminService.findAll())
                }

                get("/by-id") {
                    val adminId = UUID.fromString(call.parameters["adminId"])
                    call.respond(HttpStatusCode.OK, adminService.findById(adminId))
                }

                post("/create") {
                    val adminRequest = call.receive<AdminRequest>()
                    adminService.create(adminRequest)
                    call.respond(HttpStatusCode.OK)
                }

                put("/update") {
                    val adminRequest = call.receive<AdminRequest>()
                    adminService.update(adminRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val adminId = UUID.fromString(call.parameters["adminId"])
                    adminService.delete(adminId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}