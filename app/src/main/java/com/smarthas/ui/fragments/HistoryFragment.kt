package com.smarthas.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smarthas.R
import com.smarthas.data.api.SmartHasApi
import com.smarthas.data.database.AppDatabase
import com.smarthas.data.database.Measurement
import com.smarthas.data.preferences.TokenManager
import com.smarthas.data.repository.AuthRepository
import com.smarthas.data.repository.MeasurementRepository
import com.smarthas.presentation.viewmodel.MeasurementViewModel
import android.widget.ArrayAdapter
import android.widget.Spinner
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var viewModel: MeasurementViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var filterSpinner: Spinner
    private lateinit var adapter: MeasurementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_measurements)
        filterSpinner = view.findViewById(R.id.spinner_filter)

        // Initialize ViewModel
        val tokenManager = TokenManager(requireContext())
        val api = SmartHasApi.create { tokenManager.getToken() }
        val database = AppDatabase.getDatabase(requireContext())
        val measurementRepository = MeasurementRepository(api, database.measurementDao())
        val authRepository = AuthRepository(api, tokenManager)
        val factory = MeasurementViewModelFactory(measurementRepository, authRepository)
        viewModel = ViewModelProvider(this, factory).get(MeasurementViewModel::class.java)

        // Setup RecyclerView
        adapter = MeasurementAdapter(emptyList(), viewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Setup filter spinner
        val filterOptions = arrayOf("Todas", "Normal", "Elevada", "Hipertensão")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        filterSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                updateFilter(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        // Observe measurements
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.measurements.collect { measurements ->
                adapter.updateData(measurements)
            }
        }
    }

    private fun updateFilter(filterPosition: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.measurements.collect { measurements ->
                val filtered = when (filterPosition) {
                    0 -> measurements // Todas
                    1 -> measurements.filter { // Normal
                        val classification = viewModel.getClassification(it.systolic, it.diastolic)
                        classification.ordinal == 0
                    }
                    2 -> measurements.filter { // Elevada
                        val classification = viewModel.getClassification(it.systolic, it.diastolic)
                        classification.ordinal == 1
                    }
                    3 -> measurements.filter { // Hipertensão
                        val classification = viewModel.getClassification(it.systolic, it.diastolic)
                        classification.ordinal == 2
                    }
                    else -> measurements
                }
                adapter.updateData(filtered)
            }
        }
    }
}

class MeasurementAdapter(
    private var measurements: List<Measurement>,
    private val viewModel: MeasurementViewModel
) : RecyclerView.Adapter<MeasurementAdapter.MeasurementViewHolder>() {

    inner class MeasurementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(measurement: Measurement) {
            val textView = itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.txt_measurement_item)
            val classification = viewModel.getClassification(measurement.systolic, measurement.diastolic)
            
            val baseText = "${measurement.systolic}/${measurement.diastolic} mmHg - ${classification.label}\n${measurement.date} ${measurement.time}"
            val finalText = if (!measurement.notes.isNullOrBlank()) {
                "$baseText\nObs: ${measurement.notes}"
            } else {
                baseText
            }
            textView.text = finalText

            val color = when (classification.ordinal) {
                0 -> android.R.color.holo_green_light // Normal
                1 -> android.R.color.holo_orange_light // Elevated
                2 -> android.R.color.holo_red_light    // Hypertension
                else -> android.R.color.black
            }
            textView.setTextColor(itemView.resources.getColor(color, null))
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MeasurementViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_measurement, parent, false)
        return MeasurementViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        holder.bind(measurements[position])
    }

    override fun getItemCount() = measurements.size

    fun updateData(newData: List<Measurement>) {
        measurements = newData
        notifyDataSetChanged()
    }
}
