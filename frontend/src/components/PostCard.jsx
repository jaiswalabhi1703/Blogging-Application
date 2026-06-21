import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { CalendarDays, Sparkles } from "lucide-react";
import { PostApi } from "../lib/api";

function excerpt(text, n = 140) {
  if (!text) return "";
  const clean = text.replace(/\s+/g, " ").trim();
  return clean.length > n ? clean.slice(0, n) + "…" : clean;
}

export default function PostCard({ post, index = 0 }) {
  const hasImage = post.imageName && post.imageName !== "default.png";
  const tags = (post.tags || "").split(",").map((t) => t.trim()).filter(Boolean).slice(0, 3);

  return (
    <motion.article
      initial={{ opacity: 0, y: 16 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-40px" }}
      transition={{ duration: 0.4, delay: Math.min(index * 0.05, 0.3) }}
      className="glass group flex flex-col overflow-hidden transition hover:-translate-y-1 hover:shadow-glow"
    >
      <Link to={`/posts/${post.postId}`} className="flex h-full flex-col">
        {hasImage && (
          <div className="relative h-44 overflow-hidden">
            <img
              src={PostApi.imageUrl(post.imageName)}
              alt={post.title}
              loading="lazy"
              className="h-full w-full object-cover transition duration-500 group-hover:scale-105"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent" />
          </div>
        )}
        <div className="flex flex-1 flex-col gap-3 p-5">
          {post.category?.categoryTitle && (
            <span className="chip w-fit">{post.category.categoryTitle}</span>
          )}
          <h3 className="text-lg font-bold leading-snug group-hover:text-gradient">
            {post.title}
          </h3>
          {post.summary ? (
            <p className="flex items-start gap-1.5 text-sm text-slate-600 dark:text-slate-300">
              <Sparkles size={14} className="mt-0.5 shrink-0 text-aurora-violet" />
              <span>{excerpt(post.summary, 150)}</span>
            </p>
          ) : (
            <p className="text-sm text-slate-600 dark:text-slate-300">{excerpt(post.content)}</p>
          )}

          {tags.length > 0 && (
            <div className="flex flex-wrap gap-1.5">
              {tags.map((t) => (
                <span key={t} className="text-xs text-aurora-cyan">#{t}</span>
              ))}
            </div>
          )}

          <div className="mt-auto flex items-center justify-between pt-2 text-xs text-slate-500 dark:text-slate-400">
            <span>{post.user?.name || "Unknown"}</span>
            {post.addedDate && (
              <span className="inline-flex items-center gap-1">
                <CalendarDays size={12} />
                {new Date(post.addedDate).toLocaleDateString()}
              </span>
            )}
          </div>
        </div>
      </Link>
    </motion.article>
  );
}
