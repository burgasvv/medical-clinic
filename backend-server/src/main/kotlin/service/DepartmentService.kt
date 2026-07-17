package org.burgas.service

import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.burgas.dao.DepartmentEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.DepartmentRequest
import org.burgas.dto.DepartmentResponse
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.util.*

class DepartmentService : CollectService<DepartmentResponse>, ReadService<UUID, DepartmentEntity, DepartmentResponse>,
    CreateService<DepartmentRequest, DepartmentResponse>, UpdateService<DepartmentRequest, DepartmentResponse>,
    DeleteService<UUID>, PoiService {

    override suspend fun findAll(): List<DepartmentResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        DepartmentEntity.all().with(DepartmentEntity::categories).map { it.toResponse() }
    }

    override suspend fun findEntity(id: UUID): DepartmentEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        (DepartmentEntity.findById(id) ?: throw IllegalArgumentException("Department not found"))
            .load(DepartmentEntity::categories)
    }

    override suspend fun findById(id: UUID): DepartmentResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    override suspend fun create(request: DepartmentRequest): DepartmentResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        DepartmentEntity.new { this.create(request) }.load(DepartmentEntity::categories).toResponse()
    }

    override suspend fun update(request: DepartmentRequest): DepartmentResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        DepartmentEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!
            .load(DepartmentEntity::categories).toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        findEntity(id).delete()
    }

    @OptIn(InternalAPI::class)
    override suspend fun createByDocument(multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val fileItem = multiPartData.asFlow().filterIsInstance<PartData.FileItem>().first()
        XSSFWorkbook(fileItem.provider().readBuffer.inputStream()).use {
            it.getSheetAt(0).forEach { row ->
                val departmentRequest = DepartmentRequest(
                    name = row.getCell(0)!!.stringCellValue,
                    description = row.getCell(1)!!.stringCellValue
                )
                DepartmentEntity.new { this.create(departmentRequest) }
            }
        }
    }
}