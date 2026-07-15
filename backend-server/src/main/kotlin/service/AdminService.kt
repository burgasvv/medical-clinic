package org.burgas.service

import org.burgas.dao.AdminEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AdminRequest
import org.burgas.dto.AdminResponse
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class AdminService : CollectService<AdminResponse>, ReadService<UUID, AdminEntity, AdminResponse>,
    CreateService<AdminRequest, AdminResponse>, UpdateService<AdminRequest, AdminResponse>, DeleteService<UUID> {

    override suspend fun findAll(): List<AdminResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        AdminEntity.all().with(AdminEntity::identity).map { it.toResponse() }
    }

    override suspend fun findEntity(id: UUID): AdminEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        AdminEntity.findById(id) ?: throw IllegalArgumentException("Admin not found")
    }

    override suspend fun findById(id: UUID): AdminResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: AdminRequest): AdminResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        AdminEntity.new { this.create(request) }.load(AdminEntity::identity).toResponse()
    }

    override suspend fun update(request: AdminRequest): AdminResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        AdminEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!.toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val adminEntity = findEntity(id)
        adminEntity.delete()
        adminEntity.identity.delete()
    }
}