// lib/widgets/pressure_chart.dart
// Gráfico de linha — evolução sistólica/diastólica ao longo do tempo
// Usa fl_chart com linhas de referência nos limites clínicos (120/80 mmHg)

import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/measurement.dart';
import '../utils/app_theme.dart';

class PressureChart extends StatelessWidget {
  final List<Measurement> measurements;

  const PressureChart({super.key, required this.measurements});

  @override
  Widget build(BuildContext context) {
    // Mostra os últimos 10, em ordem cronológica (mais antigo → mais recente)
    final data = measurements.length > 10
        ? measurements.sublist(0, 10).reversed.toList()
        : measurements.reversed.toList();

    if (data.length < 2) return const SizedBox.shrink();

    final systolicSpots = <FlSpot>[];
    final diastolicSpots = <FlSpot>[];
    final dateLabels = <String>[];

    for (int i = 0; i < data.length; i++) {
      final m = data[i];
      systolicSpots.add(FlSpot(i.toDouble(), m.systolic.toDouble()));
      diastolicSpots.add(FlSpot(i.toDouble(), m.diastolic.toDouble()));
      try {
        final parsed = DateTime.parse(m.date);
        dateLabels.add(DateFormat('dd/MM').format(parsed));
      } catch (_) {
        dateLabels.add(m.date);
      }
    }

    final allValues = [
      ...data.map((m) => m.systolic),
      ...data.map((m) => m.diastolic),
    ];
    final minY = (allValues.reduce((a, b) => a < b ? a : b) - 15)
        .clamp(40, 200)
        .toDouble();
    final maxY = (allValues.reduce((a, b) => a > b ? a : b) + 15)
        .clamp(80, 220)
        .toDouble();

    return Card(
      margin: const EdgeInsets.fromLTRB(16, 8, 16, 0),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(12, 16, 20, 12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Tendência de Pressão',
              style: Theme.of(context)
                  .textTheme
                  .titleSmall
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),
            Row(
              children: [
                _LegendDot(color: Colors.red.shade600, label: 'Sistólica'),
                const SizedBox(width: 16),
                _LegendDot(
                    color: AppTheme.primaryColor, label: 'Diastólica'),
                const SizedBox(width: 16),
                _LegendDot(
                    color: Colors.grey.shade400,
                    label: 'Referência',
                    dashed: true),
              ],
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 200,
              child: LineChart(
                LineChartData(
                  minY: minY,
                  maxY: maxY,
                  clipData: const FlClipData.all(),
                  gridData: FlGridData(
                    show: true,
                    drawVerticalLine: false,
                    horizontalInterval: 20,
                    getDrawingHorizontalLine: (value) => FlLine(
                      color: Colors.grey.shade200,
                      strokeWidth: 1,
                    ),
                  ),
                  borderData: FlBorderData(
                    show: true,
                    border: Border(
                      bottom: BorderSide(color: Colors.grey.shade300),
                      left: BorderSide(color: Colors.grey.shade300),
                    ),
                  ),
                  titlesData: FlTitlesData(
                    topTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                    rightTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                    bottomTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        interval: 1,
                        getTitlesWidget: (value, meta) {
                          final idx = value.toInt();
                          if (idx < 0 || idx >= dateLabels.length) {
                            return const SizedBox.shrink();
                          }
                          return Padding(
                            padding: const EdgeInsets.only(top: 6),
                            child: Text(
                              dateLabels[idx],
                              style: TextStyle(
                                  fontSize: 10, color: Colors.grey.shade600),
                            ),
                          );
                        },
                      ),
                    ),
                    leftTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 36,
                        interval: 20,
                        getTitlesWidget: (value, meta) => Text(
                          value.toInt().toString(),
                          style: TextStyle(
                              fontSize: 10, color: Colors.grey.shade600),
                        ),
                      ),
                    ),
                  ),
                  extraLinesData: ExtraLinesData(
                    horizontalLines: [
                      // Referência sistólica — 120 mmHg
                      HorizontalLine(
                        y: 120,
                        color: Colors.red.shade200,
                        strokeWidth: 1,
                        dashArray: [6, 4],
                        label: HorizontalLineLabel(
                          show: true,
                          alignment: Alignment.topRight,
                          labelResolver: (_) => '120',
                          style: TextStyle(
                              fontSize: 9, color: Colors.red.shade300),
                        ),
                      ),
                      // Referência diastólica — 80 mmHg
                      HorizontalLine(
                        y: 80,
                        color: AppTheme.primaryColor.withOpacity(0.4),
                        strokeWidth: 1,
                        dashArray: [6, 4],
                        label: HorizontalLineLabel(
                          show: true,
                          alignment: Alignment.topRight,
                          labelResolver: (_) => '80',
                          style: TextStyle(
                              fontSize: 9,
                              color: AppTheme.primaryColor.withOpacity(0.7)),
                        ),
                      ),
                    ],
                  ),
                  lineBarsData: [
                    // Linha sistólica
                    LineChartBarData(
                      spots: systolicSpots,
                      isCurved: true,
                      curveSmoothness: 0.3,
                      color: Colors.red.shade600,
                      barWidth: 2.5,
                      dotData: FlDotData(
                        getDotPainter: (spot, _, __, ___) =>
                            FlDotCirclePainter(
                          radius: 4,
                          color: Colors.red.shade600,
                          strokeWidth: 1.5,
                          strokeColor: Colors.white,
                        ),
                      ),
                      belowBarData: BarAreaData(
                        show: true,
                        color: Colors.red.shade600.withOpacity(0.07),
                      ),
                    ),
                    // Linha diastólica
                    LineChartBarData(
                      spots: diastolicSpots,
                      isCurved: true,
                      curveSmoothness: 0.3,
                      color: AppTheme.primaryColor,
                      barWidth: 2.5,
                      dotData: FlDotData(
                        getDotPainter: (spot, _, __, ___) =>
                            FlDotCirclePainter(
                          radius: 4,
                          color: AppTheme.primaryColor,
                          strokeWidth: 1.5,
                          strokeColor: Colors.white,
                        ),
                      ),
                      belowBarData: BarAreaData(
                        show: true,
                        color: AppTheme.primaryColor.withOpacity(0.07),
                      ),
                    ),
                  ],
                  lineTouchData: LineTouchData(
                    touchTooltipData: LineTouchTooltipData(
                      getTooltipItems: (touchedSpots) {
                        return touchedSpots.map((spot) {
                          final isSystolic = spot.barIndex == 0;
                          return LineTooltipItem(
                            '${isSystolic ? "Sist" : "Diast"}: ${spot.y.toInt()} mmHg',
                            TextStyle(
                              color: isSystolic
                                  ? Colors.red.shade300
                                  : Colors.blue.shade200,
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                            ),
                          );
                        }).toList();
                      },
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _LegendDot extends StatelessWidget {
  final Color color;
  final String label;
  final bool dashed;

  const _LegendDot({
    required this.color,
    required this.label,
    this.dashed = false,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 16,
          height: 2.5,
          decoration: BoxDecoration(
            color: dashed ? Colors.transparent : color,
            border: dashed
                ? Border.all(color: color, width: 1)
                : null,
          ),
        ),
        const SizedBox(width: 4),
        Text(
          label,
          style: TextStyle(fontSize: 11, color: Colors.grey.shade700),
        ),
      ],
    );
  }
}
