package org.burgas.dao

import io.ktor.http.content.PartData
import io.ktor.utils.io.InternalAPI
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.io.readByteArray
import org.burgas.database.*
import org.burgas.dto.AdminRequest
import org.burgas.dto.AdminResponse
import org.burgas.dto.CategoryDependency
import org.burgas.dto.CategoryRequest
import org.burgas.dto.CategoryResponse
import org.burgas.dto.DepartmentDependency
import org.burgas.dto.DepartmentRequest
import org.burgas.dto.DepartmentResponse
import org.burgas.dto.Dependency
import org.burgas.dto.DoctorDependency
import org.burgas.dto.DoctorRequest
import org.burgas.dto.DoctorResponse
import org.burgas.dto.DocumentResponse
import org.burgas.dto.IdentityRequest
import org.burgas.dto.IdentityResponse
import org.burgas.dto.ImageResponse
import org.burgas.dto.PatientRequest
import org.burgas.dto.PatientResponse
import org.burgas.dto.Request
import org.burgas.dto.Response
import org.burgas.dto.ServiceDependency
import org.burgas.dto.ServiceRequest
import org.burgas.dto.ServiceResponse
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import org.mindrot.jbcrypt.BCrypt
import java.time.format.DateTimeFormatter
import java.util.*

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

class ImageEntity(id: EntityID<UUID>) : UUIDEntity(id), Uploader, ResponseMapper<ImageResponse> {
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

class DocumentEntity(id: EntityID<UUID>) : UUIDEntity(id), Uploader, ResponseMapper<DocumentResponse> {
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

class IdentityEntity(id: EntityID<UUID>) : UUIDEntity(id), Creator<IdentityRequest>,
    Modifier<IdentityRequest>, ResponseMapper<IdentityResponse> {
    companion object : UUIDEntityClass<IdentityEntity>(IdentityTable)

    var authority by IdentityTable.authority
    var email by IdentityTable.email
    var password by IdentityTable.password
    var phone by IdentityTable.phone
    var status by IdentityTable.status
    var firstname by IdentityTable.firstname
    var lastname by IdentityTable.lastname
    var patronymic by IdentityTable.patronymic

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

    override suspend fun toResponse(): IdentityResponse {
        return IdentityResponse(
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
}

class AdminEntity(id: EntityID<UUID>) : UUIDEntity(id), Creator<AdminRequest>,
    Modifier<AdminRequest>, ResponseMapper<AdminResponse> {
    companion object : UUIDEntityClass<AdminEntity>(AdminTable)

    var identity by IdentityEntity.referencedOn(AdminTable.identityId)
    var createdAt by AdminTable.createdAt

    override fun create(request: AdminRequest) {
        request.identityRequest!!.let {
            val identityEntity = IdentityEntity.new { this.create(it) }
            identityEntity.authority = Authority.ADMIN
            this.identity = identityEntity
        }
    }

    override fun update(request: AdminRequest) {
        request.identityRequest?.let { this.identity.update(it) }
    }

    override suspend fun toResponse(): AdminResponse {
        return AdminResponse(
            id = this.id.value,
            identity = this.identity.toResponse(),
            createdAt = this.createdAt.toJavaLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm"))
        )
    }
}

class PatientEntity(id: EntityID<UUID>) : UUIDEntity(id), Creator<PatientRequest>,
    Modifier<PatientRequest>, ResponseMapper<PatientResponse> {
    companion object : UUIDEntityClass<PatientEntity>(PatientTable)

    var identity by IdentityEntity.referencedOn(PatientTable.identityId)
    var passport by PatientTable.passport
    var createdAt by PatientTable.createdAt

    override fun create(request: PatientRequest) {
        request.identityRequest!!.let {
            val identityEntity = IdentityEntity.new { this.create(it) }
            identityEntity.authority = Authority.PATIENT
            this.identity = identityEntity
        }
        request.passport!!.let { this.passport = it }
    }

    override fun update(request: PatientRequest) {
        request.identityRequest?.let { this.identity.update(it) }
        request.passport?.let { this.passport = it }
    }

    override suspend fun toResponse(): PatientResponse {
        return PatientResponse(
            id = this.id.value,
            identity = this.identity.toResponse(),
            passport = this.passport,
            createdAt = this.createdAt.toJavaLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm"))
        )
    }
}

class DepartmentEntity(id: EntityID<UUID>) : UUIDEntity(id), Creator<DepartmentRequest>,
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

class CategoryEntity(id: EntityID<UUID>) : UUIDEntity(id), Creator<CategoryRequest>,
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

class DoctorEntity(id: EntityID<UUID>) : UUIDEntity(id), Creator<DoctorRequest>,
    Modifier<DoctorRequest>, DependencyMapper<DoctorDependency>, ResponseMapper<DoctorResponse> {
    companion object : UUIDEntityClass<DoctorEntity>(DoctorTable)

    var identity by IdentityEntity.referencedOn(DoctorTable.identityId)
    var category by CategoryEntity.optionalReferencedOn(DoctorTable.categoryId)
    var about by DoctorTable.about
    var image by ImageEntity.optionalReferencedOn(DoctorTable.imageId)
    var createdAt by DoctorTable.createdAt
    var services by ServiceEntity.via(DoctorServiceTable.doctorId, DoctorServiceTable.serviceId)

    override fun create(request: DoctorRequest) {
        request.identityRequest!!.let {
            val identityEntity = IdentityEntity.new { this.create(it) }
            identityEntity.authority = Authority.DOCTOR
            this.identity = identityEntity
        }
        request.categoryId!!.let { this.category = CategoryEntity.findById(it) }
        request.about?.let { this.about = it }
    }

    override fun update(request: DoctorRequest) {
        request.identityRequest?.let { this.identity.update(it) }
        request.categoryId?.let { this.category = CategoryEntity.findById(it) }
        request.about?.let { this.about = it }
    }

    override suspend fun toDependency(): DoctorDependency {
        return DoctorDependency(
            id = this.id.value,
            identity = this.identity.toResponse(),
            category = this.category?.name,
            about = this.about,
            image = this.image?.toResponse(),
            createdAt = this.createdAt.toJavaLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm"))
        )
    }

    override suspend fun toResponse(): DoctorResponse {
        return DoctorResponse(
            id = this.id.value,
            identity = this.identity.toResponse(),
            category = this.category?.toDependency(),
            about = this.about,
            image = this.image?.toResponse(),
            createdAt = this.createdAt.toJavaLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm")),
            services = this.services.map { it.toDependency() }
        )
    }
}

class ServiceEntity(id: EntityID<UUID>) : UUIDEntity(id), Creator<ServiceRequest>,
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