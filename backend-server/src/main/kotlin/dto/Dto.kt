package org.burgas.dto

import kotlinx.serialization.Serializable
import org.burgas.database.Authority
import org.burgas.serialization.UUIDSerializer
import java.util.*

interface Request

interface Dependency

interface Response

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

@Serializable
data class ImageResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val contentType: String? = null,
    val preview: Boolean? = null
) : Response

@Serializable
data class DocumentResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val contentType: String? = null
) : Response

@Serializable
data class IdentityRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val authority: Authority? = null,
    val email: String? = null,
    val password: String? = null,
    val phone: String? = null,
    val status: Boolean? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null
) : Request

@Serializable
data class IdentityResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val authority: Authority? = null,
    val email: String? = null,
    val phone: String? = null,
    val status: Boolean? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null
) : Response

@Serializable
data class AdminRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identityRequest: IdentityRequest? = null
) : Request

@Serializable
data class AdminResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityResponse? = null,
    val createdAt: String? = null
) : Response

@Serializable
data class PatientRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identityRequest: IdentityRequest? = null,
    val passport: String? = null
) : Request

@Serializable
data class PatientResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityResponse? = null,
    val passport: String? = null,
    val createdAt: String? = null
) : Response

@Serializable
data class DepartmentRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null
) : Request

@Serializable
data class DepartmentDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null
) : Dependency

@Serializable
data class DepartmentResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val categories: List<CategoryDependency>? = null
) : Response

@Serializable
data class CategoryRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    @Serializable(with = UUIDSerializer::class)
    val departmentId: UUID? = null
) : Request

@Serializable
data class CategoryDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null
) : Dependency

@Serializable
data class CategoryResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val department: DepartmentDependency? = null,
    val doctors: List<DoctorDependency>? = null
) : Response

@Serializable
data class DoctorRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identityRequest: IdentityRequest? = null,
    @Serializable(with = UUIDSerializer::class)
    val categoryId: UUID? = null,
    val about: String? = null
) : Request

@Serializable
data class DoctorDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityResponse? = null,
    val category: String? = null,
    val about: String? = null,
    val image: ImageResponse? = null,
    val createdAt: String? = null
) : Dependency

@Serializable
data class DoctorResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityResponse? = null,
    val category: CategoryDependency? = null,
    val about: String? = null,
    val image: ImageResponse? = null,
    val createdAt: String? = null,
    val services: List<ServiceDependency>? = null
) : Response

@Serializable
data class ServiceRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null
) : Request

@Serializable
data class ServiceDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null
) : Dependency

@Serializable
data class ServiceResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val doctors: List<DoctorDependency>? = null
) : Response