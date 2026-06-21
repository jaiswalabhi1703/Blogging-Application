import { Link } from "react-router-dom";
import { Ghost } from "lucide-react";

export default function NotFound() {
  return (
    <div className="glass mx-auto flex max-w-md flex-col items-center gap-4 p-12 text-center">
      <Ghost size={40} className="text-aurora-violet" />
      <h1 className="text-3xl font-extrabold">
        4<span className="text-gradient">0</span>4
      </h1>
      <p className="text-slate-600 dark:text-slate-300">This page drifted into the aurora.</p>
      <Link to="/" className="btn-primary">
        Back home
      </Link>
    </div>
  );
}
