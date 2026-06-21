export default function Spinner({ label = "Loading…" }) {
  return (
    <div className="flex items-center justify-center gap-3 py-16 text-slate-500 dark:text-slate-400">
      <span className="h-6 w-6 animate-spin rounded-full border-2 border-aurora-violet/30 border-t-aurora-violet" />
      <span className="text-sm">{label}</span>
    </div>
  );
}
