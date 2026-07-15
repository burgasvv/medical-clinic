package org.burgas

import io.ktor.server.application.Application
import org.burgas.database.configureDatabase
import org.burgas.koin.configureKoin
import org.burgas.router.configureAdminRouter
import org.burgas.router.configureCategoryRouter
import org.burgas.router.configureDepartmentRouter
import org.burgas.router.configureDocumentRouter
import org.burgas.router.configureIdentityRouter
import org.burgas.router.configureImageRouter
import org.burgas.router.configurePatientRouter
import org.burgas.router.configureSecurityRouter
import org.burgas.security.configureSecurity
import org.burgas.serialization.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.modules() {
    configureSerialization()
    configureSecurity()
    configureDatabase()
    configureKoin()
    configureSecurityRouter()
    configureImageRouter()
    configureDocumentRouter()
    configureIdentityRouter()
    configureAdminRouter()
    configurePatientRouter()
    configureDepartmentRouter()
    configureCategoryRouter()
}