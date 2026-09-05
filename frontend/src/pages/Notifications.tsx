import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button, Card, CardContent, EmptyState } from '@/components/ui';
import { ArrowLeft, Bell, Check, Trash2, Mail, ShoppingBag, Truck, Star, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';

interface Notification {
  id: string;
  type: 'order' | 'payment' | 'delivery' | 'promotion' | 'system';
  title: string;
  message: string;
  createdAt: string;
  isRead: boolean;
  link?: string;
}

const mockNotifications: Notification[] = [
  {
    id: '1',
    type: 'order',
    title: 'Order Confirmed',
    message: 'Your order #ORD-2024-002 has been confirmed and is being prepared.',
    createdAt: '2024-01-16T19:50:00Z',
    isRead: false,
    link: '/orders/ORD-2024-002',
  },
  {
    id: '2',
    type: 'delivery',
    title: 'Out for Delivery',
    message: 'Your order is on the way! Rajesh Kumar will deliver it in 15 minutes.',
    createdAt: '2024-01-16T20:15:00Z',
    isRead: false,
    link: '/orders/ORD-2024-002',
  },
  {
    id: '3',
    type: 'order',
    title: 'Order Delivered',
    message: 'Your order #ORD-2024-001 has been delivered. Enjoy your meal!',
    createdAt: '2024-01-15T13:20:00Z',
    isRead: true,
    link: '/orders/ORD-2024-001',
  },
  {
    id: '4',
    type: 'payment',
    title: 'Payment Successful',
    message: 'Payment of ₹450 for order #ORD-2024-001 was successful.',
    createdAt: '2024-01-15T12:35:00Z',
    isRead: true,
  },
  {
    id: '5',
    type: 'promotion',
    title: 'Special Offer!',
    message: 'Get 30% off on your next order. Use code MEALMESH30. Valid until Feb 1.',
    createdAt: '2024-01-14T10:00:00Z',
    isRead: true,
  },
  {
    id: '6',
    type: 'system',
    title: 'Welcome to MealMesh!',
    message: 'Thank you for joining MealMesh. Get ₹100 off on your first order.',
    createdAt: '2024-01-10T08:00:00Z',
    isRead: true,
  },
];

export function Notifications() {
  const [notifications, setNotifications] = useState<Notification[]>(mockNotifications);
  const [filter, setFilter] = useState<'all' | 'unread'>('all');

  const filteredNotifications = filter === 'unread' 
    ? notifications.filter(n => !n.isRead)
    : notifications;

  const unreadCount = notifications.filter(n => !n.isRead).length;

  const markAsRead = (id: string) => {
    setNotifications(prev => 
      prev.map(n => n.id === id ? { ...n, isRead: true } : n)
    );
  };

  const markAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    toast.success('All notifications marked as read');
  };

  const deleteNotification = (id: string) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
    toast.success('Notification deleted');
  };

  const getIcon = (type: Notification['type']) => {
    switch (type) {
      case 'order': return ShoppingBag;
      case 'payment': return Mail;
      case 'delivery': return Truck;
      case 'promotion': return Star;
      default: return AlertCircle;
    }
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-4xl mx-auto px-4">
          <div className="h-16 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/" className="text-gray-500 hover:text-gray-700">
                <ArrowLeft className="w-6 h-6" />
              </Link>
              <div>
                <h1 className="text-xl font-bold text-gray-900">Notifications</h1>
                <p className="text-sm text-gray-500">{unreadCount} unread</p>
              </div>
            </div>
            {unreadCount > 0 && (
              <Button onClick={markAllAsRead} variant="outline" size="sm">
                <Check className="w-4 h-4 mr-2" />
                Mark all read
              </Button>
            )}
          </div>
        </div>
      </header>

      <div className="max-w-4xl mx-auto px-4 py-6">
        {/* Tabs */}
        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setFilter('all')}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              filter === 'all' 
                ? 'bg-[#F4511E] text-white shadow-sm' 
                : 'bg-white text-gray-700 hover:bg-orange-50/50'
            }`}
          >
            All ({notifications.length})
          </button>
          <button
            onClick={() => setFilter('unread')}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              filter === 'unread' 
                ? 'bg-[#F4511E] text-white shadow-sm' 
                : 'bg-white text-gray-700 hover:bg-orange-50/50'
            }`}
          >
            Unread ({unreadCount})
          </button>
        </div>

        {filteredNotifications.length === 0 ? (
          <Card>
            <CardContent className="py-12">
              <EmptyState
                icon={<Bell className="w-16 h-16 text-gray-300" />}
                title="No notifications"
                description={filter === 'unread' ? "You're all caught up!" : "You don't have any notifications yet."}
              />
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardContent className="p-0">
              {filteredNotifications.map((notification, idx) => {
                const Icon = getIcon(notification.type);
                return (
                  <div
                    key={notification.id}
                    className={`p-4 ${idx !== 0 ? 'border-t border-gray-100' : ''} ${
                      !notification.isRead ? 'bg-orange-50/40' : ''
                    } hover:bg-gray-50/80 transition-colors`}
                  >
                    <div className="flex items-start gap-4">
                      <div className={`w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 ${
                        !notification.isRead ? 'bg-orange-100' : 'bg-gray-100'
                      }`}>
                        <Icon className={`w-5 h-5 ${
                          !notification.isRead ? 'text-[#F4511E]' : 'text-gray-500'
                        }`} />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-2">
                          <div>
                            <div className="flex items-center gap-2">
                              <h3 className={`font-medium ${
                                !notification.isRead ? 'text-gray-900 font-semibold' : 'text-gray-700'
                              }`}>
                                {notification.title}
                              </h3>
                              {!notification.isRead && (
                                <span className="w-2 h-2 bg-[#F4511E] rounded-full" />
                              )}
                            </div>
                            <p className={`text-sm mt-1 ${
                              !notification.isRead ? 'text-gray-700' : 'text-gray-500'
                            }`}>
                              {notification.message}
                            </p>
                            <p className="text-xs text-gray-400 mt-1">
                              {formatTime(notification.createdAt)}
                            </p>
                          </div>
                          <div className="flex items-center gap-1 flex-shrink-0">
                            {!notification.isRead && (
                              <button
                                onClick={() => markAsRead(notification.id)}
                                className="p-1 text-gray-400 hover:text-[#F4511E]"
                                title="Mark as read"
                              >
                                <Check className="w-4 h-4" />
                              </button>
                            )}
                            <button
                              onClick={() => deleteNotification(notification.id)}
                              className="p-1 text-gray-400 hover:text-red-600"
                              title="Delete"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                        {notification.link && (
                          <Link
                            to={notification.link}
                            className="text-sm text-[#F4511E] hover:text-[#E64A19] font-medium mt-2 inline-block"
                          >
                            View details →
                          </Link>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}