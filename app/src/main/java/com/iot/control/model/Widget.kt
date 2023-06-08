package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.iot.control.model.enums.Icons
import com.iot.control.model.enums.WidgetType
import java.util.UUID

@Entity(foreignKeys = [
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Device::class, parentColumns = ["id"], childColumns = ["device_id"]),
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Dashboard::class, parentColumns = ["id"], childColumns = ["dashboard_id"])
])
data class Widget(
    @PrimaryKey val id: UUID = UUID.randomUUID(),

    val name: String,
    val type: WidgetType,
    val useIcon: Boolean,
    val formatter: String? = null,
    val subtext: String? = null,
    val icon: Icons,
    val expanded: Boolean,

    var onColor: Long = (18408873036368314368UL).toLong(),
    var offColor: Long = (18408873036368314368UL).toLong(),
    val order: Int = 0,

    @ColumnInfo(name="dashboard_id", index=true) val dashboardId: UUID,
    @ColumnInfo(name="device_id", index = true) val deviceId: UUID
)


data class WidgetAndDevice(
    @Embedded val widget: Widget,
    @Relation(
        parentColumn = "device_id",
        entityColumn = "id"
    )
    val device: Device
)