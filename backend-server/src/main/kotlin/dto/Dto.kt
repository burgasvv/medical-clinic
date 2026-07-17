package org.burgas.dto

import kotlinx.datetime.LocalDateTime
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
    val identity: IdentityRequest? = null
) : Request

@Serializable
data class AdminResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityResponse? = null,
    val createdAt: LocalDateTime? = null
) : Response

@Serializable
data class PatientRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityRequest? = null,
    val passport: String? = null
) : Request

@Serializable
data class PatientDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityResponse? = null,
    val passport: String? = null,
    val createdAt: LocalDateTime? = null
) : Dependency

@Serializable
data class PatientResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityResponse? = null,
    val passport: String? = null,
    val createdAt: LocalDateTime? = null,
    val appointments: List<AppointmentDependencyInPatient>? = null
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
    val identity: IdentityRequest? = null,
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
    val createdAt: LocalDateTime? = null
) : Dependency

@Serializable
data class DoctorResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val identity: IdentityResponse? = null,
    val category: CategoryDependency? = null,
    val about: String? = null,
    val image: ImageResponse? = null,
    val createdAt: LocalDateTime? = null,
    val services: List<ServiceDependency>? = null,
    val schedules: List<ScheduleDependencyInDoctor>? = null
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

@Serializable
data class DoctorServiceRequest(
    @Serializable(with = UUIDSerializer::class)
    val doctorId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val serviceId: UUID
) : Request

@Serializable
data class ScheduleRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val datetime: LocalDateTime? = null,
    @Serializable(with = UUIDSerializer::class)
    val doctorId: UUID? = null,
    val concluded: Boolean? = null
) : Request

@Serializable
data class ScheduleDependencyInDoctor(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val datetime: LocalDateTime? = null,
    val concluded: Boolean? = null,
    val appointment: AppointmentDependencyInSchedule? = null
)

@Serializable
data class ScheduleDependencyInAppointment(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val datetime: LocalDateTime? = null,
    val concluded: Boolean? = null,
    val doctor: DoctorDependency? = null
)

@Serializable
data class ScheduleResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val datetime: LocalDateTime? = null,
    val concluded: Boolean? = null,
    val doctor: DoctorDependency? = null,
    val appointment: AppointmentDependencyInSchedule? = null
) : Response

@Serializable
data class AppointmentRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val scheduleId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val patientId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val serviceId: UUID
) : Request

@Serializable
data class AppointmentDependencyInSchedule(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val patient: PatientDependency? = null,
    val service: ServiceDependency? = null,
    val document: DocumentResponse? = null,
    val payment: PaymentDependency? = null
)

@Serializable
data class AppointmentDependencyInPayment(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val schedule: ScheduleDependencyInAppointment? = null,
    val patient: PatientDependency? = null,
    val service: ServiceDependency? = null,
    val document: DocumentResponse? = null
)

@Serializable
data class AppointmentDependencyInPatient(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val schedule: ScheduleDependencyInAppointment? = null,
    val service: ServiceDependency? = null,
    val document: DocumentResponse? = null,
    val payment: PaymentDependency? = null
)

@Serializable
data class AppointmentResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val schedule: ScheduleDependencyInAppointment? = null,
    val patient: PatientDependency? = null,
    val service: ServiceDependency? = null,
    val document: DocumentResponse? = null,
    val payment: PaymentDependency? = null
) : Response

@Serializable
data class PaymentRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val appointmentId: UUID,
    val price: Double
) : Request

@Serializable
data class PaymentDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val price: Double? = null,
    val createdAt: LocalDateTime? = null
) : Dependency

@Serializable
data class PaymentResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val appointment: AppointmentDependencyInPayment? = null,
    val price: Double? = null,
    val createdAt: LocalDateTime? = null
) : Response