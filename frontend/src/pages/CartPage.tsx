import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Product, CartItem, ShippingQuote, ShippingMethod } from '@/lib/types';
import { TIER_COLORS } from '@/lib/utils';
import { Card, CardContent, Button, Badge, Select, Separator } from '@/components/ui';
import { Plus, Minus, Trash2, Truck, Tag, ArrowRight } from 'lucide-react';

function useShippingQuote(cart: CartItem[], shippingMethod: ShippingMethod) {
  const [fetchedQuote, setFetchedQuote] = useState<ShippingQuote | null>(null);

  useEffect(() => {
    if (cart.length === 0) return;
    let cancelled = false;
    api.calculateShipping(shippingMethod, cart)
      .then(quote => {
        if (!cancelled) {
          setFetchedQuote(Array.isArray(quote) ? quote[0] : quote);
        }
      })
      .catch(() => { /* ignore */ });
    return () => { cancelled = true; };
  }, [cart, shippingMethod]);

  return cart.length === 0 ? null : fetchedQuote;
}

function ProductCard({ product, inCart, onAdd, onUpdate, onRemove }: Readonly<{
  product: Product;
  inCart: CartItem | undefined;
  onAdd: () => void;
  onUpdate: (delta: number) => void;
  onRemove: () => void;
}>) {
  return (
    <Card className="overflow-hidden">
      <CardContent className="p-4">
        <div>
          <p className="font-semibold">{product.name}</p>
          <p className="text-sm text-muted-foreground mt-0.5">{product.description}</p>
          <p className="mt-2 text-lg font-bold text-primary">${product.price.toFixed(2)}</p>
        </div>
        <div className="mt-3">
          {inCart ? (
            <div className="flex items-center gap-2">
              <Button size="sm" variant="outline" onClick={() => onUpdate(-1)}>
                <Minus size={14} />
              </Button>
              <span className="w-8 text-center font-medium">{inCart.quantity}</span>
              <Button size="sm" variant="outline" onClick={() => onUpdate(1)}>
                <Plus size={14} />
              </Button>
              <Button size="sm" variant="ghost" onClick={onRemove} className="ml-auto text-destructive">
                <Trash2 size={14} />
              </Button>
            </div>
          ) : (
            <Button size="sm" onClick={onAdd} className="w-full">
              <Plus size={14} /> Add to Cart
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

function ShippingCalculator({ shippingMethod, shippingQuote, onMethodChange }: Readonly<{
  shippingMethod: ShippingMethod;
  shippingQuote: ShippingQuote | null;
  onMethodChange: (method: ShippingMethod) => void;
}>) {
  return (
    <div className="space-y-3 rounded-lg bg-secondary/50 p-3">
      <div className="flex items-center gap-1.5 text-sm font-medium">
        <Truck size={14} /> Shipping
      </div>
      <Select
        value={shippingMethod}
        onChange={e => onMethodChange(e.target.value as ShippingMethod)}
      >
        <option value="STANDARD">Standard (5-7 days)</option>
        <option value="EXPRESS">Express (1-2 days)</option>
      </Select>
      {shippingQuote && (
        <div className="space-y-1.5 text-sm">
          <div className="flex justify-between text-muted-foreground">
            <span>Base cost</span>
            <span>${shippingQuote.baseCost.toFixed(2)}</span>
          </div>
          {shippingQuote.appliedDiscounts.map(d => (
            <div key={d} className="flex items-center gap-1 text-success text-xs">
              <Tag size={12} /> {d}
            </div>
          ))}
          <div className="flex justify-between font-semibold">
            <span>Shipping cost</span>
            <span className={shippingQuote.finalCost === 0 ? 'text-success' : ''}>
              {shippingQuote.finalCost === 0 ? 'FREE' : `$${shippingQuote.finalCost.toFixed(2)}`}
            </span>
          </div>
        </div>
      )}
    </div>
  );
}

function CartSummary({ cart, cartTotal, shippingMethod, shippingQuote, onMethodChange, onCheckout }: Readonly<{
  cart: CartItem[];
  cartTotal: number;
  shippingMethod: ShippingMethod;
  shippingQuote: ShippingQuote | null;
  onMethodChange: (method: ShippingMethod) => void;
  onCheckout: () => void;
}>) {
  if (cart.length === 0) {
    return (
      <Card>
        <CardContent className="p-4 space-y-4">
          <p className="text-center text-muted-foreground py-6">Your cart is empty</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent className="p-4 space-y-4">
        {cart.map(item => (
          <div key={item.productId} className="flex justify-between text-sm">
            <span>{item.productName} x {item.quantity}</span>
            <span className="font-medium">${(item.price * item.quantity).toFixed(2)}</span>
          </div>
        ))}
        <Separator />
        <div className="flex justify-between text-sm font-medium">
          <span>Subtotal</span>
          <span>${cartTotal.toFixed(2)}</span>
        </div>

        <ShippingCalculator
          shippingMethod={shippingMethod}
          shippingQuote={shippingQuote}
          onMethodChange={onMethodChange}
        />

        <Separator />
        <div className="flex justify-between font-bold text-lg">
          <span>Total</span>
          <span>${(cartTotal + (shippingQuote?.finalCost ?? 0)).toFixed(2)}</span>
        </div>

        <Button size="lg" className="w-full" onClick={onCheckout}>
          Proceed to Checkout <ArrowRight size={16} />
        </Button>
      </CardContent>
    </Card>
  );
}

export default function CartPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [shippingMethod, setShippingMethod] = useState<ShippingMethod>('STANDARD');
  const [loading, setLoading] = useState(true);
  const shippingQuote = useShippingQuote(cart, shippingMethod);

  useEffect(() => {
    api.getProducts().then(p => { setProducts(p); setLoading(false); });
  }, []);

  const cartTotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  function addToCart(product: Product) {
    setCart(prev => {
      const existing = prev.find(i => i.productId === product.id);
      if (existing) {
        return prev.map(i => i.productId === product.id ? { ...i, quantity: i.quantity + 1 } : i);
      }
      return [...prev, { productId: product.id, productName: product.name, price: product.price, quantity: 1 }];
    });
  }

  function updateQuantity(productId: string, delta: number) {
    setCart(prev => prev
      .map(i => i.productId === productId ? { ...i, quantity: Math.max(0, i.quantity + delta) } : i)
      .filter(i => i.quantity > 0)
    );
  }

  function removeFromCart(productId: string) {
    setCart(prev => prev.filter(i => i.productId !== productId));
  }

  function handleCheckout() {
    sessionStorage.setItem('shopease_cart', JSON.stringify({
      items: cart, shippingMethod, shippingQuote,
    }));
    navigate('/checkout');
  }

  if (loading) return <div className="flex justify-center py-20 text-muted-foreground">Loading...</div>;

  return (
    <div className="space-y-8">
      <div className="grid gap-8 lg:grid-cols-3">
        {/* Product Grid */}
        <div className="lg:col-span-2 space-y-4">
          <h2 className="text-xl font-semibold">Products</h2>
          <div className="grid gap-4 sm:grid-cols-2">
            {products.map(product => (
              <ProductCard
                key={product.id}
                product={product}
                inCart={cart.find(i => i.productId === product.id)}
                onAdd={() => addToCart(product)}
                onUpdate={(delta) => updateQuantity(product.id, delta)}
                onRemove={() => removeFromCart(product.id)}
              />
            ))}
          </div>
        </div>

        {/* Cart Sidebar */}
        <div className="space-y-4">
          <h2 className="text-xl font-semibold">Cart Summary</h2>
          <CartSummary
            cart={cart}
            cartTotal={cartTotal}
            shippingMethod={shippingMethod}
            shippingQuote={shippingQuote}
            onMethodChange={setShippingMethod}
            onCheckout={handleCheckout}
          />

          {/* Membership Info */}
          {user && (
            <Card>
              <CardContent className="p-4">
                <p className="text-xs text-muted-foreground uppercase tracking-wider mb-2">Your Membership</p>
                <div className="flex items-center gap-2">
                  <Badge className={TIER_COLORS[user.tier]}>{user.tier}</Badge>
                  <span className="text-sm text-muted-foreground">{user.fullName}</span>
                </div>
                <p className="text-xs text-muted-foreground mt-2">
                  Sign out and log in as a different demo user to see tier-based shipping discounts.
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
