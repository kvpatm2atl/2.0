import React from 'react';
import { View, Text, StyleSheet, Platform } from 'react-native';
import { Colors, Typography, Spacing } from '@/constants/theme';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

export default function Footer() {
  const insets = useSafeAreaInsets();
  
  return (
    <View style={[styles.container, { paddingBottom: Math.max(insets.bottom, Spacing.sm) }]}>
      <Text style={styles.text}>
        Created by Team NovaThink (PM SHRI KV Pattom Shift 2)
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: Colors.primaryDark,
    paddingTop: Spacing.sm,
    paddingHorizontal: Spacing.lg,
    alignItems: 'center',
    justifyContent: 'center',
    borderTopWidth: 1,
    borderTopColor: Colors.primaryLight,
    ...(Platform.OS === 'web' ? { position: 'fixed', bottom: 0, width: '100%', zIndex: 100 } : {}),
  },
  text: {
    color: Colors.textInverse,
    fontSize: 12,
    fontWeight: '500',
    textAlign: 'center',
    opacity: 0.9,
  }
});
