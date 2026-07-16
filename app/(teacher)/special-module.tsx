// Teacher Special Module — Submit Leaves, View Performance, Request Resources
// Powered by OnSpace.AI

import React, { useState, useCallback } from 'react';
import {
  View, Text, StyleSheet, ScrollView, Pressable,
  TextInput, Alert, RefreshControl, Modal,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { Card } from '@/components/ui/Card';
import { Pill } from '@/components/ui/Pill';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { Colors, Radius, Spacing } from '@/constants/theme';
import { useAuth } from '@/hooks/useAuth';

type TabKey = 'my-leaves' | 'my-performance' | 'requests';

interface LeaveEntry {
  id: string;
  leave_type: string;
  start_date: string;
  end_date: string;
  reason: string;
  status: 'Pending' | 'Approved' | 'Rejected';
  created_at: string;
}

interface ResourceEntry {
  id: string;
  item: string;
  quantity: number;
  urgency: 'Low' | 'Medium' | 'High';
  notes: string;
  status: 'Pending' | 'Approved' | 'Rejected';
  created_at: string;
}

const LEAVE_TYPES = ['Casual Leave', 'Sick Leave', 'Earned Leave', 'Duty Leave', 'Half Day'];
const URGENCY_LEVELS: ('Low' | 'Medium' | 'High')[] = ['Low', 'Medium', 'High'];

export default function TeacherSpecialModuleScreen() {
  const router = useRouter();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<TabKey>('my-leaves');
  const [refreshing, setRefreshing] = useState(false);

  // Leave state
  const [leaves, setLeaves] = useState<LeaveEntry[]>([
    { id: '1', leave_type: 'Casual Leave', start_date: '2026-07-10', end_date: '2026-07-11', reason: 'Family event', status: 'Approved', created_at: '2026-07-08' },
    { id: '2', leave_type: 'Sick Leave', start_date: '2026-07-16', end_date: '2026-07-16', reason: 'Feeling unwell', status: 'Pending', created_at: '2026-07-15' },
  ]);
  const [showLeaveModal, setShowLeaveModal] = useState(false);
  const [leaveForm, setLeaveForm] = useState({ type: 'Casual Leave', start: '', end: '', reason: '' });

  // Resource state
  const [resources, setResources] = useState<ResourceEntry[]>([
    { id: '1', item: 'Whiteboard Markers (Set)', quantity: 3, urgency: 'Medium', notes: 'Running low', status: 'Approved', created_at: '2026-07-12' },
  ]);
  const [showResourceModal, setShowResourceModal] = useState(false);
  const [resourceForm, setResourceForm] = useState({ item: '', quantity: '1', urgency: 'Medium' as 'Low' | 'Medium' | 'High', notes: '' });

  // Performance data
  const myPerformance = {
    classes_taken: 42,
    total_classes: 45,
    avg_attendance: 94,
    homework_assigned: 18,
    homework_graded: 15,
    incidents_reported: 1,
    leaves_taken: 3,
    leaves_remaining: 12,
  };

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    setTimeout(() => setRefreshing(false), 800);
  }, []);

  const submitLeave = () => {
    if (!leaveForm.start || !leaveForm.end || !leaveForm.reason) {
      Alert.alert('Missing Fields', 'Please fill in all fields.');
      return;
    }
    const newLeave: LeaveEntry = {
      id: Date.now().toString(),
      leave_type: leaveForm.type,
      start_date: leaveForm.start,
      end_date: leaveForm.end,
      reason: leaveForm.reason,
      status: 'Pending',
      created_at: new Date().toISOString(),
    };
    setLeaves(prev => [newLeave, ...prev]);
    setShowLeaveModal(false);
    setLeaveForm({ type: 'Casual Leave', start: '', end: '', reason: '' });
    Alert.alert('Submitted', 'Your leave request has been sent for approval.');
  };

  const submitResource = () => {
    if (!resourceForm.item) {
      Alert.alert('Missing Fields', 'Please specify the item name.');
      return;
    }
    const newResource: ResourceEntry = {
      id: Date.now().toString(),
      item: resourceForm.item,
      quantity: parseInt(resourceForm.quantity) || 1,
      urgency: resourceForm.urgency,
      notes: resourceForm.notes,
      status: 'Pending',
      created_at: new Date().toISOString(),
    };
    setResources(prev => [newResource, ...prev]);
    setShowResourceModal(false);
    setResourceForm({ item: '', quantity: '1', urgency: 'Medium', notes: '' });
    Alert.alert('Submitted', 'Your resource request has been sent for approval.');
  };

  const tabs: { key: TabKey; label: string; icon: any }[] = [
    { key: 'my-leaves', label: 'My Leaves', icon: 'calendar-remove' },
    { key: 'my-performance', label: 'My Stats', icon: 'chart-line' },
    { key: 'requests', label: 'Resources', icon: 'package-variant' },
  ];

  return (
    <View style={{ flex: 1, backgroundColor: Colors.background }}>
      <SafeAreaView edges={['top']} style={{ backgroundColor: '#065F46' }}>
        <LinearGradient colors={['#065F46', '#047857', '#10B981']} style={styles.hero}>
          <View style={styles.heroRow}>
            <Pressable onPress={() => router.back()} style={styles.backBtn}>
              <MaterialCommunityIcons name="arrow-left" size={24} color="#fff" />
            </Pressable>
            <View style={{ flex: 1, marginLeft: 12 }}>
              <Text style={styles.heroTitle}>Special Module</Text>
              <Text style={styles.heroSub}>{user?.name ?? 'Teacher'}</Text>
            </View>
          </View>

          <View style={styles.tabBar}>
            {tabs.map(tab => (
              <Pressable
                key={tab.key}
                style={[styles.tab, activeTab === tab.key && styles.tabActive]}
                onPress={() => setActiveTab(tab.key)}
              >
                <MaterialCommunityIcons name={tab.icon} size={18} color={activeTab === tab.key ? '#fff' : 'rgba(255,255,255,0.55)'} />
                <Text style={[styles.tabLabel, activeTab === tab.key && styles.tabLabelActive]}>{tab.label}</Text>
              </Pressable>
            ))}
          </View>
        </LinearGradient>
      </SafeAreaView>

      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      >
        {/* ── MY LEAVES ─────────────────── */}
        {activeTab === 'my-leaves' && (
          <>
            {/* Leave balance */}
            <View style={styles.balanceRow}>
              <View style={[styles.balanceCard, { backgroundColor: Colors.successBg }]}>
                <Text style={[styles.balanceValue, { color: Colors.success }]}>{myPerformance.leaves_remaining}</Text>
                <Text style={styles.balanceLabel}>Remaining</Text>
              </View>
              <View style={[styles.balanceCard, { backgroundColor: Colors.infoBg }]}>
                <Text style={[styles.balanceValue, { color: Colors.info }]}>{myPerformance.leaves_taken}</Text>
                <Text style={styles.balanceLabel}>Used</Text>
              </View>
              <View style={[styles.balanceCard, { backgroundColor: Colors.warningBg }]}>
                <Text style={[styles.balanceValue, { color: Colors.warning }]}>{leaves.filter(l => l.status === 'Pending').length}</Text>
                <Text style={styles.balanceLabel}>Pending</Text>
              </View>
            </View>

            <PrimaryButton
              label="Apply for Leave"
              onPress={() => setShowLeaveModal(true)}
              style={{ marginBottom: Spacing.lg }}
            />

            <Text style={styles.section}>My Leave History</Text>
            {leaves.map(l => (
              <Card key={l.id} style={{ marginBottom: Spacing.sm }}>
                <View style={styles.row}>
                  <View style={[styles.statusDot, {
                    backgroundColor: l.status === 'Approved' ? Colors.success : l.status === 'Rejected' ? Colors.danger : Colors.warning,
                  }]} />
                  <View style={{ flex: 1, marginLeft: 12 }}>
                    <Text style={styles.cardTitle}>{l.leave_type}</Text>
                    <Text style={styles.cardSub}>{l.start_date} → {l.end_date}</Text>
                    <Text style={styles.cardDetail}>{l.reason}</Text>
                  </View>
                  <Pill
                    label={l.status}
                    tone={l.status === 'Approved' ? 'success' : l.status === 'Rejected' ? 'danger' : 'warning'}
                  />
                </View>
              </Card>
            ))}
          </>
        )}

        {/* ── MY PERFORMANCE ─────────────── */}
        {activeTab === 'my-performance' && (
          <>
            <Text style={styles.section}>This Week's Performance</Text>

            {/* Progress ring cards */}
            <View style={styles.perfGrid}>
              <PerfCard
                label="Classes Taken"
                value={myPerformance.classes_taken}
                total={myPerformance.total_classes}
                color={Colors.info}
                bg={Colors.infoBg}
                icon="book-open-variant"
              />
              <PerfCard
                label="Avg. Attendance"
                value={myPerformance.avg_attendance}
                total={100}
                suffix="%"
                color={Colors.success}
                bg={Colors.successBg}
                icon="account-check"
              />
              <PerfCard
                label="HW Assigned"
                value={myPerformance.homework_assigned}
                total={myPerformance.homework_assigned}
                color="#6E55C2"
                bg="#F0ECFD"
                icon="notebook"
              />
              <PerfCard
                label="HW Graded"
                value={myPerformance.homework_graded}
                total={myPerformance.homework_assigned}
                color={Colors.warning}
                bg={Colors.warningBg}
                icon="check-decagram"
              />
            </View>

            <Card style={{ marginTop: Spacing.lg }}>
              <Text style={styles.cardTitle}>Performance Summary</Text>
              <View style={{ marginTop: Spacing.md, gap: Spacing.sm }}>
                <SummaryRow label="Classes Completion Rate" value={`${Math.round((myPerformance.classes_taken / myPerformance.total_classes) * 100)}%`} good />
                <SummaryRow label="Homework Grading Rate" value={`${Math.round((myPerformance.homework_graded / myPerformance.homework_assigned) * 100)}%`} good={myPerformance.homework_graded / myPerformance.homework_assigned >= 0.8} />
                <SummaryRow label="Incidents Reported" value={`${myPerformance.incidents_reported}`} good={myPerformance.incidents_reported === 0} />
                <SummaryRow label="Leave Balance" value={`${myPerformance.leaves_remaining} / ${myPerformance.leaves_taken + myPerformance.leaves_remaining}`} good={myPerformance.leaves_remaining > 5} />
              </View>
            </Card>
          </>
        )}

        {/* ── RESOURCE REQUESTS ──────────── */}
        {activeTab === 'requests' && (
          <>
            <PrimaryButton
              label="Request Resource"
              onPress={() => setShowResourceModal(true)}
              style={{ marginBottom: Spacing.lg, marginTop: Spacing.md }}
            />

            <Text style={styles.section}>My Requests</Text>
            {resources.map(r => (
              <Card key={r.id} style={{ marginBottom: Spacing.sm }}>
                <View style={styles.row}>
                  <View style={[styles.avatar, {
                    backgroundColor: r.urgency === 'High' ? Colors.dangerBg : r.urgency === 'Medium' ? Colors.warningBg : Colors.successBg,
                  }]}>
                    <MaterialCommunityIcons
                      name="package-variant"
                      size={20}
                      color={r.urgency === 'High' ? Colors.danger : r.urgency === 'Medium' ? Colors.warning : Colors.success}
                    />
                  </View>
                  <View style={{ flex: 1, marginLeft: 12 }}>
                    <Text style={styles.cardTitle}>{r.item}</Text>
                    <Text style={styles.cardSub}>Qty: {r.quantity} · {r.urgency} Priority</Text>
                    {r.notes ? <Text style={styles.cardDetail}>{r.notes}</Text> : null}
                  </View>
                  <Pill
                    label={r.status}
                    tone={r.status === 'Approved' ? 'success' : r.status === 'Rejected' ? 'danger' : 'warning'}
                  />
                </View>
              </Card>
            ))}
            {resources.length === 0 && (
              <View style={styles.emptyState}>
                <MaterialCommunityIcons name="package-variant-closed" size={48} color={Colors.textMuted} />
                <Text style={styles.emptyText}>No resource requests yet.</Text>
              </View>
            )}
          </>
        )}
      </ScrollView>

      {/* ── LEAVE MODAL ─────────────── */}
      <Modal visible={showLeaveModal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Apply for Leave</Text>
              <Pressable onPress={() => setShowLeaveModal(false)}>
                <MaterialCommunityIcons name="close" size={24} color={Colors.textMuted} />
              </Pressable>
            </View>

            <Text style={styles.inputLabel}>Leave Type</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginBottom: Spacing.md }}>
              <View style={{ flexDirection: 'row', gap: 8 }}>
                {LEAVE_TYPES.map(lt => (
                  <Pressable
                    key={lt}
                    style={[styles.chip, leaveForm.type === lt && styles.chipActive]}
                    onPress={() => setLeaveForm(f => ({ ...f, type: lt }))}
                  >
                    <Text style={[styles.chipText, leaveForm.type === lt && styles.chipTextActive]}>{lt}</Text>
                  </Pressable>
                ))}
              </View>
            </ScrollView>

            <Text style={styles.inputLabel}>Start Date (YYYY-MM-DD)</Text>
            <TextInput
              style={styles.textInput}
              placeholder="2026-07-17"
              placeholderTextColor={Colors.textMuted}
              value={leaveForm.start}
              onChangeText={t => setLeaveForm(f => ({ ...f, start: t }))}
            />

            <Text style={styles.inputLabel}>End Date (YYYY-MM-DD)</Text>
            <TextInput
              style={styles.textInput}
              placeholder="2026-07-18"
              placeholderTextColor={Colors.textMuted}
              value={leaveForm.end}
              onChangeText={t => setLeaveForm(f => ({ ...f, end: t }))}
            />

            <Text style={styles.inputLabel}>Reason</Text>
            <TextInput
              style={[styles.textInput, { height: 80, textAlignVertical: 'top' }]}
              placeholder="Why do you need leave?"
              placeholderTextColor={Colors.textMuted}
              multiline
              value={leaveForm.reason}
              onChangeText={t => setLeaveForm(f => ({ ...f, reason: t }))}
            />

            <PrimaryButton label="Submit Leave Request" onPress={submitLeave} style={{ marginTop: Spacing.lg }} />
          </View>
        </View>
      </Modal>

      {/* ── RESOURCE MODAL ─────────── */}
      <Modal visible={showResourceModal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Request Resource</Text>
              <Pressable onPress={() => setShowResourceModal(false)}>
                <MaterialCommunityIcons name="close" size={24} color={Colors.textMuted} />
              </Pressable>
            </View>

            <Text style={styles.inputLabel}>Item Name</Text>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Whiteboard Markers"
              placeholderTextColor={Colors.textMuted}
              value={resourceForm.item}
              onChangeText={t => setResourceForm(f => ({ ...f, item: t }))}
            />

            <Text style={styles.inputLabel}>Quantity</Text>
            <TextInput
              style={styles.textInput}
              placeholder="1"
              placeholderTextColor={Colors.textMuted}
              keyboardType="numeric"
              value={resourceForm.quantity}
              onChangeText={t => setResourceForm(f => ({ ...f, quantity: t }))}
            />

            <Text style={styles.inputLabel}>Urgency</Text>
            <View style={{ flexDirection: 'row', gap: 8, marginBottom: Spacing.md }}>
              {URGENCY_LEVELS.map(u => (
                <Pressable
                  key={u}
                  style={[styles.chip, resourceForm.urgency === u && styles.chipActive]}
                  onPress={() => setResourceForm(f => ({ ...f, urgency: u }))}
                >
                  <Text style={[styles.chipText, resourceForm.urgency === u && styles.chipTextActive]}>{u}</Text>
                </Pressable>
              ))}
            </View>

            <Text style={styles.inputLabel}>Notes</Text>
            <TextInput
              style={[styles.textInput, { height: 80, textAlignVertical: 'top' }]}
              placeholder="Additional details..."
              placeholderTextColor={Colors.textMuted}
              multiline
              value={resourceForm.notes}
              onChangeText={t => setResourceForm(f => ({ ...f, notes: t }))}
            />

            <PrimaryButton label="Submit Request" onPress={submitResource} style={{ marginTop: Spacing.lg }} />
          </View>
        </View>
      </Modal>
    </View>
  );
}

/* ── Helper Components ──────────── */

function PerfCard({ label, value, total, suffix, color, bg, icon }: {
  label: string; value: number; total: number; suffix?: string; color: string; bg: string; icon: any;
}) {
  const pct = Math.round((value / total) * 100);
  return (
    <View style={[styles.perfCard, { backgroundColor: bg }]}>
      <MaterialCommunityIcons name={icon} size={24} color={color} />
      <Text style={[styles.perfValue, { color }]}>{value}{suffix ?? ''}</Text>
      <Text style={styles.perfLabel}>{label}</Text>
      {total !== value && (
        <Text style={styles.perfExtra}>of {total} ({pct}%)</Text>
      )}
    </View>
  );
}

function SummaryRow({ label, value, good }: { label: string; value: string; good: boolean }) {
  return (
    <View style={styles.summaryRow}>
      <Text style={styles.summaryLabel}>{label}</Text>
      <View style={styles.row}>
        <MaterialCommunityIcons
          name={good ? 'check-circle' : 'alert-circle'}
          size={16}
          color={good ? Colors.success : Colors.warning}
          style={{ marginRight: 4 }}
        />
        <Text style={[styles.summaryValue, { color: good ? Colors.success : Colors.warning }]}>{value}</Text>
      </View>
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
  tabLabel: { color: 'rgba(255,255,255,0.55)', fontSize: 12, fontWeight: '700' },
  tabLabelActive: { color: '#fff' },

  content: { padding: Spacing.xl, paddingBottom: Spacing.xxxl },
  section: { fontSize: 17, fontWeight: '800', color: Colors.textPrimary, marginTop: Spacing.md, marginBottom: Spacing.md },

  balanceRow: { flexDirection: 'row', gap: 10, marginTop: Spacing.md, marginBottom: Spacing.lg },
  balanceCard: { flex: 1, borderRadius: Radius.lg, padding: Spacing.lg, alignItems: 'center' },
  balanceValue: { fontSize: 28, fontWeight: '900' },
  balanceLabel: { fontSize: 11, fontWeight: '700', color: Colors.textSecondary, marginTop: 4, textTransform: 'uppercase' },

  row: { flexDirection: 'row', alignItems: 'center' },
  statusDot: { width: 10, height: 10, borderRadius: 5 },
  avatar: { width: 42, height: 42, borderRadius: 14, alignItems: 'center', justifyContent: 'center' },
  cardTitle: { fontSize: 15, fontWeight: '700', color: Colors.textPrimary },
  cardSub: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },
  cardDetail: { fontSize: 13, color: Colors.textMuted, marginTop: 4, fontStyle: 'italic' },

  emptyState: { alignItems: 'center', justifyContent: 'center', paddingVertical: Spacing.xxl },
  emptyText: { fontSize: 15, color: Colors.textSecondary, fontWeight: '600', marginTop: Spacing.md },

  perfGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginTop: Spacing.sm },
  perfCard: { width: '47%', borderRadius: Radius.lg, padding: Spacing.lg, alignItems: 'center', gap: 4 },
  perfValue: { fontSize: 28, fontWeight: '900' },
  perfLabel: { fontSize: 11, fontWeight: '700', color: Colors.textSecondary, textTransform: 'uppercase', textAlign: 'center' },
  perfExtra: { fontSize: 10, color: Colors.textMuted, fontWeight: '600' },

  summaryRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 8, borderBottomWidth: 1, borderBottomColor: Colors.border },
  summaryLabel: { fontSize: 14, color: Colors.textSecondary, fontWeight: '500' },
  summaryValue: { fontSize: 14, fontWeight: '800' },

  // Modals
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'flex-end' },
  modalContent: { backgroundColor: '#fff', borderTopLeftRadius: Radius.xl, borderTopRightRadius: Radius.xl, padding: Spacing.xl, maxHeight: '85%' },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: Spacing.lg },
  modalTitle: { fontSize: 20, fontWeight: '800', color: Colors.textPrimary },

  inputLabel: { fontSize: 13, fontWeight: '600', color: Colors.textSecondary, marginBottom: 6, marginTop: Spacing.sm },
  textInput: {
    backgroundColor: Colors.surfaceMuted,
    borderRadius: Radius.md,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 15,
    color: Colors.textPrimary,
    borderWidth: 1,
    borderColor: Colors.border,
    marginBottom: Spacing.sm,
  },

  chip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20, backgroundColor: Colors.surfaceMuted, borderWidth: 1, borderColor: Colors.border },
  chipActive: { backgroundColor: Colors.primary, borderColor: Colors.primary },
  chipText: { fontSize: 13, fontWeight: '600', color: Colors.textSecondary },
  chipTextActive: { color: '#fff' },
});
