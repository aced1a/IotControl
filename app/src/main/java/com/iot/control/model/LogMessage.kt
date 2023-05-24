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

    val address: String,
    val topic: String,
    val message: String,

    val success: Boolean = true
)
