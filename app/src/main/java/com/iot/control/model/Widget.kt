package com.iot.control.model

import java.util.UUID

data class Widget(
    val id: UUID = UUID.randomUUID(),
    val type: Int,
    val formatter: String?,

    val deviceId: UUID
)