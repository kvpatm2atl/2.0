import React from 'react';
import { View, Text, StyleSheet, Pressable, Alert } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useNotifications, Notification } from '@/contexts/NotificationContext';
import { Colors, Radius, Shadows, Spacing } from '@/constants/theme';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import Animated, { FadeInUp, FadeOutUp } from 'react-native-reanimated';

export function FloatingAlerts() {
  const { notifications, markAsRead } = useNotifications();
  const insets = useSafeAreaInsets();
  
  const unread = notifications.filter(n => !n.isRead);

  if (unread.length === 0) return null;

  return (
    <View style={[styles.container, { top: insets.top + Spacing.sm }]}>
      {unread.slice(0, 2).map((notif) => (
        <Animated.View key={notif.id} entering={FadeInUp} exiting={FadeOutUp}>
          <Pressable onPress={() => {
            if (notif.type !== 'action_required') {
              markAsRead(notif.id);
            }
          }}>
            <View style={[styles.alertCard, notif.type === 'action_required' && styles.actionAlert]}>
              <View style={styles.iconBox}>
                <MaterialCommunityIcons 
                  name={notif.type === 'action_required' ? 'alert' : 'bell'} 
                  size={20} 
                  color={notif.type === 'action_required' ? Colors.danger : Colors.info} 
                />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={styles.title}>{notif.title}</Text>
                <Text style={styles.message} numberOfLines={2}>{notif.message}</Text>
                
                {notif.type === 'action_required' && (
                  <View style={styles.actionButtons}>
                    <Pressable 
                      style={[styles.btn, { backgroundColor: Colors.success }]}
                      onPress={() => {
                        Alert.alert('Confirmed', 'You have approved this request.');
                        markAsRead(notif.id);
                      }}
                    >
                      <Text style={styles.btnText}>Approve</Text>
                    </Pressable>
                    <Pressable 
                      style={[styles.btn, { backgroundColor: Colors.surfaceMuted, borderWidth: 1, borderColor: Colors.border }]}
                      onPress={() => {
                        Alert.alert('Denied', 'You have denied this request.');
                        markAsRead(notif.id);
                      }}
                    >
                      <Text style={[styles.btnText, { color: Colors.textPrimary }]}>Deny</Text>
                    </Pressable>
                  </View>
                )}
              </View>
              {notif.type !== 'action_required' && (
                <MaterialCommunityIcons name="close" size={16} color={Colors.textMuted} />
              )}
            </View>
          </Pressable>
        </Animated.View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    left: Spacing.xl,
    right: Spacing.xl,
    zIndex: 1000,
    gap: Spacing.sm,
  },
  alertCard: {
    backgroundColor: '#fff',
    borderRadius: Radius.lg,
    padding: Spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
    ...Shadows.raised,
    borderLeftWidth: 4,
    borderLeftColor: Colors.info,
  },
  actionAlert: {
    borderLeftColor: Colors.danger,
    backgroundColor: Colors.dangerBg,
  },
  iconBox: {
    width: 32,
    height: 32,
    borderRadius: Radius.sm,
    backgroundColor: Colors.surfaceMuted,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: 14,
    fontWeight: '800',
    color: Colors.textPrimary,
  },
  message: {
    fontSize: 12,
    color: Colors.textSecondary,
    marginTop: 2,
    lineHeight: 16,
  },
  actionButtons: {
    flexDirection: 'row',
    gap: Spacing.sm,
    marginTop: Spacing.md,
  },
  btn: {
    paddingHorizontal: 16,
    paddingVertical: 6,
    borderRadius: Radius.md,
  },
  btnText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '700',
  }
});
