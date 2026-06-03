// lib/screens/home_screen.dart
// Espelha HomeFragment.kt — saudação, última medição, classificação e clima

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../providers/measurement_provider.dart';
import '../services/weather_service.dart';
import '../models/user.dart';
import '../models/measurement.dart';
import '../widgets/pressure_card.dart';
import '../widgets/weather_widget.dart';
import '../utils/app_theme.dart';
import 'profile_screen.dart';

class HomeScreen extends StatefulWidget {
  final VoidCallback onNewMeasurement;

  const HomeScreen({super.key, required this.onNewMeasurement});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _weatherService = WeatherService();
  WeatherData? _weatherData;
  bool _weatherLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    // Carrega medições
    final auth = context.read<AuthProvider>();
    final measurements = context.read<MeasurementProvider>();
    final token = await auth.getToken();
    if (token != null) {
      await measurements.loadMeasurements(token);
    }

    // Carrega clima (segunda API)
    try {
      final weather = await _weatherService.getWeatherByCity('São Paulo');
      if (mounted) setState(() => _weatherData = weather);
    } catch (_) {}
    if (mounted) setState(() => _weatherLoading = false);
  }

  @override
  void dispose() {
    _weatherService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Smart HAS'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_outline),
            tooltip: 'Meu Perfil',
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const ProfileScreen()),
            ),
          ),
          Consumer<AuthProvider>(
            builder: (_, auth, __) => IconButton(
              icon: const Icon(Icons.logout),
              tooltip: 'Sair',
              onPressed: () => auth.logout(),
            ),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _loadData,
        child: Consumer2<AuthProvider, MeasurementProvider>(
          builder: (context, auth, measurements, _) {
            final latest = measurements.latestMeasurement;

            return SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.only(bottom: 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header de saudação — equivalente ao txt_greeting
                  Container(
                    width: double.infinity,
                    color: AppTheme.primaryColor,
                    padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Olá, ${auth.userName ?? 'Usuário'} 👋',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 4),
                        const Text(
                          'Acompanhe sua pressão arterial',
                          style: TextStyle(color: Colors.white70, fontSize: 14),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 16),

                  // Widget de clima — segunda API externa
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: WeatherWidget(
                      weatherData: _weatherData,
                      isLoading: _weatherLoading,
                    ),
                  ),

                  const SizedBox(height: 16),

                  // Última medição — equivalente ao txt_last_measurement
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Text(
                      'Última medição',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                  ),
                  const SizedBox(height: 8),

                  if (measurements.status == MeasurementStatus.loading)
                    const Padding(
                      padding: EdgeInsets.all(32),
                      child: Center(child: CircularProgressIndicator()),
                    )
                  else if (latest != null)
                    PressureCard(measurement: latest)
                  else
                    Padding(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 8),
                      child: Card(
                        child: Padding(
                          padding: const EdgeInsets.all(24),
                          child: Center(
                            child: Column(
                              children: [
                                Icon(Icons.monitor_heart_outlined,
                                    size: 48, color: Colors.grey[400]),
                                const SizedBox(height: 12),
                                Text(
                                  'Nenhuma medição registrada',
                                  style: TextStyle(color: Colors.grey[600]),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ),

                  const SizedBox(height: 24),

                  // Botão nova medição — equivalente ao btn_new_measurement
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: ElevatedButton.icon(
                      onPressed: widget.onNewMeasurement,
                      icon: const Icon(Icons.add),
                      label: const Text('Nova Medição'),
                    ),
                  ),

                  // Resumo rápido de classificação
                  if (latest != null) ...[
                    const SizedBox(height: 24),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: _ClassificationSummary(measurement: latest),
                    ),
                  ],
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}

class _ClassificationSummary extends StatelessWidget {
  final Measurement measurement;
  const _ClassificationSummary({required this.measurement});

  @override
  Widget build(BuildContext context) {
    final c = measurement.classification;
    final color = AppTheme.classificationColor(c.label);

    return Card(
      color: color.withOpacity(0.08),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Icon(Icons.info_outline, color: color),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Classificação: ${c.label}',
                    style: TextStyle(
                        fontWeight: FontWeight.bold, color: color),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    _advice(c),
                    style: const TextStyle(fontSize: 12),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _advice(BloodPressureClassification c) {
    switch (c) {
      case BloodPressureClassification.normal:
        return 'Ótimo! Continue mantendo hábitos saudáveis.';
      case BloodPressureClassification.elevated:
        return 'Atenção: adote hábitos saudáveis e monitore com frequência.';
      case BloodPressureClassification.hypertension:
        return 'Consulte um médico. Pressão acima do recomendado.';
    }
  }
}
