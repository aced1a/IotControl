package com.iot.control.infrastructure.repository

import com.iot.control.infrastructure.dao.WidgetDao
import com.iot.control.model.Widget

class WidgetRepository(val dao: WidgetDao) {

    fun getAll() = dao.getAll()

    suspend fun add(widget: Widget) = dao.add(widget)

    suspend fun update(widget: Widget) = dao.update(widget)

    suspend fun delete(widget: Widget) = dao.delete(widget)
}