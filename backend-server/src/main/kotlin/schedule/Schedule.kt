package org.burgas.schedule

import io.ktor.server.application.*
import korlibs.time.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import org.burgas.dao.ScheduleEntity
import org.burgas.database.DatabaseConnection
import org.burgas.database.ScheduleTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection
import java.time.LocalDateTime

fun Application.configureSchedule() {

    launch(Dispatchers.Default) {
        while (true) {
            suspendTransaction (
                db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
            ) {
                ScheduleEntity.find { ScheduleTable.concluded eq false }.forEach {
                    val scheduleTime = it.dateTime.toJavaLocalDateTime()
                    val now = LocalDateTime.now()
                    if (scheduleTime.isBefore(now) && it.appointment == null) it.concluded = true
                }
                delay(10.seconds)
            }
        }
    }
}