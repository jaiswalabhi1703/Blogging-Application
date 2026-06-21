import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { Sparkles, PenSquare, LogOut, User as UserIcon, Menu, X } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import ThemeToggle from "./ThemeToggle";

const navItem = ({ isActive }) =>
  `px-3 py-2 rounded-lg text-sm font-medium transition ${
    isActive
      ? "text-aurora-violet dark:text-fuchsia-200 bg-aurora-violet/10"
      : "text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white hover:bg-black/5 dark:hover:bg-white/5"
  }`;

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const doLogout = async () => {
    await logout();
    navigate("/");
  };

  return (
    <header className="sticky top-0 z-40 px-4 pt-4">
      <nav className="glass-strong mx-auto flex max-w-6xl items-center justify-between px-4 py-2.5">
        <Link to="/" className="flex items-center gap-2 font-display text-lg font-bold">
          <span className="grid h-8 w-8 place-items-center rounded-xl bg-gradient-to-br from-aurora-violet to-aurora-cyan text-white shadow-glow">
            <Sparkles size={18} />
          </span>
          <span className="text-gradient">Blogify</span>
        </Link>

        <div className="hidden items-center gap-1 md:flex">
          <NavLink to="/" end className={navItem}>
            Discover
          </NavLink>
          <NavLink to="/categories" className={navItem}>
            Topics
          </NavLink>
          {user && (
            <NavLink to="/write" className={navItem}>
              Write
            </NavLink>
          )}
        </div>

        <div className="hidden items-center gap-2 md:flex">
          <ThemeToggle />
          {user ? (
            <>
              <Link to="/write" className="btn-primary">
                <PenSquare size={16} /> New post
              </Link>
              <Link to="/profile" className="btn-ghost" title={user.name}>
                <UserIcon size={16} /> {user.name?.split(" ")[0]}
              </Link>
              <button onClick={doLogout} className="btn-ghost" title="Log out">
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-ghost">
                Sign in
              </Link>
              <Link to="/register" className="btn-primary">
                Get started
              </Link>
            </>
          )}
        </div>

        <div className="flex items-center gap-2 md:hidden">
          <ThemeToggle />
          <button onClick={() => setOpen((o) => !o)} className="btn-ghost px-2" aria-label="Menu">
            {open ? <X size={18} /> : <Menu size={18} />}
          </button>
        </div>
      </nav>

      {open && (
        <div className="glass mx-auto mt-2 max-w-6xl space-y-1 p-3 md:hidden">
          <NavLink to="/" end className={navItem} onClick={() => setOpen(false)}>
            Discover
          </NavLink>
          <NavLink to="/categories" className={navItem} onClick={() => setOpen(false)}>
            Topics
          </NavLink>
          {user ? (
            <>
              <NavLink to="/write" className={navItem} onClick={() => setOpen(false)}>
                Write
              </NavLink>
              <NavLink to="/profile" className={navItem} onClick={() => setOpen(false)}>
                Profile
              </NavLink>
              <button
                onClick={() => {
                  setOpen(false);
                  doLogout();
                }}
                className="block w-full rounded-lg px-3 py-2 text-left text-sm text-slate-600 dark:text-slate-300"
              >
                Log out
              </button>
            </>
          ) : (
            <div className="flex gap-2 pt-1">
              <Link to="/login" className="btn-ghost flex-1" onClick={() => setOpen(false)}>
                Sign in
              </Link>
              <Link to="/register" className="btn-primary flex-1" onClick={() => setOpen(false)}>
                Get started
              </Link>
            </div>
          )}
        </div>
      )}
    </header>
  );
}
