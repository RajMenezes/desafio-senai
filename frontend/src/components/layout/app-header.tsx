import { Fragment } from 'react'
import { Link } from 'react-router'
import { useCurrentPageTitle } from '@/app/page-title'
import { useRouteHandle } from '@/app/route-handle'
import { RefreshButton } from '@/components/layout/refresh-button'
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'

export function AppHeader() {
  const handle = useRouteHandle()
  const pageTitle = useCurrentPageTitle()

  return (
    <header className="flex h-14 shrink-0 items-center gap-2 border-b px-4">
      <SidebarTrigger className="-ml-1" />
      <Separator
        orientation="vertical"
        className="mr-2 data-vertical:h-4 data-vertical:self-center"
      />
      {handle && (
        <Breadcrumb className="min-w-0">
          <BreadcrumbList className="flex-nowrap">
            <BreadcrumbItem>{handle.section}</BreadcrumbItem>
            {handle.parent && (
              <Fragment>
                <BreadcrumbSeparator />
                <BreadcrumbItem>
                  <BreadcrumbLink asChild>
                    <Link to={handle.parent.to}>{handle.parent.title}</Link>
                  </BreadcrumbLink>
                </BreadcrumbItem>
              </Fragment>
            )}
            <BreadcrumbSeparator />
            <BreadcrumbItem className="min-w-0">
              <BreadcrumbPage className="truncate">{pageTitle ?? handle.title}</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>
      )}
      <div className="ml-auto">
        <RefreshButton />
      </div>
    </header>
  )
}
