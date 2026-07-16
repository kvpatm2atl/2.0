import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Dimensions, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Colors, Radius, Spacing, Shadows } from '@/constants/theme';
import { ScreenHeader } from '@/components/layout/ScreenHeader';
import { getSupabaseClient } from '@/template';
import { useAuth } from '@/hooks/useAuth';
import MapView, { Marker } from 'react-native-maps';
import { fetchParentStudent } from '@/services/schoolData';

const supabase = getSupabaseClient();

export default function BusTracking() {
  const { user } = useAuth();
  const [bus, setBus] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadBus();
  }, [user?.id]);

  const loadBus = async () => {
    if (!user?.id) return;
    const student = await fetchParentStudent(user.id);
    if (student?.bus_id) {
      const { data } = await supabase.from('buses').select('*').eq('id', student.bus_id).single();
      setBus(data);
    }
    setLoading(false);
  };

  return (
    <View style={styles.container}>
      <SafeAreaView edges={['top']}>
        <ScreenHeader title="Bus Tracking" subtitle={bus ? `Route: ${bus.route}` : 'Loading...'} />
      </SafeAreaView>

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator color={Colors.primary} size="large" />
        </View>
      ) : !bus ? (
        <View style={styles.center}>
          <MaterialCommunityIcons name="bus-alert" size={48} color={Colors.textMuted} />
          <Text style={styles.emptyText}>No bus assigned to your child.</Text>
        </View>
      ) : (
        <View style={{ flex: 1 }}>
          <View style={styles.mapContainer}>
            {bus.latitude && bus.longitude ? (
              <MapView
                style={StyleSheet.absoluteFillObject}
                initialRegion={{
                  latitude: bus.latitude,
                  longitude: bus.longitude,
                  latitudeDelta: 0.05,
                  longitudeDelta: 0.05,
                }}
              >
                <Marker
                  coordinate={{ latitude: bus.latitude, longitude: bus.longitude }}
                  title={bus.number}
                  description={bus.status}
                >
                  <View style={styles.markerBg}>
                    <MaterialCommunityIcons name="bus" size={20} color="#fff" />
                  </View>
                </Marker>
              </MapView>
            ) : (
              <View style={[styles.center, { backgroundColor: Colors.surfaceMuted }]}>
                <MaterialCommunityIcons name="map-marker-off" size={48} color={Colors.textMuted} />
                <Text style={styles.emptyText}>Bus location not active</Text>
                <Text style={styles.subText}>The bus is currently {bus.status}</Text>
              </View>
            )}
          </View>

          <View style={styles.infoCard}>
            <View style={styles.row}>
              <View style={[styles.iconWrap, { backgroundColor: Colors.infoBg }]}>
                <MaterialCommunityIcons name="steering" color={Colors.info} size={24} />
              </View>
              <View style={{ flex: 1, marginLeft: Spacing.md }}>
                <Text style={styles.busNumber}>{bus.number}</Text>
                <Text style={styles.driverName}>Driver: {bus.driver}</Text>
              </View>
              <View style={[styles.statusPill, { backgroundColor: bus.status === 'At School' ? Colors.successBg : Colors.warningBg }]}>
                <Text style={[styles.statusText, { color: bus.status === 'At School' ? Colors.success : Colors.warning }]}>
                  {bus.status}
                </Text>
              </View>
            </View>
            
            <View style={styles.statsRow}>
              <View style={styles.stat}>
                <Text style={styles.statLabel}>ETA</Text>
                <Text style={styles.statVal}>{bus.eta || '—'}</Text>
              </View>
              <View style={styles.stat}>
                <Text style={styles.statLabel}>Speed</Text>
                <Text style={styles.statVal}>{bus.speed || 0} km/h</Text>
              </View>
            </View>
          </View>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  emptyText: { fontSize: 16, fontWeight: '700', color: Colors.textSecondary, marginTop: Spacing.md },
  subText: { fontSize: 13, color: Colors.textMuted, marginTop: Spacing.xs },
  mapContainer: { flex: 1, overflow: 'hidden' },
  markerBg: { width: 36, height: 36, borderRadius: 18, backgroundColor: Colors.primary, alignItems: 'center', justifyContent: 'center', borderWidth: 2, borderColor: '#fff' },
  infoCard: { backgroundColor: Colors.surface, padding: Spacing.xl, borderTopLeftRadius: Radius.xl, borderTopRightRadius: Radius.xl, ...Shadows.raised, paddingBottom: 40 },
  row: { flexDirection: 'row', alignItems: 'center' },
  iconWrap: { width: 48, height: 48, borderRadius: Radius.md, alignItems: 'center', justifyContent: 'center' },
  busNumber: { fontSize: 18, fontWeight: '800', color: Colors.textPrimary },
  driverName: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },
  statusPill: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: Radius.pill },
  statusText: { fontSize: 12, fontWeight: '700' },
  statsRow: { flexDirection: 'row', marginTop: Spacing.lg, paddingTop: Spacing.md, borderTopWidth: 1, borderTopColor: Colors.border },
  stat: { flex: 1 },
  statLabel: { fontSize: 11, color: Colors.textMuted, fontWeight: '600', textTransform: 'uppercase' },
  statVal: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary, marginTop: 4 },
});
