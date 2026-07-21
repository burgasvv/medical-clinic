package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AuthToken
import org.burgas.dto.IdentityRequest
import org.burgas.service.IdentityService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject

fun Application.configureIdentityRouter() {

    val identityService by inject<IdentityService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path().equals("/api/v1/identities/change-password", false)) {

            val authToken = call.principal<AuthToken>()
                ?: throw IllegalArgumentException("Not authenticated intercept identity for changing password")
            val identityRequest = call.receive(IdentityRequest::class)

            val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                IdentityEntity.findById(identityRequest.id!!)
                    ?: throw IllegalArgumentException("Not found identity intercept for changing password")
            }
            if (identityEntity.email == authToken.email) {
                proceed()
            } else {
                throw IllegalArgumentException("Not authorized intercept identity for changing password")
            }

        } else if (call.request.path().equals("/api/v1/identities/change-status", false)) {

            val authToken = call.principal<AuthToken>()
                ?: throw IllegalArgumentException("Not authenticated intercept identity for changing status")
            val identityRequest = call.receive(IdentityRequest::class)

            val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                IdentityEntity.findById(identityRequest.id!!)
                    ?: throw IllegalArgumentException("Not found identity intercept for status")
            }
            if (identityEntity.email != authToken.email) {
                proceed()
            } else {
                throw IllegalArgumentException("Not authorized intercept identity for changing status. Emails matched")
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/identities") {

            authenticate("session-auth") {

                get("/authenticated") {
                    val authToken = call.principal<AuthToken>()
                        ?: throw IllegalArgumentException("Not authenticated identity")
                    call.respond(HttpStatusCode.OK, identityService.findAuthenticated(authToken))
                }

                put("/change-password") {
                    val identityRequest = call.receive<IdentityRequest>()
                    identityService.changePassword(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("session-auth-admin") {

                put("/change-status") {
                    val identityRequest = call.receive<IdentityRequest>()
                    identityService.changeStatus(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}