import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export const CURRENCY_SYMBOLS: Record<string, string> = {
  USD: '$',
  GHS: 'GH₵',
  EUR: '€',
};

export const TIER_COLORS: Record<string, string> = {
  STANDARD: 'bg-gray-100 text-gray-700',
  GOLD: 'bg-amber-100 text-amber-800',
};

export const CHANNEL_ICONS: Record<string, string> = {
  EMAIL: '✉️',
  SMS: '📱',
};
