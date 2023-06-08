package com.iot.control.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Dashboard(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val order: Int
)
