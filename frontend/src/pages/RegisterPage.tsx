import { useState } from 'react';
import { useAuth } from '@/lib/auth';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, Button } from '@/components/ui';
import { UserPlus, Loader2, Eye, EyeOff } from 'lucide-react';

const TIER_OPTIONS = [
  { value: 'STANDARD', label: 'Standard', description: 'No shipping discount' },
  { value: 'GOLD', label: 'Gold', description: '20% off shipping' },
];

export default function RegisterPage({ onSwitchToLogin }: Readonly<{ onSwitchToLogin: () => void }>) {
  const { register } = useAuth();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [tier, setTier] = useState('STANDARD');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleRegister(e: React.SyntheticEvent<HTMLFormElement>) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await register({ fullName, email, password, phone: phone || undefined, tier });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  const inputClass =
    'w-full h-10 rounded-md border border-border bg-card px-3 text-sm focus:outline-none focus:ring-2 focus:ring-ring';

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center">
          <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-primary text-primary-foreground text-xl font-bold mb-4">
            S
          </div>
          <h1 className="text-2xl font-bold">ShopEase Checkout</h1>
          <p className="text-muted-foreground mt-1">Create your account</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Sign Up</CardTitle>
            <CardDescription>Fill in your details to get started</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleRegister} className="space-y-4">
              <div className="space-y-1.5">
                <label htmlFor="reg-fullname" className="text-sm font-medium">Full Name</label>
                <input
                  id="reg-fullname"
                  type="text"
                  value={fullName}
                  onChange={e => setFullName(e.target.value)}
                  className={inputClass}
                  placeholder="Kwame Asante"
                  required
                />
              </div>
              <div className="space-y-1.5">
                <label htmlFor="reg-email" className="text-sm font-medium">Email</label>
                <input
                  id="reg-email"
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className={inputClass}
                  placeholder="you@example.com"
                  required
                />
              </div>
              <div className="space-y-1.5">
                <label htmlFor="reg-password" className="text-sm font-medium">Password</label>
                <div className="relative">
                  <input
                    id="reg-password"
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    className={`${inputClass} pr-10`}
                    placeholder="••••••••"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(v => !v)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground cursor-pointer"
                  >
                    {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>
              <div className="space-y-1.5">
                <label htmlFor="reg-phone" className="text-sm font-medium">Phone <span className="text-muted-foreground font-normal">(optional)</span></label>
                <input
                  id="reg-phone"
                  type="tel"
                  value={phone}
                  onChange={e => setPhone(e.target.value)}
                  className={inputClass}
                  placeholder="+233 24 000 0000"
                />
              </div>
              <fieldset className="space-y-1.5 border-none p-0 m-0">
                <legend className="text-sm font-medium">Membership Tier</legend>
                <div className="flex gap-2">
                  {TIER_OPTIONS.map(option => (
                    <button
                      key={option.value}
                      type="button"
                      onClick={() => setTier(option.value)}
                      className={`flex-1 rounded-lg border-2 p-3 text-center transition-all cursor-pointer ${
                        tier === option.value
                          ? 'border-primary bg-primary/5'
                          : 'border-border hover:border-primary/40'
                      }`}
                    >
                      <p className="font-medium text-sm">{option.label}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">{option.description}</p>
                    </button>
                  ))}
                </div>
              </fieldset>
              {error && <p className="text-sm text-destructive">{error}</p>}

              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? <Loader2 size={16} className="animate-spin" /> : <UserPlus size={16} />}
                Create Account
              </Button>
            </form>
          </CardContent>
        </Card>

        <p className="text-center text-sm text-muted-foreground">
          Already have an account?{' '}
          <button onClick={onSwitchToLogin} className="text-primary hover:underline font-medium cursor-pointer">
            Sign In
          </button>
        </p>
      </div>
    </div>
  );
}
