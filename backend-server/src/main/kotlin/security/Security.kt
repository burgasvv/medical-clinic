package org.burgas.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.burgas.dto.AuthToken
import org.burgas.dto.CsrfToken
import org.burgas.dto.ExceptionResponse
import kotlin.time.Duration.Companion.days

fun Application.configureSecurity() {

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
        cookie<CsrfToken>("CSRF_TOKEN") {
            val secretEncryptKey = "0011223344556677".toByteArray()
            val secretSignKey = "ffeeddccbbaa99887766554433221100".toByteArray()
            cookie.httpOnly = true
            cookie.maxAge = 1.days
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
        cookie<AuthToken>("AUTH_TOKEN") {
            val secretKey = "000102030405060708090a0b0c0d0e0f".toByteArray()
            cookie.httpOnly = true
            transform(SessionTransportTransformerMessageAuthentication(secretKey))
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