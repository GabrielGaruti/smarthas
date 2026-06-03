// lib/screens/profile_screen.dart
// Tela de perfil do usuário com estatísticas gerais de pressão arterial
// Acessível via ícone no AppBar da tela inicial

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../providers/measurement_provider.dart';
import '../models/measurement.dart';
import '../utils/app_theme.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Meu Perfil'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: Consumer2<AuthProvider, MeasurementProvider>(
        builder: (context, auth, measurements, _) {
          final name = auth.userName ?? 'Usuário';
          final email = auth.userEmail ?? '';
          final allMeasurements = measurements.measurements;

          // Estatísticas calculadas localmente
          final stats = _ProfileStats.compute(allMeasurements);

          return SingleChildScrollView(
            padding: const EdgeInsets.only(bottom: 32),
            child: Column(
              children: [
                // Header com avatar e nome
                _ProfileHeader(name: name, email: email),

                const SizedBox(height: 20),

                // Cards de estatísticas
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Text(
                    'Estatísticas Gerais',
                    style: Theme.of(context)
                        .textTheme
                        .titleMedium
                        ?.copyWith(fontWeight: FontWeight.bold),
                  ),
                ),
                const SizedBox(height: 12),

                if (allMeasurements.isEmpty)
                  _EmptyStats()
                else ...[
                  // Total + médias
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Row(
                      children: [
                        Expanded(
                          child: _StatCard(
                            icon: Icons.favorite_outline,
                            iconColor: AppTheme.primaryColor,
                            label: 'Total',
                            value: '${stats.total}',
                            unit: 'medições',
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: _StatCard(
                            icon: Icons.trending_up,
                            iconColor: Colors.red.shade600,
                            label: 'Média Sistólica',
                            value: '${stats.avgSystolic}',
                            unit: 'mmHg',
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: _StatCard(
                            icon: Icons.trending_down,
                            iconColor: AppTheme.accentColor,
                            label: 'Média Diastólica',
                            value: '${stats.avgDiastolic}',
                            unit: 'mmHg',
                          ),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 16),

                  // Classificação predominante
                  _PredominantCard(stats: stats),

                  const SizedBox(height: 16),

                  // Distribuição por classificação
                  _DistributionCard(stats: stats),
                ],

                const SizedBox(height: 32),

                // Botão de logout
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: OutlinedButton.icon(
                    onPressed: () {
                      auth.logout();
                      Navigator.of(context).pop();
                    },
                    icon: const Icon(Icons.logout, color: Colors.red),
                    label: const Text(
                      'Sair da conta',
                      style: TextStyle(color: Colors.red),
                    ),
                    style: OutlinedButton.styleFrom(
                      minimumSize: const Size(double.infinity, 48),
                      side: const BorderSide(color: Colors.red),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

// ─── Header ──────────────────────────────────────────────────────────────────

class _ProfileHeader extends StatelessWidget {
  final String name;
  final String email;
  const _ProfileHeader({required this.name, required this.email});

  @override
  Widget build(BuildContext context) {
    final initial = name.isNotEmpty ? name[0].toUpperCase() : 'U';
    return Container(
      width: double.infinity,
      color: AppTheme.primaryColor,
      padding: const EdgeInsets.fromLTRB(24, 24, 24, 32),
      child: Column(
        children: [
          CircleAvatar(
            radius: 44,
            backgroundColor: Colors.white.withOpacity(0.25),
            child: Text(
              initial,
              style: const TextStyle(
                fontSize: 40,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
          ),
          const SizedBox(height: 14),
          Text(
            name,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          if (email.isNotEmpty) ...[
            const SizedBox(height: 4),
            Text(
              email,
              style: const TextStyle(color: Colors.white70, fontSize: 13),
            ),
          ],
        ],
      ),
    );
  }
}

// ─── Stat Card ───────────────────────────────────────────────────────────────

class _StatCard extends StatelessWidget {
  final IconData icon;
  final Color iconColor;
  final String label;
  final String value;
  final String unit;

  const _StatCard({
    required this.icon,
    required this.iconColor,
    required this.label,
    required this.value,
    required this.unit,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 8),
        child: Column(
          children: [
            Icon(icon, color: iconColor, size: 28),
            const SizedBox(height: 8),
            Text(
              value,
              style: const TextStyle(
                  fontSize: 22, fontWeight: FontWeight.bold),
            ),
            Text(
              unit,
              style: TextStyle(fontSize: 10, color: Colors.grey[600]),
            ),
            const SizedBox(height: 4),
            Text(
              label,
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 11, color: Colors.grey[700]),
            ),
          ],
        ),
      ),
    );
  }
}

// ─── Predominant Classification ───────────────────────────────────────────────

class _PredominantCard extends StatelessWidget {
  final _ProfileStats stats;
  const _PredominantCard({required this.stats});

  @override
  Widget build(BuildContext context) {
    final color = AppTheme.classificationColor(stats.predominant.label);
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      color: color.withOpacity(0.08),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            CircleAvatar(
              backgroundColor: color.withOpacity(0.15),
              child: Icon(_classIcon(stats.predominant), color: color),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Classificação predominante',
                    style: TextStyle(fontSize: 12, color: Colors.black54),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    stats.predominant.label,
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: color,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  IconData _classIcon(BloodPressureClassification c) {
    switch (c) {
      case BloodPressureClassification.normal:
        return Icons.check_circle_outline;
      case BloodPressureClassification.elevated:
        return Icons.warning_amber_outlined;
      case BloodPressureClassification.hypertension:
        return Icons.dangerous_outlined;
    }
  }
}

// ─── Distribution Card ────────────────────────────────────────────────────────

class _DistributionCard extends StatelessWidget {
  final _ProfileStats stats;
  const _DistributionCard({required this.stats});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Distribuição de classificações',
              style: Theme.of(context)
                  .textTheme
                  .titleSmall
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            _DistBar(
              label: 'Normal',
              count: stats.normalCount,
              total: stats.total,
              color: AppTheme.normalColor,
            ),
            const SizedBox(height: 10),
            _DistBar(
              label: 'Elevada',
              count: stats.elevatedCount,
              total: stats.total,
              color: AppTheme.elevatedColor,
            ),
            const SizedBox(height: 10),
            _DistBar(
              label: 'Hipertensão',
              count: stats.hypertensionCount,
              total: stats.total,
              color: AppTheme.hypertensionColor,
            ),
          ],
        ),
      ),
    );
  }
}

class _DistBar extends StatelessWidget {
  final String label;
  final int count;
  final int total;
  final Color color;

  const _DistBar({
    required this.label,
    required this.count,
    required this.total,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final pct = total > 0 ? count / total : 0.0;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: const TextStyle(fontSize: 13)),
            Text(
              '$count (${(pct * 100).toStringAsFixed(0)}%)',
              style: TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: color),
            ),
          ],
        ),
        const SizedBox(height: 4),
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: LinearProgressIndicator(
            value: pct,
            minHeight: 8,
            backgroundColor: Colors.grey.shade200,
            valueColor: AlwaysStoppedAnimation<Color>(color),
          ),
        ),
      ],
    );
  }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

class _EmptyStats extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        children: [
          Icon(Icons.bar_chart_outlined, size: 56, color: Colors.grey[400]),
          const SizedBox(height: 12),
          Text(
            'Nenhuma medição registrada ainda.\nAs estatísticas aparecerão aqui.',
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.grey[600]),
          ),
        ],
      ),
    );
  }
}

// ─── Stats model ─────────────────────────────────────────────────────────────

class _ProfileStats {
  final int total;
  final int avgSystolic;
  final int avgDiastolic;
  final int normalCount;
  final int elevatedCount;
  final int hypertensionCount;
  final BloodPressureClassification predominant;

  const _ProfileStats({
    required this.total,
    required this.avgSystolic,
    required this.avgDiastolic,
    required this.normalCount,
    required this.elevatedCount,
    required this.hypertensionCount,
    required this.predominant,
  });

  static _ProfileStats compute(List<Measurement> measurements) {
    if (measurements.isEmpty) {
      return _ProfileStats(
        total: 0,
        avgSystolic: 0,
        avgDiastolic: 0,
        normalCount: 0,
        elevatedCount: 0,
        hypertensionCount: 0,
        predominant: BloodPressureClassification.normal,
      );
    }

    int normal = 0, elevated = 0, hypertension = 0;
    int sumSys = 0, sumDia = 0;

    for (final m in measurements) {
      sumSys += m.systolic;
      sumDia += m.diastolic;
      switch (m.classification) {
        case BloodPressureClassification.normal:
          normal++;
        case BloodPressureClassification.elevated:
          elevated++;
        case BloodPressureClassification.hypertension:
          hypertension++;
      }
    }

    final total = measurements.length;

    BloodPressureClassification predominant;
    if (normal >= elevated && normal >= hypertension) {
      predominant = BloodPressureClassification.normal;
    } else if (elevated >= hypertension) {
      predominant = BloodPressureClassification.elevated;
    } else {
      predominant = BloodPressureClassification.hypertension;
    }

    return _ProfileStats(
      total: total,
      avgSystolic: (sumSys / total).round(),
      avgDiastolic: (sumDia / total).round(),
      normalCount: normal,
      elevatedCount: elevated,
      hypertensionCount: hypertension,
      predominant: predominant,
    );
  }
}
