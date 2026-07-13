package org.burgas.dto

import kotlinx.serialization.Serializable
import org.burgas.serialization.UUIDSerializer
import java.util.UUID

@Serializable
data class ExceptionResponse(
    val status: String,
    val code: Int,
    val message: String?
)

@Serializable
data class CsrfToken(@Serializable(with = UUIDSerializer::class) val token: UUID)