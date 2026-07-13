package org.burgas.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection

object DatabaseConnection {

    private val config = ApplicationConfig("application.yaml")

    private val hikariConfig = HikariConfig()

    init {
        hikariConfig.driverClassName = "org.postgresql.Driver"
        hikariConfig.jdbcUrl = config.property("postgres.url").getString()
        hikariConfig.username = config.property("postgres.user").getString()
        hikariConfig.password = config.property("postgres.password").getString()
        hikariConfig.minimumIdle = 5
        hikariConfig.maximumPoolSize = 100
        hikariConfig.isAutoCommit = false
        hikariConfig.validate()
    }

    val postgres = Database.connect(
        datasource = HikariDataSource(hikariConfig),
        databaseConfig = DatabaseConfig { explicitDialect = PostgreSQLDialect() }
    )
}

object ImageTable : UUIDTable("image") {
    val name = varchar("name", 250)
    val contentType = varchar("content_type", 250).check { it like "image/%" }
    val preview = bool("preview").default(true)
    val data = blob("data")
}

object DocumentTable : UUIDTable("document") {
    val name = varchar("name", 250)
    val contentType = varchar("content_type", 100).check { it like "application/%" }
    val data = blob("data")
}

enum class Authority {
    ADMIN, USER, DOCTOR, PATIENT
}

object IdentityTable : UUIDTable("identity") {
    val authority = enumerationByName<Authority>("authority", 50).default(Authority.USER)
    val email = varchar("email", 50).uniqueIndex()
        .check { it regexp "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$" }
    val password = varchar("password", 250)
    val phone = varchar("phone", 20).uniqueIndex().check { it regexp "^\\+?[0-9\\s\\-\\(\\)]{7,25}$" }
    val status = bool("status").default(true)
    val firstname = varchar("firstname", 250)
    val lastname = varchar("lastname", 250)
    val patronymic = varchar("patronymic", 250)

    init {
        uniqueIndex("idx_id_authority", id, authority)
    }
}

object AdminTable : UUIDTable("admin") {
    val identityId = reference(
        name = "identity_id", refColumn = IdentityTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

object PatientTable : UUIDTable("patient") {
    val identityId = reference(
        name = "identity_id", refColumn = IdentityTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
    val passport = varchar("passport", 20).check { it regexp "^[0-9]{4}\\s[0-9]{6}$" }
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

object DepartmentTable : UUIDTable("department") {
    val name = varchar("name", 100).uniqueIndex()
    val description = text("description").uniqueIndex()
}

object CategoryTable : UUIDTable("category") {
    val name = varchar("name", 100).uniqueIndex()
    val description = text("description").uniqueIndex()
    val departmentId = reference(
        name = "department_id", refColumn = DepartmentTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
}

object DoctorTable : UUIDTable("doctor") {
    val identityId = reference(
        name = "identity_id", refColumn = IdentityTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
    val categoryId = optReference(
        name = "category_id", refColumn = CategoryTable.id,
        onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE
    )
    val about = text("about").nullable()
    val imageId = optReference(
        name = "image_id", refColumn = ImageTable.id,
        onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

object ServiceTable : UUIDTable("service") {
    val name = varchar("name", 250).uniqueIndex()
    val description = text("description").uniqueIndex()
    val price = double("price").default(0.0).check { it greaterEq 0.0 }
}

object DoctorServiceTable : CompositeIdTable("doctor_service") {
    val doctorId = reference(
        name = "doctor_id", refColumn = DoctorTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val serviceId = reference(
        name = "service_id", refColumn = ServiceTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(doctorId, serviceId))
    init {
        addIdColumn(doctorId)
        addIdColumn(serviceId)
    }
}

object ScheduleTable : IdTable<LocalDateTime>("schedule") {
    override val id: Column<EntityID<LocalDateTime>>
        get() = datetime("datetime").defaultExpression(CurrentDateTime).entityId()
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

object DoctorScheduleTable : CompositeIdTable("doctor_schedule") {
    val doctorId = reference(
        name = "doctor_id", refColumn = DoctorTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val dateTime = reference(
        name = "datetime", refColumn = ScheduleTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val busy = bool("busy").default(false)
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(doctorId, dateTime))
    init {
        addIdColumn(doctorId)
        addIdColumn(dateTime)
    }
}

object AppointmentTable : CompositeIdTable("appointment") {
    val doctorId = reference(
        name = "doctor_id", refColumn = DoctorScheduleTable.doctorId,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val dateTime = reference(
        name = "datetime", refColumn = DoctorScheduleTable.dateTime,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val patientId = reference(
        name = "patient_id", refColumn = PatientTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val serviceId = reference(
        name = "service_id", refColumn = ServiceTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val documentId = optReference(
        name = "document_id", refColumn = DocumentTable.id,
        onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
    val concluded = bool("concluded").default(false)
    val paid = bool("paid").default(false)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(doctorId, dateTime))

    init {
        addIdColumn(doctorId)
        addIdColumn(dateTime)
    }
}

object PaymentTable : CompositeIdTable("payment") {
    val doctorId = reference(
        name = "doctor_id", refColumn = AppointmentTable.doctorId,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val dateTime = reference(
        name = "datetime", refColumn = AppointmentTable.dateTime,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val price = double("price").default(0.0).check { it greaterEq 0.0 }
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(arrayOf(doctorId, dateTime))
    init {
        addIdColumn(doctorId)
        addIdColumn(dateTime)
    }
}

suspend fun configureDatabase() = suspendTransaction(
    db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
) {
    SchemaUtils.create(
        ImageTable, DocumentTable, IdentityTable, AdminTable, PatientTable,
        DepartmentTable, CategoryTable, DoctorTable, ServiceTable, DoctorServiceTable,
        ScheduleTable, DoctorScheduleTable, AppointmentTable, PaymentTable
    )
}