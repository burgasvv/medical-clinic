package org.burgas.koin

import io.ktor.server.application.*
import org.burgas.service.AdminService
import org.burgas.service.CategoryService
import org.burgas.service.DepartmentService
import org.burgas.service.DoctorService
import org.burgas.service.DocumentService
import org.burgas.service.IdentityService
import org.burgas.service.ImageService
import org.burgas.service.PatientService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {

    val modules = module {
        singleOf(::ImageService)
        singleOf(::DocumentService)
        singleOf(::IdentityService)
        singleOf(::AdminService)
        singleOf(::PatientService)
        singleOf(::DepartmentService)
        singleOf(::CategoryService)
        singleOf(::DoctorService)
    }

    install(Koin) {
        slf4jLogger()
        modules(modules)
    }
}