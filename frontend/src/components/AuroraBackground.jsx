// Fixed, full-viewport animated gradient blobs — the "aurora" behind the glass UI.
export default function AuroraBackground() {
  return (
    <div className="pointer-events-none fixed inset-0 -z-10 overflow-hidden">
      <div className="absolute inset-0 bg-slate-50 dark:bg-[#070912]" />
      <div className="absolute -top-40 -left-32 h-[42rem] w-[42rem] rounded-full bg-aurora-violet/30 blur-[120px] animate-aurora-shift" />
      <div className="absolute top-1/3 -right-40 h-[38rem] w-[38rem] rounded-full bg-aurora-fuchsia/25 blur-[120px] animate-aurora-shift [animation-delay:-6s]" />
      <div className="absolute -bottom-48 left-1/4 h-[40rem] w-[40rem] rounded-full bg-aurora-cyan/25 blur-[130px] animate-aurora-shift [animation-delay:-12s]" />
      {/* subtle grid texture */}
      <div
        className="absolute inset-0 opacity-[0.04] dark:opacity-[0.06]"
        style={{
          backgroundImage:
            "linear-gradient(to right, currentColor 1px, transparent 1px), linear-gradient(to bottom, currentColor 1px, transparent 1px)",
          backgroundSize: "44px 44px",
        }}
      />
    </div>
  );
}
