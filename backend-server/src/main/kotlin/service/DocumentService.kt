package org.burgas.service

import io.ktor.http.content.MultiPartData
import org.burgas.dao.DocumentEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.DocumentResponse
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UploadService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.UUID

class DocumentService : ReadService<UUID, DocumentEntity, DocumentResponse>,
    UploadService<DocumentEntity>, DeleteService<UUID> {

    override suspend fun findEntity(id: UUID): DocumentEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        DocumentEntity.findById(id) ?: throw IllegalArgumentException("Document not found")
    }

    override suspend fun findById(id: UUID): DocumentResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun upload(multiPartData: MultiPartData): DocumentEntity = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val partData = multiPartData.readPart()!!
        DocumentEntity.new { this.upload(partData) }
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        findEntity(id).delete()
    }
}