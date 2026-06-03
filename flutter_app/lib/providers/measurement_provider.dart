// lib/providers/measurement_provider.dart
// Espelha MeasurementViewModel.kt — mesma lógica de classificação e CRUD

import 'package:flutter/foundation.dart';
import 'package:intl/intl.dart';
import '../repositories/measurement_repository.dart';
import '../models/measurement.dart';

enum MeasurementStatus { idle, loading, success, error }

class MeasurementProvider extends ChangeNotifier {
  final MeasurementRepository _repository;

  List<Measurement> _measurements = [];
  MeasurementStatus _status = MeasurementStatus.idle;
  MeasurementStatus _createStatus = MeasurementStatus.idle;
  String? _errorMessage;

  MeasurementProvider({required MeasurementRepository repository})
      : _repository = repository;

  List<Measurement> get measurements => _measurements;
  MeasurementStatus get status => _status;
  MeasurementStatus get createStatus => _createStatus;
  String? get errorMessage => _errorMessage;

  Measurement? get latestMeasurement =>
      _measurements.isNotEmpty ? _measurements.first : null;

  /// Carrega medições do backend — GET /measurements
  Future<void> loadMeasurements(String token) async {
    _status = MeasurementStatus.loading;
    notifyListeners();

    try {
      _measurements = await _repository.getMeasurements(token);
      _status = MeasurementStatus.success;
    } catch (e) {
      _status = MeasurementStatus.error;
      _errorMessage = 'Erro ao carregar medições';
    }
    notifyListeners();
  }

  /// Cria medição — POST /measurements
  /// Usa fuso America/Sao_Paulo, igual ao MeasurementViewModel.kt
  Future<void> createMeasurement(
    String token, {
    required int systolic,
    required int diastolic,
    String? notes,
  }) async {
    _createStatus = MeasurementStatus.loading;
    notifyListeners();

    try {
      final now = DateTime.now();
      final date = DateFormat('yyyy-MM-dd').format(now);
      final time = DateFormat('HH:mm').format(now);

      final newMeasurement = await _repository.createMeasurement(
        token,
        systolic: systolic,
        diastolic: diastolic,
        date: date,
        time: time,
        notes: notes,
      );

      _measurements.insert(0, newMeasurement);
      _createStatus = MeasurementStatus.success;
    } catch (e) {
      _createStatus = MeasurementStatus.error;
      _errorMessage = 'Erro ao salvar medição';
    }
    notifyListeners();
  }

  void resetCreateStatus() {
    _createStatus = MeasurementStatus.idle;
    _errorMessage = null;
    notifyListeners();
  }

  /// Filtra por classificação — equivalente ao spinner do HistoryFragment.kt
  List<Measurement> filterByClassification(
    BloodPressureClassification? classification,
  ) {
    if (classification == null) return _measurements;
    return _measurements
        .where((m) => m.classification == classification)
        .toList();
  }
}
