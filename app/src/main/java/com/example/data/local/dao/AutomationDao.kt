package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AutomationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automations")
    fun getAllAutomations(): Flow<List<AutomationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomation(entity: AutomationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<AutomationEntity>)

    @Query("UPDATE automations SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE automations SET lastExecutedTime = :timestamp WHERE id = :id")
    suspend fun updateLastExecuted(id: String, timestamp: Long)

    @Query("DELETE FROM automations WHERE id = :id")
    suspend fun deleteAutomation(id: String)

    @Query("DELETE FROM automations")
    suspend fun clearAllAutomations()

    @Query("SELECT COUNT(*) FROM automations")
    suspend fun countAutomations(): Int
}
