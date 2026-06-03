// lib/screens/add_measurement_screen.dart
// Espelha AddMeasurementFragment.kt — formulário de nova medição
// Integra com MeasurementProvider e dispara alerta FCM se pressão elevada

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../providers/measurement_provider.dart';
import '../services/notification_service.dart' show NotificationService;
import '../models/measurement.dart';
import '../widgets/loading_button.dart';

class AddMeasurementScreen extends StatefulWidget {
  final VoidCallback onSuccess;

  const AddMeasurementScreen({super.key, required this.onSuccess});

  @override
  State<AddMeasurementScreen> createState() => _AddMeasurementScreenState();
}

class _AddMeasurementScreenState extends State<AddMeasurementScreen> {
  final _systolicController = TextEditingController();
  final _diastolicController = TextEditingController();
  final _notesController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _systolicController.dispose();
    _diastolicController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;

    final authProvider = context.read<AuthProvider>();
    final measurementProvider = context.read<MeasurementProvider>();

    final token = await authProvider.getToken();
    if (token == null) return;

    final systolic = int.parse(_systolicController.text.trim());
    final diastolic = int.parse(_diastolicController.text.trim());
    final notes = _notesController.text.trim();

    await measurementProvider.createMeasurement(
      token,
      systolic: systolic,
      diastolic: diastolic,
      notes: notes.isNotEmpty ? notes : null,
    );

    if (!mounted) return;

    if (measurementProvider.createStatus == MeasurementStatus.success) {
      // Verifica se pressão está elevada e dispara notificação FCM
      // Título: Smart HAS | Corpo: Pressão arterial acima do recomendado.
      final temp = Measurement(
        systolic: systolic,
        diastolic: diastolic,
        date: '',
        time: '',
      );
      if (temp.classification != BloodPressureClassification.normal) {
        await NotificationService().simulateHighPressureAlert();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text(
                '⚠️ Smart HAS: Pressão arterial acima do recomendado.',
              ),
              backgroundColor: Colors.orange,
              duration: Duration(seconds: 4),
            ),
          );
        }
      }

      measurementProvider.resetCreateStatus();
      widget.onSuccess();
    } else if (measurementProvider.createStatus == MeasurementStatus.error) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
              measurementProvider.errorMessage ?? 'Erro ao salvar medição'),
          backgroundColor: Colors.red,
        ),
      );
      measurementProvider.resetCreateStatus();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Nova Medição')),
      body: Consumer<MeasurementProvider>(
        builder: (context, provider, _) {
          final isLoading = provider.createStatus == MeasurementStatus.loading;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Instrução
                  Card(
                    color: const Color(0xFF1976D2).withOpacity(0.07),
                    child: const Padding(
                      padding: EdgeInsets.all(14),
                      child: Row(
                        children: [
                          Icon(Icons.info_outline,
                              color: Color(0xFF1976D2), size: 20),
                          SizedBox(width: 10),
                          Expanded(
                            child: Text(
                              'Insira os valores do seu medidor de pressão arterial.',
                              style: TextStyle(fontSize: 13),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Sistólica (pressão máxima)
                  TextFormField(
                    controller: _systolicController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Pressão Sistólica (máxima)',
                      hintText: 'Ex: 120',
                      suffixText: 'mmHg',
                      prefixIcon: Icon(Icons.arrow_upward),
                    ),
                    validator: (v) {
                      if (v == null || v.trim().isEmpty) {
                        return 'Informe a pressão sistólica';
                      }
                      final val = int.tryParse(v.trim());
                      if (val == null || val < 60 || val > 250) {
                        return 'Valor inválido (60–250)';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),

                  // Diastólica (pressão mínima)
                  TextFormField(
                    controller: _diastolicController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Pressão Diastólica (mínima)',
                      hintText: 'Ex: 80',
                      suffixText: 'mmHg',
                      prefixIcon: Icon(Icons.arrow_downward),
                    ),
                    validator: (v) {
                      if (v == null || v.trim().isEmpty) {
                        return 'Informe a pressão diastólica';
                      }
                      final val = int.tryParse(v.trim());
                      if (val == null || val < 40 || val > 150) {
                        return 'Valor inválido (40–150)';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),

                  // Observações (opcional)
                  TextFormField(
                    controller: _notesController,
                    maxLines: 3,
                    decoration: const InputDecoration(
                      labelText: 'Observações (opcional)',
                      hintText: 'Ex: Após atividade física...',
                      prefixIcon: Icon(Icons.note_outlined),
                      alignLabelWithHint: true,
                    ),
                  ),
                  const SizedBox(height: 32),

                  LoadingButton(
                    label: 'Salvar Medição',
                    isLoading: isLoading,
                    onPressed: _handleSave,
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
