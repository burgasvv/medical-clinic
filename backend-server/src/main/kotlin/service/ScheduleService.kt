package org.burgas.service

import org.burgas.dao.ScheduleEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.ScheduleRequest
import org.burgas.dto.ScheduleResponse
import org.burgas.service.contract.CreateService
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UpdateService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class ScheduleService : ReadService<UUID, ScheduleEntity, ScheduleResponse>,
    CreateService<ScheduleRequest, ScheduleResponse>, UpdateService<ScheduleRequest, ScheduleResponse>,
    DeleteService<UUID> {

    override suspend fun findEntity(id: UUID): ScheduleEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        (ScheduleEntity.findById(id) ?: throw IllegalArgumentException("Schedule not found"))
            .load(ScheduleEntity::doctor, ScheduleEntity::appointment)
    }

    override suspend fun findById(id: UUID): ScheduleResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: ScheduleRequest): ScheduleResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ScheduleEntity.new { this.create(request) }
            .load(ScheduleEntity::doctor, ScheduleEntity::appointment).toResponse()
    }

    override suspend fun update(request: ScheduleRequest): ScheduleResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        ScheduleEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(ScheduleEntity::doctor, ScheduleEntity::appointment).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        findEntity(id).delete()
    }
}