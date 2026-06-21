import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { Search, Sparkles, ArrowRight, X } from "lucide-react";
import { PostApi } from "../lib/api";
import PostCard from "../components/PostCard";
import Spinner from "../components/Spinner";

export default function Home() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState(null);
  const [searching, setSearching] = useState(false);

  const load = useCallback(async (p) => {
    setLoading(true);
    try {
      const res = await PostApi.list({ PageNumber: p, PageSize: 9, sortBy: "postId", sortDir: "desc" });
      setData(res);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(page);
  }, [page, load]);

  const onSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;
    setSearching(true);
    try {
      setResults(await PostApi.search(query.trim()));
    } finally {
      setSearching(false);
    }
  };

  const clearSearch = () => {
    setQuery("");
    setResults(null);
  };

  const posts = results ?? data?.content ?? [];

  return (
    <div className="space-y-10">
      {/* Hero */}
      <motion.section
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="glass-strong relative overflow-hidden p-8 sm:p-12"
      >
        <div className="relative z-10 max-w-2xl">
          <span className="chip mb-4 inline-flex items-center gap-1.5">
            <Sparkles size={14} /> AI-assisted blogging
          </span>
          <h1 className="text-4xl font-extrabold leading-tight sm:text-5xl">
            Stories that <span className="text-gradient">glow</span>.
          </h1>
          <p className="mt-4 text-lg text-slate-600 dark:text-slate-300">
            Read and write beautifully. Every post can be summarized and tagged by OpenAI in one click.
          </p>
          <form onSubmit={onSearch} className="mt-6 flex max-w-md items-center gap-2">
            <div className="relative flex-1">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search posts by title…"
                className="input pl-9"
              />
              {results && (
                <button
                  type="button"
                  onClick={clearSearch}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                >
                  <X size={16} />
                </button>
              )}
            </div>
            <button type="submit" className="btn-primary">
              Search
            </button>
          </form>
        </div>
      </motion.section>

      {/* Feed */}
      <section>
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-xl font-bold">
            {results ? `Results for “${query}”` : "Latest posts"}
          </h2>
          {results && (
            <button onClick={clearSearch} className="text-sm text-aurora-violet hover:underline">
              Back to feed
            </button>
          )}
        </div>

        {loading || searching ? (
          <Spinner />
        ) : posts.length === 0 ? (
          <EmptyState search={!!results} />
        ) : (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {posts.map((p, i) => (
              <PostCard key={p.postId} post={p} index={i} />
            ))}
          </div>
        )}

        {/* Pagination (only for the main feed) */}
        {!results && data && data.totalPages > 1 && (
          <div className="mt-8 flex items-center justify-center gap-3">
            <button
              className="btn-ghost"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              Previous
            </button>
            <span className="text-sm text-slate-500 dark:text-slate-400">
              Page {data.pageNumber + 1} of {data.totalPages}
            </span>
            <button
              className="btn-ghost"
              disabled={data.lastPage}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        )}
      </section>
    </div>
  );
}

function EmptyState({ search }) {
  return (
    <div className="glass flex flex-col items-center gap-3 p-12 text-center">
      <Sparkles className="text-aurora-violet" />
      <p className="text-slate-600 dark:text-slate-300">
        {search ? "No posts matched your search." : "No posts yet — be the first to write one."}
      </p>
      {!search && (
        <Link to="/write" className="btn-primary">
          Write a post <ArrowRight size={16} />
        </Link>
      )}
    </div>
  );
}
