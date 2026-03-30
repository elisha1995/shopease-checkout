import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '@/lib/api';
import type { PaymentMethodInfo, CurrencyInfo, CartItem, ShippingQuote, PaymentMethod, CurrencyCode } from '@/lib/types';
import { CURRENCY_SYMBOLS } from '@/lib/utils';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, Button, Badge, Separator } from '@/components/ui';
import { CreditCard, Wallet, Bitcoin, Loader2, CheckCircle, XCircle } from 'lucide-react';

const PAYMENT_ICONS: Record<string, typeof CreditCard> = {
  STRIPE: CreditCard,
  PAYPAL: Wallet,
  CRYPTO: Bitcoin,
};

interface CartState {
  items: CartItem[];
  shippingMethod: string;
  shippingQuote: ShippingQuote;
}

export default function CheckoutPage() {
  const navigate = useNavigate();
  const [paymentMethods, setPaymentMethods] = useState<PaymentMethodInfo[]>([]);
  const [currencies, setCurrencies] = useState<CurrencyInfo[]>([]);
  const [selectedPayment, setSelectedPayment] = useState<PaymentMethod>('STRIPE');
  const [selectedCurrency, setSelectedCurrency] = useState<CurrencyCode>('USD');
  const [cartState, setCartState] = useState<CartState | null>(null);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const saved = sessionStorage.getItem('shopease_cart');
    if (!saved) { navigate('/'); return; }
    setCartState(JSON.parse(saved));
    Promise.all([api.getPaymentMethods(), api.getCurrencies()]).then(([m, c]) => {
      setPaymentMethods(m);
      setCurrencies(c);
    });
  }, [navigate]);

  if (!cartState) return null;

  const subtotal = cartState.items.reduce((sum, i) => sum + i.price * i.quantity, 0);
  const shipping = cartState.shippingQuote?.finalCost ?? 0;
  const totalUsd = subtotal + shipping;
  const rate = currencies.find(c => c.code === selectedCurrency)?.rateToUsd ?? 1;
  const totalConverted = Math.round(totalUsd * rate * 100) / 100;
  const symbol = CURRENCY_SYMBOLS[selectedCurrency] || '$';

  async function handleCheckout() {
    setProcessing(true);
    setError(null);
    try {
      const result = await api.checkout({
        items: cartState!.items,
        paymentMethod: selectedPayment,
        shippingMethod: cartState!.shippingMethod as 'STANDARD' | 'EXPRESS',
        currency: selectedCurrency,
      });
      if (result.success) {
        sessionStorage.removeItem('shopease_cart');
        navigate(`/confirmation/${result.orderId}`);
      } else {
        setError(result.message);
      }
    } catch (e: any) {
      setError(e.message || 'Checkout failed');
    } finally {
      setProcessing(false);
    }
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Checkout</h1>
        <p className="text-muted-foreground mt-1">Select your payment method and currency</p>
      </div>

      {/* Payment Method Selection */}
      <Card>
        <CardHeader>
          <CardTitle>Payment Method</CardTitle>
          <CardDescription>Each option uses a different payment adapter (Strategy + Adapter pattern)</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {paymentMethods.map(method => {
            const Icon = PAYMENT_ICONS[method.key] || CreditCard;
            return (
              <button
                key={method.key}
                onClick={() => setSelectedPayment(method.key)}
                className={`w-full flex items-center gap-3 rounded-lg border-2 p-4 text-left transition-all cursor-pointer ${
                  selectedPayment === method.key ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/40'
                }`}
              >
                <div className={`flex h-10 w-10 items-center justify-center rounded-lg ${
                  selectedPayment === method.key ? 'bg-primary text-primary-foreground' : 'bg-secondary'
                }`}>
                  <Icon size={20} />
                </div>
                <div>
                  <p className="font-medium">{method.displayName}</p>
                  <p className="text-xs text-muted-foreground">Provider: {method.key}</p>
                </div>
                {selectedPayment === method.key && <CheckCircle size={20} className="ml-auto text-primary" />}
              </button>
            );
          })}
        </CardContent>
      </Card>

      {/* Currency Selection */}
      <Card>
        <CardHeader>
          <CardTitle>Currency</CardTitle>
          <CardDescription>Multi-currency support (extra "What-If" scenario)</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-3">
            {currencies.map(currency => (
              <button
                key={currency.code}
                onClick={() => setSelectedCurrency(currency.code)}
                className={`flex-1 rounded-lg border-2 p-3 text-center transition-all cursor-pointer ${
                  selectedCurrency === currency.code ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/40'
                }`}
              >
                <p className="text-2xl font-bold">{CURRENCY_SYMBOLS[currency.code]}</p>
                <p className="text-sm text-muted-foreground">{currency.code}</p>
                {currency.code !== 'USD' && <p className="text-xs text-muted-foreground mt-1">1 USD = {currency.rateToUsd}</p>}
              </button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Order Summary */}
      <Card>
        <CardHeader><CardTitle>Order Summary</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          {cartState.items.map(item => (
            <div key={item.productId} className="flex justify-between text-sm">
              <span>{item.productName} x {item.quantity}</span>
              <span>${(item.price * item.quantity).toFixed(2)}</span>
            </div>
          ))}
          <Separator />
          <div className="flex justify-between text-sm">
            <span>Subtotal</span><span>${subtotal.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span>Shipping ({cartState.shippingMethod})</span>
            <span className={shipping === 0 ? 'text-success font-medium' : ''}>
              {shipping === 0 ? 'FREE' : `$${shipping.toFixed(2)}`}
            </span>
          </div>
          <Separator />
          <div className="flex justify-between font-bold text-lg">
            <span>Total ({selectedCurrency})</span>
            <span>{symbol}{(selectedCurrency === 'USD' ? totalUsd : totalConverted).toFixed(2)}</span>
          </div>
          {error && (
            <div className="flex items-center gap-2 rounded-lg bg-destructive/10 p-3 text-sm text-destructive">
              <XCircle size={16} /> {error}
            </div>
          )}
          <Button size="lg" className="w-full mt-2" onClick={handleCheckout} disabled={processing}>
            {processing
              ? <><Loader2 size={16} className="animate-spin" /> Processing...</>
              : <>Pay {symbol}{(selectedCurrency === 'USD' ? totalUsd : totalConverted).toFixed(2)} with {selectedPayment}</>
            }
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
