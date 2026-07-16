// Admin Special Module — Leave Approvals, Performance Overview, Resource Requests
// Powered by OnSpace.AI

import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, StyleSheet, ScrollView, Pressable,
  ActivityIndicator, RefreshControl, Alert, TextInput,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { Card } from '@/components/ui/Card';
import { Pill } from '@/components/ui/Pill';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { Colors, Radius, Spacing, Shadows } from '@/constants/theme';
import { getSupabaseClient } from '@/template';

const supabase = getSupabaseClient();

type TabKey = 'leaves' | 'performance' | 'resources';

interface LeaveRequest {
  id: string;
  teacher_name: string;
  leave_type: string;
  start_date: string;
  end_date: string;
  reason: string;
  status: 'Pending' | 'Approved' | 'Rejected';
  created_at: string;
}

interface ResourceRequest {
  id: string;
  teacher_name: string;
  item: string;
  quantity: number;
  urgency: 'Low' | 'Medium' | 'High';
  status: 'Pending' | 'Approved' | 'Rejected';
  notes: string;
  created_at: string;
}

interface PerformanceMetric {
  teacher_name: string;
  classes_taken: number;
  avg_attendance: number;
  homework_assigned: number;
  incidents_reported: number;
}

// Mock data since tables may not exist yet
const MOCK_LEAVES: LeaveRequest[] = [
  { id: '1', teacher_name: 'BROMLY THOMAS', leave_type: 'Casual Leave', start_date: '2026-07-17', end_date: '2026-07-18', reason: 'Personal work', status: 'Pending', created_at: '2026-07-16T05:00:00Z' },
  { id: '2', teacher_name: 'SANTHA D', leave_type: 'Sick Leave', start_date: '2026-07-16', end_date: '2026-07-16', reason: 'Feeling unwell', status: 'Pending', created_at: '2026-07-15T10:30:00Z' },
  { id: '3', teacher_name: 'T KUMARI JAYA', leave_type: 'Earned Leave', start_date: '2026-07-20', end_date: '2026-07-25', reason: 'Family function', status: 'Approved', created_at: '2026-07-14T08:00:00Z' },
];

const MOCK_RESOURCES: ResourceRequest[] = [
  { id: '1', teacher_name: 'BROMLY THOMAS', item: 'Chemistry Lab Reagents', quantity: 5, urgency: 'High', status: 'Pending', notes: 'Needed for Class 12 practicals next week', created_at: '2026-07-16T04:00:00Z' },
  { id: '2', teacher_name: 'SANTHA D', item: 'Whiteboard Markers (Set)', quantity: 3, urgency: 'Medium', status: 'Pending', notes: 'Running low in Section 10B', created_at: '2026-07-15T09:00:00Z' },
  { id: '3', teacher_name: 'T KUMARI JAYA', item: 'Projector Bulb Replacement', quantity: 1, urgency: 'Low', status: 'Approved', notes: 'Room 204 projector is dim', created_at: '2026-07-13T11:00:00Z' },
];

const MOCK_PERFORMANCE: PerformanceMetric[] = [
  { teacher_name: 'BROMLY THOMAS', classes_taken: 42, avg_attendance: 94, homework_assigned: 18, incidents_reported: 1 },
  { teacher_name: 'SANTHA D', classes_taken: 38, avg_attendance: 89, homework_assigned: 22, incidents_reported: 0 },
  { teacher_name: 'T KUMARI JAYA', classes_taken: 45, avg_attendance: 96, homework_assigned: 15, incidents_reported: 2 },
  { teacher_name: 'RAJESH KUMAR', classes_taken: 40, avg_attendance: 91, homework_assigned: 20, incidents_reported: 0 },
  { teacher_name: 'ANITA SHARMA', classes_taken: 44, avg_attendance: 97, homework_assigned: 25, incidents_reported: 0 },
];

export default function AdminSpecialModuleScreen() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<TabKey>('leaves');
  const [leaves, setLeaves] = useState<LeaveRequest[]>(MOCK_LEAVES);
  const [resources, setResources] = useState<ResourceRequest[]>(MOCK_RESOURCES);
  const [performance, setPerformance] = useState<PerformanceMetric[]>(MOCK_PERFORMANCE);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    // In production: fetch from Supabase
    setTimeout(() => setRefreshing(false), 800);
  }, []);

  const handleLeaveAction = (id: string, action: 'Approved' | 'Rejected') => {
    setLeaves(prev => prev.map(l => l.id === id ? { ...l, status: action } : l));
    Alert.alert('Done', `Leave request ${action.toLowerCase()}.`);
  };

  const handleResourceAction = (id: string, action: 'Approved' | 'Rejected') => {
    setResources(prev => prev.map(r => r.id === id ? { ...r, status: action } : r));
    Alert.alert('Done', `Resource request ${action.toLowerCase()}.`);
  };

  const pendingLeaves = leaves.filter(l => l.status === 'Pending').length;
  const pendingResources = resources.filter(r => r.status === 'Pending').length;

  const tabs: { key: TabKey; label: string; icon: any; badge?: number }[] = [
    { key: 'leaves', label: 'Leave Requests', icon: 'calendar-remove', badge: pendingLeaves },
    { key: 'performance', label: 'Performance', icon: 'chart-bar' },
    { key: 'resources', label: 'Resources', icon: 'package-variant', badge: pendingResources },
  ];

  return (
    <View style={{ flex: 1, backgroundColor: Colors.background }}>
      <SafeAreaView edges={['top']} style={{ backgroundColor: '#1E3A5F' }}>
        <LinearGradient colors={['#1E3A5F', '#2C5F8A', '#3B82A8']} style={styles.hero}>
          <View style={styles.heroRow}>
            <Pressable onPress={() => router.back()} style={styles.backBtn}>
              <MaterialCommunityIcons name="arrow-left" size={24} color="#fff" />
            </Pressable>
            <View style={{ flex: 1, marginLeft: 12 }}>
              <Text style={styles.heroTitle}>Special Module</Text>
              <Text style={styles.heroSub}>Admin Control Center</Text>
            </View>
          </View>

          {/* Tab bar */}
          <View style={styles.tabBar}>
            {tabs.map(tab => (
              <Pressable
                key={tab.key}
                style={[styles.tab, activeTab === tab.key && styles.tabActive]}
                onPress={() => setActiveTab(tab.key)}
              >
                <MaterialCommunityIcons name={tab.icon} size={18} color={activeTab === tab.key ? '#fff' : 'rgba(255,255,255,0.55)'} />
                <Text style={[styles.tabLabel, activeTab === tab.key && styles.tabLabelActive]}>{tab.label}</Text>
                {tab.badge != null && tab.badge > 0 && (
                  <View style={styles.tabBadge}>
                    <Text style={styles.tabBadgeText}>{tab.badge}</Text>
                  </View>
                )}
              </Pressable>
            ))}
          </View>
        </LinearGradient>
      </SafeAreaView>

      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      >
        {activeTab === 'leaves' && (
          <>
            <Text style={styles.section}>Pending Approval</Text>
            {leaves.filter(l => l.status === 'Pending').map(l => (
              <Card key={l.id} style={{ marginBottom: Spacing.md }}>
                <View style={styles.row}>
                  <View style={[styles.avatar, { backgroundColor: Colors.infoBg }]}>
                    <MaterialCommunityIcons name="account" size={22} color={Colors.info} />
                  </View>
                  <View style={{ flex: 1, marginLeft: 12 }}>
                    <Text style={styles.cardTitle}>{l.teacher_name}</Text>
                    <Text style={styles.cardSub}>{l.leave_type} · {l.start_date} → {l.end_date}</Text>
                    <Text style={styles.cardDetail}>{l.reason}</Text>
                  </View>
                </View>
                <View style={styles.actionRow}>
                  <Pressable style={[styles.actionBtn, styles.approveBtn]} onPress={() => handleLeaveAction(l.id, 'Approved')}>
                    <MaterialCommunityIcons name="check" size={16} color="#fff" />
                    <Text style={styles.actionBtnText}>Approve</Text>
                  </Pressable>
                  <Pressable style={[styles.actionBtn, styles.rejectBtn]} onPress={() => handleLeaveAction(l.id, 'Rejected')}>
                    <MaterialCommunityIcons name="close" size={16} color={Colors.danger} />
                    <Text style={[styles.actionBtnText, { color: Colors.danger }]}>Reject</Text>
                  </Pressable>
                </View>
              </Card>
            ))}
            {leaves.filter(l => l.status === 'Pending').length === 0 && (
              <View style={styles.emptyState}>
                <MaterialCommunityIcons name="check-circle-outline" size={48} color={Colors.success} />
                <Text style={styles.emptyText}>All leave requests have been reviewed!</Text>
              </View>
            )}

            <Text style={styles.section}>History</Text>
            {leaves.filter(l => l.status !== 'Pending').map(l => (
              <Card key={l.id} style={{ marginBottom: Spacing.sm }}>
                <View style={styles.row}>
                  <View style={{ flex: 1 }}>
                    <Text style={styles.cardTitle}>{l.teacher_name}</Text>
                    <Text style={styles.cardSub}>{l.leave_type} · {l.start_date} → {l.end_date}</Text>
                  </View>
                  <Pill label={l.status} tone={l.status === 'Approved' ? 'success' : 'danger'} />
                </View>
              </Card>
            ))}
          </>
        )}

        {activeTab === 'performance' && (
          <>
            <Text style={styles.section}>Teacher Performance Overview</Text>
            <Text style={styles.sectionSub}>Weekly metrics across all teaching staff</Text>

            {/* Summary cards */}
            <View style={styles.metricsGrid}>
              <View style={[styles.metricCard, { backgroundColor: Colors.infoBg }]}>
                <Text style={[styles.metricValue, { color: Colors.info }]}>{performance.reduce((a, p) => a + p.classes_taken, 0)}</Text>
                <Text style={styles.metricLabel}>Total Classes</Text>
              </View>
              <View style={[styles.metricCard, { backgroundColor: Colors.successBg }]}>
                <Text style={[styles.metricValue, { color: Colors.success }]}>
                  {Math.round(performance.reduce((a, p) => a + p.avg_attendance, 0) / performance.length)}%
                </Text>
                <Text style={styles.metricLabel}>Avg Attendance</Text>
              </View>
              <View style={[styles.metricCard, { backgroundColor: Colors.warningBg }]}>
                <Text style={[styles.metricValue, { color: Colors.warning }]}>{performance.reduce((a, p) => a + p.homework_assigned, 0)}</Text>
                <Text style={styles.metricLabel}>Homework Given</Text>
              </View>
              <View style={[styles.metricCard, { backgroundColor: Colors.dangerBg }]}>
                <Text style={[styles.metricValue, { color: Colors.danger }]}>{performance.reduce((a, p) => a + p.incidents_reported, 0)}</Text>
                <Text style={styles.metricLabel}>Incidents</Text>
              </View>
            </View>

            {/* Per-teacher breakdown */}
            <Text style={styles.section}>Individual Breakdown</Text>
            {performance
              .sort((a, b) => b.avg_attendance - a.avg_attendance)
              .map((p, idx) => (
                <Card key={idx} style={{ marginBottom: Spacing.sm }}>
                  <View style={styles.row}>
                    <View style={[styles.rankBadge, idx === 0 ? { backgroundColor: '#FEF3C7' } : { backgroundColor: Colors.surfaceMuted }]}>
                      <Text style={[styles.rankText, idx === 0 && { color: '#D97706' }]}>#{idx + 1}</Text>
                    </View>
                    <View style={{ flex: 1, marginLeft: 12 }}>
                      <Text style={styles.cardTitle}>{p.teacher_name}</Text>
                      <View style={styles.statRow}>
                        <View style={styles.stat}>
                          <Text style={styles.statValue}>{p.classes_taken}</Text>
                          <Text style={styles.statLabel}>Classes</Text>
                        </View>
                        <View style={styles.stat}>
                          <Text style={[styles.statValue, { color: p.avg_attendance >= 95 ? Colors.success : p.avg_attendance >= 90 ? Colors.info : Colors.warning }]}>
                            {p.avg_attendance}%
                          </Text>
                          <Text style={styles.statLabel}>Attendance</Text>
                        </View>
                        <View style={styles.stat}>
                          <Text style={styles.statValue}>{p.homework_assigned}</Text>
                          <Text style={styles.statLabel}>HW</Text>
                        </View>
                        <View style={styles.stat}>
                          <Text style={[styles.statValue, { color: p.incidents_reported > 0 ? Colors.danger : Colors.success }]}>
                            {p.incidents_reported}
                          </Text>
                          <Text style={styles.statLabel}>Incidents</Text>
                        </View>
                      </View>
                    </View>
                  </View>
                </Card>
              ))}
          </>
        )}

        {activeTab === 'resources' && (
          <>
            <Text style={styles.section}>Pending Resource Requests</Text>
            {resources.filter(r => r.status === 'Pending').map(r => (
              <Card key={r.id} style={{ marginBottom: Spacing.md }}>
                <View style={styles.row}>
                  <View style={[styles.avatar, { backgroundColor: r.urgency === 'High' ? Colors.dangerBg : r.urgency === 'Medium' ? Colors.warningBg : Colors.successBg }]}>
                    <MaterialCommunityIcons
                      name="package-variant"
                      size={22}
                      color={r.urgency === 'High' ? Colors.danger : r.urgency === 'Medium' ? Colors.warning : Colors.success}
                    />
                  </View>
                  <View style={{ flex: 1, marginLeft: 12 }}>
                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                      <Text style={styles.cardTitle}>{r.item}</Text>
                      <Pill label={r.urgency} tone={r.urgency === 'High' ? 'danger' : r.urgency === 'Medium' ? 'warning' : 'success'} />
                    </View>
                    <Text style={styles.cardSub}>Requested by {r.teacher_name} · Qty: {r.quantity}</Text>
                    <Text style={styles.cardDetail}>{r.notes}</Text>
                  </View>
                </View>
                <View style={styles.actionRow}>
                  <Pressable style={[styles.actionBtn, styles.approveBtn]} onPress={() => handleResourceAction(r.id, 'Approved')}>
                    <MaterialCommunityIcons name="check" size={16} color="#fff" />
                    <Text style={styles.actionBtnText}>Approve</Text>
                  </Pressable>
                  <Pressable style={[styles.actionBtn, styles.rejectBtn]} onPress={() => handleResourceAction(r.id, 'Rejected')}>
                    <MaterialCommunityIcons name="close" size={16} color={Colors.danger} />
                    <Text style={[styles.actionBtnText, { color: Colors.danger }]}>Reject</Text>
                  </Pressable>
                </View>
              </Card>
            ))}
            {resources.filter(r => r.status === 'Pending').length === 0 && (
              <View style={styles.emptyState}>
                <MaterialCommunityIcons name="check-circle-outline" size={48} color={Colors.success} />
                <Text style={styles.emptyText}>All resource requests have been reviewed!</Text>
              </View>
            )}

            <Text style={styles.section}>Processed</Text>
            {resources.filter(r => r.status !== 'Pending').map(r => (
              <Card key={r.id} style={{ marginBottom: Spacing.sm }}>
                <View style={styles.row}>
                  <View style={{ flex: 1 }}>
                    <Text style={styles.cardTitle}>{r.item}</Text>
                    <Text style={styles.cardSub}>{r.teacher_name} · Qty: {r.quantity}</Text>
                  </View>
                  <Pill label={r.status} tone={r.status === 'Approved' ? 'success' : 'danger'} />
                </View>
              </Card>
            ))}
          </>
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  hero: {
    paddingHorizontal: Spacing.xl,
    paddingTop: Spacing.md,
    paddingBottom: 0,
    borderBottomLeftRadius: Radius.xl,
    borderBottomRightRadius: Radius.xl,
  },
  heroRow: { flexDirection: 'row', alignItems: 'center' },
  backBtn: { padding: Spacing.xs },
  heroTitle: { color: '#fff', fontSize: 22, fontWeight: '800' },
  heroSub: { color: 'rgba(255,255,255,0.7)', fontSize: 13, marginTop: 2 },

  tabBar: { flexDirection: 'row', marginTop: Spacing.lg, gap: 4 },
  tab: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    paddingHorizontal: 8,
    borderTopLeftRadius: Radius.md,
    borderTopRightRadius: Radius.md,
    gap: 6,
  },
  tabActive: { backgroundColor: 'rgba(255,255,255,0.15)' },
  tabLabel: { color: 'rgba(255,255,255,0.55)', fontSize: 11, fontWeight: '700' },
  tabLabelActive: { color: '#fff' },
  tabBadge: { backgroundColor: Colors.danger, borderRadius: 10, paddingHorizontal: 6, paddingVertical: 1, marginLeft: 2 },
  tabBadgeText: { color: '#fff', fontSize: 10, fontWeight: '900' },

  content: { padding: Spacing.xl, paddingBottom: Spacing.xxxl },
  section: { fontSize: 17, fontWeight: '800', color: Colors.textPrimary, marginTop: Spacing.lg, marginBottom: Spacing.md },
  sectionSub: { fontSize: 13, color: Colors.textSecondary, marginTop: -Spacing.sm, marginBottom: Spacing.md },

  row: { flexDirection: 'row', alignItems: 'center' },
  avatar: { width: 42, height: 42, borderRadius: 14, alignItems: 'center', justifyContent: 'center' },
  cardTitle: { fontSize: 15, fontWeight: '700', color: Colors.textPrimary },
  cardSub: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },
  cardDetail: { fontSize: 13, color: Colors.textMuted, marginTop: 4, fontStyle: 'italic' },

  actionRow: { flexDirection: 'row', gap: Spacing.sm, marginTop: Spacing.md, justifyContent: 'flex-end' },
  actionBtn: { flexDirection: 'row', alignItems: 'center', gap: 4, paddingVertical: 8, paddingHorizontal: 14, borderRadius: Radius.sm },
  actionBtnText: { fontSize: 13, fontWeight: '700', color: '#fff' },
  approveBtn: { backgroundColor: Colors.success },
  rejectBtn: { backgroundColor: Colors.dangerBg, borderWidth: 1, borderColor: Colors.danger },

  emptyState: { alignItems: 'center', justifyContent: 'center', paddingVertical: Spacing.xxl },
  emptyText: { fontSize: 15, color: Colors.textSecondary, fontWeight: '600', marginTop: Spacing.md, textAlign: 'center' },

  metricsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  metricCard: { width: '47%', borderRadius: Radius.lg, padding: Spacing.lg, alignItems: 'center' },
  metricValue: { fontSize: 28, fontWeight: '900' },
  metricLabel: { fontSize: 11, fontWeight: '700', color: Colors.textSecondary, marginTop: 4, textTransform: 'uppercase', letterSpacing: 0.5 },

  rankBadge: { width: 36, height: 36, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  rankText: { fontWeight: '900', fontSize: 14, color: Colors.textSecondary },

  statRow: { flexDirection: 'row', marginTop: 8, gap: Spacing.md },
  stat: { alignItems: 'center' },
  statValue: { fontSize: 16, fontWeight: '800', color: Colors.textPrimary },
  statLabel: { fontSize: 10, color: Colors.textMuted, fontWeight: '600', marginTop: 2 },
});
