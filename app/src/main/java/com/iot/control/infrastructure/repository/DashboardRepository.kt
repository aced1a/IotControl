package com.iot.control.infrastructure.repository

import com.iot.control.infrastructure.dao.DashboardDao
import com.iot.control.model.Dashboard


class DashboardRepository(val dao: DashboardDao) {

    fun getAll() = dao.getAll()

    suspend fun add(dashboard: Dashboard) = dao.add(dashboard)

    suspend fun update(dashboard: Dashboard) = dao.update(dashboard)

    suspend fun delete(dashboard: Dashboard) = dao.delete(dashboard)
}