package org.burgas.service

import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.IdentityRequest
import org.burgas.dto.IdentityResponse
import org.burgas.service.contract.ReadService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection
import java.util.*

class IdentityService : ReadService<UUID, IdentityEntity, IdentityResponse> {

    override suspend fun findEntity(id: UUID): IdentityEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.findById(id) ?: throw IllegalArgumentException("Identity not found")
    }

    override suspend fun findById(id: UUID): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    suspend fun changeStatus(identityRequest: IdentityRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = findEntity(identityRequest.id!!)
        if (identityEntity.status == identityRequest.status!!) throw IllegalArgumentException("Statuses matched")
        identityEntity.status = identityRequest.status
    }

    suspend fun changePassword(identityRequest: IdentityRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = findEntity(identityRequest.id!!)
        if (BCrypt.checkpw(identityRequest.password!!, identityEntity.password))
            throw IllegalArgumentException("Passwords matched")
        identityEntity.password = BCrypt.hashpw(identityRequest.password, BCrypt.gensalt())
    }
}