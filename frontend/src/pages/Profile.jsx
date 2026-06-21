import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { PenSquare, Mail, ShieldCheck } from "lucide-react";
import { PostApi } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import PostCard from "../components/PostCard";
import Spinner from "../components/Spinner";

export default function Profile() {
  const { user, isAdmin } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    PostApi.byUser(user.id)
      .then(setPosts)
      .finally(() => setLoading(false));
  }, [user]);

  if (!user) return null;

  return (
    <div className="space-y-8">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        className="glass-strong flex flex-col gap-4 p-8 sm:flex-row sm:items-center"
      >
        <div className="grid h-20 w-20 place-items-center rounded-2xl bg-gradient-to-br from-aurora-violet to-aurora-cyan text-2xl font-bold text-white shadow-glow">
          {user.name?.charAt(0).toUpperCase()}
        </div>
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold">{user.name}</h1>
            {isAdmin && (
              <span className="chip inline-flex items-center gap-1">
                <ShieldCheck size={13} /> Admin
              </span>
            )}
          </div>
          <p className="mt-1 inline-flex items-center gap-1.5 text-sm text-slate-500 dark:text-slate-400">
            <Mail size={14} /> {user.email}
          </p>
          {user.about && <p className="mt-2 text-slate-600 dark:text-slate-300">{user.about}</p>}
        </div>
        <Link to="/write" className="btn-primary">
          <PenSquare size={16} /> New post
        </Link>
      </motion.div>

      <div>
        <h2 className="mb-4 text-xl font-bold">Your posts ({posts.length})</h2>
        {loading ? (
          <Spinner />
        ) : posts.length === 0 ? (
          <p className="glass p-8 text-center text-slate-500 dark:text-slate-400">
            You haven't written anything yet.
          </p>
        ) : (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {posts.map((p, i) => (
              <PostCard key={p.postId} post={p} index={i} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
