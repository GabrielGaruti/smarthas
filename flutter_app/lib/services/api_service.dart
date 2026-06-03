// lib/services/api_service.dart
// Consome a API REST do backend SmartHAS
// Na web: usa URL relativa baseada na origem da página
// No Android: usa http://10.0.2.2:8000 (emulador) ou IP do servidor

import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/measurement.dart';
import '../models/user.dart';

class ApiService {
  // Na web, usa URL relativa ao host atual (proxy via Express → FastAPI)
  // No Android/iOS, usa o servidor direto
  static String get baseUrl {
    if (kIsWeb) {
      // Flutter web: chama /api/auth e /api/measurements pelo proxy Express
      return '/api';
    }
    // Android emulador: 10.0.2.2 aponta para localhost do host
    return 'http://10.0.2.2:8000';
  }

  final http.Client _client;

  ApiService({http.Client? client}) : _client = client ?? http.Client();

  Map<String, String> _headers({String? token}) {
    final headers = {'Content-Type': 'application/json'};
    if (token != null) {
      headers['Authorization'] = 'Bearer $token';
    }
    return headers;
  }

  Uri _uri(String path) {
    if (kIsWeb) {
      // Flutter web: URI relativa à origem — o browser resolve corretamente
      return Uri.parse('$baseUrl$path');
    }
    return Uri.parse('$baseUrl$path');
  }

  /// POST /auth/login
  Future<LoginResponse> login(String email, String password) async {
    final uri = _uri('/auth/login');

    final response = await _client.post(
      uri,
      headers: _headers(),
      body: jsonEncode({'email': email, 'password': password}),
    );

    if (response.statusCode == 200) {
      return LoginResponse.fromJson(jsonDecode(response.body));
    } else {
      final error = jsonDecode(response.body);
      throw Exception(error['detail'] ?? 'Erro ao fazer login');
    }
  }

  /// POST /auth/register
  Future<void> register(String fullName, String email, String password) async {
    final uri = _uri('/auth/register');

    final response = await _client.post(
      uri,
      headers: _headers(),
      body: jsonEncode({
        'fullName': fullName,
        'email': email,
        'password': password,
      }),
    );

    if (response.statusCode != 200) {
      final error = jsonDecode(response.body);
      throw Exception(error['detail'] ?? 'Erro ao cadastrar');
    }
  }

  /// GET /measurements
  Future<List<Measurement>> getMeasurements(String token) async {
    final uri = _uri('/measurements');

    final response = await _client.get(
      uri,
      headers: _headers(token: token),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((e) => Measurement.fromJson(e)).toList();
    } else {
      throw Exception('Erro ao buscar medições');
    }
  }

  /// POST /measurements
  Future<Measurement> createMeasurement(
    String token, {
    required int systolic,
    required int diastolic,
    required String date,
    required String time,
    String? notes,
  }) async {
    final uri = _uri('/measurements');

    final response = await _client.post(
      uri,
      headers: _headers(token: token),
      body: jsonEncode({
        'systolic': systolic,
        'diastolic': diastolic,
        'date': date,
        'time': time,
        if (notes != null) 'notes': notes,
      }),
    );

    if (response.statusCode == 200) {
      return Measurement.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Erro ao criar medição');
    }
  }

  void dispose() {
    _client.close();
  }
}
