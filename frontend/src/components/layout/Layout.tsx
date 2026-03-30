import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/lib/auth';
import { cn, TIER_COLORS } from '@/lib/utils';
import { Badge } from '@/components/ui';
import { ShoppingCart, CreditCard, Package, LogOut } from 'lucide-react';

const NAV = [
  { to: '/', label: 'Cart', icon: ShoppingCart },
  { to: '/checkout', label: 'Checkout', icon: CreditCard },
];

export function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-50 border-b border-border bg-card/80 backdrop-blur-md">
        <div className="mx-auto flex h-16 max-w-5xl items-center justify-between px-6">
          <Link to="/" className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <Package size={20} />
            </div>
            <span className="text-lg font-bold tracking-tight">ShopEase</span>
          </Link>

          <nav className="flex items-center gap-1">
            {NAV.map(({ to, label, icon: Icon }) => (
              <Link
                key={to}
                to={to}
                className={cn(
                  'flex items-center gap-1.5 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                  location.pathname === to
                    ? 'bg-primary/10 text-primary'
                    : 'text-muted-foreground hover:text-foreground'
                )}
              >
                <Icon size={16} />
                {label}
              </Link>
            ))}
          </nav>

          {user && (
            <div className="flex items-center gap-3">
              <div className="text-right hidden sm:block">
                <p className="text-sm font-medium">{user.fullName}</p>
                <Badge className={`text-[10px] ${TIER_COLORS[user.tier]}`}>{user.tier}</Badge>
              </div>
              <button
                onClick={logout}
                className="flex h-9 w-9 items-center justify-center rounded-lg text-muted-foreground hover:text-foreground hover:bg-secondary transition-colors cursor-pointer"
                title="Sign out"
              >
                <LogOut size={18} />
              </button>
            </div>
          )}
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-6 py-8">
        {children}
      </main>
    </div>
  );
}
