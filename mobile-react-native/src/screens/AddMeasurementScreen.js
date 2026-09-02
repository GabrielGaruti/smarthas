import React, { useState } from 'react';
import {
  View, Text, TextInput, Button, StyleSheet, Alert, ScrollView,
} from 'react-native';
import { api } from '../api/client';

// Formulario de nova medicao. Usa TextInput e Button (nucleo do React Native).
export default function AddMeasurementScreen({ navigation }) {
  const today = new Date();
  const [systolic, setSystolic] = useState('120');
  const [diastolic, setDiastolic] = useState('80');
  const [date, setDate] = useState(today.toISOString().substring(0, 10));
  const [time, setTime] = useState('08:00');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  const save = async () => {
    setSaving(true);
    try {
      await api.createMeasurement({
        systolic: parseInt(systolic, 10),
        diastolic: parseInt(diastolic, 10),
        date,
        time,
        notes,
      });
      Alert.alert('Sucesso', 'Medicao registrada!');
      navigation.navigate('Inicio');
    } catch (e) {
      Alert.alert('Erro', e.message || 'Nao foi possivel salvar.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.label}>Sistolica (mmHg)</Text>
      <TextInput style={styles.input} value={systolic} onChangeText={setSystolic} keyboardType="numeric" />

      <Text style={styles.label}>Diastolica (mmHg)</Text>
      <TextInput style={styles.input} value={diastolic} onChangeText={setDiastolic} keyboardType="numeric" />

      <Text style={styles.label}>Data (AAAA-MM-DD)</Text>
      <TextInput style={styles.input} value={date} onChangeText={setDate} />

      <Text style={styles.label}>Hora (HH:MM)</Text>
      <TextInput style={styles.input} value={time} onChangeText={setTime} />

      <Text style={styles.label}>Observacoes</Text>
      <TextInput style={styles.input} value={notes} onChangeText={setNotes} placeholder="opcional" />

      <View style={styles.button}>
        <Button title={saving ? 'Salvando...' : 'Salvar medicao'} color="#b91c1c" onPress={save} disabled={saving} />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 20, backgroundColor: '#f4f6fb', flexGrow: 1 },
  label: { fontSize: 14, fontWeight: '600', marginTop: 12, marginBottom: 4 },
  input: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#d1d5db', borderRadius: 8, padding: 12, fontSize: 15 },
  button: { marginTop: 24 },
});
