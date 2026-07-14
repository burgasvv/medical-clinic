package org.burgas.service.contract

import org.burgas.dto.Request
import org.burgas.dto.Response

interface UpdateService<Req : Request, Res : Response> {

    suspend fun update(request: Req): Res
}