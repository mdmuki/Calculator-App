package com.example.data.repository

import com.example.data.local.CalculationHistory
import com.example.data.local.CalculationHistoryDao
import kotlinx.coroutines.flow.Flow

class CalculatorRepository(private val dao: CalculationHistoryDao) {
    val historyList: Flow<List<CalculationHistory>> = dao.getAllHistory()

    suspend fun saveCalculation(expression: String, result: String, isScientific: Boolean) {
        if (expression.isBlank() || result.isBlank() || result == "Error") return
        dao.insertHistory(
            CalculationHistory(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis(),
                isScientific = isScientific
            )
        )
    }

    suspend fun deleteHistory(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }
}
