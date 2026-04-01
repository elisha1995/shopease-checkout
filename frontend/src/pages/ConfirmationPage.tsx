import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '@/lib/api';
import type { Order, OrderNotification } from '@/lib/types';
import { CURRENCY_SYMBOLS, CHANNEL_ICONS } from '@/lib/utils';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, Button, Badge, Separator } from '@/components/ui';
import { CheckCircle, XCircle, ArrowLeft } from 'lucide-react';

function OrderDetailsCard({ order, symbol }: Readonly<{ order: Order; symbol: string }>) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Order Details</CardTitle>
        <CardDescription>
          Transaction: <span className="font-mono">{order.transactionId}</span>
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {order.items.map(item => (
          <div key={item.productName} className="flex justify-between text-sm">
            <span>{item.productName} × {item.quantity}</span>
            <span>${(item.price * item.quantity).toFixed(2)}</span>
          </div>
        ))}
        <Separator />
        <div className="flex justify-between text-sm">
          <span>Subtotal</span>
          <span>${order.subtotal.toFixed(2)}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span>Shipping ({order.shippingMethod})</span>
          <span className={order.shippingCost === 0 ? 'text-success font-medium' : ''}>
            {order.shippingCost === 0 ? 'FREE' : `$${order.shippingCost.toFixed(2)}`}
          </span>
        </div>
        <Separator />
        <div className="flex justify-between font-bold text-lg">
          <span>Total ({order.currency})</span>
          <span>{symbol}{order.total.toFixed(2)}</span>
        </div>
        <div className="flex gap-2 mt-2">
          <Badge variant="secondary">Paid via {order.paymentMethod}</Badge>
          <Badge variant="secondary">{order.currency}</Badge>
        </div>
      </CardContent>
    </Card>
  );
}

function NotificationStatusCard({ notifications }: Readonly<{ notifications: OrderNotification[] }>) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Notifications Sent</CardTitle>
        <CardDescription>
          Each channel was dispatched via the Observer pattern (with retry + fallback)
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {notifications.length === 0 ? (
          <p className="text-sm text-muted-foreground">No notification data available</p>
        ) : (
          notifications.map(notif => (
            <div key={notif.channel} className="flex items-center gap-3 rounded-lg border border-border p-3">
              <span className="text-xl">{CHANNEL_ICONS[notif.channel] || '📨'}</span>
              <div className="flex-1 min-w-0">
                <p className="font-medium text-sm">{notif.channel}</p>
                <p className="text-xs text-muted-foreground truncate">{notif.message}</p>
              </div>
              {notif.success ? (
                <CheckCircle size={18} className="text-success shrink-0" />
              ) : (
                <XCircle size={18} className="text-destructive shrink-0" />
              )}
            </div>
          ))
        )}
      </CardContent>
    </Card>
  );
}

const DESIGN_PATTERNS = [
  { pattern: 'Strategy', where: 'Payment, Shipping, Notifications', color: 'bg-blue-50 text-blue-700 border-blue-200' },
  { pattern: 'Factory', where: 'PaymentProcessorFactory, NotificationSenderFactory', color: 'bg-purple-50 text-purple-700 border-purple-200' },
  { pattern: 'Adapter', where: 'Stripe, PayPal, Crypto adapters', color: 'bg-green-50 text-green-700 border-green-200' },
  { pattern: 'Observer', where: 'OrderPlacedEvent → NotificationListener', color: 'bg-amber-50 text-amber-700 border-amber-200' },
];

export default function ConfirmationPage() {
  const { orderNumber } = useParams<{ orderNumber: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!orderNumber) return;
    api.getOrder(orderNumber)
      .then(setOrder)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load order'))
      .finally(() => setLoading(false));
  }, [orderNumber]);

  if (loading) return <div className="flex justify-center py-20 text-muted-foreground">Loading order...</div>;
  if (error || !order) return (
    <div className="text-center py-20">
      <p className="text-destructive">{error || 'Order not found'}</p>
      <Link to="/" className="text-primary underline mt-2 block">Back to Cart</Link>
    </div>
  );

  const symbol = CURRENCY_SYMBOLS[order.currency] || '$';

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Success Banner */}
      <div className="flex items-center gap-4 rounded-xl bg-success/10 p-6">
        <div className="flex h-14 w-14 items-center justify-center rounded-full bg-success text-success-foreground">
          <CheckCircle size={28} />
        </div>
        <div>
          <h1 className="text-xl font-bold text-success">Order Confirmed!</h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            Order <span className="font-mono font-medium text-foreground">{order.orderNumber}</span> has been placed
          </p>
        </div>
      </div>

      <OrderDetailsCard order={order} symbol={symbol} />
      <NotificationStatusCard notifications={order.notifications} />

      {/* Design Pattern Summary */}
      <Card>
        <CardHeader>
          <CardTitle>Design Patterns Used in This Order</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 sm:grid-cols-2">
            {DESIGN_PATTERNS.map(({ pattern, where, color }) => (
              <div key={pattern} className={`rounded-lg border p-3 ${color}`}>
                <p className="font-semibold text-sm">{pattern}</p>
                <p className="text-xs mt-0.5 opacity-80">{where}</p>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-center">
        <Link to="/">
          <Button variant="outline" size="lg">
            <ArrowLeft size={16} /> Place Another Order
          </Button>
        </Link>
      </div>
    </div>
  );
}