package org.burgas.service

import org.burgas.dao.CategoryEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.CategoryRequest
import org.burgas.dto.CategoryResponse
import org.burgas.service.contract.CreateService
import org.burgas.service.contract.DeleteService
import org.burgas.service.contract.ReadService
import org.burgas.service.contract.UpdateService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.UUID

class CategoryService : ReadService<UUID, CategoryEntity, CategoryResponse>,
    CreateService<CategoryRequest, CategoryResponse>, UpdateService<CategoryRequest, CategoryResponse>, DeleteService<UUID> {

    override suspend fun findEntity(id: UUID): CategoryEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        (CategoryEntity.findById(id) ?: throw IllegalArgumentException("Category not found"))
            .load(CategoryEntity::department, CategoryEntity::doctors)
    }

    override suspend fun findById(id: UUID): CategoryResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: CategoryRequest): CategoryResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        CategoryEntity.new { this.create(request) }
            .load(CategoryEntity::department, CategoryEntity::doctors).toResponse()
    }

    override suspend fun update(request: CategoryRequest): CategoryResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        CategoryEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(CategoryEntity::department, CategoryEntity::doctors).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
    db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        findEntity(id).delete()
    }
}