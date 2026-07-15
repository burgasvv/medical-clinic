package org.burgas.service.contract

import io.ktor.http.content.*
import org.burgas.dao.File

interface UploadService<E : File> {

    suspend fun upload(multiPartData: MultiPartData): E
}