package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) untuk manipulasi data riwayat bekal di Room Database
 */
@Dao
interface HistoryDao {
    
    @Query("SELECT * FROM lunch_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)
    
    @Query("DELETE FROM lunch_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)
    
    @Query("DELETE FROM lunch_history")
    suspend fun clearAllHistory()
}
