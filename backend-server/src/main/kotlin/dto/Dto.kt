package org.burgas.dto

import kotlinx.serialization.Serializable
import org.burgas.database.Authority
import org.burgas.serialization.UUIDSerializer
import java.util.*

@Serializable
data class ExceptionResponse(
    val status: String,
    val code: Int,
    val message: String?
)

@Serializable
data class CsrfToken(@Serializable(with = UUIDSerializer::class) val token: UUID)

@Serializable
data class AuthToken(
    val email: String,
    val authority: Authority
)