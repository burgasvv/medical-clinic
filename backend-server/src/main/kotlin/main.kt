package org.burgas

import io.ktor.server.application.*
import org.burgas.database.configureDatabase
import org.burgas.koin.configureKoin
import org.burgas.router.*
import org.burgas.schedule.configureSchedule
import org.burgas.security.configureSecurity
import org.burgas.serialization.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.modules() {
    configureDatabase()
    configureSerialization()
    configureSecurity()
    configureKoin()
    configureSchedule()
    configureSecurityRouter()
    configureImageRouter()
    configureDocumentRouter()
    configureIdentityRouter()
    configureAdminRouter()
    configurePatientRouter()
    configureDepartmentRouter()
    configureCategoryRouter()
    configureDoctorRouter()
    configureServiceRouter()
    configureScheduleRouter()
    configureAppointmentRouter()
    configurePaymentRouter()
}