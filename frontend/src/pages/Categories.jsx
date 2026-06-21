import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { Layers, ArrowRight } from "lucide-react";
import { CategoryApi } from "../lib/api";
import Spinner from "../components/Spinner";

export default function Categories() {
  const [cats, setCats] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    CategoryApi.list()
      .then(setCats)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="flex items-center gap-2 text-2xl font-bold">
        <Layers className="text-aurora-violet" /> Topics
      </h1>
      {cats.length === 0 ? (
        <p className="glass p-8 text-center text-slate-500 dark:text-slate-400">
          No topics yet.
        </p>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {cats.map((c, i) => (
            <motion.div
              key={c.categoryId}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: Math.min(i * 0.05, 0.3) }}
            >
              <Link
                to={`/categories/${c.categoryId}`}
                className="glass group flex h-full flex-col gap-2 p-6 transition hover:-translate-y-1 hover:shadow-glow"
              >
                <h3 className="text-lg font-bold group-hover:text-gradient">{c.categoryTitle}</h3>
                <p className="text-sm text-slate-600 dark:text-slate-300">{c.categoryDescription}</p>
                <span className="mt-auto inline-flex items-center gap-1 pt-3 text-sm text-aurora-violet">
                  Browse <ArrowRight size={15} />
                </span>
              </Link>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
