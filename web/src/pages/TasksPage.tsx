import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plus,
  Filter,
  CheckCircle,
  Clock,
  AlertCircle,
  Loader,
  X,
  Check,
} from 'lucide-react';
import { apiClient } from '../api/client';
import BottomNav from '../components/BottomNav';
import { Task, TaskStatus, TaskPriority, TaskType } from '../types';
import { formatDistanceToNow } from 'date-fns';

export default function TasksPage() {
  const navigate = useNavigate();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterStatus, setFilterStatus] = useState<TaskStatus | 'ALL'>('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    taskType: TaskType.GENERAL,
    dueDate: '',
    priority: TaskPriority.MEDIUM,
    notes: '',
  });

  useEffect(() => {
    loadTasks();
  }, [filterStatus]);

  const loadTasks = async () => {
    try {
      setLoading(true);
      setError(null);

      let tasksData;
      if (filterStatus === 'ALL') {
        tasksData = await apiClient.getTasks();
      } else {
        tasksData = await apiClient.getTasks({ status: filterStatus });
      }

      // Parse dates
      const parsedTasks = (tasksData as any[]).map((task: any) => ({
        ...task,
        dueDate: new Date(task.dueDate),
        createdAt: new Date(task.createdAt),
        updatedAt: new Date(task.updatedAt),
        completedDate: task.completedDate ? new Date(task.completedDate) : undefined,
        reminderDate: task.reminderDate ? new Date(task.reminderDate) : undefined,
      }));

      setTasks(parsedTasks);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load tasks');
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteTask = async (taskId: string) => {
    try {
      await apiClient.completeTask(taskId);
      loadTasks(); // Reload tasks
    } catch (err) {
      console.error('Failed to complete task:', err);
    }
  };

  const handleDeleteTask = async (taskId: string) => {
    if (!confirm('Are you sure you want to delete this task?')) return;

    try {
      await apiClient.deleteTask(taskId);
      loadTasks(); // Reload tasks
    } catch (err) {
      console.error('Failed to delete task:', err);
    }
  };

  const handleCreateTask = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await apiClient.createTask({
        ...newTask,
        dueDate: new Date(newTask.dueDate).toISOString(),
      });
      setShowCreateModal(false);
      setNewTask({
        title: '',
        description: '',
        taskType: TaskType.GENERAL,
        dueDate: '',
        priority: TaskPriority.MEDIUM,
        notes: '',
      });
      loadTasks();
    } catch (err) {
      console.error('Failed to create task:', err);
      alert('Failed to create task');
    }
  };

  const getStatusColor = (task: Task) => {
    if (task.status === TaskStatus.COMPLETED) {
      return 'border-status-healthy bg-status-healthy/10';
    }
    if (task.status === TaskStatus.OVERDUE) {
      return 'border-status-alert bg-status-alert/10';
    }
    if (task.status === TaskStatus.IN_PROGRESS) {
      return 'border-status-warning bg-status-warning/10';
    }
    return 'border-card-border bg-card-dark';
  };

  const getStatusIcon = (task: Task) => {
    if (task.status === TaskStatus.COMPLETED) {
      return <CheckCircle className="w-5 h-5 text-status-healthy" />;
    }
    if (task.status === TaskStatus.OVERDUE) {
      return <AlertCircle className="w-5 h-5 text-status-alert" />;
    }
    if (task.status === TaskStatus.IN_PROGRESS) {
      return <Clock className="w-5 h-5 text-status-warning" />;
    }
    return <Calendar className="w-5 h-5 text-text-secondary" />;
  };

  const getPriorityColor = (priority: TaskPriority) => {
    switch (priority) {
      case TaskPriority.URGENT:
        return 'text-status-alert';
      case TaskPriority.HIGH:
        return 'text-status-warning';
      case TaskPriority.MEDIUM:
        return 'text-beekeeper-gold';
      case TaskPriority.LOW:
        return 'text-text-secondary';
      default:
        return 'text-text-secondary';
    }
  };

  const formatTaskType = (type: TaskType) => {
    return type.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  };

  const formatDueDate = (date: Date) => {
    const now = new Date();
    const isOverdue = date < now;
    const distance = formatDistanceToNow(date, { addSuffix: true });

    return (
      <span className={isOverdue ? 'text-status-alert' : 'text-text-secondary'}>
        {distance}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background-dark flex items-center justify-center">
        <Loader className="w-8 h-8 text-beekeeper-gold animate-spin" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-background-dark flex items-center justify-center p-4">
        <div className="bg-status-alert/20 border border-status-alert/50 rounded-xl p-6 max-w-md">
          <div className="flex items-center gap-2 mb-4">
            <AlertCircle className="w-6 h-6 text-status-alert" />
            <p className="text-text-primary font-semibold">{error}</p>
          </div>
          <button
            onClick={loadTasks}
            className="w-full bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background-dark pb-20">
      {/* Header */}
      <div className="bg-card-dark border-b border-card-border p-4">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <h1 className="text-2xl font-bold text-text-primary">My Tasks</h1>
          <button
            onClick={() => setShowCreateModal(true)}
            className="bg-beekeeper-gold text-black p-2 rounded-full hover:bg-beekeeper-gold/90 transition"
          >
            <Plus className="w-6 h-6" />
          </button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="bg-card-dark border-b border-card-border">
        <div className="max-w-6xl mx-auto flex overflow-x-auto">
          {['ALL', TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.OVERDUE, TaskStatus.COMPLETED].map((status) => (
            <button
              key={status}
              onClick={() => setFilterStatus(status as TaskStatus | 'ALL')}
              className={`px-6 py-3 font-medium transition whitespace-nowrap ${
                filterStatus === status
                  ? 'text-beekeeper-gold border-b-2 border-beekeeper-gold'
                  : 'text-text-secondary hover:text-text-primary'
              }`}
            >
              {status === 'ALL' ? 'All' : status.replace(/_/g, ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Tasks List */}
      <div className="max-w-6xl mx-auto p-4 space-y-3">
        {tasks.length === 0 ? (
          <div className="bg-card-dark border border-card-border rounded-xl p-8 text-center">
            <Calendar className="w-12 h-12 text-text-secondary mx-auto mb-4" />
            <p className="text-text-secondary">No tasks found</p>
            <button
              onClick={() => setShowCreateModal(true)}
              className="mt-4 bg-beekeeper-gold text-black px-6 py-2 rounded-lg font-medium"
            >
              Create Your First Task
            </button>
          </div>
        ) : (
          tasks.map((task) => (
            <div
              key={task.id}
              className={`border-l-4 ${getStatusColor(task)} rounded-r-xl overflow-hidden transition hover:shadow-lg`}
            >
              <div className="p-4">
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-start gap-3 flex-1">
                    <div className="mt-1">{getStatusIcon(task)}</div>
                    <div className="flex-1">
                      <h3 className="text-text-primary font-semibold text-lg">
                        {task.title}
                      </h3>
                      {task.description && (
                        <p className="text-text-secondary text-sm mt-1">
                          {task.description}
                        </p>
                      )}
                      <div className="flex flex-wrap items-center gap-4 mt-2 text-sm">
                        <span className="text-text-secondary">
                          {formatTaskType(task.taskType)}
                        </span>
                        <span className={getPriorityColor(task.priority)}>
                          {task.priority} Priority
                        </span>
                        <span className="text-text-secondary">
                          Due {formatDueDate(task.dueDate)}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2 ml-4">
                    {task.status !== TaskStatus.COMPLETED && (
                      <button
                        onClick={() => handleCompleteTask(task.id)}
                        className="p-2 bg-status-healthy/20 text-status-healthy rounded-lg hover:bg-status-healthy/30 transition"
                        title="Mark as complete"
                      >
                        <Check className="w-5 h-5" />
                      </button>
                    )}
                    <button
                      onClick={() => handleDeleteTask(task.id)}
                      className="p-2 bg-status-alert/20 text-status-alert rounded-lg hover:bg-status-alert/30 transition"
                      title="Delete task"
                    >
                      <X className="w-5 h-5" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Create Task Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-50">
          <div className="bg-card-dark border border-card-border rounded-xl max-w-2xl w-full p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-text-primary">Create New Task</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-text-secondary hover:text-text-primary"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            <form onSubmit={handleCreateTask} className="space-y-4">
              <div>
                <label className="block text-text-primary font-medium mb-2">
                  Title *
                </label>
                <input
                  type="text"
                  required
                  value={newTask.title}
                  onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
                  className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary focus:outline-none focus:ring-2 focus:ring-beekeeper-gold"
                  placeholder="Enter task title"
                />
              </div>

              <div>
                <label className="block text-text-primary font-medium mb-2">
                  Description
                </label>
                <textarea
                  value={newTask.description}
                  onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
                  className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary focus:outline-none focus:ring-2 focus:ring-beekeeper-gold h-24"
                  placeholder="Enter task description"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-text-primary font-medium mb-2">
                    Task Type *
                  </label>
                  <select
                    required
                    value={newTask.taskType}
                    onChange={(e) => setNewTask({ ...newTask, taskType: e.target.value as TaskType })}
                    className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary focus:outline-none focus:ring-2 focus:ring-beekeeper-gold"
                  >
                    {Object.values(TaskType).map((type) => (
                      <option key={type} value={type}>
                        {formatTaskType(type)}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-text-primary font-medium mb-2">
                    Priority *
                  </label>
                  <select
                    required
                    value={newTask.priority}
                    onChange={(e) => setNewTask({ ...newTask, priority: e.target.value as TaskPriority })}
                    className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary focus:outline-none focus:ring-2 focus:ring-beekeeper-gold"
                  >
                    {Object.values(TaskPriority).map((priority) => (
                      <option key={priority} value={priority}>
                        {priority}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-text-primary font-medium mb-2">
                  Due Date *
                </label>
                <input
                  type="datetime-local"
                  required
                  value={newTask.dueDate}
                  onChange={(e) => setNewTask({ ...newTask, dueDate: e.target.value })}
                  className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary focus:outline-none focus:ring-2 focus:ring-beekeeper-gold"
                />
              </div>

              <div>
                <label className="block text-text-primary font-medium mb-2">
                  Notes
                </label>
                <textarea
                  value={newTask.notes}
                  onChange={(e) => setNewTask({ ...newTask, notes: e.target.value })}
                  className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary focus:outline-none focus:ring-2 focus:ring-beekeeper-gold h-24"
                  placeholder="Additional notes"
                />
              </div>

              <div className="flex gap-4 pt-4">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 bg-card-border text-text-primary px-6 py-3 rounded-lg font-medium hover:bg-card-border/80 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 bg-beekeeper-gold text-black px-6 py-3 rounded-lg font-medium hover:bg-beekeeper-gold/90 transition"
                >
                  Create Task
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <BottomNav />
    </div>
  );
}
