package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.utils.io.*
import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.database.IdentityTable
import org.burgas.dto.AuthToken
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*


@OptIn(InternalAPI::class)
fun Application.configureSecurityRouter() {

    val config = ApplicationConfig("application.yaml")
    val methods: List<HttpMethod> = listOf(
        HttpMethod.Post, HttpMethod.Delete, HttpMethod.Patch, HttpMethod.Put
    )

    intercept(ApplicationCallPipeline.Setup) {
        if (methods.contains(call.request.httpMethod)) {
            call.request.setHeader(
                HttpHeaders.Origin,
                listOf(config.property("api.backend-server.url").getString())
            )
            call.request.setHeader(
                "X-CSRF-Token",
                listOf(UUID.randomUUID().toString())
            )
        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/security") {

            authenticate("basic-auth") {

                post("/login") {
                    val authToken = call.sessions.get(AuthToken::class)
                    if (authToken != null) {
                        call.respond(HttpStatusCode.OK, "You already logged in: ${authToken.email}")
                    } else {
                        val credential = call.principal<UserPasswordCredential>()
                            ?: throw IllegalArgumentException("Input auth data in login")
                        val identity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                            IdentityEntity.find {
                                (IdentityTable.email eq credential.name) or (IdentityTable.phone eq credential.name)
                            }.singleOrNull()
                        }
                        if (identity != null) {
                            val authToken = AuthToken(email = identity.email, authority = identity.authority)
                            call.sessions.set(authToken, AuthToken::class)
                            call.respond(HttpStatusCode.OK, "You successfully logged in: ${authToken.email}")
                        } else {
                            throw IllegalArgumentException("Authentication identity not found")
                        }
                    }
                }
            }

            authenticate("session-auth") {

                post("/logout") {
                    call.sessions.clear(AuthToken::class)
                    call.respond(HttpStatusCode.OK, "You successfully logged out")
                }
            }
        }
    }
}