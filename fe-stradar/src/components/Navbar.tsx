import { useNavigate } from 'react-router-dom';
import { useAppState } from '@/context/AppContext';
import { toast } from 'sonner';
import { LogOut, Radar } from 'lucide-react';

interface NavbarProps {
  variant?: 'home' | 'platform' | 'organization';
}

export default function Navbar({ variant = 'home' }: NavbarProps) {
  const navigate = useNavigate();
  const { saas, organization, resetSaas, resetOrganization } = useAppState();

  const signOut = () => {
    if (variant === 'platform') { resetSaas(); }
    else { resetOrganization(); }
    navigate('/');
    toast.success('Signed out');
  };

  if (variant === 'organization') {
    return (
      <header className="flex h-14 shrink-0 items-center justify-between border-b border-border bg-background/85 px-4 backdrop-blur-xl md:px-5">
        <div className="flex min-w-0 items-center gap-3">
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-foreground">
              {organization.orgName || 'Organization'}
            </p>
            <p className="text-[9px] text-muted-foreground">Strategy workspace</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {organization.email && (
            <span className="hidden text-[12px] text-muted-foreground md:inline">{organization.email}</span>
          )}
          <button
            onClick={signOut}
            className="inline-flex items-center gap-1.5 rounded-md border border-border bg-background px-3 py-1.5 text-[12px] font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
          >
            <LogOut className="size-3.5" /> Sign out
          </button>
        </div>
      </header>
    );
  }

  return (
    <nav className="flex h-14 shrink-0 items-center justify-between border-b border-border bg-background/85 px-6 backdrop-blur-xl">
      <div
        className="flex cursor-pointer items-center gap-2.5 font-bold tracking-tight"
        onClick={() => navigate('/')}
      >
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-lg shadow-primary/10">
          <Radar className="size-4" />
        </div>
        <span className="text-sm">STRADAR</span>
      </div>

      <div className="flex items-center gap-3 text-sm">
        {variant === 'home' && (
          <span className="text-[12px] text-muted-foreground">Strategic Radar Platform</span>
        )}
        {variant === 'platform' && (
          <>
            <span className="rounded border border-primary/20 bg-primary/8 px-3 py-1 text-[11px] font-semibold tracking-wide text-primary">
              PLATFORM
            </span>
            {saas.email && (
              <span className="text-[12px] text-muted-foreground">{saas.email}</span>
            )}
            <button
              onClick={signOut}
              className="inline-flex items-center gap-1.5 rounded-lg border border-border bg-background px-3 py-1.5 text-[12px] font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
            >
              <LogOut className="size-3.5" /> Sign Out
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
