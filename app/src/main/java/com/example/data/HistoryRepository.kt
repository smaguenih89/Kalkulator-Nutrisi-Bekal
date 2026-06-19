package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositori untuk menjembatani query database antara ViewModel dan DAO
 */
class HistoryRepository(private val historyDao: HistoryDao) {
    
    // Aliran data reaktif berurutan waktu (Flow)
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    
    suspend fun insert(history: HistoryEntity) {
        historyDao.insertHistory(history)
    }
    
    suspend fun deleteById(id: Int) {
        historyDao.deleteHistoryById(id)
    }
    
    suspend fun clearAll() {
        historyDao.clearAllHistory()
    }
}
