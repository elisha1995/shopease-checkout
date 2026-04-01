import type {
  Product, ShippingQuote, PaymentMethodInfo,
  CurrencyInfo, CheckoutRequest, CheckoutResult, Order, CartItem
} from './types';

const BASE_URL = '/api';

function getToken(): string | null {
  try {
    const stored = sessionStorage.getItem('shopease_auth');
    if (stored) return JSON.parse(stored).token;
  } catch { /* ignore */ }
  return null;
}

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${url}`, { headers, ...options });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.message || body.detail || `Request failed: ${res.status}`);
  }
  return res.json();
}

export const api = {
  // Public
  getProducts: () => fetchJson<Product[]>('/products'),
  getPaymentMethods: () => fetchJson<PaymentMethodInfo[]>('/payment/methods'),
  getCurrencies: () => fetchJson<CurrencyInfo[]>('/payment/currencies'),

  // Authenticated
  calculateShipping: (method: string | null, items: CartItem[]) =>
    fetchJson<ShippingQuote | ShippingQuote[]>('/shipping/calculate', {
      method: 'POST',
      body: JSON.stringify({ method, items }),
    }),

  checkout: (request: Omit<CheckoutRequest, 'userId'>) =>
    fetchJson<CheckoutResult>('/orders/checkout', {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  getOrder: (orderNumber: string) => fetchJson<Order>(`/orders/${orderNumber}`),
  getMyOrders: () => fetchJson<Order[]>('/orders'),
};
