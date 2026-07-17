package org.burgas.service

import org.burgas.dao.PaymentEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.PaymentRequest
import org.burgas.dto.PaymentResponse
import org.burgas.service.contract.CreateService
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class PaymentService : ReadService<UUID, PaymentEntity, PaymentResponse>,
    CreateService<PaymentRequest, PaymentResponse>, DeleteService<UUID> {

    override suspend fun findEntity(id: UUID): PaymentEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        (PaymentEntity.findById(id) ?: throw IllegalArgumentException("Payment not found"))
            .load(PaymentEntity::appointment)
    }

    override suspend fun findById(id: UUID): PaymentResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: PaymentRequest): PaymentResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        PaymentEntity.new { this.create(request) }.load(PaymentEntity::appointment).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).delete()
    }
}