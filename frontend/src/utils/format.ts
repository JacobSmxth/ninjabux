export function formatBux(value: number | null | undefined): string {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '0';
  }

  return Math.round(value).toLocaleString();
}

