import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { motion } from "framer-motion";
import { Sparkles, ImagePlus, Plus, Save } from "lucide-react";
import { PostApi, CategoryApi } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import Spinner from "../components/Spinner";

export default function Editor() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const { user } = useAuth();

  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ title: "", content: "", categoryId: "" });
  const [file, setFile] = useState(null);
  const [imageName, setImageName] = useState(null);
  const [loading, setLoading] = useState(editing);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  // inline category creation
  const [newCat, setNewCat] = useState(false);
  const [cat, setCat] = useState({ categoryTitle: "", categoryDescription: "" });

  useEffect(() => {
    (async () => {
      const cats = await CategoryApi.list();
      setCategories(cats);
      if (editing) {
        try {
          const post = await PostApi.get(id);
          setForm({ title: post.title, content: post.content, categoryId: post.category?.categoryId || "" });
          setImageName(post.imageName);
        } catch {
          setError("Could not load this post.");
        }
      } else if (cats.length) {
        setForm((f) => ({ ...f, categoryId: cats[0].categoryId }));
      }
      setLoading(false);
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const createCategory = async () => {
    if (!cat.categoryTitle.trim() || cat.categoryDescription.trim().length < 6) {
      setError("Category needs a title and a description of at least 6 characters.");
      return;
    }
    setError("");
    const created = await CategoryApi.create(cat);
    setCategories((c) => [...c, created]);
    setForm((f) => ({ ...f, categoryId: created.categoryId }));
    setNewCat(false);
    setCat({ categoryTitle: "", categoryDescription: "" });
  };

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      let post;
      if (editing) {
        post = await PostApi.update(id, { title: form.title, content: form.content, imageName });
      } else {
        post = await PostApi.create(user.id, form.categoryId, {
          title: form.title,
          content: form.content,
        });
      }
      if (file) {
        post = await PostApi.uploadImage(post.postId, file);
      }
      navigate(`/posts/${post.postId}`);
    } catch (err) {
      setError(err.response?.data?.message || "Could not save the post.");
      setBusy(false);
    }
  };

  if (loading) return <Spinner />;

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      className="mx-auto max-w-2xl"
    >
      <h1 className="mb-6 text-2xl font-bold">{editing ? "Edit post" : "Write a new post"}</h1>

      {error && (
        <div className="mb-4 rounded-xl border border-red-400/40 bg-red-500/10 px-4 py-2.5 text-sm text-red-500">
          {error}
        </div>
      )}

      <form onSubmit={submit} className="glass-strong space-y-5 p-6">
        <div>
          <label className="mb-1.5 block text-sm font-medium">Title</label>
          <input
            required
            className="input"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            placeholder="A catchy headline"
          />
        </div>

        {!editing && (
          <div>
            <div className="mb-1.5 flex items-center justify-between">
              <label className="text-sm font-medium">Category</label>
              <button
                type="button"
                onClick={() => setNewCat((v) => !v)}
                className="inline-flex items-center gap-1 text-xs text-aurora-violet hover:underline"
              >
                <Plus size={13} /> New category
              </button>
            </div>
            {newCat ? (
              <div className="space-y-2 rounded-xl border border-dashed border-aurora-violet/40 p-3">
                <input
                  className="input"
                  placeholder="Category title"
                  value={cat.categoryTitle}
                  onChange={(e) => setCat({ ...cat, categoryTitle: e.target.value })}
                />
                <input
                  className="input"
                  placeholder="Short description"
                  value={cat.categoryDescription}
                  onChange={(e) => setCat({ ...cat, categoryDescription: e.target.value })}
                />
                <button type="button" onClick={createCategory} className="btn-primary w-full">
                  Add category
                </button>
              </div>
            ) : categories.length ? (
              <select
                className="input"
                value={form.categoryId}
                onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                required
              >
                {categories.map((c) => (
                  <option key={c.categoryId} value={c.categoryId}>
                    {c.categoryTitle}
                  </option>
                ))}
              </select>
            ) : (
              <p className="text-sm text-slate-500 dark:text-slate-400">
                No categories yet — create one above.
              </p>
            )}
          </div>
        )}

        <div>
          <label className="mb-1.5 block text-sm font-medium">Content</label>
          <textarea
            required
            rows={12}
            className="input resize-y leading-relaxed"
            value={form.content}
            onChange={(e) => setForm({ ...form, content: e.target.value })}
            placeholder="Write your story…"
          />
        </div>

        <div>
          <label className="mb-1.5 block text-sm font-medium">Cover image (optional)</label>
          <label className="flex cursor-pointer items-center gap-2 rounded-xl border border-dashed border-slate-300 px-4 py-3 text-sm text-slate-500 transition hover:border-aurora-violet dark:border-white/15">
            <ImagePlus size={16} />
            {file ? file.name : "Choose an image…"}
            <input
              type="file"
              accept="image/*"
              className="hidden"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
            />
          </label>
        </div>

        <div className="flex items-center justify-between gap-3 pt-2">
          <p className="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
            <Sparkles size={13} className="text-aurora-violet" />
            Tip: after publishing, generate an AI summary from the post page.
          </p>
          <button type="submit" disabled={busy || (!editing && !form.categoryId)} className="btn-primary">
            <Save size={16} /> {busy ? "Saving…" : editing ? "Save changes" : "Publish"}
          </button>
        </div>
      </form>
    </motion.div>
  );
}
