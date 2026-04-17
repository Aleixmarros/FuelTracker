package com.aleixmaro.fueltracker.data.repository

import com.aleixmaro.fueltracker.data.local.dao.RefuelDao
import com.aleixmaro.fueltracker.data.local.entity.RefuelEntity
import kotlinx.coroutines.flow.Flow

class RefuelRepository(private val dao: RefuelDao) {

    suspend fun insert(record: RefuelEntity) = dao.insertRecord(record)

    fun getAllRefuels(): Flow<List<RefuelEntity>> =
        dao.getAllRecordsFlow()
    suspend fun delete(record: RefuelEntity) = dao.deleteRecord(record)

    suspend fun update(record: RefuelEntity) = dao.updateRecord(record)

    suspend fun getById(id: Long) = dao.getById(id)

}
