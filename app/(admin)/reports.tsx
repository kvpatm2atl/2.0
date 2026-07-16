import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Pressable, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { Card } from '@/components/ui/Card';
import { Colors, Radius, Spacing } from '@/constants/theme';
import { PrimaryButton } from '@/components/ui/PrimaryButton';

export default function ReportsScreen() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [reportGenerated, setReportGenerated] = useState(false);

  const handleGenerate = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      setReportGenerated(true);
    }, 1500);
  };

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} style={styles.backBtn}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={Colors.textPrimary} />
        </Pressable>
        <Text style={styles.title}>Reports</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView contentContainerStyle={styles.content}>
        <Card style={styles.optionsCard}>
          <Text style={styles.sectionTitle}>Report Options</Text>
          <View style={styles.filterRow}>
            <View style={styles.filterBox}>
              <Text style={styles.filterLabel}>Type</Text>
              <Text style={styles.filterValue}>Teacher Attendance</Text>
            </View>
            <View style={styles.filterBox}>
              <Text style={styles.filterLabel}>Date Range</Text>
              <Text style={styles.filterValue}>This Week</Text>
            </View>
          </View>
          <PrimaryButton 
            label={loading ? "Generating..." : "Generate Report"} 
            onPress={handleGenerate} 
            loading={loading}
          />
        </Card>

        {loading ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color={Colors.primary} />
            <Text style={styles.loadingText}>Compiling data...</Text>
          </View>
        ) : reportGenerated ? (
          <View style={styles.reportContainer}>
            <View style={styles.reportHeader}>
              <Text style={styles.sectionTitle}>Generated Report</Text>
              <Pressable style={styles.iconBtn}>
                <MaterialCommunityIcons name="download" size={20} color={Colors.primary} />
              </Pressable>
            </View>
            <Card style={styles.reportDataCard}>
              <View style={styles.dataRow}>
                <Text style={styles.dataLabel}>Total Teachers:</Text>
                <Text style={styles.dataValue}>45</Text>
              </View>
              <View style={styles.dataRow}>
                <Text style={styles.dataLabel}>Avg. Daily Attendance:</Text>
                <Text style={styles.dataValue}>92%</Text>
              </View>
              <View style={styles.dataRow}>
                <Text style={styles.dataLabel}>Total Absences:</Text>
                <Text style={styles.dataValue}>8</Text>
              </View>
              <View style={styles.dataRow}>
                <Text style={styles.dataLabel}>Substitutes Assigned:</Text>
                <Text style={styles.dataValue}>8</Text>
              </View>
            </Card>
          </View>
        ) : (
          <View style={styles.emptyContainer}>
            <MaterialCommunityIcons name="file-document-outline" size={48} color={Colors.textMuted} />
            <Text style={styles.emptyText}>Select options and generate a report to view data here.</Text>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md, backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: Colors.border },
  backBtn: { padding: Spacing.xs },
  title: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary },
  content: { padding: Spacing.lg },
  optionsCard: { marginBottom: Spacing.xl },
  sectionTitle: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary, marginBottom: Spacing.md },
  filterRow: { flexDirection: 'row', gap: Spacing.md, marginBottom: Spacing.lg },
  filterBox: { flex: 1, backgroundColor: Colors.background, padding: Spacing.md, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border },
  filterLabel: { fontSize: 12, color: Colors.textSecondary, marginBottom: 4 },
  filterValue: { fontSize: 14, fontWeight: '600', color: Colors.textPrimary },
  loadingContainer: { alignItems: 'center', justifyContent: 'center', padding: Spacing.xxl },
  loadingText: { marginTop: Spacing.md, color: Colors.textSecondary, fontWeight: '500' },
  reportContainer: { marginTop: Spacing.sm },
  reportHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: Spacing.md },
  iconBtn: { backgroundColor: Colors.surfaceMuted, padding: 8, borderRadius: 8 },
  reportDataCard: { gap: Spacing.md },
  dataRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingBottom: Spacing.sm, borderBottomWidth: 1, borderBottomColor: Colors.border },
  dataLabel: { fontSize: 14, color: Colors.textSecondary },
  dataValue: { fontSize: 16, fontWeight: '700', color: Colors.textPrimary },
  emptyContainer: { alignItems: 'center', justifyContent: 'center', padding: Spacing.xxl, marginTop: Spacing.xl },
  emptyText: { marginTop: Spacing.md, color: Colors.textMuted, textAlign: 'center', paddingHorizontal: Spacing.xl },
});
