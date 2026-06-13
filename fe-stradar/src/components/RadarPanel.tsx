import { useState } from 'react';
import type { RadarElement } from '@/context/AppContext';
import RadarSVG, { QUADRANTS } from './RadarSVG';

interface Theme {
    label: string;
    preview: [string, string, string];
    vars: Record<string, string>;
}

const THEMES: Record<string, Theme> = {
    light: {
        label: 'Light',
        preview: ['hsl(221 83% 53%)', 'hsl(0 0% 100%)', 'hsl(221 83% 53%)'],
        vars: {
            '--background': '0 0% 100%',
            '--foreground': '222 47% 11%',
            '--card': '0 0% 98%',
            '--border': '214 32% 91%',
            '--muted-foreground': '215 16% 47%',
            '--primary': '221 83% 53%',
            '--primary-foreground': '0 0% 100%',
            '--radar-line': '214 25% 62%',
            '--radar-high': '4 86% 55%',
            '--radar-medium': '38 95% 50%',
            '--radar-low': '142 69% 40%',
        },
    },
    dark: {
        label: 'Dark',
        preview: ['hsl(142 70% 50%)', 'hsl(0 0% 2%)', 'hsl(142 70% 50%)'],
        vars: {
            '--background': '0 0% 2%',
            '--foreground': '142 40% 92%',
            '--card': '0 0% 7%',
            '--border': '142 20% 20%',
            '--muted-foreground': '142 15% 68%',
            '--primary': '142 70% 52%',
            '--primary-foreground': '0 0% 2%',
            '--radar-line': '142 28% 52%',
            '--radar-high': '4 90% 65%',
            '--radar-medium': '38 95% 58%',
            '--radar-low': '142 65% 56%',
        },
    },
    business: {
        label: 'Business',
        preview: ['hsl(25 95% 58%)', 'hsl(223 44% 7%)', 'hsl(25 95% 58%)'],
        vars: {
            '--background': '223 44% 7%',
            '--foreground': '210 20% 92%',
            '--card': '223 44% 11%',
            '--border': '223 20% 22%',
            '--muted-foreground': '223 20% 68%',
            '--primary': '25 95% 58%',
            '--primary-foreground': '223 44% 7%',
            '--radar-line': '220 28% 52%',
            '--radar-high': '4 90% 64%',
            '--radar-medium': '38 90% 55%',
            '--radar-low': '142 58% 54%',
        },
    },
};

type ThemeKey = keyof typeof THEMES;

interface Props {
    elements: RadarElement[];
    onEdit: (el: RadarElement) => void;
}

export default function RadarPanel({ elements, onEdit }: Props) {
    const [activeQ, setActiveQ] = useState<number | null>(null);
    const [themeKey, setThemeKey] = useState<ThemeKey>('light');

    const R = 280;
    const PAD = 90;
    const theme = THEMES[themeKey];

    const themeStyle: React.CSSProperties = {
        ...(theme.vars as React.CSSProperties),
        backgroundColor: `hsl(${theme.vars['--background']})`,
        color: `hsl(${theme.vars['--foreground']})`,
    };

    // Site primary color — hardcoded so buttons match Detect regardless of radar theme
    const SITE_PRIMARY = 'oklch(0.73 0.105 190)';
    const SITE_PRIMARY_FG = 'oklch(0.205 0.065 248)';

    return (
        <div className="flex flex-col lg:flex-row gap-4 p-3 rounded-lg w-full transition-colors duration-300" style={themeStyle}>

            {/* ── RADAR VISUALIZATION ────────────────────────────────────────── */}
            <div className="flex-1 min-w-0 flex items-start justify-center pt-2 bg-background/30 rounded">
                <div className="relative w-full p-2" style={{ maxWidth: (R + PAD) * 2 + 'px' }}>
                    <RadarSVG
                        elements={elements}
                        activeQ={activeQ}
                        selectedEl={null}
                        onSelectEl={(el) => { if (el) onEdit(el); }}
                        onClickLabel={(geom) => setActiveQ(prev => prev === geom ? null : geom)}
                    />
                </div>
            </div>

            {/* ── CONTROL SIDEBAR ────────────────────────────────────────────── */}
            <div className="w-full lg:w-[230px] flex-shrink-0 flex flex-col gap-3">

                {/* Theme Switcher */}
                <section>
                    <header className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-1.5">App Theme</header>
                    <div className="grid grid-cols-3 gap-1">
                        {Object.entries(THEMES).map(([key, t]) => (
                            <button
                                key={key}
                                onClick={() => setThemeKey(key as ThemeKey)}
                                style={themeKey === key ? { backgroundColor: SITE_PRIMARY, color: SITE_PRIMARY_FG } : {}}
                                className={`flex flex-col items-center justify-center gap-1 px-2 py-2 rounded text-[10px] font-bold transition-all ${
                                    themeKey === key ? '' : 'text-muted-foreground hover:text-foreground'
                                }`}
                            >
                                <div className="flex -space-x-1">
                                    <div className="w-3.5 h-3.5 rounded-full" style={{ background: t.preview[0] }} />
                                    <div className="w-3.5 h-3.5 rounded-full" style={{ background: t.preview[1] }} />
                                </div>
                                {t.label}
                            </button>
                        ))}
                    </div>
                </section>

                <div className="h-px bg-border/50" />

                {/* Quadrants */}
                <section>
                    <header className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-1.5">Quadrants</header>
                    <div className="grid grid-cols-2 lg:grid-cols-1 gap-0.5">
                        {Object.entries(QUADRANTS).sort((a, b) => a[1].geom - b[1].geom).map(([key, q]) => {
                            const isActive = activeQ === q.geom;
                            return (
                                <button
                                    key={key}
                                    onClick={() => setActiveQ(prev => prev === q.geom ? null : q.geom)}
                                    className="w-full text-left px-2.5 py-1.5 rounded-md text-[11px] font-medium transition-colors truncate"
                                    style={isActive ? {
                                        backgroundColor: SITE_PRIMARY,
                                        color: SITE_PRIMARY_FG,
                                    } : { color: 'var(--muted-foreground)' }}
                                    onMouseEnter={e => { if (!isActive) (e.currentTarget as HTMLElement).style.backgroundColor = `color-mix(in oklab, ${SITE_PRIMARY} 12%, transparent)`; }}
                                    onMouseLeave={e => { if (!isActive) (e.currentTarget as HTMLElement).style.backgroundColor = ''; }}
                                >
                                    {q.label}
                                </button>
                            );
                        })}
                    </div>
                </section>

                {/* Legend */}
                <section className="p-2 rounded">
                    <header className="text-[9px] font-bold text-muted-foreground uppercase tracking-widest mb-2">Legend</header>
                    <div className="flex flex-wrap gap-x-3 gap-y-2">
                        {(['HIGH', 'MEDIUM', 'LOW'] as const).map(level => (
                            <div key={level} className="flex items-center gap-1.5 text-[10px] font-mono uppercase">
                                <div className="w-2 h-2 rounded-full" style={{ background: `hsl(var(--radar-${level.toLowerCase()}))` }} />
                                {level}
                            </div>
                        ))}
                    </div>
                </section>
            </div>
        </div>
    );
}
