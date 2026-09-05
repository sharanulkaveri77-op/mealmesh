import { Link } from 'react-router-dom';
import { Button, Card, CardContent, Badge, EmptyState } from '@/components/ui';
import { Clock, MapPin, ChevronRight, ArrowLeft, TrendingUp } from 'lucide-react';
import type { Order, OrderStatus } from '@/types';

const mockOrders: Order[] = [
  {
    id: 'ORD-2024-001',
    customerId: '1',
    restaurantId: '1',
    items: [],
    status: 'DELIVERED',
    totalAmount: 450,
    deliveryAddress: '123 Main Street, Downtown',
    createdAt: '2024-01-15T12:30:00Z',
    updatedAt: '2024-01-15T13:15:00Z',
  },
  {
    id: 'ORD-2024-002',
    customerId: '1',
    restaurantId: '2',
    items: [],
    status: 'OUT_FOR_DELIVERY',
    totalAmount: 320,
    deliveryAddress: '123 Main Street, Downtown',
    createdAt: '2024-01-16T19:45:00Z',
    updatedAt: '2024-01-16T20:10:00Z',
  },
  {
    id: 'ORD-2024-003',
    customerId: '1',
    restaurantId: '3',
    items: [],
    status: 'PREPARING',
    totalAmount: 580,
    deliveryAddress: '123 Main Street, Downtown',
    createdAt: '2024-01-17T13:00:00Z',
    updatedAt: '2024-01-17T13:05:00Z',
  },
  {
    id: 'ORD-2024-004',
    customerId: '1',
    restaurantId: '1',
    items: [],
    status: 'CANCELLED',
    totalAmount: 280,
    deliveryAddress: '123 Main Street, Downtown',
    createdAt: '2024-01-14T20:00:00Z',
    updatedAt: '2024-01-14T20:10:00Z',
  },
];

const statusColors: Record<OrderStatus, string> = {
  CREATED: 'bg-gray-100 text-gray-800',
  PAYMENT_PENDING: 'bg-yellow-100 text-yellow-800',
  PAYMENT_CONFIRMED: 'bg-orange-100 text-orange-800',
  RESTAURANT_PENDING: 'bg-purple-100 text-purple-800',
  RESTAURANT_ACCEPTED: 'bg-orange-100 text-orange-800',
  PREPARING: 'bg-amber-100 text-amber-800',
  READY_FOR_PICKUP: 'bg-amber-100 text-amber-800',
  DELIVERY_PARTNER_ASSIGNED: 'bg-teal-100 text-teal-800',
  PICKED_UP: 'bg-cyan-100 text-cyan-800',
  OUT_FOR_DELIVERY: 'bg-orange-100 text-orange-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
  PAYMENT_FAILED: 'bg-red-100 text-red-800',
  RESTAURANT_REJECTED: 'bg-red-100 text-red-800',
};

const statusLabels: Record<OrderStatus, string> = {
  CREATED: 'Order Created',
  PAYMENT_PENDING: 'Payment Pending',
  PAYMENT_CONFIRMED: 'Payment Confirmed',
  RESTAURANT_PENDING: 'Restaurant Notified',
  RESTAURANT_ACCEPTED: 'Restaurant Accepted',
  PREPARING: 'Preparing',
  READY_FOR_PICKUP: 'Ready for Pickup',
  DELIVERY_PARTNER_ASSIGNED: 'Partner Assigned',
  PICKED_UP: 'Picked Up',
  OUT_FOR_DELIVERY: 'Out for Delivery',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
  PAYMENT_FAILED: 'Payment Failed',
  RESTAURANT_REJECTED: 'Rejected',
};

const statusOrder: OrderStatus[] = [
  'CREATED', 'PAYMENT_PENDING', 'PAYMENT_CONFIRMED', 'RESTAURANT_PENDING', 
  'RESTAURANT_ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'DELIVERY_PARTNER_ASSIGNED',
  'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED'
];

export function Orders() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4">
          <div className="h-16 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/" className="text-gray-500 hover:text-gray-700">
                <ArrowLeft className="w-6 h-6" />
              </Link>
              <h1 className="text-xl font-bold text-gray-900">My Orders</h1>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {mockOrders.length === 0 ? (
          <div className="max-w-2xl mx-auto">
            <EmptyState
              icon={
                <svg className="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                </svg>
              }
              title="No orders yet"
              description="When you place orders, they'll appear here with real-time tracking."
              action={<Link to="/restaurants"><Button>Explore Restaurants</Button></Link>}
            />
          </div>
        ) : (
          <div className="space-y-6">
            {mockOrders.map((order) => (
              <Link key={order.id} to={`/orders/${order.id}`} className="block">
                <Card className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                      <div className="flex items-start gap-4">
                        <div className="w-16 h-16 bg-orange-50 rounded-xl flex-shrink-0 flex items-center justify-center">
                          <TrendingUp className="w-8 h-8 text-[#F4511E]" />
                        </div>
                        <div>
                          <div className="flex items-center gap-3 mb-1">
                            <span className="font-semibold text-gray-900">{order.id}</span>
                            <Badge className={statusColors[order.status]}>
                              {statusLabels[order.status]}
                            </Badge>
                          </div>
                          <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500">
                            <span className="flex items-center gap-1">
                              <Clock className="w-4 h-4" />
                              {new Date(order.createdAt).toLocaleDateString('en-IN', { 
                                day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' 
                              })}
                            </span>
                            <span className="flex items-center gap-1">
                              <MapPin className="w-4 h-4" />
                              ₹{order.totalAmount}
                            </span>
                            {order.status !== 'DELIVERED' && order.status !== 'CANCELLED' && (
                              <span className="flex items-center gap-1 text-[#F4511E] font-semibold">
                                <TrendingUp className="w-4 h-4" />
                                Track Order
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <ChevronRight className="w-5 h-5 text-gray-400" />
                      </div>
                    </div>
                    
                    {/* Progress Tracker for active orders */}
                    {order.status !== 'DELIVERED' && order.status !== 'CANCELLED' && (
                      <div className="mt-4 pt-4 border-t border-gray-100">
                        <div className="flex items-center">
                          {statusOrder.slice(0, statusOrder.indexOf(order.status) + 1).map((status, idx) => (
                            <div key={status} className="flex items-center">
                              <div className={`w-3 h-3 rounded-full ${idx <= statusOrder.indexOf(order.status) ? 'bg-[#F4511E]' : 'bg-gray-200'}`} />
                              {idx < statusOrder.indexOf(order.status) && (
                                <div className="flex-1 h-0.5 bg-[#F4511E]" />
                              )}
                              {idx === statusOrder.indexOf(order.status) && (
                                <div className="flex-1 h-0.5 bg-gray-200" />
                              )}
                            </div>
                          ))}
                        </div>
                        <div className="flex justify-between mt-2 text-xs text-gray-500">
                          {statusOrder.slice(0, statusOrder.indexOf(order.status) + 1).map((status, idx) => (
                            <span key={status} className={`w-20 text-center ${idx === statusOrder.indexOf(order.status) ? 'font-semibold text-[#F4511E]' : ''}`}>
                              {statusLabels[status].split(' ').slice(-1)[0]}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}