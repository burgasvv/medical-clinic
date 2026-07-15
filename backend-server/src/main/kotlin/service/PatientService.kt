package org.burgas.service

import org.burgas.dao.PatientEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.PatientRequest
import org.burgas.dto.PatientResponse
import org.burgas.service.contract.CollectService
import org.burgas.service.contract.CreateService
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UpdateService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class PatientService : CollectService<PatientResponse>, ReadService<UUID, PatientEntity, PatientResponse>,
    CreateService<PatientRequest, PatientResponse>, UpdateService<PatientRequest, PatientResponse>, DeleteService<UUID> {

    override suspend fun findAll(): List<PatientResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        PatientEntity.all()
            .with(PatientEntity::identity, PatientEntity::appointments)
            .map { it.toResponse() }
    }

    override suspend fun findEntity(id: UUID): PatientEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        PatientEntity.findById(id) ?: throw IllegalArgumentException("Patient not found")
    }

    override suspend fun findById(id: UUID): PatientResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: PatientRequest): PatientResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        PatientEntity.new { this.create(request) }
            .load(PatientEntity::identity, PatientEntity::appointments).toResponse()
    }

    override suspend fun update(request: PatientRequest): PatientResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        PatientEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(PatientEntity::identity, PatientEntity::appointments).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val patientEntity = findEntity(id)
        patientEntity.delete()
        patientEntity.identity.delete()
    }
}