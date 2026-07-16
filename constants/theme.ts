// KVS EduShield AI theme tokens
// Powered by OnSpace.AI & Team NovaThink

export const Colors = {
  // Brand (KVS & PM SHRI)
  primary: '#092A5E', // KVS Navy Blue
  primaryDark: '#05183A',
  primaryLight: '#1C4A94',
  saffron: '#FF9933', // Indian Saffron
  saffronLight: '#FFB870',
  green: '#138808', // Indian Green

  // Surfaces
  background: '#F0F4F8', // Slightly bluish grey for a clean look
  surface: '#FFFFFF',
  surfaceMuted: '#E2E8F0',
  surfaceTint: '#D1DEEF',

  // Text
  textPrimary: '#0F172A',
  textSecondary: '#475569',
  textMuted: '#94A3B8',
  textInverse: '#FFFFFF',

  // Semantic
  success: '#10B981',
  successBg: '#D1FAE5',
  warning: '#F59E0B',
  warningBg: '#FEF3C7',
  danger: '#EF4444',
  dangerBg: '#FEE2E2',
  info: '#3B82F6',
  infoBg: '#DBEAFE',

  // Border
  border: '#CBD5E1',
  borderStrong: '#94A3B8',

  // Misc
  overlay: 'rgba(9, 42, 94, 0.7)', // Dark blue overlay
};

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
  xxxl: 32,
};

export const Radius = {
  sm: 6,
  md: 10,
  lg: 16,
  xl: 24,
  pill: 999,
};

export const Typography = {
  pageTitle: { fontSize: 24, fontWeight: '800' as const, color: Colors.primary },
  sectionTitle: { fontSize: 18, fontWeight: '700' as const, color: Colors.textPrimary },
  cardTitle: { fontSize: 16, fontWeight: '600' as const, color: Colors.textPrimary },
  body: { fontSize: 15, fontWeight: '400' as const, color: Colors.textSecondary },
  bodyStrong: { fontSize: 15, fontWeight: '600' as const, color: Colors.textPrimary },
  caption: { fontSize: 13, fontWeight: '500' as const, color: Colors.textSecondary },
  micro: { fontSize: 11, fontWeight: '600' as const, color: Colors.textMuted },
  button: { fontSize: 16, fontWeight: '700' as const, color: Colors.textInverse },
};

export const Shadows = {
  card: {
    shadowColor: Colors.primary,
    shadowOpacity: 0.08,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 4 },
    elevation: 3,
  },
  raised: {
    shadowColor: Colors.primary,
    shadowOpacity: 0.15,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 8 },
    elevation: 6,
  },
};
