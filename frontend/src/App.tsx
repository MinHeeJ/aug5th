import OrganizationManagementPage from "./pages/OrganizationManagementPage";
import UserManagementPage from "./pages/UserManagementPage";

export default function App() {
  const path = window.location.pathname;

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <div className="flex min-h-screen">
        <aside className="hidden w-20 shrink-0 flex-col items-center gap-4 bg-slate-950 py-5 text-white md:flex">
          <div className="grid h-11 w-11 place-items-center rounded-2xl bg-indigo-500 font-bold">
            KN
          </div>
          <nav
            className="flex flex-col gap-3 text-xs"
            aria-label="미니 사이드바"
          >
            <a className="rounded-xl bg-white/10 px-3 py-2" href="/admin/users">
              사용자
            </a>
            <a
              className="rounded-xl bg-white/10 px-3 py-2"
              href="/admin/organizations"
            >
              조직
            </a>
          </nav>
        </aside>
        <main className="flex-1 p-4 md:p-8">
          {path === "/admin/organizations" ? (
            <OrganizationManagementPage />
          ) : (
            <UserManagementPage />
          )}
        </main>
      </div>
    </div>
  );
}
