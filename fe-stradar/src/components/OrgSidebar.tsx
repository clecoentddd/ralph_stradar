import { useEffect, useState, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Users, Radar as RadarIcon, Target, LayoutDashboard,
  ChevronDown, ChevronLeft, ChevronsUpDown, Settings, LogOut,
} from 'lucide-react';
import { useAppState } from '@/context/AppContext';
import { useAdminApi } from '@/services/api';
import { toast } from 'sonner';
import type { Team } from '@/context/AppContext';

interface OrgSidebarProps {
  activeTeamId?: string;
  activePage?: 'radar' | 'strategy' | 'overview';
}

function NavItem({
  icon,
  label,
  isActive,
  isCollapsed,
  onClick,
}: {
  icon: ReactNode;
  label: string;
  isActive: boolean;
  isCollapsed: boolean;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      title={isCollapsed ? label : undefined}
      className={`flex h-9 w-full items-center gap-3 rounded-md px-2.5 text-xs transition-colors ${
        isCollapsed ? 'justify-center' : ''
      } ${
        isActive
          ? 'bg-sidebar-accent text-sidebar-accent-foreground'
          : 'text-muted-foreground hover:bg-sidebar-accent/60 hover:text-foreground'
      }`}
    >
      <span className={`size-4 shrink-0 ${isActive ? 'text-primary' : ''}`}>{icon}</span>
      {!isCollapsed && <span>{label}</span>}
    </button>
  );
}

export default function OrgSidebar({ activeTeamId, activePage }: OrgSidebarProps) {
  const navigate = useNavigate();
  const { organization, teams, setTeams, resetOrganization } = useAppState();
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [expandedLevels, setExpandedLevels] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (teams.length === 0 && organization.orgId) {
      useAdminApi.getTeamListByOrg(organization.orgId, organization.userId!)
        .then(r => setTeams(r.teams || []))
        .catch(() => {});
    }
  }, [organization.orgId]);

  useEffect(() => {
    if (activeTeamId && teams.length > 0) {
      const team = teams.find(t => t.teamId === activeTeamId);
      if (team) {
        setExpandedLevels(prev => new Set(prev).add(team.level ?? 1));
      }
    }
  }, [activeTeamId, teams]);

  const grouped = teams.reduce<Record<number, Team[]>>((acc, t) => {
    const lvl = t.level ?? 1;
    if (!acc[lvl]) acc[lvl] = [];
    acc[lvl].push(t);
    return acc;
  }, {});

  const levels = Object.entries(grouped)
    .map(([lvl, t]) => ({ level: Number(lvl), teams: t }))
    .sort((a, b) => a.level - b.level);

  const toggleLevel = (lvl: number) => {
    setExpandedLevels(prev => {
      const s = new Set(prev);
      s.has(lvl) ? s.delete(lvl) : s.add(lvl);
      return s;
    });
  };

  const goTo = (page: 'radar' | 'strategy', team: Team) => {
    const params = `teamId=${team.teamId}&organizationId=${team.organizationId}&teamName=${encodeURIComponent(team.name)}`;
    navigate(`/dashboard/${page}?${params}`);
  };

  const orgInitials = organization.orgName
    ? organization.orgName.split(' ').filter(Boolean).map(w => w[0]).join('').slice(0, 2).toUpperCase()
    : 'OR';

  const signOut = () => {
    resetOrganization();
    navigate('/');
    toast.success('Signed out');
  };

  return (
    <aside
      className={`flex shrink-0 flex-col border-r border-sidebar-border bg-sidebar transition-[width] duration-200 overflow-hidden ${
        isCollapsed ? 'w-14' : 'w-60'
      }`}
    >
      {/* Brand header */}
      <div className="flex h-14 shrink-0 items-center gap-3 border-b border-sidebar-border px-3">
        <div className="relative grid size-9 shrink-0 place-items-center rounded-md bg-primary text-primary-foreground shadow-lg shadow-primary/10">
          <span className="absolute size-4 rounded-full border border-current opacity-70" />
          <span className="absolute h-px w-5 rotate-45 bg-current" />
          <span className="size-1.5 rounded-full bg-current" />
        </div>
        {!isCollapsed && (
          <div className="min-w-0">
            <p className="text-sm font-semibold tracking-tight">STRADAR</p>
            <p className="text-[9px] tracking-[.14em] text-muted-foreground">STRATEGIC THINKING</p>
          </div>
        )}
      </div>

      {/* Scrollable content */}
      <div className="flex flex-1 flex-col overflow-y-auto overflow-x-hidden p-2 pt-3">

        {/* Org selector */}
        <button
          onClick={() => navigate('/dashboard/organization')}
          className={`mb-5 flex items-center gap-2 rounded-lg border border-sidebar-border bg-sidebar-accent text-left transition-colors hover:bg-sidebar-accent/80 ${
            isCollapsed ? 'justify-center px-1.5 py-2' : 'px-2.5 py-2'
          }`}
        >
          <span className="grid size-7 shrink-0 place-items-center rounded bg-primary/15 text-[10px] font-bold text-primary">
            {orgInitials}
          </span>
          {!isCollapsed && (
            <>
              <span className="min-w-0 flex-1">
                <span className="block truncate text-xs font-medium">{organization.orgName || 'Organization'}</span>
                <span className="block text-[9px] capitalize text-muted-foreground">{organization.role || 'member'}</span>
              </span>
              <ChevronsUpDown className="size-3 shrink-0 text-muted-foreground" />
            </>
          )}
        </button>

        {/* Main nav */}
        <nav className="space-y-0.5" aria-label="Workspace navigation">
          <NavItem
            icon={<LayoutDashboard className="size-4" />}
            label="Overview"
            isActive={activePage === 'overview'}
            isCollapsed={isCollapsed}
            onClick={() => navigate('/dashboard/organization')}
          />
        </nav>

        {/* Team space */}
        <div className="mt-5 border-t border-sidebar-border pt-4">
          {!isCollapsed && (
            <p className="mb-2 px-2.5 text-[9px] font-bold tracking-[.16em] text-muted-foreground">TEAM SPACE</p>
          )}

          {levels.map(({ level, teams: levelTeams }) => {
            const isOpen = expandedLevels.has(level);
            return (
              <div key={level}>
                <button
                  onClick={() => toggleLevel(level)}
                  title={isCollapsed ? `Level ${level}` : undefined}
                  className={`mt-1 flex w-full items-center gap-2 rounded-md py-1.5 text-[11px] font-semibold text-muted-foreground transition-colors hover:bg-sidebar-accent/40 hover:text-foreground ${
                    isCollapsed ? 'justify-center px-2' : 'px-2.5'
                  }`}
                >
                  {!isCollapsed && (
                    <ChevronDown className={`size-3 shrink-0 transition-transform ${isOpen ? '' : '-rotate-90'}`} />
                  )}
                  <Users className="size-3.5 shrink-0" />
                  {!isCollapsed && (
                    <>
                      <span>Level {level}</span>
                      <span className="ml-auto text-[10px] opacity-50">{levelTeams.length}</span>
                    </>
                  )}
                </button>

                {isOpen && !isCollapsed && (
                  <div className="ml-3 mt-0.5 space-y-px border-l border-sidebar-border/50 pl-2">
                    {levelTeams.map(team => {
                      const isActive = team.teamId === activeTeamId;
                      return (
                        <div key={team.teamId} className="space-y-px">
                          <div className={`truncate rounded-md px-2 py-1 text-[11px] font-medium ${
                            isActive ? 'bg-primary/8 text-primary' : 'text-foreground/70'
                          }`}>
                            {team.name}
                          </div>
                          <div className="flex gap-0.5 px-1">
                            <button
                              onClick={() => goTo('radar', team)}
                              className={`flex items-center gap-1 rounded px-2 py-1 text-[10px] font-medium transition-colors ${
                                isActive && activePage === 'radar'
                                  ? 'bg-primary/10 text-primary'
                                  : 'text-muted-foreground hover:bg-sidebar-accent hover:text-foreground'
                              }`}
                            >
                              <RadarIcon className="size-3" /> Radar
                            </button>
                            <button
                              onClick={() => goTo('strategy', team)}
                              className={`flex items-center gap-1 rounded px-2 py-1 text-[10px] font-medium transition-colors ${
                                isActive && activePage === 'strategy'
                                  ? 'bg-primary/10 text-primary'
                                  : 'text-muted-foreground hover:bg-sidebar-accent hover:text-foreground'
                              }`}
                            >
                              <Target className="size-3" /> Strategy
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Footer actions */}
      <div className="shrink-0 space-y-0.5 border-t border-sidebar-border p-2">
        <NavItem
          icon={<Settings className="size-4" />}
          label="Settings"
          isActive={false}
          isCollapsed={isCollapsed}
          onClick={() => {}}
        />
        <NavItem
          icon={<LogOut className="size-4" />}
          label="Sign out"
          isActive={false}
          isCollapsed={isCollapsed}
          onClick={signOut}
        />
        <button
          onClick={() => setIsCollapsed(!isCollapsed)}
          title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          className={`flex h-9 w-full items-center gap-3 rounded-md px-2.5 text-xs text-muted-foreground transition-colors hover:bg-sidebar-accent hover:text-foreground ${
            isCollapsed ? 'justify-center' : ''
          }`}
        >
          <ChevronLeft className={`size-4 shrink-0 transition-transform duration-200 ${isCollapsed ? 'rotate-180' : ''}`} />
          {!isCollapsed && <span>Collapse</span>}
        </button>
      </div>
    </aside>
  );
}
