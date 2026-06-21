import { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import {
  CalendarDays,
  Sparkles,
  Pencil,
  Trash2,
  Send,
  MessageCircle,
  ArrowLeft,
} from "lucide-react";
import { PostApi, CommentApi } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import Spinner from "../components/Spinner";

export default function PostDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAdmin } = useAuth();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [comment, setComment] = useState("");
  const [posting, setPosting] = useState(false);
  const [summarizing, setSummarizing] = useState(false);
  const [error, setError] = useState("");

  const isOwner = user && post && user.id === post.user?.id;

  const load = async () => {
    setLoading(true);
    try {
      setPost(await PostApi.get(id));
    } catch {
      setError("Post not found.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const addComment = async (e) => {
    e.preventDefault();
    if (!comment.trim()) return;
    setPosting(true);
    try {
      await CommentApi.create(post.postId, { content: comment.trim() });
      setComment("");
      await load();
    } finally {
      setPosting(false);
    }
  };

  const deleteComment = async (cid) => {
    await CommentApi.remove(cid);
    await load();
  };

  const summarize = async () => {
    setSummarizing(true);
    setError("");
    try {
      setPost(await PostApi.summarize(post.postId));
    } catch (err) {
      setError(err.response?.data?.message || "AI summarization is unavailable right now.");
    } finally {
      setSummarizing(false);
    }
  };

  const removePost = async () => {
    if (!confirm("Delete this post? This cannot be undone.")) return;
    await PostApi.remove(post.postId);
    navigate("/");
  };

  if (loading) return <Spinner />;
  if (!post) return <p className="glass p-8 text-center">{error || "Post not found."}</p>;

  const hasImage = post.imageName && post.imageName !== "default.png";
  const tags = (post.tags || "").split(",").map((t) => t.trim()).filter(Boolean);
  const comments = post.comments || [];

  return (
    <motion.article
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      className="mx-auto max-w-3xl space-y-6"
    >
      <Link to="/" className="inline-flex items-center gap-1.5 text-sm text-slate-500 hover:text-aurora-violet">
        <ArrowLeft size={15} /> Back to feed
      </Link>

      <header className="space-y-4">
        {post.category?.categoryTitle && <span className="chip">{post.category.categoryTitle}</span>}
        <h1 className="text-3xl font-extrabold leading-tight sm:text-4xl">{post.title}</h1>
        <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-slate-500 dark:text-slate-400">
          <span>By {post.user?.name || "Unknown"}</span>
          {post.addedDate && (
            <span className="inline-flex items-center gap-1">
              <CalendarDays size={14} /> {new Date(post.addedDate).toLocaleDateString()}
            </span>
          )}
          {(isOwner || isAdmin) && (
            <span className="ml-auto flex items-center gap-2">
              {isOwner && (
                <Link to={`/posts/${post.postId}/edit`} className="btn-ghost px-2.5 py-1.5">
                  <Pencil size={14} /> Edit
                </Link>
              )}
              <button onClick={removePost} className="btn-ghost px-2.5 py-1.5 text-red-500">
                <Trash2 size={14} /> Delete
              </button>
            </span>
          )}
        </div>
      </header>

      {hasImage && (
        <img
          src={PostApi.imageUrl(post.imageName)}
          alt={post.title}
          className="max-h-[28rem] w-full rounded-2xl object-cover"
        />
      )}

      {/* AI summary panel */}
      {(post.summary || isOwner) && (
        <div className="glass space-y-3 p-5">
          <div className="flex items-center justify-between">
            <h3 className="flex items-center gap-2 font-semibold">
              <Sparkles size={16} className="text-aurora-violet" /> AI summary
            </h3>
            {isOwner && (
              <button onClick={summarize} disabled={summarizing} className="btn-primary px-3 py-1.5 text-xs">
                <Sparkles size={14} />
                {summarizing ? "Generating…" : post.summary ? "Regenerate" : "Generate with OpenAI"}
              </button>
            )}
          </div>
          {post.summary ? (
            <>
              <p className="text-sm text-slate-600 dark:text-slate-300">{post.summary}</p>
              {tags.length > 0 && (
                <div className="flex flex-wrap gap-1.5">
                  {tags.map((t) => (
                    <span key={t} className="chip">#{t}</span>
                  ))}
                </div>
              )}
            </>
          ) : (
            <p className="text-sm text-slate-500 dark:text-slate-400">
              No summary yet. Generate one with OpenAI in a click.
            </p>
          )}
          {error && <p className="text-sm text-red-500">{error}</p>}
        </div>
      )}

      {/* Body */}
      <div className="prose-content whitespace-pre-wrap text-[1.05rem] leading-relaxed text-slate-700 dark:text-slate-200">
        {post.content}
      </div>

      {/* Comments */}
      <section className="space-y-4 pt-4">
        <h3 className="flex items-center gap-2 text-lg font-bold">
          <MessageCircle size={18} /> Comments ({comments.length})
        </h3>

        {user ? (
          <form onSubmit={addComment} className="glass flex items-center gap-2 p-2">
            <input
              className="input border-0 bg-transparent dark:bg-transparent"
              placeholder="Share your thoughts…"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
            />
            <button type="submit" disabled={posting} className="btn-primary shrink-0">
              <Send size={15} /> Post
            </button>
          </form>
        ) : (
          <p className="glass p-4 text-sm text-slate-500 dark:text-slate-400">
            <Link to="/login" className="font-semibold text-aurora-violet hover:underline">
              Sign in
            </Link>{" "}
            to join the conversation.
          </p>
        )}

        <div className="space-y-3">
          {comments.map((c) => (
            <div key={c.id} className="glass flex items-start justify-between gap-3 p-4">
              <p className="text-sm text-slate-700 dark:text-slate-200">{c.content}</p>
              {(isOwner || isAdmin) && (
                <button
                  onClick={() => deleteComment(c.id)}
                  className="shrink-0 text-slate-400 hover:text-red-500"
                  title="Delete comment"
                >
                  <Trash2 size={15} />
                </button>
              )}
            </div>
          ))}
          {comments.length === 0 && (
            <p className="text-sm text-slate-500 dark:text-slate-400">No comments yet.</p>
          )}
        </div>
      </section>
    </motion.article>
  );
}
