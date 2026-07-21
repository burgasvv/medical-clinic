package org.burgas.dao

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.io.readByteArray
import org.burgas.database.*
import org.burgas.dto.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime
import java.util.*

interface File

interface Dao

interface Uploader {
    fun upload(partData: PartData)
}

interface Creator<R : Request> {
    fun create(request: R)
}

interface Modifier<R : Request> {
    fun update(request: R)
}

interface DependencyMapper<D : Dependency> {
    suspend fun toDependency(): D
}

interface ResponseMapper<R : Response> {
    suspend fun toResponse(): R
}

class ImageEntity(id: EntityID<UUID>) : UUIDEntity(id), File, Uploader, ResponseMapper<ImageResponse> {
    companion object : UUIDEntityClass<ImageEntity>(ImageTable)

    var name by ImageTable.name
    var contentType by ImageTable.contentType
    var preview by ImageTable.preview
    var data by ImageTable.data

    @OptIn(InternalAPI::class)
    override fun upload(partData: PartData) {
        if (partData is PartData.FileItem) {
            this.name = partData.originalFileName!!
            this.contentType = "${partData.contentType!!.contentType}/${partData.contentType!!.contentSubtype}"
            this.preview = true
            this.data = ExposedBlob(partData.provider().readBuffer.readByteArray())
        } else {
            throw IllegalArgumentException("Part data is not FileItem")
        }
    }

    override suspend fun toResponse(): ImageResponse {
        return ImageResponse(
            id = this.id.value,
            name = this.name,
            contentType = this.contentType,
            preview = this.preview
        )
    }
}

class DocumentEntity(id: EntityID<UUID>) : UUIDEntity(id), File, Uploader, ResponseMapper<DocumentResponse> {
    companion object : UUIDEntityClass<DocumentEntity>(DocumentTable)

    var name by DocumentTable.name
    var contentType by DocumentTable.contentType
    var data by DocumentTable.data

    @OptIn(InternalAPI::class)
    override fun upload(partData: PartData) {
        if (partData is PartData.FileItem) {
            this.name = partData.originalFileName!!
            this.contentType = "${partData.contentType!!.contentType}/${partData.contentType!!.contentSubtype}"
            this.data = ExposedBlob(partData.provider().readBuffer.readByteArray())
        } else {
            throw IllegalArgumentException("Part data is not FileItem")
        }
    }

    override suspend fun toResponse(): DocumentResponse {
        return DocumentResponse(
            id = this.id.value,
            name = this.name,
            contentType = this.contentType
        )
    }
}

class IdentityEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<IdentityRequest>,
    Modifier<IdentityRequest>, DependencyMapper<IdentityDependency>, ResponseMapper<IdentityResponse> {
    companion object : UUIDEntityClass<IdentityEntity>(IdentityTable)

    var authority by IdentityTable.authority
    var email by IdentityTable.email
    var password by IdentityTable.password
    var phone by IdentityTable.phone
    var status by IdentityTable.status
    var firstname by IdentityTable.firstname
    var lastname by IdentityTable.lastname
    var patronymic by IdentityTable.patronymic
    val admin by AdminEntity.optionalBackReferencedOn(AdminTable.identityId)
    val patient by PatientEntity.optionalBackReferencedOn(PatientTable.identityId)
    val doctor by DoctorEntity.optionalBackReferencedOn(DoctorTable.identityId)

    override fun create(request: IdentityRequest) {
        request.email!!.let { this.email = it }
        request.password!!.let { this.password = BCrypt.hashpw(it, BCrypt.gensalt()) }
        request.phone!!.let { this.phone = it }
        request.firstname!!.let { this.firstname = it }
        request.lastname!!.let { this.lastname = it }
        request.patronymic!!.let { this.patronymic = it }
    }

    override fun update(request: IdentityRequest) {
        request.email?.let { this.email = it }
        request.phone?.let { this.phone = it }
        request.firstname?.let { this.firstname = it }
        request.lastname?.let { this.lastname = it }
        request.patronymic?.let { this.patronymic = it }
    }

    override suspend fun toDependency(): IdentityDependency {
        return IdentityDependency(
            id = this.id.value,
            authority = this.authority,
            email = this.email,
            phone = this.phone,
            status = this.status,
            firstname = this.firstname,
            lastname = this.lastname,
            patronymic = this.patronymic
        )
    }

    override suspend fun toResponse(): IdentityResponse {
        return IdentityResponse(
            id = this.id.value,
            authority = this.authority,
            email = this.email,
            phone = this.phone,
            status = this.status,
            firstname = this.firstname,
            lastname = this.lastname,
            patronymic = this.patronymic,
            admin = this.admin?.toAdminDependencyInIdentity(),
            patient = this.patient?.toPatientDependencyInIdentity(),
            doctor = this.doctor?.toDoctorDependencyInIdentity()
        )
    }
}

class AdminEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<AdminRequest>,
    Modifier<AdminRequest>, ResponseMapper<AdminResponse> {
    companion object : UUIDEntityClass<AdminEntity>(AdminTable)

    var identity by IdentityEntity.referencedOn(AdminTable.identityId)
    var createdAt by AdminTable.createdAt

    override fun create(request: AdminRequest) {
        request.identity!!.let {
            this.identity = IdentityEntity.new {
                this.create(it)
                authority = Authority.ADMIN
            }
        }
    }

    override fun update(request: AdminRequest) {
        request.identity?.let { this.identity.update(it) }
    }

    fun toAdminDependencyInIdentity(): AdminDependencyInIdentity {
        return AdminDependencyInIdentity(
            id = this.id.value,
            createdAt = this.createdAt
        )
    }

    override suspend fun toResponse(): AdminResponse {
        return AdminResponse(
            id = this.id.value,
            identity = this.identity.toDependency(),
            createdAt = this.createdAt
        )
    }
}

class PatientEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<PatientRequest>,
    Modifier<PatientRequest>, DependencyMapper<PatientDependency>, ResponseMapper<PatientResponse> {
    companion object : UUIDEntityClass<PatientEntity>(PatientTable)

    var identity by IdentityEntity.referencedOn(PatientTable.identityId)
    var passport by PatientTable.passport
    var createdAt by PatientTable.createdAt
    val appointments by AppointmentEntity.referrersOn(AppointmentTable.patientId)

    override fun create(request: PatientRequest) {
        request.identity!!.let {
            this.identity = IdentityEntity.new {
                this.create(it)
                authority = Authority.PATIENT
            }
        }
        request.passport!!.let { this.passport = it }
    }

    override fun update(request: PatientRequest) {
        request.identity?.let { this.identity.update(it) }
        request.passport?.let { this.passport = it }
    }

    override suspend fun toDependency(): PatientDependency {
        return PatientDependency(
            id = this.id.value,
            identity = this.identity.toDependency(),
            passport = this.passport,
            createdAt = this.createdAt
        )
    }

    fun toPatientDependencyInIdentity(): PatientDependencyInIdentity {
        return PatientDependencyInIdentity(
            id = this.id.value,
            passport = this.passport,
            createdAt = this.createdAt
        )
    }

    override suspend fun toResponse(): PatientResponse {
        return PatientResponse(
            id = this.id.value,
            identity = this.identity.toDependency(),
            passport = this.passport,
            createdAt = this.createdAt,
            appointments = this.appointments.map { it.toDependencyInPatient() }
        )
    }
}

class DepartmentEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<DepartmentRequest>,
    Modifier<DepartmentRequest>, DependencyMapper<DepartmentDependency>, ResponseMapper<DepartmentResponse> {
    companion object : UUIDEntityClass<DepartmentEntity>(DepartmentTable)

    var name by DepartmentTable.name
    var description by DepartmentTable.description
    val categories by CategoryEntity.referrersOn(CategoryTable.departmentId)

    override fun create(request: DepartmentRequest) {
        request.name!!.let { this.name = it }
        request.description!!.let { this.description = it }
    }

    override fun update(request: DepartmentRequest) {
        request.name?.let { this.name = it }
        request.description?.let { this.description = it }
    }

    override suspend fun toDependency(): DepartmentDependency {
        return DepartmentDependency(
            id = this.id.value,
            name = this.name,
            description = this.description
        )
    }

    override suspend fun toResponse(): DepartmentResponse {
        return DepartmentResponse(
            id = this.id.value,
            name = this.name,
            description = this.description,
            categories = this.categories.map { it.toDependency() }
        )
    }
}

class CategoryEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<CategoryRequest>,
    Modifier<CategoryRequest>, DependencyMapper<CategoryDependency>, ResponseMapper<CategoryResponse> {
    companion object : UUIDEntityClass<CategoryEntity>(CategoryTable)

    var name by CategoryTable.name
    var description by CategoryTable.description
    var department by DepartmentEntity.referencedOn(CategoryTable.departmentId)
    val doctors by DoctorEntity.optionalReferrersOn(DoctorTable.categoryId)

    override fun create(request: CategoryRequest) {
        request.name!!.let { this.name = it }
        request.description!!.let { this.description = it }
        request.departmentId!!.let { this.department = DepartmentEntity.findById(it)!! }
    }

    override fun update(request: CategoryRequest) {
        request.name?.let { this.name = it }
        request.description?.let { this.description = it }
        request.departmentId?.let { this.department = DepartmentEntity.findById(it)!! }
    }

    override suspend fun toDependency(): CategoryDependency {
        return CategoryDependency(
            id = this.id.value,
            name = this.name,
            description = this.description
        )
    }

    override suspend fun toResponse(): CategoryResponse {
        return CategoryResponse(
            id = this.id.value,
            name = this.name,
            description = this.description,
            department = this.department.toDependency(),
            doctors = this.doctors.map { it.toDependency() }
        )
    }
}

class DoctorEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<DoctorRequest>,
    Modifier<DoctorRequest>, DependencyMapper<DoctorDependency>, ResponseMapper<DoctorResponse> {
    companion object : UUIDEntityClass<DoctorEntity>(DoctorTable)

    var identity by IdentityEntity.referencedOn(DoctorTable.identityId)
    var category by CategoryEntity.optionalReferencedOn(DoctorTable.categoryId)
    var about by DoctorTable.about
    var image by ImageEntity.optionalReferencedOn(DoctorTable.imageId)
    var createdAt by DoctorTable.createdAt
    var services by ServiceEntity.via(DoctorServiceTable.doctorId, DoctorServiceTable.serviceId)
    val schedules by ScheduleEntity.referrersOn(ScheduleTable.doctorId)

    override fun create(request: DoctorRequest) {
        request.identity!!.let {
            this.identity = IdentityEntity.new {
                this.create(it)
                authority = Authority.DOCTOR
            }
        }
        request.categoryId!!.let { this.category = CategoryEntity.findById(it) }
        request.about?.let { this.about = it }
    }

    override fun update(request: DoctorRequest) {
        request.identity?.let { this.identity.update(it) }
        request.categoryId?.let { this.category = CategoryEntity.findById(it) }
        request.about?.let { this.about = it }
    }

    suspend fun toDoctorDependencyInIdentity(): DoctorDependencyInIdentity {
        return DoctorDependencyInIdentity(
            id = this.id.value,
            category = this.category?.name,
            about = this.about,
            image = this.image?.toResponse(),
            createdAt = this.createdAt
        )
    }

    override suspend fun toDependency(): DoctorDependency {
        return DoctorDependency(
            id = this.id.value,
            identity = this.identity.toDependency(),
            category = this.category?.name,
            about = this.about,
            image = this.image?.toResponse(),
            createdAt = this.createdAt
        )
    }

    override suspend fun toResponse(): DoctorResponse {
        return DoctorResponse(
            id = this.id.value,
            identity = this.identity.toDependency(),
            category = this.category?.toDependency(),
            about = this.about,
            image = this.image?.toResponse(),
            createdAt = this.createdAt,
            services = this.services.map { it.toDependency() },
            schedules = this.schedules.map { it.toDependencyInDoctor() }
        )
    }
}

class ServiceEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<ServiceRequest>,
    Modifier<ServiceRequest>, DependencyMapper<ServiceDependency>, ResponseMapper<ServiceResponse> {
    companion object : UUIDEntityClass<ServiceEntity>(ServiceTable)

    var name by ServiceTable.name
    var description by ServiceTable.description
    var price by ServiceTable.price
    var doctors by DoctorEntity.via(DoctorServiceTable.serviceId, DoctorServiceTable.doctorId)

    override fun create(request: ServiceRequest) {
        request.name!!.let { this.name = it }
        request.description!!.let { this.description = it }
        request.price!!.let { this.price = it }
    }

    override fun update(request: ServiceRequest) {
        request.name?.let { this.name = it }
        request.description?.let { this.description = it }
        request.price?.let { this.price = it }
    }

    override suspend fun toDependency(): ServiceDependency {
        return ServiceDependency(
            id = this.id.value,
            name = this.name,
            description = this.description,
            price = this.price
        )
    }

    override suspend fun toResponse(): ServiceResponse {
        return ServiceResponse(
            id = this.id.value,
            name = this.name,
            description = this.description,
            price = this.price,
            doctors = this.doctors.map { it.toDependency() }
        )
    }
}

class ScheduleEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<ScheduleRequest>,
    Modifier<ScheduleRequest>, ResponseMapper<ScheduleResponse> {
    companion object : UUIDEntityClass<ScheduleEntity>(ScheduleTable)

    var dateTime by ScheduleTable.dateTime
    var doctor by DoctorEntity.referencedOn(ScheduleTable.doctorId)
    var concluded by ScheduleTable.concluded
    val appointment by AppointmentEntity.optionalBackReferencedOn(AppointmentTable.scheduleId)

    override fun create(request: ScheduleRequest) {
        request.datetime!!.let {
            if (it.toJavaLocalDateTime().isAfter(LocalDateTime.now())) {
                this.dateTime = it
            } else {
                throw IllegalArgumentException("Input datetime is passed")
            }
        }
        request.doctorId!!.let { this.doctor = DoctorEntity.findById(it)!! }
    }

    override fun update(request: ScheduleRequest) {
        request.datetime?.let {
            if (it.toJavaLocalDateTime().isAfter(LocalDateTime.now())) {
                this.dateTime = it
            } else {
                throw IllegalArgumentException("Input datetime is passed")
            }
        }
        request.doctorId?.let { this.doctor = DoctorEntity.findById(it)!! }
    }

    suspend fun toDependencyInDoctor(): ScheduleDependencyInDoctor {
        return ScheduleDependencyInDoctor(
            id = this.id.value,
            datetime = this.dateTime,
            concluded = this.concluded,
            appointment = this.appointment?.toDependencyInSchedule()
        )
    }

    suspend fun toDependencyInAppointment(): ScheduleDependencyInAppointment {
        return ScheduleDependencyInAppointment(
            id = this.id.value,
            datetime = this.dateTime,
            concluded = this.concluded,
            doctor = this.doctor.toDependency()
        )
    }

    override suspend fun toResponse(): ScheduleResponse {
        return ScheduleResponse(
            id = this.id.value,
            datetime = this.dateTime,
            concluded = this.concluded,
            doctor = this.doctor.toDependency(),
            appointment = this.appointment?.toDependencyInSchedule()
        )
    }
}

class AppointmentEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao,
    Creator<AppointmentRequest>, ResponseMapper<AppointmentResponse> {
    companion object : UUIDEntityClass<AppointmentEntity>(AppointmentTable)

    var schedule by ScheduleEntity.referencedOn(AppointmentTable.scheduleId)
    var patient by PatientEntity.referencedOn(AppointmentTable.patientId)
    var service by ServiceEntity.referencedOn(AppointmentTable.serviceId)
    var document by DocumentEntity.optionalReferencedOn(AppointmentTable.documentId)
    val payment by PaymentEntity.optionalBackReferencedOn(PaymentTable.appointmentId)

    override fun create(request: AppointmentRequest) {
        val scheduleEntity = request.scheduleId.let {
            val findSchedule = ScheduleEntity.findById(it)!!
            if (!findSchedule.concluded && findSchedule.appointment == null) {
                this.schedule = findSchedule
            } else {
                throw IllegalArgumentException("Input schedule already busy or concluded")
            }
            this.schedule
        }
        request.patientId.let { this.patient = PatientEntity.findById(it)!! }
        request.serviceId.let {
            val serviceEntity = ServiceEntity.findById(it)!!
            if (scheduleEntity.doctor.services.map { service -> service.id.value }.contains(serviceEntity.id.value)) {
                this.service = serviceEntity
            } else {
                throw IllegalArgumentException("Doctor don't have input service in list")
            }
        }
    }

    suspend fun toDependencyInSchedule(): AppointmentDependencyInSchedule {
        return AppointmentDependencyInSchedule(
            id = this.id.value,
            patient = this.patient.toDependency(),
            service = this.service.toDependency(),
            document = this.document?.toResponse(),
            payment = this.payment?.toDependency()
        )
    }

    suspend fun toDependencyInPatient(): AppointmentDependencyInPatient {
        return AppointmentDependencyInPatient(
            id = this.id.value,
            schedule = this.schedule.toDependencyInAppointment(),
            service = this.service.toDependency(),
            document = this.document?.toResponse(),
            payment = this.payment?.toDependency()
        )
    }

    suspend fun toDependencyInPayment(): AppointmentDependencyInPayment {
        return AppointmentDependencyInPayment(
            id = this.id.value,
            schedule = this.schedule.toDependencyInAppointment(),
            service = this.service.toDependency(),
            document = this.document?.toResponse(),
            patient = this.patient.toDependency()
        )
    }

    override suspend fun toResponse(): AppointmentResponse {
        return AppointmentResponse(
            id = this.id.value,
            schedule = this.schedule.toDependencyInAppointment(),
            patient = this.patient.toDependency(),
            service = this.service.toDependency(),
            document = this.document?.toResponse(),
            payment = this.payment?.toDependency()
        )
    }
}

class PaymentEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao, Creator<PaymentRequest>,
    DependencyMapper<PaymentDependency>, ResponseMapper<PaymentResponse> {
    companion object : UUIDEntityClass<PaymentEntity>(PaymentTable)

    var appointment by AppointmentEntity.referencedOn(PaymentTable.appointmentId)
    var price by PaymentTable.price
    var createdAt by PaymentTable.createdAt

    override fun create(request: PaymentRequest) {
        val appointment = request.appointmentId.let {
            val appointmentEntity = AppointmentEntity.findById(it)!!
            if (appointmentEntity.payment == null) {
                this.appointment = appointmentEntity
            } else {
                throw IllegalArgumentException("Appointment already have payment")
            }
            this.appointment
        }
        this.price = appointment.service.price
    }

    override suspend fun toDependency(): PaymentDependency {
        return PaymentDependency(
            id = this.id.value,
            price = this.price,
            createdAt = this.createdAt
        )
    }

    override suspend fun toResponse(): PaymentResponse {
        return PaymentResponse(
            id = this.id.value,
            appointment = this.appointment.toDependencyInPayment(),
            price = this.price,
            createdAt = this.createdAt
        )
    }
}