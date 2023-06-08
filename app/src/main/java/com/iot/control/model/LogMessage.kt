package com.iot.control.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class LogMessage(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val date: Date = Date(),

    val resolved: Boolean = false,
    val event: Boolean = true,

    val name: String? = null,
    val address: String? = null,
    val topic: String? = null,
    val message: String,
)
