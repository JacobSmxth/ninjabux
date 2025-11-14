export interface BeltTheme {
  primary: string;
  secondary: string;
  textColor: string;
  accent: string;
  accentText: string;
}

const BELT_THEMES: Record<string, BeltTheme> = {
  white: {
    primary: '#d1d5db',
    secondary: '#111827',
    textColor: '#1f2937',
    accent: '#4b5563',
    accentText: '#ffffff',
  },
  yellow: {
    primary: '#fce7a2',
    secondary: '#111827',
    textColor: '#78350f',
    accent: '#b45309',
    accentText: '#ffffff',
  },
  orange: {
    primary: '#fdba74',
    secondary: '#111827',
    textColor: '#9a3412',
    accent: '#ea580c',
    accentText: '#ffffff',
  },
  green: {
    primary: '#86efac',
    secondary: '#0f172a',
    textColor: '#065f46',
    accent: '#059669',
    accentText: '#ffffff',
  },
  blue: {
    primary: '#93c5fd',
    secondary: '#0f172a',
    textColor: '#1d4ed8',
    accent: '#2563eb',
    accentText: '#ffffff',
  },
  purple: {
    primary: '#d8b4fe',
    secondary: '#2e1065',
    textColor: '#6b21a8',
    accent: '#9333ea',
    accentText: '#ffffff',
  },
  red: {
    primary: '#fca5a5',
    secondary: '#111827',
    textColor: '#b91c1c',
    accent: '#dc2626',
    accentText: '#ffffff',
  },
  brown: {
    primary: '#d6b48c',
    secondary: '#1f2937',
    textColor: '#78350f',
    accent: '#92400e',
    accentText: '#ffffff',
  },
  black: {
    primary: '#1f2937',
    secondary: '#ffffff',
    textColor: '#f3f4f6',
    accent: '#fbbf24',
    accentText: '#1f2937',
  },
};

export const defaultBeltTheme: BeltTheme = BELT_THEMES.red;

export const getBeltTheme = (belt: string): BeltTheme => {
  const key = belt.toLowerCase();
  return BELT_THEMES[key] || defaultBeltTheme;
};
