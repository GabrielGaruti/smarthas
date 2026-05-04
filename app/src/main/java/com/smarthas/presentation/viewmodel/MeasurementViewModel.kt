package com.smarthas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthas.data.database.Measurement
import com.smarthas.data.repository.MeasurementRepository
import com.smarthas.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MeasurementViewModel(
    private val measurementRepository: MeasurementRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val measurements: Flow<List<Measurement>> = measurementRepository.getAllMeasurements()

    val latestMeasurement: Flow<Measurement?> = measurementRepository.getLatestMeasurementFlow()

    private val _createState = MutableStateFlow<CreateMeasurementState>(CreateMeasurementState.Idle)
    val createState: StateFlow<CreateMeasurementState> = _createState

    fun createMeasurement(systolic: Int, diastolic: Int, notes: String? = null) {
        viewModelScope.launch {
            _createState.value = CreateMeasurementState.Loading
            try {
                val token = authRepository.getToken() ?: throw Exception("Token not found")
                
                // Configura fuso horário de Brasília
                val brazilTimeZone = TimeZone.getTimeZone("America/Sao_Paulo")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = brazilTimeZone }
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = brazilTimeZone }
                
                val currentDate = dateFormat.format(Date())
                val currentTime = timeFormat.format(Date())

                measurementRepository.createMeasurement(
                    token = token,
                    systolic = systolic,
                    diastolic = diastolic,
                    date = currentDate,
                    time = currentTime,
                    notes = notes
                )
                _createState.value = CreateMeasurementState.Success
            } catch (e: Exception) {
                _createState.value = CreateMeasurementState.Error(e.message ?: "Erro ao salvar")
            }
        }
    }

    fun deleteMeasurement(id: Int) {
        viewModelScope.launch {
            try {
                measurementRepository.deleteMeasurement(id)
            } catch (e: Exception) {
                _createState.value = CreateMeasurementState.Error("Erro ao deletar")
            }
        }
    }

    fun resetCreateState() {
        _createState.value = CreateMeasurementState.Idle
    }

    fun getClassification(systolic: Int, diastolic: Int): BloodPressureClassification {
        return when {
            systolic < 120 && diastolic < 80 -> BloodPressureClassification.NORMAL
            systolic in 120..139 || diastolic in 80..89 -> BloodPressureClassification.ELEVATED
            else -> BloodPressureClassification.HYPERTENSION
        }
    }
}

sealed class CreateMeasurementState {
    object Idle : CreateMeasurementState()
    object Loading : CreateMeasurementState()
    object Success : CreateMeasurementState()
    data class Error(val message: String) : CreateMeasurementState()
}

enum class BloodPressureClassification(val label: String, val colorResId: Int) {
    NORMAL("Normal", android.R.color.holo_green_light),
    ELEVATED("Elevada", android.R.color.holo_orange_light),
    HYPERTENSION("Hipertensão", android.R.color.holo_red_light)
}
