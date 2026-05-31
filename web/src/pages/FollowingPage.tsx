import { useState, useEffect } from 'react';
import {
  Search,
  Users,
  UserPlus,
  UserMinus,
  Calendar,
  Bug,
  Loader,
  AlertCircle,
  Rss,
  Thermometer,
  ClipboardList,
  Flag,
} from 'lucide-react';
import { apiClient } from '../api/client';
import BottomNav from '../components/BottomNav';
import { UserSummary, FeedItem } from '../types';
import { format, formatDistanceToNow } from 'date-fns';

export default function FollowingPage() {
  const [following, setFollowing] = useState<UserSummary[]>([]);
  const [feed, setFeed] = useState<FeedItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [query, setQuery] = useState('');
  const [searchResults, setSearchResults] = useState<UserSummary[]>([]);
  const [searching, setSearching] = useState(false);
  const [busyId, setBusyId] = useState<string | null>(null);

  const FEED_PAGE_SIZE = 30;
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(false);

  useEffect(() => {
    loadFollowingAndFeed();
  }, []);

  const loadFollowingAndFeed = async () => {
    try {
      setLoading(true);
      setError(null);
      const [followingData, feedData] = await Promise.all([
        apiClient.getFollowing() as Promise<UserSummary[]>,
        apiClient.getFeed({ limit: FEED_PAGE_SIZE }) as Promise<FeedItem[]>,
      ]);
      setFollowing(followingData);
      setFeed(feedData);
      setHasMore(feedData.length === FEED_PAGE_SIZE);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load feed');
    } finally {
      setLoading(false);
    }
  };

  const handleLoadMore = async () => {
    if (feed.length === 0) return;
    const lastOccurredAt = feed[feed.length - 1].occurredAt;
    try {
      setLoadingMore(true);
      const more = (await apiClient.getFeed({
        limit: FEED_PAGE_SIZE,
        before: lastOccurredAt,
      })) as FeedItem[];
      setFeed((prev) => [...prev, ...more]);
      setHasMore(more.length === FEED_PAGE_SIZE);
    } catch (err) {
      console.error('Load more failed:', err);
    } finally {
      setLoadingMore(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }
    try {
      setSearching(true);
      const results = (await apiClient.searchUsers(query.trim())) as UserSummary[];
      setSearchResults(results);
    } catch (err) {
      console.error('Search failed:', err);
    } finally {
      setSearching(false);
    }
  };

  const followingIds = new Set(following.map((u) => u.id));

  const handleFollow = async (userId: string) => {
    try {
      setBusyId(userId);
      await apiClient.followUser(userId);
      await loadFollowingAndFeed();
    } catch (err) {
      console.error('Follow failed:', err);
    } finally {
      setBusyId(null);
    }
  };

  const handleUnfollow = async (userId: string) => {
    try {
      setBusyId(userId);
      await apiClient.unfollowUser(userId);
      await loadFollowingAndFeed();
    } catch (err) {
      console.error('Unfollow failed:', err);
    } finally {
      setBusyId(null);
    }
  };

  const formatEnum = (value?: string) =>
    value
      ? value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (l) => l.toUpperCase())
      : '';

  if (loading) {
    return (
      <div className="min-h-screen bg-background-dark flex items-center justify-center">
        <Loader className="w-8 h-8 text-beekeeper-gold animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background-dark pb-20">
      {/* Header */}
      <div className="bg-card-dark border-b border-card-border p-4">
        <div className="max-w-6xl mx-auto flex items-center gap-2">
          <Rss className="w-6 h-6 text-beekeeper-gold" />
          <h1 className="text-2xl font-bold text-text-primary">Following</h1>
        </div>
      </div>

      <div className="max-w-6xl mx-auto p-4 space-y-6">
        {/* Search */}
        <div className="bg-card-dark border border-card-border rounded-xl p-4">
          <form onSubmit={handleSearch} className="flex gap-2">
            <div className="flex-1 relative">
              <Search className="w-5 h-5 text-text-secondary absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Find beekeepers by name…"
                className="w-full bg-background-dark border border-card-border rounded-lg pl-10 pr-4 py-2 text-text-primary"
              />
            </div>
            <button
              type="submit"
              className="bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium hover:bg-beekeeper-gold/90 transition"
            >
              {searching ? 'Searching…' : 'Search'}
            </button>
          </form>

          {searchResults.length > 0 && (
            <div className="mt-4 space-y-2">
              {searchResults.map((u) => {
                const isFollowing = followingIds.has(u.id);
                return (
                  <div
                    key={u.id}
                    className="flex items-center justify-between bg-background-dark rounded-lg px-4 py-3"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 bg-beekeeper-gold rounded-full flex items-center justify-center">
                        <Users className="w-5 h-5 text-black" />
                      </div>
                      <span className="text-text-primary font-medium">{u.fullName}</span>
                    </div>
                    <button
                      onClick={() => (isFollowing ? handleUnfollow(u.id) : handleFollow(u.id))}
                      disabled={busyId === u.id}
                      className={`flex items-center gap-1 px-3 py-1.5 rounded-lg text-sm font-medium transition ${
                        isFollowing
                          ? 'bg-card-border text-text-primary hover:bg-card-border/80'
                          : 'bg-beekeeper-gold text-black hover:bg-beekeeper-gold/90'
                      }`}
                    >
                      {isFollowing ? (
                        <>
                          <UserMinus className="w-4 h-4" /> Unfollow
                        </>
                      ) : (
                        <>
                          <UserPlus className="w-4 h-4" /> Follow
                        </>
                      )}
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Who you follow */}
        {following.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {following.map((u) => (
              <button
                key={u.id}
                onClick={() => handleUnfollow(u.id)}
                disabled={busyId === u.id}
                title="Click to unfollow"
                className="flex items-center gap-1.5 bg-card-dark border border-card-border rounded-full pl-3 pr-2 py-1.5 text-sm text-text-primary hover:border-status-alert/50 group transition"
              >
                <Users className="w-4 h-4 text-beekeeper-gold" />
                {u.fullName}
                <UserMinus className="w-3.5 h-3.5 text-text-secondary group-hover:text-status-alert" />
              </button>
            ))}
          </div>
        )}

        {/* Feed */}
        {error ? (
          <div className="bg-status-alert/20 border border-status-alert/50 rounded-xl p-6">
            <div className="flex items-center gap-2 mb-4">
              <AlertCircle className="w-6 h-6 text-status-alert" />
              <p className="text-text-primary font-semibold">{error}</p>
            </div>
            <button
              onClick={loadFollowingAndFeed}
              className="bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium"
            >
              Retry
            </button>
          </div>
        ) : feed.length === 0 ? (
          <div className="bg-card-dark border border-card-border rounded-xl p-8 text-center">
            <Rss className="w-12 h-12 text-text-secondary mx-auto mb-4" />
            <p className="text-text-secondary">
              {following.length === 0
                ? 'Follow other beekeepers to see what they’re doing.'
                : 'No recent activity from the beekeepers you follow.'}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {feed.map((item, idx) => {
              const when = new Date(item.occurredAt);
              const insp = item.inspection;
              const task = item.task;
              return (
                <div
                  key={`${item.type}-${insp?.id ?? task?.id ?? idx}`}
                  className="bg-card-dark border border-card-border rounded-xl p-4"
                >
                  {/* Author + when */}
                  <div className="flex items-center gap-3 mb-3">
                    <div className="w-9 h-9 bg-beekeeper-gold rounded-full flex items-center justify-center">
                      {item.type === 'task' ? (
                        <ClipboardList className="w-5 h-5 text-black" />
                      ) : (
                        <Users className="w-5 h-5 text-black" />
                      )}
                    </div>
                    <div>
                      <p className="text-text-primary font-semibold leading-tight">
                        {item.author.fullName}
                      </p>
                      <p className="text-xs text-text-secondary">
                        {item.type === 'task' ? (
                          <>scheduled ▸ {task?.title}</>
                        ) : (
                          <>inspected a hive</>
                        )}{' '}
                        · {format(when, 'MMM dd, yyyy')} (
                        {formatDistanceToNow(when, { addSuffix: true })})
                      </p>
                    </div>
                  </div>

                  {item.type === 'inspection' && insp && (
                    <>
                      {/* Tags — the actionable signal for followers */}
                      <div className="flex flex-wrap gap-2">
                        {insp.treatmentApplied && (
                          <span className="px-2 py-1 bg-status-warning/20 text-status-warning text-xs rounded">
                            Treatment{insp.treatmentNotes ? `: ${insp.treatmentNotes}` : ' applied'}
                          </span>
                        )}
                        {insp.feedingDone && (
                          <span className="px-2 py-1 bg-status-healthy/20 text-status-healthy text-xs rounded">
                            Fed{insp.feedingNotes ? `: ${insp.feedingNotes}` : ''}
                          </span>
                        )}
                        {insp.varroaMitesDetected && (
                          <span className="px-2 py-1 bg-status-alert/20 text-status-alert text-xs rounded flex items-center gap-1">
                            <Bug className="w-3 h-3" /> Varroa detected
                          </span>
                        )}
                        {insp.broodPattern && (
                          <span className="px-2 py-1 bg-beekeeper-gold/20 text-beekeeper-gold text-xs rounded">
                            Brood: {formatEnum(insp.broodPattern)}
                          </span>
                        )}
                      </div>

                      {/* Weather context */}
                      {insp.weatherTemp != null && (
                        <div className="flex items-center gap-2 text-sm text-text-secondary mt-3">
                          <Thermometer className="w-4 h-4" />
                          <span>{insp.weatherTemp}°F</span>
                          {insp.weatherConditions && <span>· {insp.weatherConditions}</span>}
                        </div>
                      )}

                      {insp.notes && (
                        <div className="mt-3 p-3 bg-background-dark rounded-lg">
                          <p className="text-sm text-text-secondary">{insp.notes}</p>
                        </div>
                      )}

                      {insp.nextInspectionDate && (
                        <div className="mt-3 flex items-center gap-2 text-sm text-beekeeper-gold">
                          <Calendar className="w-4 h-4" />
                          <span>Plans to return {formatDistanceToNow(new Date(insp.nextInspectionDate), { addSuffix: true })}</span>
                        </div>
                      )}
                    </>
                  )}

                  {item.type === 'task' && task && (
                    <>
                      {/* Tags — type, due, priority */}
                      <div className="flex flex-wrap gap-2">
                        <span className="px-2 py-1 bg-beekeeper-gold/20 text-beekeeper-gold text-xs rounded">
                          {formatEnum(task.taskType)}
                        </span>
                        <span className="px-2 py-1 bg-background-dark text-text-secondary text-xs rounded flex items-center gap-1">
                          <Flag className="w-3 h-3" /> {formatEnum(task.priority)}
                        </span>
                        <span className="px-2 py-1 bg-background-dark text-text-secondary text-xs rounded flex items-center gap-1">
                          <Calendar className="w-3 h-3" /> Due {format(when, 'MMM dd, yyyy')}
                        </span>
                      </div>

                      {task.description && (
                        <div className="mt-3 p-3 bg-background-dark rounded-lg">
                          <p className="text-sm text-text-secondary">{task.description}</p>
                        </div>
                      )}
                    </>
                  )}
                </div>
              );
            })}

            {hasMore && (
              <button
                onClick={handleLoadMore}
                disabled={loadingMore}
                className="w-full bg-card-dark border border-card-border rounded-xl py-3 text-sm font-medium text-beekeeper-gold hover:bg-card-border/30 transition disabled:opacity-60"
              >
                {loadingMore ? 'Loading…' : 'Load more'}
              </button>
            )}
          </div>
        )}
      </div>

      <BottomNav />
    </div>
  );
}
