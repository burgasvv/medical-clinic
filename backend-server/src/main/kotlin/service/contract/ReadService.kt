package org.burgas.service.contract

import org.burgas.dto.Response
import org.jetbrains.exposed.v1.dao.java.UUIDEntity

interface ReadService<ID, E : UUIDEntity, R : Response> {

    suspend fun findEntity(id: ID): E

    suspend fun findById(id: ID): R
}