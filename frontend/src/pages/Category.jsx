import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { PostApi, CategoryApi } from "../lib/api";
import PostCard from "../components/PostCard";
import Spinner from "../components/Spinner";

export default function Category() {
  const { id } = useParams();
  const [category, setCategory] = useState(null);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    Promise.all([CategoryApi.get(id), PostApi.byCategory(id)])
      .then(([c, p]) => {
        setCategory(c);
        setPosts(p);
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Spinner />;

  return (
    <div className="space-y-6">
      <Link to="/categories" className="inline-flex items-center gap-1.5 text-sm text-slate-500 hover:text-aurora-violet">
        <ArrowLeft size={15} /> All topics
      </Link>
      <div>
        <h1 className="text-2xl font-bold">{category?.categoryTitle}</h1>
        <p className="mt-1 text-slate-600 dark:text-slate-300">{category?.categoryDescription}</p>
      </div>
      {posts.length === 0 ? (
        <p className="glass p-8 text-center text-slate-500 dark:text-slate-400">
          No posts in this topic yet.
        </p>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {posts.map((p, i) => (
            <PostCard key={p.postId} post={p} index={i} />
          ))}
        </div>
      )}
    </div>
  );
}
