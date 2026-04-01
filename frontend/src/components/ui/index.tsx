import React from 'react';
import { cn } from '@/lib/utils';

/* ─── Button ────────────────────────────────────────── */
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'default' | 'secondary' | 'outline' | 'ghost' | 'destructive';
  size?: 'sm' | 'md' | 'lg';
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'default', size = 'md', ...props }, ref) => {
    const variants = {
      default: 'bg-primary text-primary-foreground hover:bg-primary/90',
      secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
      outline: 'border border-border bg-transparent hover:bg-secondary',
      ghost: 'hover:bg-secondary',
      destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
    };
    const sizes = {
      sm: 'h-8 px-3 text-sm',
      md: 'h-10 px-4 text-sm',
      lg: 'h-12 px-6 text-base',
    };
    return (
      <button
        ref={ref}
        className={cn(
          'inline-flex items-center justify-center gap-2 rounded-md font-medium',
          'transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
          'disabled:pointer-events-none disabled:opacity-50 cursor-pointer',
          variants[variant], sizes[size], className
        )}
        {...props}
      />
    );
  }
);
Button.displayName = 'Button';

/* ─── Card ──────────────────────────────────────────── */
export function Card({ className, ...props }: Readonly<React.HTMLAttributes<HTMLDivElement>>) {
  return (
    <div className={cn('rounded-lg border border-border bg-card text-card-foreground shadow-sm', className)} {...props} />
  );
}
export function CardHeader({ className, ...props }: Readonly<React.HTMLAttributes<HTMLDivElement>>) {
  return <div className={cn('flex flex-col gap-1.5 p-6', className)} {...props} />;
}
export function CardTitle({ className, children, ...props }: Readonly<React.HTMLAttributes<HTMLHeadingElement>>) {
  return <h3 className={cn('text-lg font-semibold leading-none tracking-tight', className)} {...props}>{children}</h3>;
}
export function CardDescription({ className, ...props }: Readonly<React.HTMLAttributes<HTMLParagraphElement>>) {
  return <p className={cn('text-sm text-muted-foreground', className)} {...props} />;
}
export function CardContent({ className, ...props }: Readonly<React.HTMLAttributes<HTMLDivElement>>) {
  return <div className={cn('p-6 pt-0', className)} {...props} />;
}

/* ─── Badge ─────────────────────────────────────────── */
interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'default' | 'secondary' | 'outline' | 'success';
}
export function Badge({ className, variant = 'default', ...props }: Readonly<BadgeProps>) {
  const variants = {
    default: 'bg-primary text-primary-foreground',
    secondary: 'bg-secondary text-secondary-foreground',
    outline: 'border border-border text-foreground',
    success: 'bg-success text-success-foreground',
  };
  return (
    <span
      className={cn('inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium', variants[variant], className)}
      {...props}
    />
  );
}

/* ─── Select ────────────────────────────────────────── */
interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
}
export function Select({ className, label, children, ...props }: Readonly<SelectProps>) {
  return (
    <div className="flex flex-col gap-1.5">
      {label && <label className="text-sm font-medium text-foreground">{label}</label>}
      <select
        className={cn(
          'h-10 rounded-md border border-border bg-card px-3 text-sm',
          'focus:outline-none focus:ring-2 focus:ring-ring',
          className
        )}
        {...props}
      >
        {children}
      </select>
    </div>
  );
}

/* ─── Separator ─────────────────────────────────────── */
export function Separator({ className, ...props }: Readonly<React.HTMLAttributes<HTMLHRElement>>) {
  return <hr className={cn('border-border', className)} {...props} />;
}
