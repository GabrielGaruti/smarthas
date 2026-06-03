// lib/services/weather_service.dart
// Segunda API externa: OpenWeatherMap
// Relevante para HAS: temperatura e umidade influenciam pressão arterial

import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user.dart';

class WeatherService {
  // API pública do OpenWeatherMap — substitua pela sua chave em produção
  // Para demo/acadêmico, utiliza dados simulados quando sem chave
  static const String _apiKey = 'YOUR_OPENWEATHERMAP_API_KEY';
  static const String _baseUrl = 'https://api.openweathermap.org/data/2.5';

  final http.Client _client;

  WeatherService({http.Client? client}) : _client = client ?? http.Client();

  /// Busca dados climáticos por cidade
  /// Se a chave não estiver configurada, retorna dados simulados
  Future<WeatherData> getWeatherByCity(String city) async {
    if (_apiKey == 'YOUR_OPENWEATHERMAP_API_KEY') {
      return _getSimulatedWeather(city);
    }

    try {
      final response = await _client.get(
        Uri.parse(
          '$_baseUrl/weather?q=$city&appid=$_apiKey&units=metric&lang=pt_br',
        ),
      );

      if (response.statusCode == 200) {
        return WeatherData.fromJson(jsonDecode(response.body));
      } else {
        return _getSimulatedWeather(city);
      }
    } catch (_) {
      return _getSimulatedWeather(city);
    }
  }

  /// Dados simulados para demo acadêmico
  WeatherData _getSimulatedWeather(String city) {
    return WeatherData(
      city: city.isEmpty ? 'São Paulo' : city,
      temperature: 24.5,
      description: 'Parcialmente nublado',
      icon: '02d',
      humidity: 68,
    );
  }

  void dispose() {
    _client.close();
  }
}
