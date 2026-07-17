package org.burgas.koin

import io.ktor.server.application.*
import org.burgas.service.*
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
        single {
            DoctorService(
                get<ImageService>(),
                get<MedicalService>()
            )
        }
        singleOf(::MedicalService)
        singleOf(::ScheduleService)
        single {
            AppointmentService(get<DocumentService>())
        }
        singleOf(::PaymentService)
    }

    install(Koin) {
        slf4jLogger()
        modules(modules)
    }
}