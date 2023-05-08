package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Timer(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val timer: Date,
    val repeat: Boolean,
    val interval: Int,
    @ColumnInfo(name="boot_init") val initOnBoot: Boolean
)