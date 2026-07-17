package org.burgas.service.contract

import io.ktor.http.content.MultiPartData

interface PoiService {

    suspend fun createByDocument(multiPartData: MultiPartData)
}