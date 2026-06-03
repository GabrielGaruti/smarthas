import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:smarthas_flutter/main.dart';

void main() {
  testWidgets('SmartHAS app smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const SmartHasApp());
    expect(find.byType(MaterialApp), findsOneWidget);
  });
}
