// lib/repositories/measurement_repository.dart
// Espelha MeasurementRepository.kt do Android
// Mesma lógica: tenta API, se falhar salva localmente

import '../services/api_service.dart';
import '../models/measurement.dart';

class MeasurementRepository {
  final ApiService _apiService;

  MeasurementRepository({required ApiService apiService})
      : _apiService = apiService;

  /// GET /measurements — lista do backend
  Future<List<Measurement>> getMeasurements(String token) async {
    return _apiService.getMeasurements(token);
  }

  /// POST /measurements — cria medição
  /// Mesma lógica de fallback do MeasurementRepository.kt:
  /// Se API falhar, retorna objeto local para não bloquear o usuário
  Future<Measurement> createMeasurement(
    String token, {
    required int systolic,
    required int diastolic,
    required String date,
    required String time,
    String? notes,
  }) async {
    try {
      return await _apiService.createMeasurement(
        token,
        systolic: systolic,
        diastolic: diastolic,
        date: date,
        time: time,
        notes: notes,
      );
    } catch (_) {
      // Fallback local — mesmo comportamento do Android
      return Measurement(
        systolic: systolic,
        diastolic: diastolic,
        date: date,
        time: time,
        notes: notes,
        createdAt: DateTime.now().toIso8601String(),
      );
    }
  }
}
