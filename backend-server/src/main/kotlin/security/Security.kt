package org.burgas.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.burgas.dao.IdentityEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.database.IdentityTable
import org.burgas.dto.AuthToken
import org.burgas.dto.ExceptionResponse
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt

fun Application.configureSecurity() {

    authentication {
        basic(name = "basic-auth") {
            validate { credentials ->
                val identity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity.find {
                        (IdentityTable.email eq credentials.name) or (IdentityTable.phone eq credentials.name)
                    }.singleOrNull()
                }
                if (
                    identity != null && identity.status &&
                    BCrypt.checkpw(credentials.password, identity.password)
                ) {
                    UserPasswordCredential(credentials.name, credentials.password)
                } else {
                    null
                }
            }
        }
        session<AuthToken>("session-auth") {
            validate { it }
            challenge {
                val exceptionResponse = ExceptionResponse(
                    status = HttpStatusCode.Unauthorized.description,
                    code = HttpStatusCode.Unauthorized.value,
                    message = "Not authorized in session"
                )
                call.respond(HttpStatusCode.Unauthorized, exceptionResponse)
            }
        }
        session<AuthToken>("session-auth-admin") {
            validate { if (it.authority == Authority.ADMIN) it else null }
            challenge {
                val exceptionResponse = ExceptionResponse(
                    status = HttpStatusCode.Unauthorized.description,
                    code = HttpStatusCode.Unauthorized.value,
                    message = "Not authorized in session, must be ADMIN"
                )
                call.respond(HttpStatusCode.Unauthorized, exceptionResponse)
            }
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val exceptionResponse = ExceptionResponse(
                status = HttpStatusCode.BadRequest.description,
                code = HttpStatusCode.BadRequest.value,
                message = cause.message
            )
            call.respond(HttpStatusCode.BadRequest, exceptionResponse)
        }
    }

    install(Sessions) {
        cookie<AuthToken>("AUTH_TOKEN") {
            val secretKey = "000102030405060708090a0b0c0d0e0f"
            cookie.httpOnly = false
            transform(SessionTransportTransformerMessageAuthentication(secretKey.toByteArray()))
        }
    }

    install(CORS) {
        anyMethod()

        allowHeader(HttpHeaders.Host)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-CSRF-Token")

        allowCredentials = true
        allowSameOrigin = true

        anyHost()
    }

    install(CSRF) {
        allowOrigin("http://localhost:9000")
        checkHeader("X-CSRF-Token")
        onFailure { errorResult ->
            respond(HttpStatusCode.BadRequest, errorResult)
        }
    }
}