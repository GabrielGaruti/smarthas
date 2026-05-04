package com.smarthas.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.smarthas.R
import com.smarthas.data.api.SmartHasApi
import com.smarthas.data.database.AppDatabase
import com.smarthas.data.preferences.TokenManager
import com.smarthas.data.repository.AuthRepository
import com.smarthas.data.repository.MeasurementRepository
import com.smarthas.presentation.viewmodel.MeasurementViewModel
import com.smarthas.presentation.viewmodel.BloodPressureClassification
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var viewModel: MeasurementViewModel
    private lateinit var greetingText: MaterialTextView
    private lateinit var lastMeasurementText: MaterialTextView
    private lateinit var statusText: MaterialTextView
    private lateinit var newMeasurementButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        greetingText = view.findViewById(R.id.txt_greeting)
        lastMeasurementText = view.findViewById(R.id.txt_last_measurement)
        statusText = view.findViewById(R.id.txt_status)
        newMeasurementButton = view.findViewById(R.id.btn_new_measurement)

        // Initialize ViewModel
        val tokenManager = TokenManager(requireContext())
        val api = SmartHasApi.create { tokenManager.getToken() }
        val database = AppDatabase.getDatabase(requireContext())
        val measurementRepository = MeasurementRepository(api, database.measurementDao())
        val authRepository = AuthRepository(api, tokenManager)
        val factory = MeasurementViewModelFactory(measurementRepository, authRepository)
        viewModel = ViewModelProvider(this, factory).get(MeasurementViewModel::class.java)

        // Set greeting
        val userName = tokenManager.getUserName() ?: "Usuário"
        greetingText.text = "Olá, $userName 👋"

        // Set up listeners
        newMeasurementButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_new_measurement)
        }

        // Observe latest measurement
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.latestMeasurement.collect { measurement ->
                if (measurement != null) {
                    val baseText = "${measurement.systolic}/${measurement.diastolic} mmHg"
                    val timeText = "\n${measurement.date} às ${measurement.time}"
                    val noteText = if (!measurement.notes.isNullOrBlank()) "\nObs: ${measurement.notes}" else ""
                    
                    lastMeasurementText.text = "$baseText$timeText$noteText"
                    
                    val classification = viewModel.getClassification(measurement.systolic, measurement.diastolic)
                    statusText.text = classification.label
                    
                    // Set color based on classification
                    val color = when (classification) {
                        BloodPressureClassification.NORMAL -> android.R.color.holo_green_light
                        BloodPressureClassification.ELEVATED -> android.R.color.holo_orange_light
                        BloodPressureClassification.HYPERTENSION -> android.R.color.holo_red_light
                    }
                    statusText.setTextColor(resources.getColor(color, null))
                } else {
                    lastMeasurementText.text = "Nenhuma medição registrada"
                    statusText.text = "-"
                }
            }
        }
    }
}

class MeasurementViewModelFactory(
    private val measurementRepository: MeasurementRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return MeasurementViewModel(measurementRepository, authRepository) as T
    }
}
