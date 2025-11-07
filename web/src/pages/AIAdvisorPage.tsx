import { useState, useEffect, useRef } from 'react';
import { Menu, Bell, CheckCircle, Sparkles, AlertTriangle, Info, ArrowLeft, Send, RefreshCw, MessageCircle, Loader } from 'lucide-react';
import { apiClient } from '../api/client';
import { Alert, AlertSeverity, AlertType, ChatMessage, MessageRole } from '../types';
import BottomNav from '../components/BottomNav';

export default function AIAdvisorPage() {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      content: "Hello! I'm your AI beekeeping advisor. I can help you with questions about hive management, colony health, pest control, seasonal tasks, and more. What would you like to know?",
      role: MessageRole.ASSISTANT,
      timestamp: new Date(),
    }
  ]);
  const [messageText, setMessageText] = useState('');
  const [showChat, setShowChat] = useState(false);
  const [loadingAlerts, setLoadingAlerts] = useState(false);
  const [loadingChat, setLoadingChat] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadAlerts();
  }, []);

  useEffect(() => {
    if (showChat) {
      scrollToBottom();
    }
  }, [messages, showChat]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadAlerts = async () => {
    try {
      setLoadingAlerts(true);
      setError(null);
      const alertsData = await apiClient.getAdvisorAlerts();
      setAlerts(alertsData as Alert[]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load alerts');
    } finally {
      setLoadingAlerts(false);
    }
  };

  const sendMessage = async () => {
    if (!messageText.trim() || loadingChat) return;

    const userMessage: ChatMessage = {
      id: `user-${Date.now()}`,
      content: messageText,
      role: MessageRole.USER,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setMessageText('');
    setLoadingChat(true);
    setError(null);

    try {
      const response = await apiClient.sendChatMessage(messageText);
      const assistantMessage = response as ChatMessage;
      setMessages((prev) => [...prev, assistantMessage]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send message');
    } finally {
      setLoadingChat(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const getAlertColors = (severity: AlertSeverity) => {
    switch (severity) {
      case AlertSeverity.INFO:
        return { bg: 'bg-blue-500/15', border: 'border-blue-500/50', iconColor: 'text-blue-400' };
      case AlertSeverity.WARNING:
        return { bg: 'bg-status-warning/15', border: 'border-status-warning/50', iconColor: 'text-status-warning' };
      case AlertSeverity.CRITICAL:
        return { bg: 'bg-status-alert/15', border: 'border-status-alert/50', iconColor: 'text-status-alert' };
      default:
        return { bg: 'bg-beekeeper-gold/15', border: 'border-beekeeper-gold/50', iconColor: 'text-beekeeper-gold' };
    }
  };

  const getAlertIcon = (severity: AlertSeverity) => {
    switch (severity) {
      case AlertSeverity.CRITICAL:
        return AlertTriangle;
      case AlertSeverity.WARNING:
        return Info;
      case AlertSeverity.INFO:
        return Info;
      default:
        return Info;
    }
  };

  const formatTime = (date: Date) => {
    const hours = date.getHours();
    const minutes = date.getMinutes();
    const hour = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;
    const ampm = hours < 12 ? 'AM' : 'PM';
    const minute = minutes.toString().padStart(2, '0');
    return `${hour}:${minute} ${ampm}`;
  };

  return (
    <div className="min-h-screen bg-background-dark pb-20">
      {/* Top Bar */}
      <div className="bg-beekeeper-green-dark px-4 py-4 flex items-center justify-between">
        {!showChat ? (
          <>
            <button className="p-2">
              <Menu className="w-6 h-6 text-text-primary" />
            </button>
            <div className="text-center">
              <h1 className="text-xl font-bold text-text-primary">AI Advisor</h1>
              <p className="text-xs text-text-secondary">Smart alerts & expert advice</p>
            </div>
            <button className="p-2" onClick={loadAlerts}>
              <RefreshCw className={`w-6 h-6 text-beekeeper-gold ${loadingAlerts ? 'animate-spin' : ''}`} />
            </button>
          </>
        ) : (
          <>
            <button className="p-2" onClick={() => setShowChat(false)}>
              <ArrowLeft className="w-6 h-6 text-text-primary" />
            </button>
            <div className="text-center">
              <h1 className="text-xl font-bold text-text-primary">AI Beekeeping Advisor</h1>
              <p className="text-xs text-text-secondary">Powered by Claude</p>
            </div>
            <div className="w-10" />
          </>
        )}
      </div>

      {/* Content */}
      {!showChat ? (
        // Alerts View
        <div className="flex flex-col h-[calc(100vh-180px)]">
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {/* Header */}
            <div>
              <h2 className="text-xl font-bold text-text-primary">Active Alerts & Recommendations</h2>
              <p className="text-sm text-text-secondary">Based on your hive data and seasonal patterns</p>
            </div>

            {/* Loading state */}
            {loadingAlerts && (
              <div className="flex items-center justify-center py-12">
                <Loader className="w-12 h-12 text-beekeeper-gold animate-spin" />
              </div>
            )}

            {/* Alerts list */}
            {!loadingAlerts && alerts.length > 0 && (
              <div className="space-y-3">
                {alerts.map((alert) => {
                  const colors = getAlertColors(alert.severity);
                  const Icon = getAlertIcon(alert.severity);

                  return (
                    <div key={alert.id} className={`${colors.bg} border ${colors.border} rounded-xl p-4`}>
                      <div className="flex gap-3">
                        <Icon className={`w-6 h-6 ${colors.iconColor} flex-shrink-0 mt-0.5`} />
                        <div className="flex-1">
                          <h3 className="font-semibold text-text-primary mb-1">{alert.title}</h3>
                          <p className="text-sm text-text-secondary leading-relaxed">{alert.message}</p>
                          {alert.hiveIds && alert.hiveIds.length > 0 && (
                            <div className="flex flex-wrap gap-2 mt-2">
                              {alert.hiveIds.map((hiveId) => (
                                <span
                                  key={hiveId}
                                  className={`text-xs px-2 py-1 rounded ${colors.iconColor} bg-opacity-20`}
                                  style={{ backgroundColor: `${colors.iconColor.replace('text-', 'rgba(var(--'))}20%)` }}
                                >
                                  Hive {hiveId}
                                </span>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {/* Empty state */}
            {!loadingAlerts && alerts.length === 0 && (
              <div className="bg-status-healthy/15 border border-status-healthy/50 rounded-xl p-8 text-center">
                <CheckCircle className="w-12 h-12 text-status-healthy mx-auto mb-3" />
                <h3 className="text-xl font-bold text-status-healthy mb-2">All Good!</h3>
                <p className="text-text-secondary">No active alerts at this time. Your hives are looking good!</p>
              </div>
            )}
          </div>

          {/* Ask AI Expert Button */}
          <div className="p-4 bg-card-background border-t border-beekeeper-green-light">
            <div className="border-t border-beekeeper-green-light mb-4" />
            <button
              onClick={() => setShowChat(true)}
              className="w-full bg-beekeeper-gold text-black px-6 py-4 rounded-xl font-bold flex items-center justify-center gap-2 hover:bg-beekeeper-gold/90 transition-colors"
            >
              <MessageCircle className="w-5 h-5" />
              Ask AI Expert
            </button>
            <p className="text-xs text-text-secondary text-center mt-3">
              Have questions? Chat with our AI beekeeping expert
            </p>
          </div>
        </div>
      ) : (
        // Chat View
        <div className="flex flex-col h-[calc(100vh-180px)]">
          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`flex ${message.role === MessageRole.USER ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[80%] rounded-2xl px-4 py-3 ${
                    message.role === MessageRole.USER
                      ? 'bg-beekeeper-gold text-black rounded-br-sm'
                      : 'bg-card-background text-text-primary rounded-bl-sm'
                  }`}
                >
                  <p className="text-sm leading-relaxed whitespace-pre-wrap">{message.content}</p>
                  <p
                    className={`text-xs mt-2 ${
                      message.role === MessageRole.USER ? 'text-black/60' : 'text-text-secondary'
                    }`}
                  >
                    {formatTime(message.timestamp)}
                  </p>
                </div>
              </div>
            ))}

            {/* Loading indicator */}
            {loadingChat && (
              <div className="flex justify-start">
                <div className="bg-card-background rounded-2xl rounded-bl-sm px-4 py-3 flex items-center gap-2">
                  <Loader className="w-4 h-4 text-beekeeper-gold animate-spin" />
                  <span className="text-sm text-text-secondary">Thinking...</span>
                </div>
              </div>
            )}

            {/* Error message */}
            {error && (
              <div className="bg-status-alert/20 border border-status-alert/50 rounded-xl p-3 flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 text-status-alert flex-shrink-0" />
                <p className="text-sm text-status-alert flex-1">{error}</p>
                <button onClick={() => setError(null)} className="text-status-alert">
                  ✕
                </button>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Input area */}
          <div className="p-4 bg-card-background border-t border-beekeeper-green-light">
            <div className="flex gap-2">
              <textarea
                value={messageText}
                onChange={(e) => setMessageText(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Ask about beekeeping..."
                className="flex-1 bg-background-dark text-text-primary border border-beekeeper-green-light rounded-xl px-4 py-3 resize-none focus:outline-none focus:border-beekeeper-gold placeholder-text-secondary"
                rows={2}
                disabled={loadingChat}
              />
              <button
                onClick={sendMessage}
                disabled={!messageText.trim() || loadingChat}
                className={`w-14 h-14 rounded-full flex items-center justify-center self-end ${
                  !messageText.trim() || loadingChat
                    ? 'bg-beekeeper-green-light text-text-secondary'
                    : 'bg-beekeeper-gold text-black hover:bg-beekeeper-gold/90'
                } transition-colors`}
              >
                <Send className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      <BottomNav />
    </div>
  );
}
