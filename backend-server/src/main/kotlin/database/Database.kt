package org.burgas.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.burgas.dao.AdminEntity
import org.burgas.dao.IdentityEntity
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection
import java.util.*

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

object DoctorServiceTable : Table("doctor_service") {
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
}

object ScheduleTable : UUIDTable("schedule") {
    val dateTime = datetime("datetime").defaultExpression(CurrentDateTime)
    val doctorId = reference(
        name = "doctor_id", refColumn = DoctorTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val concluded = bool("concluded").default(false)
    init {
        uniqueIndex(dateTime, doctorId)
    }
}

object AppointmentTable : UUIDTable("appointment") {
    val scheduleId = reference(
        name = "schedule_id", refColumn = ScheduleTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
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
}

object PaymentTable : UUIDTable("payment") {
    val appointmentId = reference(
        name = "appointment_id", refColumn = AppointmentTable.id,
        onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE
    )
    val price = double("price").default(0.0).check { it greaterEq 0.0 }
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

suspend fun configureDatabase() = suspendTransaction(
    db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
) {
    SchemaUtils.create(
        ImageTable, DocumentTable, IdentityTable, AdminTable, PatientTable,
        DepartmentTable, CategoryTable, DoctorTable, ServiceTable, DoctorServiceTable,
        ScheduleTable, AppointmentTable, PaymentTable
    )
    val burgasIdentityId = UUID.fromString("6d8c7d99-0a45-454d-9bf2-cc0c8d2bd079")
    val burgasIdentity = IdentityEntity.findById(burgasIdentityId) ?: IdentityEntity.new(burgasIdentityId) {
        authority = Authority.ADMIN
        email = "burgasvv@gmail.com"
        password = BCrypt.hashpw("burgasvv", BCrypt.gensalt())
        phone = "+79123456798"
        status = true
        firstname = "Бургас"
        lastname = "Вячеслав"
        patronymic = "Васильевич"
    }
    val burgasAdminId = UUID.fromString("a091ed91-7446-4009-a160-02b4a1d556b0")
    AdminEntity.findById(burgasAdminId) ?: AdminEntity.new(burgasAdminId) {
        identity = burgasIdentity
    }
    return@suspendTransaction
}