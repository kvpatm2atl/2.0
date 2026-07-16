// Responsive layout hook — auto-adapts UI for mobile, tablet, and desktop
// Uses useWindowDimensions for real-time width tracking

import { useWindowDimensions, Platform } from 'react-native';

export type DeviceType = 'mobile' | 'tablet' | 'desktop';

export interface ResponsiveLayout {
  /** Current device type based on screen width */
  deviceType: DeviceType;
  /** Screen width in px */
  width: number;
  /** Screen height in px */
  height: number;
  /** True if width >= 768 (tablet+) */
  isTablet: boolean;
  /** True if width >= 1024 (desktop) */
  isDesktop: boolean;
  /** True if width < 768 (phone) */
  isMobile: boolean;
  /** True if running in a web browser */
  isWeb: boolean;
  /** Number of columns for grid layouts */
  gridCols: number;
  /** Max content width for centering on large screens */
  contentMaxWidth: number;
  /** Sidebar width when visible */
  sidebarWidth: number;
  /** Whether to show a sidebar navigation */
  showSidebar: boolean;
  /** Horizontal padding scaled to device */
  horizontalPadding: number;
  /** Card width for grid items (e.g. op cards on admin dashboard) */
  cardWidth: string;
}

const BREAKPOINTS = {
  tablet: 768,
  desktop: 1024,
  wideDesktop: 1440,
};

export function useResponsive(): ResponsiveLayout {
  const { width, height } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';

  const isMobile = width < BREAKPOINTS.tablet;
  const isTablet = width >= BREAKPOINTS.tablet && width < BREAKPOINTS.desktop;
  const isDesktop = width >= BREAKPOINTS.desktop;

  const deviceType: DeviceType = isDesktop ? 'desktop' : isTablet ? 'tablet' : 'mobile';

  // Grid columns: mobile=3, tablet=4, desktop=6
  const gridCols = isDesktop ? 6 : isTablet ? 4 : 3;

  // Max content width — prevents content stretching on ultra-wide screens
  const contentMaxWidth = isDesktop ? 1200 : isTablet ? 900 : width;

  // Sidebar
  const sidebarWidth = isDesktop ? 260 : isTablet ? 220 : 0;
  const showSidebar = width >= BREAKPOINTS.tablet;

  // Padding
  const horizontalPadding = isDesktop ? 32 : isTablet ? 24 : 20;

  // Card width percentages
  const cardWidth = isDesktop ? '15%' : isTablet ? '23%' : '31%';

  return {
    deviceType,
    width,
    height,
    isTablet,
    isDesktop,
    isMobile,
    isWeb,
    gridCols,
    contentMaxWidth,
    sidebarWidth,
    showSidebar,
    horizontalPadding,
    cardWidth,
  };
}

/** Utility to pick a value based on device type */
export function responsive<T>(layout: ResponsiveLayout, mobile: T, tablet: T, desktop: T): T {
  if (layout.isDesktop) return desktop;
  if (layout.isTablet) return tablet;
  return mobile;
}
