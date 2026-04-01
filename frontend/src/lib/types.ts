export type MembershipTier = 'STANDARD' | 'GOLD';
export type NotificationChannel = 'EMAIL' | 'SMS';
export type CurrencyCode = 'USD' | 'GHS' | 'EUR';
export type PaymentMethod = 'STRIPE' | 'PAYPAL' | 'CRYPTO';
export type ShippingMethod = 'STANDARD' | 'EXPRESS';

export interface Product {
  id: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  category: string;
}

export interface CartItem {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
}

export interface ShippingQuote {
  method: string;
  label: string;
  baseCost: number;
  finalCost: number;
  appliedDiscounts: string[];
}

export interface PaymentMethodInfo {
  key: PaymentMethod;
  displayName: string;
}

export interface CurrencyInfo {
  code: CurrencyCode;
  rateToUsd: number;
}

export interface OrderNotification {
  channel: string;
  success: boolean;
  message: string;
}

export interface Order {
  orderNumber: string;
  userId: string;
  items: { productName: string; price: number; quantity: number }[];
  subtotal: number;
  shippingCost: number;
  total: number;
  currency: CurrencyCode;
  paymentMethod: string;
  transactionId: string;
  shippingMethod: string;
  status: string;
  createdAt: string;
  notifications: OrderNotification[];
}

export interface CheckoutRequest {
  items: CartItem[];
  paymentMethod: PaymentMethod;
  shippingMethod: ShippingMethod;
  currency: CurrencyCode;
  notificationChannels: NotificationChannel[];
}

export interface CheckoutResult {
  success: boolean;
  orderNumber: string;
  message: string;
}
