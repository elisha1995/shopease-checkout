import { useState } from 'react';
import { useAuth } from '@/lib/auth';
import { TIER_COLORS } from '@/lib/utils';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, Button, Badge } from '@/components/ui';
import { LogIn, Loader2 } from 'lucide-react';

const DEMO_ACCOUNTS = [
  { email: 'kwame@shopease.dev', name: 'Kwame Asante', tier: 'STANDARD' },
  { email: 'ama@shopease.dev', name: 'Ama Serwaa', tier: 'SILVER' },
  { email: 'kofi@shopease.dev', name: 'Kofi Mensah', tier: 'GOLD' },
  { email: 'abena@shopease.dev', name: 'Abena Osei', tier: 'PLATINUM' },
];

export default function LoginPage({ onSwitchToRegister }: { onSwitchToRegister?: () => void }) {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleLogin(e?: React.FormEvent, demoEmail?: string) {
    e?.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await login(demoEmail || email, demoEmail ? 'demo1234' : password);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center">
          <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-primary text-primary-foreground text-xl font-bold mb-4">
            S
          </div>
          <h1 className="text-2xl font-bold">ShopEase Checkout</h1>
          <p className="text-muted-foreground mt-1">Design Patterns Demo</p>
        </div>

        {/* Manual Login */}
        <Card>
          <CardHeader>
            <CardTitle>Sign In</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-sm font-medium">Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className="w-full h-10 rounded-md border border-border bg-card px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  placeholder="you@example.com"
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium">Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full h-10 rounded-md border border-border bg-card px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  placeholder="••••••••"
                />
              </div>
              {error && (
                <p className="text-sm text-destructive">{error}</p>
              )}
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? <Loader2 size={16} className="animate-spin" /> : <LogIn size={16} />}
                Sign In
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Demo Quick Login */}
        <Card>
          <CardHeader>
            <CardTitle>Demo Accounts</CardTitle>
            <CardDescription>
              Quick login to test different membership tiers (password: demo1234)
            </CardDescription>
          </CardHeader>
          <CardContent className="grid grid-cols-2 gap-2">
            {DEMO_ACCOUNTS.map(account => (
              <button
                key={account.email}
                onClick={() => handleLogin(undefined, account.email)}
                disabled={loading}
                className="flex flex-col items-start gap-1 rounded-lg border border-border p-3 text-left hover:bg-secondary/50 transition-colors cursor-pointer disabled:opacity-50"
              >
                <span className="text-sm font-medium">{account.name}</span>
                <Badge className={TIER_COLORS[account.tier]}>{account.tier}</Badge>
              </button>
            ))}
          </CardContent>
        </Card>

        {onSwitchToRegister && (
          <p className="text-center text-sm text-muted-foreground">
            Don't have an account?{' '}
            <button onClick={onSwitchToRegister} className="text-primary hover:underline font-medium cursor-pointer">
              Sign Up
            </button>
          </p>
        )}
      </div>
    </div>
  );
}
