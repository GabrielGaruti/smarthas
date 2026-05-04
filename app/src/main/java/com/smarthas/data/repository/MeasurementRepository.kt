package com.smarthas.data.repository

import com.smarthas.data.api.SmartHasApi
import com.smarthas.data.api.MeasurementRequest
import com.smarthas.data.database.MeasurementDao
import com.smarthas.data.database.Measurement
import kotlinx.coroutines.flow.Flow

class MeasurementRepository(
    private val api: SmartHasApi,
    private val dao: MeasurementDao
) {
    fun getAllMeasurements(): Flow<List<Measurement>> = dao.getAllMeasurements()

    fun getMeasurementCount(): Flow<Int> = dao.getMeasurementCount()

    suspend fun createMeasurement(
        token: String,
        systolic: Int,
        diastolic: Int,
        date: String,
        time: String,
        notes: String?
    ) {
        val request = MeasurementRequest(systolic, diastolic, date, time, notes)
        try {
            val response = api.createMeasurement("Bearer $token", request)
            // Save locally
            val measurement = Measurement(
                systolic = response.systolic,
                diastolic = response.diastolic,
                date = response.date,
                time = response.time,
                notes = response.notes
            )
            dao.insert(measurement)
        } catch (e: Exception) {
            // If API fails, still save locally
            val measurement = Measurement(
                systolic = systolic,
                diastolic = diastolic,
                date = date,
                time = time,
                notes = notes
            )
            dao.insert(measurement)
        }
    }

    suspend fun deleteMeasurement(id: Int) {
        dao.delete(id)
    }

    suspend fun getLatestMeasurement(): Measurement? {
        return dao.getLatestMeasurement()
    }

    fun getLatestMeasurementFlow(): Flow<Measurement?> {
        return dao.getLatestMeasurementFlow()
    }
}
