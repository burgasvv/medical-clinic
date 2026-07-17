package org.burgas.service

import io.ktor.http.content.*
import org.burgas.dao.AppointmentEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AppointmentRequest
import org.burgas.dto.AppointmentResponse
import org.burgas.service.contract.CreateService
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class AppointmentService : ReadService<UUID, AppointmentEntity, AppointmentResponse>,
    CreateService<AppointmentRequest, AppointmentResponse>, DeleteService<UUID> {

    private val documentService: DocumentService

    constructor(documentService: DocumentService) {
        this.documentService = documentService
    }

    override suspend fun findEntity(id: UUID): AppointmentEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        (AppointmentEntity.findById(id) ?: throw IllegalArgumentException("Appointment not found"))
            .load(
                AppointmentEntity::schedule, AppointmentEntity::patient,
                AppointmentEntity::service, AppointmentEntity::payment, AppointmentEntity::document
            )
    }

    override suspend fun findById(id: UUID): AppointmentResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: AppointmentRequest): AppointmentResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        AppointmentEntity.new { this.create(request) }
            .load(
                AppointmentEntity::schedule, AppointmentEntity::patient,
                AppointmentEntity::service, AppointmentEntity::payment, AppointmentEntity::document
            )
            .toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val appointmentEntity = findEntity(id)
        if (!appointmentEntity.schedule.concluded) {
            appointmentEntity.document?.delete()
            appointmentEntity.delete()
        } else {
            throw IllegalArgumentException("Appointment already concluded and can't be deleted")
        }
    }

    suspend fun addDocument(appointmentId: UUID, multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val appointmentEntity = findEntity(appointmentId)
        if (appointmentEntity.document == null) {
            appointmentEntity.document = documentService.upload(multiPartData)
        } else {
            throw IllegalArgumentException("Appointment document already set")
        }
    }

    suspend fun removeDocument(appointmentId: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val appointmentEntity = findEntity(appointmentId)
        val document = appointmentEntity.document
        if (document != null) {
            appointmentEntity.document = null
            document.delete()
        } else {
            throw IllegalArgumentException("Appointment document is null and can't be removed")
        }
    }

    suspend fun conclude(appointmentId: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val appointmentEntity = findEntity(appointmentId)
        if (!appointmentEntity.schedule.concluded) {
            appointmentEntity.schedule.concluded = true
        } else {
            throw IllegalArgumentException("Appointment already concluded")
        }
    }
}