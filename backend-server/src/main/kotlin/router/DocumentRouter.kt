package org.burgas.router

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.burgas.service.DocumentService
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureDocumentRouter() {

    val documentService by inject<DocumentService>()

    routing {

        route("/api/v1/documents") {

            get("/by-id") {
                val documentId = UUID.fromString(call.parameters["documentId"])
                val documentEntity = documentService.findEntity(documentId)
                call.respondBytes(
                    ContentType.parse(documentEntity.contentType), HttpStatusCode.OK
                ) { documentEntity.data.bytes }
            }
        }
    }
}