// lib/screens/history_screen.dart
// Espelha HistoryFragment.kt — RecyclerView + spinner de filtros
// Flutter: ListView + DropdownButton com mesmas opções de filtro

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/measurement_provider.dart';
import '../models/measurement.dart';
import '../widgets/pressure_card.dart';
import '../widgets/pressure_chart.dart';

class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  // Equivalente ao filterOptions do HistoryFragment.kt
  BloodPressureClassification? _selectedFilter;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Histórico')),
      body: Consumer<MeasurementProvider>(
        builder: (context, provider, _) {
          final filtered = provider.filterByClassification(_selectedFilter);

          return Column(
            children: [
              // Gráfico de tendência — visível quando há ≥ 2 medições
              if (provider.measurements.length >= 2)
                PressureChart(measurements: provider.measurements),

              // Filtro — equivalente ao spinner_filter do HistoryFragment.kt
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                child: Row(
                  children: [
                    const Text(
                      'Filtrar por: ',
                      style: TextStyle(fontWeight: FontWeight.w500),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: DropdownButton<BloodPressureClassification?>(
                        value: _selectedFilter,
                        isExpanded: true,
                        underline: const SizedBox(),
                        items: [
                          const DropdownMenuItem(
                            value: null,
                            child: Text('Todas'),
                          ),
                          ...BloodPressureClassification.values.map(
                            (c) => DropdownMenuItem(
                              value: c,
                              child: Text(c.label),
                            ),
                          ),
                        ],
                        onChanged: (value) {
                          setState(() => _selectedFilter = value);
                        },
                      ),
                    ),
                  ],
                ),
              ),

              const Divider(height: 1),

              // Lista de medições
              Expanded(
                child: provider.status == MeasurementStatus.loading
                    ? const Center(child: CircularProgressIndicator())
                    : filtered.isEmpty
                        ? _EmptyState(hasFilter: _selectedFilter != null)
                        : ListView.builder(
                            padding: const EdgeInsets.only(bottom: 16),
                            itemCount: filtered.length,
                            itemBuilder: (context, index) {
                              return PressureCard(
                                measurement: filtered[index],
                              );
                            },
                          ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  final bool hasFilter;
  const _EmptyState({required this.hasFilter});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.history, size: 56, color: Colors.grey[300]),
          const SizedBox(height: 12),
          Text(
            hasFilter
                ? 'Nenhuma medição com este filtro'
                : 'Nenhuma medição registrada',
            style: TextStyle(color: Colors.grey[500]),
          ),
        ],
      ),
    );
  }
}
