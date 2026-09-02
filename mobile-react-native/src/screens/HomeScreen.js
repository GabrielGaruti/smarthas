import React, { useCallback, useState } from 'react';
import {
  View, Text, Image, Button, StyleSheet, ScrollView, ActivityIndicator,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';

// Tela inicial: mostra a ultima medicao com a classificacao e o nivel de risco.
export default function HomeScreen({ navigation }) {
  const { user, signOut } = useAuth();
  const [last, setLast] = useState(null);
  const [rec, setRec] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const list = await api.getMeasurements();
      setLast(list && list.length > 0 ? list[0] : null);
      setRec(await api.getRecommendations());
    } catch (e) {
      // silencioso: mostra estado vazio
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#b91c1c" /></View>;
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.header}>
        <Image source={require('../assets/logo.png')} style={styles.logo} />
        <View>
          <Text style={styles.hello}>Ola,</Text>
          <Text style={styles.name}>{user?.fullName}</Text>
        </View>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Ultima medicao</Text>
        {last ? (
          <>
            <Text style={styles.pressure}>{last.systolic}/{last.diastolic} <Text style={styles.mmhg}>mmHg</Text></Text>
            <View style={[styles.badge, { backgroundColor: last.colorHex }]}>
              <Text style={styles.badgeText}>{last.classificationLabel}</Text>
            </View>
            <Text style={styles.date}>{last.date} as {last.time}</Text>
          </>
        ) : (
          <Text style={styles.empty}>Nenhuma medicao ainda. Registre a primeira!</Text>
        )}
      </View>

      {rec && (
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Nivel de risco</Text>
          <Text style={[styles.risk, { color: riskColor(rec.riskLevel) }]}>{rec.riskLevel}</Text>
          {rec.recommendations?.map((r, i) => (
            <Text key={i} style={styles.rec}>{'\u2022'} {r}</Text>
          ))}
        </View>
      )}

      <View style={styles.actions}>
        <Button title="Nova medicao" color="#b91c1c" onPress={() => navigation.navigate('Nova')} />
      </View>
      <View style={styles.actions}>
        <Button title="Sair" color="#6b7280" onPress={signOut} />
      </View>
    </ScrollView>
  );
}

function riskColor(level) {
  if (level === 'ALTO') return '#dc2626';
  if (level === 'MODERADO') return '#f59e0b';
  if (level === 'BAIXO') return '#16a34a';
  return '#6b7280';
}

const styles = StyleSheet.create({
  container: { padding: 20, backgroundColor: '#f4f6fb', flexGrow: 1 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', marginBottom: 20 },
  logo: { width: 56, height: 56, marginRight: 14 },
  hello: { color: '#6b7280', fontSize: 14 },
  name: { fontSize: 20, fontWeight: 'bold', color: '#1f2937' },
  card: { backgroundColor: '#fff', borderRadius: 12, padding: 20, marginBottom: 16, elevation: 2 },
  cardTitle: { fontSize: 14, color: '#6b7280', marginBottom: 8, fontWeight: '600' },
  pressure: { fontSize: 40, fontWeight: 'bold', color: '#1f2937' },
  mmhg: { fontSize: 16, color: '#6b7280', fontWeight: 'normal' },
  badge: { alignSelf: 'flex-start', paddingHorizontal: 12, paddingVertical: 4, borderRadius: 999, marginTop: 8 },
  badgeText: { color: '#fff', fontWeight: '600', fontSize: 13 },
  date: { color: '#6b7280', marginTop: 8 },
  empty: { color: '#6b7280' },
  risk: { fontSize: 24, fontWeight: 'bold', marginBottom: 8 },
  rec: { color: '#374151', marginTop: 4 },
  actions: { marginBottom: 10 },
});
