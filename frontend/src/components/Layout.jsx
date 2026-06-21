import { Outlet } from "react-router-dom";
import AuroraBackground from "./AuroraBackground";
import Navbar from "./Navbar";

export default function Layout() {
  return (
    <div className="flex min-h-screen flex-col">
      <AuroraBackground />
      <Navbar />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>
      <footer className="mx-auto w-full max-w-6xl px-4 py-8 text-center text-sm text-slate-500 dark:text-slate-400">
        <p>
          Built with React + Spring Boot ·{" "}
          <span className="text-gradient font-semibold">Blogify</span> · AI-powered
        </p>
      </footer>
    </div>
  );
}
