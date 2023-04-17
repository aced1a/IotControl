package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iot.control.model.enums.DeviceType
import java.util.UUID

@Entity
data class Device(@PrimaryKey val id: UUID = UUID.randomUUID(),
                  var name: String="",
                  var value: String="",
                  var type: DeviceType = DeviceType.Button,
                  @ColumnInfo(name="is_displayable") var idDisplayable: Boolean=true,
                  @ColumnInfo(name="mqtt_id") var mqttConnectionId: UUID?=null,
                  @ColumnInfo(name="sms_id") var smsConnectionId: UUID?=null)
