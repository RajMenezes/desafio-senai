import { Clock, ListOrdered, Plus, Shield, Swords, Users, type LucideIcon } from 'lucide-react'
import { Link, matchPath, NavLink, useLocation } from 'react-router'
import { paths } from '@/app/paths'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupAction,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuBadge,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar'
import { useClubs } from '@/features/clubs/api'
import { usePlayers } from '@/features/players/api'
import { removeAccents } from '@/lib/text'
import { cn } from '@/lib/utils'

interface NavItem {
  to: string
  label: string
  icon: LucideIcon
  count?: number
}

const ACTIVE_ITEM_CLASSES =
  'data-active:bg-background data-active:shadow-[0_0_0_1px_var(--sidebar-border)] data-active:[&>svg]:text-primary'

function isActive(pathname: string, to: string, end = false): boolean {
  return matchPath({ path: end ? to : `${to}/*` }, pathname) !== null
}

function NavGroup({ label, items }: { label: string; items: NavItem[] }) {
  const { pathname } = useLocation()
  return (
    <SidebarGroup>
      <SidebarGroupLabel>{label}</SidebarGroupLabel>
      <SidebarGroupContent>
        <SidebarMenu>
          {items.map((item) => (
            <SidebarMenuItem key={item.to}>
              <SidebarMenuButton
                asChild
                isActive={isActive(pathname, item.to)}
                className={ACTIVE_ITEM_CLASSES}
              >
                <NavLink to={item.to}>
                  <item.icon aria-hidden />
                  <span>{item.label}</span>
                </NavLink>
              </SidebarMenuButton>
              {item.count !== undefined && (
                <SidebarMenuBadge aria-hidden className="text-muted-foreground">
                  {item.count}
                </SidebarMenuBadge>
              )}
            </SidebarMenuItem>
          ))}
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  )
}

/** Atalhos para a página de cada clube, na ordem por nome devolvida pela API. */
function ClubsGroup() {
  const { pathname } = useLocation()
  const clubsQuery = useClubs()
  const clubs = clubsQuery.data ?? []

  return (
    <SidebarGroup>
      <SidebarGroupLabel>Clubes</SidebarGroupLabel>
      <SidebarGroupAction asChild title="Novo clube">
        <Link to={paths.newClub} aria-label="Novo clube">
          <Plus aria-hidden />
        </Link>
      </SidebarGroupAction>
      <SidebarGroupContent>
        {clubsQuery.data && clubs.length === 0 && (
          <p className="px-2 py-1 text-xs text-muted-foreground">Nenhum clube ainda.</p>
        )}
        <SidebarMenu>
          {clubs.map((club) => {
            const active = isActive(pathname, paths.club(club.id), true)
            return (
              <SidebarMenuItem key={club.id}>
                <SidebarMenuButton asChild isActive={active} className={ACTIVE_ITEM_CLASSES}>
                  <NavLink to={paths.club(club.id)}>
                    <span
                      aria-hidden
                      className={cn(
                        'flex size-5 shrink-0 items-center justify-center rounded border bg-background text-[0.65rem] font-semibold',
                        active && 'border-primary bg-primary text-primary-foreground',
                      )}
                    >
                      {removeAccents(club.name.charAt(0)).toUpperCase()}
                    </span>
                    <span>{club.name}</span>
                  </NavLink>
                </SidebarMenuButton>
              </SidebarMenuItem>
            )
          })}
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  )
}

export function AppSidebar() {
  const playersCount = usePlayers({ limit: 1 }).data?.meta.total_items
  const clubsCount = useClubs().data?.length

  const marketItems: NavItem[] = [
    { to: paths.players, label: 'Jogadores', icon: Users, count: playersCount },
    { to: paths.clubs, label: 'Clubes', icon: Shield, count: clubsCount },
  ]
  const leagueItems: NavItem[] = [
    { to: paths.matches, label: 'Partidas', icon: Swords },
    { to: paths.standings, label: 'Classificação', icon: ListOrdered },
  ]

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
          <NavGroup label="Mercado" items={marketItems} />
          <NavGroup label="Liga" items={leagueItems} />
          <ClubsGroup />
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
