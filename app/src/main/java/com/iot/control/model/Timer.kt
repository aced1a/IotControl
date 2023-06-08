package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@Entity
data class Timer(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val date: Date?,
    val repeat: Boolean,
    val interval: Long,
    @ColumnInfo(name="boot_init") val initOnBoot: Boolean
) {
    fun formatDate(): String {
        val format = SimpleDateFormat("dd.MM.yyyy hh:mm")

        return format.format(date ?: Date())
    }
}