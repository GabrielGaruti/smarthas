// lib/models/measurement.dart
// Espelha os modelos existentes no backend (main.py: MeasurementResponse)
// e no Android (data/database/Measurement.kt + data/api/ApiModels.kt)

class Measurement {
  final int? id;
  final int systolic;
  final int diastolic;
  final String date;
  final String time;
  final String? notes;
  final String? createdAt;

  const Measurement({
    this.id,
    required this.systolic,
    required this.diastolic,
    required this.date,
    required this.time,
    this.notes,
    this.createdAt,
  });

  factory Measurement.fromJson(Map<String, dynamic> json) {
    return Measurement(
      id: json['id'] as int?,
      systolic: json['systolic'] as int,
      diastolic: json['diastolic'] as int,
      date: json['date'] as String,
      time: json['time'] as String,
      notes: json['notes'] as String?,
      createdAt: json['createdAt'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'systolic': systolic,
      'diastolic': diastolic,
      'date': date,
      'time': time,
      if (notes != null) 'notes': notes,
    };
  }

  /// Lógica de classificação reaproveitada do MeasurementViewModel.kt
  BloodPressureClassification get classification {
    if (systolic < 120 && diastolic < 80) {
      return BloodPressureClassification.normal;
    } else if ((systolic >= 120 && systolic <= 139) ||
        (diastolic >= 80 && diastolic <= 89)) {
      return BloodPressureClassification.elevated;
    } else {
      return BloodPressureClassification.hypertension;
    }
  }

  String get formattedPressure => '$systolic/$diastolic mmHg';
}

enum BloodPressureClassification {
  normal,
  elevated,
  hypertension;

  String get label {
    switch (this) {
      case BloodPressureClassification.normal:
        return 'Normal';
      case BloodPressureClassification.elevated:
        return 'Elevada';
      case BloodPressureClassification.hypertension:
        return 'Hipertensão';
    }
  }

  // Retorna string de cor para uso no UI
  String get colorHex {
    switch (this) {
      case BloodPressureClassification.normal:
        return '#4CAF50';
      case BloodPressureClassification.elevated:
        return '#FF9800';
      case BloodPressureClassification.hypertension:
        return '#F44336';
    }
  }
}
