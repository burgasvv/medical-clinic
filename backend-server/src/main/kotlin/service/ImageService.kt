package org.burgas.service

import io.ktor.http.content.MultiPartData
import org.burgas.dao.ImageEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.ImageResponse
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UploadService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.UUID

class ImageService : ReadService<UUID, ImageEntity, ImageResponse>, UploadService<ImageEntity>, DeleteService<UUID> {

    override suspend fun findEntity(id: UUID): ImageEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        ImageEntity.findById(id) ?: throw IllegalArgumentException("Image not found")
    }

    override suspend fun findById(id: UUID): ImageResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun upload(multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val partData = multiPartData.readPart()!!
        ImageEntity.new { this.upload(partData) }
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        findEntity(id).delete()
    }
}