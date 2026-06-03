// lib/widgets/pressure_card.dart
// Widget reutilizável para exibir uma medição de pressão arterial
// Componentização — evita duplicação entre HomeScreen e HistoryScreen

import 'package:flutter/material.dart';
import '../models/measurement.dart';
import '../utils/app_theme.dart';

class PressureCard extends StatelessWidget {
  final Measurement measurement;
  final VoidCallback? onDelete;

  const PressureCard({
    super.key,
    required this.measurement,
    this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final classification = measurement.classification;
    final color = AppTheme.classificationColor(classification.label);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            // Indicador colorido de classificação
            Container(
              width: 6,
              height: 60,
              decoration: BoxDecoration(
                color: color,
                borderRadius: BorderRadius.circular(3),
              ),
            ),
            const SizedBox(width: 16),

            // Dados da medição
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    measurement.formattedPressure,
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${measurement.date} às ${measurement.time}',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Colors.grey[600],
                        ),
                  ),
                  if (measurement.notes != null &&
                      measurement.notes!.isNotEmpty) ...[
                    const SizedBox(height: 4),
                    Text(
                      measurement.notes!,
                      style: Theme.of(context).textTheme.bodySmall,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ],
              ),
            ),

            // Badge de classificação
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: color.withOpacity(0.15),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(color: color.withOpacity(0.4)),
                  ),
                  child: Text(
                    classification.label,
                    style: TextStyle(
                      color: color,
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                if (onDelete != null) ...[
                  const SizedBox(height: 8),
                  GestureDetector(
                    onTap: onDelete,
                    child: Icon(
                      Icons.delete_outline,
                      color: Colors.grey[400],
                      size: 20,
                    ),
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }
}
