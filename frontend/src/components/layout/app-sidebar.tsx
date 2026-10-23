import { Clock, ListOrdered, Shield, Swords, Users, type LucideIcon } from 'lucide-react'
import { matchPath, NavLink, useLocation } from 'react-router'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar'

interface NavItem {
  to: string
  label: string
  icon: LucideIcon
}

interface NavGroup {
  label: string
  items: NavItem[]
}

const NAV_GROUPS: NavGroup[] = [
  {
    label: 'Mercado',
    items: [
      { to: '/jogadores', label: 'Jogadores', icon: Users },
      { to: '/clubes', label: 'Clubes', icon: Shield },
    ],
  },
  {
    label: 'Liga',
    items: [
      { to: '/partidas', label: 'Partidas', icon: Swords },
      { to: '/classificacao', label: 'Classificação', icon: ListOrdered },
    ],
  },
]

const ACTIVE_ITEM_CLASSES =
  'data-active:bg-background data-active:shadow-[0_0_0_1px_var(--sidebar-border)] data-active:[&>svg]:text-primary'

export function AppSidebar() {
  const { pathname } = useLocation()

  return (
    <Sidebar variant="inset">
      <SidebarHeader>
        <div className="flex items-center gap-3 px-2 py-1.5">
          <div
            className="flex size-8 items-center justify-center rounded-lg bg-primary text-lg font-bold text-primary-foreground"
            aria-hidden
          >
            P
          </div>
          <div className="leading-tight">
            <p className="font-semibold">Passe</p>
            <p className="text-xs text-muted-foreground">Gestão da liga</p>
          </div>
        </div>
      </SidebarHeader>
      <SidebarContent>
        <nav aria-label="Menu principal">
          {NAV_GROUPS.map((group) => (
            <SidebarGroup key={group.label}>
              <SidebarGroupLabel>{group.label}</SidebarGroupLabel>
              <SidebarGroupContent>
                <SidebarMenu>
                  {group.items.map((item) => (
                    <SidebarMenuItem key={item.to}>
                      <SidebarMenuButton
                        asChild
                        isActive={matchPath({ path: `${item.to}/*` }, pathname) !== null}
                        className={ACTIVE_ITEM_CLASSES}
                      >
                        <NavLink to={item.to}>
                          <item.icon aria-hidden />
                          <span>{item.label}</span>
                        </NavLink>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  ))}
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          ))}
        </nav>
      </SidebarContent>
      <SidebarFooter>
        <p className="flex items-center gap-2 px-2 py-1 text-xs text-muted-foreground">
          <Clock className="size-3.5" aria-hidden />
          Horário de Brasília (UTC−3)
        </p>
      </SidebarFooter>
    </Sidebar>
  )
}
