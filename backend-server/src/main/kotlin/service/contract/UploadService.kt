package org.burgas.service.contract

import io.ktor.http.content.*
import org.jetbrains.exposed.v1.dao.java.UUIDEntity

interface UploadService<E : UUIDEntity> {

    suspend fun upload(multiPartData: MultiPartData): E
}