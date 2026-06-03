// lib/widgets/weather_widget.dart
// Widget reutilizável para exibir dados climáticos (segunda API externa)
// Relevância clínica: temperatura/umidade influenciam pressão arterial

import 'package:flutter/material.dart';
import '../models/user.dart';
import '../utils/app_theme.dart';

class WeatherWidget extends StatelessWidget {
  final WeatherData? weatherData;
  final bool isLoading;

  const WeatherWidget({
    super.key,
    this.weatherData,
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Card(
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Center(child: CircularProgressIndicator()),
        ),
      );
    }

    if (weatherData == null) return const SizedBox.shrink();

    return Card(
      color: AppTheme.primaryColor.withOpacity(0.06),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            const Icon(Icons.wb_sunny_outlined,
                color: AppTheme.primaryColor, size: 28),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    weatherData!.city,
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  Text(
                    weatherData!.description,
                    style: TextStyle(color: Colors.grey[600], fontSize: 12),
                  ),
                ],
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '${weatherData!.temperature.toStringAsFixed(1)}°C',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 18,
                    color: AppTheme.primaryColor,
                  ),
                ),
                Text(
                  'Umidade: ${weatherData!.humidity}%',
                  style: TextStyle(color: Colors.grey[600], fontSize: 11),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
