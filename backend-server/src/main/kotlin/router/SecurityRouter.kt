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
import org.burgas.dto.AuthToken
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
                        call.respond(
                            HttpStatusCode.OK,
                            "You already logged in: ${authToken.email} :: ${authToken.authority}"
                        )
                    } else {
                        val identity = call.principal<IdentityEntity>()
                            ?: throw IllegalArgumentException("Input auth data in login")
                        val authToken = AuthToken(email = identity.email, authority = identity.authority)
                        call.sessions.set(authToken, AuthToken::class)
                        call.respond(
                            HttpStatusCode.OK,
                            "You successfully logged in: ${authToken.email} :: ${authToken.authority}"
                        )
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