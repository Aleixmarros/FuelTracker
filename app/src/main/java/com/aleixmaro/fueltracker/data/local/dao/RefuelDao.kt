package com.aleixmaro.fueltracker.data.local.dao

import androidx.room.*
import com.aleixmaro.fueltracker.data.local.entity.RefuelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RefuelEntity)

    @Query("SELECT * FROM refuel_records ORDER BY fecha DESC")
    fun getAllRecordsFlow(): Flow<List<RefuelEntity>>

    @Delete
    suspend fun deleteRecord(record: RefuelEntity)

    @Update
    suspend fun updateRecord(record: RefuelEntity)

    @Query("SELECT * FROM refuel_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RefuelEntity?

}
