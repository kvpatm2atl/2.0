// ResponsiveContainer — wraps page content with optional sidebar and max-width centering
// Automatically adapts layout for mobile, tablet, and desktop

import React from 'react';
import { View, ScrollView, Pressable, Text, StyleSheet, Platform } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useResponsive } from '@/hooks/useResponsive';
import { Colors, Radius, Spacing, Shadows } from '@/constants/theme';

interface SidebarItem {
  icon: keyof typeof MaterialCommunityIcons.glyphMap;
  label: string;
  route: string;
  badge?: string;
}

interface ResponsiveContainerProps {
  children: React.ReactNode;
  sidebarItems?: SidebarItem[];
  activeRoute?: string;
  /** If true, do not show sidebar even on desktop */
  noSidebar?: boolean;
}

export function ResponsiveContainer({
  children,
  sidebarItems,
  activeRoute,
  noSidebar = false,
}: ResponsiveContainerProps) {
  const layout = useResponsive();
  const router = useRouter();

  const showSidebar = layout.showSidebar && !noSidebar && sidebarItems && sidebarItems.length > 0;

  if (!showSidebar) {
    // Mobile — just render children directly
    return <>{children}</>;
  }

  // Tablet/Desktop — sidebar + main content
  return (
    <View style={styles.container}>
      {/* Sidebar */}
      <View style={[styles.sidebar, { width: layout.sidebarWidth }]}>
        <ScrollView contentContainerStyle={styles.sidebarContent} showsVerticalScrollIndicator={false}>
          <Text style={styles.sidebarTitle}>Navigation</Text>
          {sidebarItems!.map((item, idx) => {
            const isActive = activeRoute === item.route;
            return (
              <Pressable
                key={idx}
                style={[styles.sidebarItem, isActive && styles.sidebarItemActive]}
                onPress={() => router.push(item.route as any)}
              >
                <MaterialCommunityIcons
                  name={item.icon}
                  size={20}
                  color={isActive ? Colors.primary : Colors.textSecondary}
                />
                <Text style={[styles.sidebarLabel, isActive && styles.sidebarLabelActive]}>
                  {item.label}
                </Text>
                {item.badge && (
                  <View style={styles.sidebarBadge}>
                    <Text style={styles.sidebarBadgeText}>{item.badge}</Text>
                  </View>
                )}
              </Pressable>
            );
          })}
        </ScrollView>
      </View>

      {/* Main content */}
      <View style={[styles.mainContent, { maxWidth: layout.contentMaxWidth }]}>
        {children}
      </View>
    </View>
  );
}

/**
 * A responsive grid wrapper that auto-adjusts column count based on screen size.
 */
export function ResponsiveGrid({
  children,
  style,
}: {
  children: React.ReactNode;
  style?: any;
}) {
  const layout = useResponsive();

  return (
    <View
      style={[
        styles.grid,
        {
          gap: layout.isDesktop ? 16 : layout.isTablet ? 12 : 10,
        },
        style,
      ]}
    >
      {React.Children.map(children, (child) => {
        if (!React.isValidElement(child)) return child;
        return (
          <View style={{ width: layout.cardWidth as any }}>
            {child}
          </View>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'row',
    backgroundColor: Colors.background,
  },
  sidebar: {
    backgroundColor: '#fff',
    borderRightWidth: 1,
    borderRightColor: Colors.border,
    ...Shadows.card,
  },
  sidebarContent: {
    paddingTop: Spacing.xl,
    paddingBottom: Spacing.xxxl,
  },
  sidebarTitle: {
    fontSize: 11,
    fontWeight: '800',
    color: Colors.textMuted,
    textTransform: 'uppercase',
    letterSpacing: 1,
    paddingHorizontal: Spacing.lg,
    marginBottom: Spacing.md,
  },
  sidebarItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: Spacing.lg,
    gap: 12,
    marginHorizontal: Spacing.sm,
    borderRadius: Radius.md,
  },
  sidebarItemActive: {
    backgroundColor: Colors.surfaceTint,
  },
  sidebarLabel: {
    flex: 1,
    fontSize: 14,
    fontWeight: '600',
    color: Colors.textSecondary,
  },
  sidebarLabelActive: {
    color: Colors.primary,
    fontWeight: '700',
  },
  sidebarBadge: {
    backgroundColor: Colors.danger,
    borderRadius: 10,
    paddingHorizontal: 7,
    paddingVertical: 2,
  },
  sidebarBadgeText: {
    color: '#fff',
    fontSize: 10,
    fontWeight: '900',
  },
  mainContent: {
    flex: 1,
    alignSelf: 'center',
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
});
