package com.smarthas.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.smarthas.R
import com.smarthas.data.api.SmartHasApi
import com.smarthas.data.database.AppDatabase
import com.smarthas.data.preferences.TokenManager
import com.smarthas.data.repository.AuthRepository
import com.smarthas.data.repository.MeasurementRepository
import com.smarthas.presentation.viewmodel.MeasurementViewModel
import com.smarthas.presentation.viewmodel.CreateMeasurementState
import kotlinx.coroutines.launch

class NewMeasurementFragment : Fragment() {

    private lateinit var viewModel: MeasurementViewModel
    private lateinit var systolicInput: TextInputEditText
    private lateinit var diastolicInput: TextInputEditText
    private lateinit var notesInput: TextInputEditText
    private lateinit var saveButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_measurement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        systolicInput = view.findViewById(R.id.input_systolic)
        diastolicInput = view.findViewById(R.id.input_diastolic)
        notesInput = view.findViewById(R.id.input_notes)
        saveButton = view.findViewById(R.id.btn_save)

        // Initialize ViewModel
        val tokenManager = TokenManager(requireContext())
        val api = SmartHasApi.create { tokenManager.getToken() }
        val database = AppDatabase.getDatabase(requireContext())
        val measurementRepository = MeasurementRepository(api, database.measurementDao())
        val authRepository = AuthRepository(api, tokenManager)
        val factory = MeasurementViewModelFactory(measurementRepository, authRepository)
        viewModel = ViewModelProvider(this, factory).get(MeasurementViewModel::class.java)

        // Set up listeners
        saveButton.setOnClickListener {
            val systolic = systolicInput.text.toString().trim()
            val diastolic = diastolicInput.text.toString().trim()
            val notes = notesInput.text.toString().trim()

            if (validateInputs(systolic, diastolic)) {
                viewModel.createMeasurement(
                    systolic = systolic.toInt(),
                    diastolic = diastolic.toInt(),
                    notes = notes.ifEmpty { null }
                )
            }
        }

        // Observe create state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createState.collect { state ->
                when (state) {
                    CreateMeasurementState.Loading -> {
                        saveButton.isEnabled = false
                        saveButton.text = "Salvando..."
                    }
                    CreateMeasurementState.Success -> {
                        Toast.makeText(requireContext(), "Medição salva com sucesso!", Toast.LENGTH_SHORT).show()
                        clearInputs()
                        saveButton.isEnabled = true
                        saveButton.text = getString(R.string.measurement_save_button)
                        viewModel.resetCreateState()
                        findNavController().popBackStack()
                    }
                    is CreateMeasurementState.Error -> {
                        saveButton.isEnabled = true
                        saveButton.text = getString(R.string.measurement_save_button)
                        Toast.makeText(
                            requireContext(),
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CreateMeasurementState.Idle -> {
                        saveButton.isEnabled = true
                        saveButton.text = getString(R.string.measurement_save_button)
                    }
                }
            }
        }
    }

    private fun validateInputs(systolic: String, diastolic: String): Boolean {
        return when {
            systolic.isEmpty() -> {
                Toast.makeText(requireContext(), "Sistólica é obrigatória", Toast.LENGTH_SHORT).show()
                false
            }
            diastolic.isEmpty() -> {
                Toast.makeText(requireContext(), "Diastólica é obrigatória", Toast.LENGTH_SHORT).show()
                false
            }
            systolic.toIntOrNull() == null -> {
                Toast.makeText(requireContext(), "Sistólica deve ser um número", Toast.LENGTH_SHORT).show()
                false
            }
            diastolic.toIntOrNull() == null -> {
                Toast.makeText(requireContext(), "Diastólica deve ser um número", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun clearInputs() {
        systolicInput.text?.clear()
        diastolicInput.text?.clear()
        notesInput.text?.clear()
    }
}
