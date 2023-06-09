package com.iot.control.model

import com.iot.control.model.enums.EventType
import java.util.UUID

data class EventDto(
    val deviceId: UUID,
    val name: String,
    val type: EventType,
)


