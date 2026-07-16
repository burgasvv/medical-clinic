package org.burgas.service

import org.burgas.dao.ServiceEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.ServiceRequest
import org.burgas.dto.ServiceResponse
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class MedicalService : CollectService<ServiceResponse>, ReadService<UUID, ServiceEntity, ServiceResponse>,
    CreateService<ServiceRequest, ServiceResponse>, UpdateService<ServiceRequest, ServiceResponse>, DeleteService<UUID> {

    override suspend fun findAll(): List<ServiceResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        ServiceEntity.all().with(ServiceEntity::doctors).map { it.toResponse() }
    }

    override suspend fun findEntity(id: UUID): ServiceEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        (ServiceEntity.findById(id) ?: throw IllegalArgumentException("Service not found"))
            .load(ServiceEntity::doctors)
    }

    override suspend fun findById(id: UUID): ServiceResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: ServiceRequest): ServiceResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ServiceEntity.new { this.create(request) }.load(ServiceEntity::doctors).toResponse()
    }

    override suspend fun update(request: ServiceRequest): ServiceResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ServiceEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(ServiceEntity::doctors).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        findEntity(id).delete()
    }
}