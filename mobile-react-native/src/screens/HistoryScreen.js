import React, { useCallback, useState } from 'react';
import {
  View, Text, FlatList, StyleSheet, ActivityIndicator,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { api } from '../api/client';

// Historico completo de medicoes usando FlatList.
export default function HistoryScreen() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setItems(await api.getMeasurements());
    } catch (e) {
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#b91c1c" /></View>;
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={items}
        keyExtractor={(item) => String(item.id)}
        ListEmptyComponent={<Text style={styles.empty}>Nenhuma medicao registrada.</Text>}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <View>
              <Text style={styles.pressure}>{item.systolic}/{item.diastolic} mmHg</Text>
              <Text style={styles.date}>{item.date} as {item.time}</Text>
              {item.notes ? <Text style={styles.notes}>{item.notes}</Text> : null}
            </View>
            <View style={[styles.badge, { backgroundColor: item.colorHex }]}>
              <Text style={styles.badgeText}>{item.classificationLabel}</Text>
            </View>
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f4f6fb', padding: 16 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  row: {
    backgroundColor: '#fff', borderRadius: 12, padding: 16, marginBottom: 10,
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', elevation: 1,
  },
  pressure: { fontSize: 18, fontWeight: 'bold', color: '#1f2937' },
  date: { color: '#6b7280', marginTop: 2, fontSize: 13 },
  notes: { color: '#9ca3af', marginTop: 2, fontSize: 12, fontStyle: 'italic' },
  badge: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999 },
  badgeText: { color: '#fff', fontWeight: '600', fontSize: 12 },
  empty: { textAlign: 'center', color: '#6b7280', marginTop: 40 },
});
