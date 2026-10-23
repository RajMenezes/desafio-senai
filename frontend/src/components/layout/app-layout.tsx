import { useMemo, useState } from 'react'
import { Outlet } from 'react-router'
import { PageTitleContext } from '@/app/page-title'
import { AppHeader } from '@/components/layout/app-header'
import { AppSidebar } from '@/components/layout/app-sidebar'
import { SidebarInset, SidebarProvider } from '@/components/ui/sidebar'

export function AppLayout() {
  const [title, setTitle] = useState<string>()
  const pageTitle = useMemo(() => ({ title, setTitle }), [title])

  return (
    <PageTitleContext.Provider value={pageTitle}>
      <SidebarProvider>
        <AppSidebar />
        <SidebarInset className="min-w-0">
          <AppHeader />
          <div className="flex flex-1 flex-col gap-6 p-4 md:p-8">
            <Outlet />
          </div>
        </SidebarInset>
      </SidebarProvider>
    </PageTitleContext.Provider>
  )
}
