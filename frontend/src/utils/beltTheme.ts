export interface BeltTheme {
  primary: string;
  secondary: string;
  textColor: string;
  accent: string;
  accentText: string;
}

const BELT_THEMES: Record<string, BeltTheme> = {
  white: {
    primary: '#e5e7eb',
    secondary: '#0f172a',
    textColor: '#0b1324',
    accent: '#94a3b8',
    accentText: '#0b1324',
  },
  yellow: {
    primary: '#fff44f',
    secondary: '#0b1021',
    textColor: '#1f1400',
    accent: '#ffd100',
    accentText: '#140c00',
  },
  orange: {
    primary: '#ffb079',
    secondary: '#0f172a',
    textColor: '#3c1d0a',
    accent: '#ff7a3d',
    accentText: '#1a0d05',
  },
  green: {
    primary: '#8ee0c3',
    secondary: '#0b1720',
    textColor: '#0f3d2e',
    accent: '#1ea672',
    accentText: '#0b1720',
  },
  blue: {
    primary: '#8ec5ff',
    secondary: '#0b1226',
    textColor: '#0b2b5f',
    accent: '#1f6feb',
    accentText: '#e5efff',
  },
  purple: {
    primary: '#a96bff',
    secondary: '#120828',
    textColor: '#1b0f33',
    accent: '#7c3aed',
    accentText: '#f8f6ff',
  },
  red: {
    primary: '#ff6b6b',
    secondary: '#1a0b0b',
    textColor: '#2a0808',
    accent: '#ff1f1f',
    accentText: '#fff7f7',
  },
  brown: {
    primary: '#caa074',
    secondary: '#120a05',
    textColor: '#2d1a0c',
    accent: '#9c6938',
    accentText: '#fff7ec',
  },
  black: {
    primary: '#0b0f19',
    secondary: '#05060a',
    textColor: '#e5e7eb',
    accent: '#1f2937',
    accentText: '#e5e7eb',
  },
};

export const defaultBeltTheme: BeltTheme = BELT_THEMES.red;

export const getBeltTheme = (belt: string): BeltTheme => {
  const key = belt.toLowerCase();
  return BELT_THEMES[key] || defaultBeltTheme;
};
