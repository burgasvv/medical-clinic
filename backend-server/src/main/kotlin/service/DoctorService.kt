package org.burgas.service

import io.ktor.http.content.*
import org.burgas.dao.DoctorEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.DoctorRequest
import org.burgas.dto.DoctorResponse
import org.burgas.dto.DoctorServiceRequest
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.SizedCollection
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class DoctorService : CollectService<DoctorResponse>, ReadService<UUID, DoctorEntity, DoctorResponse>,
    CreateService<DoctorRequest, DoctorResponse>, UpdateService<DoctorRequest, DoctorResponse>, DeleteService<UUID> {

    private val imageService: ImageService
    private val medicalService: MedicalService

    constructor(imageService: ImageService, medicalService: MedicalService) {
        this.imageService = imageService
        this.medicalService = medicalService
    }

    override suspend fun findAll(): List<DoctorResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        DoctorEntity.all()
            .with(
                DoctorEntity::identity, DoctorEntity::category,
                DoctorEntity::schedules, DoctorEntity::services
            )
            .map { it.toResponse() }
    }

    override suspend fun findEntity(id: UUID): DoctorEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        (DoctorEntity.findById(id) ?: throw IllegalArgumentException("Doctor not found"))
            .load(
                DoctorEntity::identity, DoctorEntity::category,
                DoctorEntity::schedules, DoctorEntity::services
            )
    }

    override suspend fun findById(id: UUID): DoctorResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: DoctorRequest): DoctorResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        DoctorEntity.new { this.create(request) }
            .load(
                DoctorEntity::identity, DoctorEntity::category,
                DoctorEntity::schedules, DoctorEntity::services
            )
            .toResponse()
    }

    override suspend fun update(request: DoctorRequest): DoctorResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        DoctorEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(
                DoctorEntity::identity, DoctorEntity::category,
                DoctorEntity::schedules, DoctorEntity::services
            )
            .toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val doctorEntity = findEntity(id)
        doctorEntity.delete()
        doctorEntity.identity.delete()
    }

    suspend fun addImage(doctorId: UUID, multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val doctorEntity = findEntity(doctorId)
        if (doctorEntity.image == null) {
            doctorEntity.image = imageService.upload(multiPartData)
        } else {
            throw IllegalArgumentException("Doctor image already set")
        }
    }

    suspend fun removeImage(doctorId: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val doctorEntity = findEntity(doctorId)
        val image = doctorEntity.image
        if (image != null) {
            doctorEntity.image = null
            image.delete()
        } else {
            throw IllegalArgumentException("Doctor image is null, nothing to remove")
        }
    }

    suspend fun addService(doctorServiceRequest: DoctorServiceRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val doctorEntity = findEntity(doctorServiceRequest.doctorId)
        val serviceEntity = medicalService.findEntity(doctorServiceRequest.serviceId)
        if (!doctorEntity.services.map { it.id.value }.contains(serviceEntity.id.value)) {
            doctorEntity.services = SizedCollection(doctorEntity.services + serviceEntity)
        } else {
            throw IllegalArgumentException("Service already in doctor list")
        }
    }

    suspend fun removeService(doctorServiceRequest: DoctorServiceRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val doctorEntity = findEntity(doctorServiceRequest.doctorId)
        val serviceEntity = medicalService.findEntity(doctorServiceRequest.serviceId)
        if (doctorEntity.services.map { it.id.value }.contains(serviceEntity.id.value)) {
            doctorEntity.services = SizedCollection(doctorEntity.services - serviceEntity)
        } else {
            throw IllegalArgumentException("Service is not in doctor list for remove")
        }
    }
}