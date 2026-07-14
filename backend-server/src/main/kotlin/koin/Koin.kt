package org.burgas.koin

import io.ktor.server.application.*
import org.burgas.service.DocumentService
import org.burgas.service.ImageService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {

    val modules = module {
        singleOf(::ImageService)
        singleOf(::DocumentService)
    }

    install(Koin) {
        slf4jLogger()
        modules(modules)
    }
}