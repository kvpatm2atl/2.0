import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Pressable, Switch } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { Colors, Radius, Spacing } from '@/constants/theme';
import { Card } from '@/components/ui/Card';

export default function SettingsScreen() {
  const router = useRouter();
  
  // Settings state
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [darkModeEnabled, setDarkModeEnabled] = useState(false);
  const [strictPasswordPolicy, setStrictPasswordPolicy] = useState(true);
  const [locationTracking, setLocationTracking] = useState(true);

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} style={styles.backBtn}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={Colors.textPrimary} />
        </Pressable>
        <Text style={styles.title}>Settings</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView contentContainerStyle={styles.content}>
        
        {/* User Management */}
        <Text style={styles.sectionTitle}>User Management</Text>
        <Card padded={false} style={styles.cardGroup}>
          <Pressable style={styles.settingRow} onPress={() => router.push('/(admin)/teachers' as any)}>
            <View style={[styles.iconBg, { backgroundColor: Colors.infoBg }]}>
              <MaterialCommunityIcons name="account-tie" size={20} color={Colors.info} />
            </View>
            <Text style={styles.settingLabel}>Manage Teachers</Text>
            <MaterialCommunityIcons name="chevron-right" size={20} color={Colors.textMuted} />
          </Pressable>
          <View style={styles.divider} />
          <Pressable style={styles.settingRow} onPress={() => router.push('/(admin)/students' as any)}>
            <View style={[styles.iconBg, { backgroundColor: Colors.surfaceMuted }]}>
              <MaterialCommunityIcons name="account-group" size={20} color={Colors.primary} />
            </View>
            <Text style={styles.settingLabel}>Manage Students</Text>
            <MaterialCommunityIcons name="chevron-right" size={20} color={Colors.textMuted} />
          </Pressable>
          <View style={styles.divider} />
          <Pressable style={styles.settingRow} onPress={() => router.push('/(admin)/fleet' as any)}>
            <View style={[styles.iconBg, { backgroundColor: Colors.warningBg }]}>
              <MaterialCommunityIcons name="bus-school" size={20} color={Colors.warning} />
            </View>
            <Text style={styles.settingLabel}>Manage Transport Staff</Text>
            <MaterialCommunityIcons name="chevron-right" size={20} color={Colors.textMuted} />
          </Pressable>
        </Card>

        {/* App Configuration */}
        <Text style={styles.sectionTitle}>App Configuration</Text>
        <Card padded={false} style={styles.cardGroup}>
          <View style={styles.settingRow}>
            <View style={[styles.iconBg, { backgroundColor: Colors.successBg }]}>
              <MaterialCommunityIcons name="bell-ring" size={20} color={Colors.success} />
            </View>
            <Text style={styles.settingLabel}>Push Notifications</Text>
            <Switch
              value={notificationsEnabled}
              onValueChange={setNotificationsEnabled}
              trackColor={{ false: '#d1d5db', true: Colors.success }}
            />
          </View>
          <View style={styles.divider} />
          <View style={styles.settingRow}>
            <View style={[styles.iconBg, { backgroundColor: '#F0ECFD' }]}>
              <MaterialCommunityIcons name="theme-light-dark" size={20} color="#6E55C2" />
            </View>
            <Text style={styles.settingLabel}>Dark Mode (Beta)</Text>
            <Switch
              value={darkModeEnabled}
              onValueChange={setDarkModeEnabled}
              trackColor={{ false: '#d1d5db', true: '#6E55C2' }}
            />
          </View>
          <View style={styles.divider} />
          <View style={styles.settingRow}>
            <View style={[styles.iconBg, { backgroundColor: '#FFE9DC' }]}>
              <MaterialCommunityIcons name="map-marker-radius" size={20} color={Colors.saffron} />
            </View>
            <Text style={styles.settingLabel}>Bus Location Tracking</Text>
            <Switch
              value={locationTracking}
              onValueChange={setLocationTracking}
              trackColor={{ false: '#d1d5db', true: Colors.saffron }}
            />
          </View>
        </Card>

        {/* Security Settings */}
        <Text style={styles.sectionTitle}>Security Settings</Text>
        <Card padded={false} style={styles.cardGroup}>
          <View style={styles.settingRow}>
            <View style={[styles.iconBg, { backgroundColor: Colors.dangerBg }]}>
              <MaterialCommunityIcons name="shield-lock" size={20} color={Colors.danger} />
            </View>
            <Text style={styles.settingLabel}>Strict Password Policy</Text>
            <Switch
              value={strictPasswordPolicy}
              onValueChange={setStrictPasswordPolicy}
              trackColor={{ false: '#d1d5db', true: Colors.danger }}
            />
          </View>
          <View style={styles.divider} />
          <Pressable style={styles.settingRow}>
            <View style={[styles.iconBg, { backgroundColor: Colors.surfaceTint }]}>
              <MaterialCommunityIcons name="history" size={20} color={Colors.primary} />
            </View>
            <Text style={styles.settingLabel}>View Audit Logs</Text>
            <MaterialCommunityIcons name="chevron-right" size={20} color={Colors.textMuted} />
          </Pressable>
        </Card>

        {/* Danger Zone */}
        <Pressable style={styles.logoutBtn} onPress={() => {}}>
          <MaterialCommunityIcons name="logout" size={20} color={Colors.danger} />
          <Text style={styles.logoutText}>Log Out</Text>
        </Pressable>

      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md, backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: Colors.border },
  backBtn: { padding: Spacing.xs },
  title: { fontSize: 18, fontWeight: '700', color: Colors.textPrimary },
  content: { padding: Spacing.lg, paddingBottom: Spacing.xxxl },
  sectionTitle: { fontSize: 14, fontWeight: '700', color: Colors.textSecondary, textTransform: 'uppercase', marginBottom: Spacing.sm, marginTop: Spacing.md, marginLeft: Spacing.sm },
  cardGroup: { marginBottom: Spacing.lg, overflow: 'hidden' },
  settingRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.md },
  iconBg: { width: 36, height: 36, borderRadius: Radius.sm, alignItems: 'center', justifyContent: 'center', marginRight: Spacing.md },
  settingLabel: { flex: 1, fontSize: 16, fontWeight: '500', color: Colors.textPrimary },
  divider: { height: 1, backgroundColor: Colors.border, marginLeft: 68 }, // align with text
  logoutBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', padding: Spacing.lg, marginTop: Spacing.xl, backgroundColor: '#fff', borderRadius: Radius.lg, borderWidth: 1, borderColor: Colors.dangerBg },
  logoutText: { color: Colors.danger, fontWeight: '700', fontSize: 16, marginLeft: 8 },
});
