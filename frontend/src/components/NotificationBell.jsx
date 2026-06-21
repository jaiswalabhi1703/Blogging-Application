import { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, Check } from "lucide-react";
import { NotificationApi } from "../lib/api";
import { useAuth } from "../context/AuthContext";

export default function NotificationBell() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [open, setOpen] = useState(false);
  const esRef = useRef(null);

  const unread = items.filter((n) => !n.seen).length;

  const load = useCallback(async () => {
    try {
      setItems(await NotificationApi.list());
    } catch {
      /* ignore */
    }
  }, []);

  // Initial load + live updates via Server-Sent Events
  useEffect(() => {
    if (!user) return undefined;
    load();

    const es = new EventSource(NotificationApi.streamUrl());
    esRef.current = es;
    es.addEventListener("notification", (e) => {
      try {
        const n = JSON.parse(e.data);
        setItems((prev) => [n, ...prev]);
      } catch {
        load();
      }
    });
    es.onerror = () => {
      // browser auto-reconnects; close on unmount handles teardown
    };

    return () => es.close();
  }, [user, load]);

  if (!user) return null;

  const markRead = async (n) => {
    if (!n.seen) {
      try {
        await NotificationApi.markRead(n.id);
        setItems((prev) => prev.map((x) => (x.id === n.id ? { ...x, seen: true } : x)));
      } catch {
        /* ignore */
      }
    }
    if (n.postId) {
      setOpen(false);
      navigate(`/posts/${n.postId}`);
    }
  };

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className="relative grid h-9 w-9 place-items-center rounded-xl border border-slate-200 bg-white/60 text-slate-700 transition hover:bg-white dark:border-white/10 dark:bg-white/5 dark:text-slate-200 dark:hover:bg-white/10"
        aria-label="Notifications"
      >
        <Bell size={18} />
        {unread > 0 && (
          <span className="absolute -right-1 -top-1 grid h-4 min-w-4 place-items-center rounded-full bg-gradient-to-br from-aurora-fuchsia to-aurora-violet px-1 text-[10px] font-bold text-white">
            {unread > 9 ? "9+" : unread}
          </span>
        )}
      </button>

      {open && (
        <>
          <div className="fixed inset-0 z-30" onClick={() => setOpen(false)} />
          <div className="glass-strong absolute right-0 z-40 mt-2 max-h-96 w-80 overflow-y-auto p-2">
            <div className="px-2 py-1.5 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
              Notifications
            </div>
            {items.length === 0 ? (
              <p className="px-2 py-6 text-center text-sm text-slate-500 dark:text-slate-400">
                You're all caught up.
              </p>
            ) : (
              items.map((n) => (
                <button
                  key={n.id}
                  onClick={() => markRead(n)}
                  className={`flex w-full items-start gap-2 rounded-lg p-2.5 text-left text-sm transition hover:bg-black/5 dark:hover:bg-white/5 ${
                    n.seen ? "opacity-60" : ""
                  }`}
                >
                  <span
                    className={`mt-1 h-2 w-2 shrink-0 rounded-full ${
                      n.seen ? "bg-transparent" : "bg-aurora-fuchsia"
                    }`}
                  />
                  <span className="flex-1">
                    <span className="block text-slate-700 dark:text-slate-200">{n.message}</span>
                    <span className="text-xs text-slate-400">
                      {new Date(n.createdAt).toLocaleString()}
                    </span>
                  </span>
                  {n.seen && <Check size={14} className="mt-1 text-slate-400" />}
                </button>
              ))
            )}
          </div>
        </>
      )}
    </div>
  );
}
