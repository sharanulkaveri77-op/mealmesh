import { useParams, Link } from 'react-router-dom';
import { Button, Card, CardHeader, CardContent, Badge } from '@/components/ui';
import { MapPin, Star, ArrowLeft, Truck, Check, Circle, User, Shield, CreditCard } from 'lucide-react';
import type { Order, OrderStatus } from '@/types';

const mockOrder: Order = {
  id: 'ORD-2024-002',
  customerId: '1',
  restaurantId: '2',
  items: [
    { id: '1', menuItem: { id: '1', restaurantId: '2', categoryId: '1', name: 'Margherita Pizza', description: '', price: 280, imageUrl: undefined, isVegetarian: true, isAvailable: true }, quantity: 1 },
    { id: '2', menuItem: { id: '2', restaurantId: '2', categoryId: '1', name: 'Garlic Bread', description: '', price: 120, imageUrl: undefined, isVegetarian: true, isAvailable: true }, quantity: 1 },
  ],
  status: 'OUT_FOR_DELIVERY',
  totalAmount: 400,
  deliveryAddress: '123 Main Street, Downtown, Mumbai 400001',
  createdAt: '2024-01-16T19:45:00Z',
  updatedAt: '2024-01-16T20:10:00Z',
};

const restaurant = {
  id: '2',
  name: 'Pizza Palace',
  address: '456 Oak Avenue, Midtown',
  phone: '+91 98765 43211',
  rating: 4.3,
  imageUrl: undefined,
};

const partner = {
  id: 'DP-001',
  name: 'Rajesh Kumar',
  phone: '+91 98765 12345',
  rating: 4.8,
  vehicle: 'Bike - MH01AB1234',
  location: { lat: 19.0760, lng: 72.8777 },
};

const statusOrder: OrderStatus[] = [
  'CREATED', 'PAYMENT_PENDING', 'PAYMENT_CONFIRMED', 'RESTAURANT_PENDING', 
  'RESTAURANT_ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'DELIVERY_PARTNER_ASSIGNED',
  'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED'
];

const statusLabels: Record<OrderStatus, string> = {
  CREATED: 'Order Placed',
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
  RESTAURANT_REJECTED: 'Restaurant Rejected',
};

const getStatusBadgeColor = (status: OrderStatus) => {
  if (['CREATED', 'PAYMENT_PENDING', 'PAYMENT_CONFIRMED', 'RESTAURANT_PENDING'].includes(status)) return 'bg-blue-100 text-blue-800';
  if (['RESTAURANT_ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP'].includes(status)) return 'bg-orange-100 text-orange-800';
  if (['DELIVERY_PARTNER_ASSIGNED', 'PICKED_UP'].includes(status)) return 'bg-teal-100 text-teal-800';
  if (['OUT_FOR_DELIVERY'].includes(status)) return 'bg-pink-100 text-pink-800';
  if (['DELIVERED'].includes(status)) return 'bg-green-100 text-green-800';
  if (['CANCELLED', 'PAYMENT_FAILED', 'RESTAURANT_REJECTED'].includes(status)) return 'bg-red-100 text-red-800';
  return 'bg-gray-100 text-gray-800';
};

export function OrderDetail() {
  const { id } = useParams<{ id: string }>();
  const orderId = id || mockOrder.id;
  const currentStatusIndex = statusOrder.indexOf(mockOrder.status);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4">
          <div className="h-16 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/orders" className="text-gray-500 hover:text-gray-700">
                <ArrowLeft className="w-6 h-6" />
              </Link>
              <div>
                <h1 className="text-xl font-bold text-gray-900">Order Details</h1>
                <p className="text-sm text-gray-500">{orderId}</p>
              </div>
            </div>
            <Badge className={`text-sm px-3 py-1 ${getStatusBadgeColor(mockOrder.status)}`}>
              {statusLabels[mockOrder.status]}
            </Badge>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Timeline */}
            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Order Timeline</h2>
              </CardHeader>
              <CardContent>
                <div className="relative pl-6">
                  <div className="absolute left-1 top-0 bottom-0 w-0.5 bg-gray-200" />
                  {statusOrder.map((status, idx) => (
                    <div key={status} className="relative flex items-start gap-4 mb-6 last:mb-0">
                      <div className="flex-shrink-0 relative z-10">
                        <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${
                          idx < currentStatusIndex ? 'bg-blue-600 border-blue-600' : 
                          idx === currentStatusIndex ? 'bg-white border-blue-600' : 'bg-white border-gray-200'
                        }`}>
                          {idx < currentStatusIndex && <Check className="w-3.5 h-3.5 text-white" />}
                          {idx === currentStatusIndex && <Circle className="w-2.5 h-2.5 text-blue-600" />}
                        </div>
                      </div>
                      <div className="flex-1 min-w-0 pt-1">
                        <p className={`font-medium ${idx <= currentStatusIndex ? 'text-gray-900' : 'text-gray-500'}`}>
                          {statusLabels[status]}
                        </p>
                        <p className="text-sm text-gray-500">
                          {idx === 0 ? 'Your order has been placed' :
                           idx === 1 ? 'Waiting for payment confirmation' :
                           idx === 2 ? 'Payment received successfully' :
                           idx === 3 ? 'Restaurant has been notified' :
                           idx === 4 ? 'Restaurant confirmed your order' :
                           idx === 5 ? 'Your food is being prepared' :
                           idx === 6 ? 'Order is ready for pickup' :
                           idx === 7 ? 'Delivery partner assigned' :
                           idx === 8 ? 'Partner picked up your order' :
                           idx === 9 ? 'Order is on the way' :
                           'Order delivered successfully'}
                        </p>
                        {idx <= currentStatusIndex && (
                          <p className="text-sm text-blue-600 mt-1">
                            {new Date(Date.parse(mockOrder.createdAt) + idx * 15 * 60000).toLocaleTimeString('en-IN', { 
                              hour: '2-digit', minute: '2-digit' 
                            })}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Order Items */}
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <h2 className="text-lg font-semibold">Items</h2>
                  <Badge variant="info">{mockOrder.items.length} items</Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {mockOrder.items.map((item) => (
                    <div key={item.id} className="flex gap-4">
                      <div className="w-20 h-20 bg-gray-100 rounded-lg flex-shrink-0 overflow-hidden">
                        {item.menuItem.imageUrl ? (
                          <img src={item.menuItem.imageUrl} alt={item.menuItem.name} className="w-full h-full object-cover" />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-gray-400">
                            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
                            </svg>
                          </div>
                        )}
                      </div>
                      <div className="flex-1 min-w-0">
                        <h4 className="font-medium text-gray-900">{item.menuItem.name}</h4>
                        <p className="text-sm text-gray-500">Qty: {item.quantity} × ₹{item.menuItem.price}</p>
                      </div>
                      <div className="text-right">
                        <p className="font-medium text-gray-900">₹{(item.menuItem.price * item.quantity).toFixed(2)}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Delivery Partner */}
            {['DELIVERY_PARTNER_ASSIGNED', 'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(mockOrder.status) && (
              <Card>
                <CardHeader>
                  <h2 className="text-lg font-semibold flex items-center gap-2">
                    <Truck className="w-5 h-5 text-blue-600" />
                    Delivery Partner
                  </h2>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-xl">
                    <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
                      <User className="w-8 h-8 text-blue-600" />
                    </div>
                    <div className="flex-1">
                      <h4 className="font-semibold text-gray-900">{partner.name}</h4>
                      <p className="text-sm text-gray-500">{partner.vehicle}</p>
                      <div className="flex items-center gap-2 mt-1">
                        <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                        <span className="text-sm font-medium">{partner.rating}</span>
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                      <Button variant="outline" size="sm">Call</Button>
                      <Button variant="outline" size="sm">Chat</Button>
                    </div>
                  </div>
                  <div className="mt-4 p-4 bg-blue-50 rounded-xl">
                    <div className="flex items-center gap-3">
                      <Truck className="w-6 h-6 text-blue-600" />
                      <div>
                        <p className="font-medium text-gray-900">Live Tracking</p>
                        <p className="text-sm text-gray-500">Your order is on the way</p>
                      </div>
                    </div>
                    <div className="mt-3 h-32 bg-gray-100 rounded-lg flex items-center justify-center">
                      <div className="text-center text-gray-400">
                        <MapPin className="w-8 h-8 mx-auto mb-1" />
                        <p className="text-sm">Map would appear here</p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Payment Info */}
            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold flex items-center gap-2">
                  <CreditCard className="w-5 h-5 text-blue-600" />
                  Payment Details
                </h2>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Subtotal</span>
                    <span>₹{mockOrder.items.reduce((sum, i) => sum + i.menuItem.price * i.quantity, 0)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600 flex items-center gap-1">
                      <Truck className="w-4 h-4" />
                      Delivery Fee
                    </span>
                    <span>₹30</span>
                  </div>
                  <div className="flex justify-between text-green-600">
                    <span className="flex items-center gap-1">
                      <Shield className="w-4 h-4" />
                      Platform Fee
                    </span>
                    <span>Free</span>
                  </div>
                  <div className="border-t border-gray-100 pt-3 flex justify-between text-lg font-bold">
                    <span>Total Paid</span>
                    <span>₹{mockOrder.totalAmount}</span>
                  </div>
                  <p className="text-sm text-gray-500 text-center">
                    Paid via Cash on Delivery
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Sidebar */}
          <div className="lg:col-span-1 space-y-6">
            {/* Restaurant Info */}
            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Restaurant</h2>
              </CardHeader>
              <CardContent>
                <div className="flex items-center gap-4">
                  <div className="w-16 h-16 bg-gray-100 rounded-xl overflow-hidden flex-shrink-0">
                    {restaurant.imageUrl ? (
                      <img src={restaurant.imageUrl} alt={restaurant.name} className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-gray-400">
                        <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <h4 className="font-semibold text-gray-900 truncate">{restaurant.name}</h4>
                    <p className="text-sm text-gray-500">{restaurant.address}</p>
                    <div className="flex items-center gap-2 mt-1">
                      <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                      <span className="text-sm font-medium">{restaurant.rating}</span>
                    </div>
                  </div>
                </div>
                <div className="mt-4 flex gap-2">
                  <Button variant="outline" size="sm" className="flex-1">Call</Button>
                  <Button variant="outline" size="sm" className="flex-1">Reorder</Button>
                </div>
              </CardContent>
            </Card>

            {/* Order Summary */}
            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Order Summary</h2>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Order ID</span>
                  <span className="font-medium">{mockOrder.id}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Order Time</span>
                  <span className="font-medium">{new Date(mockOrder.createdAt).toLocaleString('en-IN')}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Delivery Address</span>
                </div>
                <p className="text-sm text-gray-500">{mockOrder.deliveryAddress}</p>
                <div className="border-t border-gray-100 pt-3 flex justify-between text-lg font-bold">
                  <span>Total</span>
                  <span>₹{mockOrder.totalAmount}</span>
                </div>
              </CardContent>
            </Card>

            {/* Actions */}
            {mockOrder.status === 'DELIVERED' && (
              <Card>
                <CardContent className="pt-0">
                  <Button variant="outline" className="w-full" onClick={() => {}}>
                    <Star className="w-4 h-4 mr-2" />
                    Rate Your Experience
                  </Button>
                </CardContent>
              </Card>
            )}

            {['CREATED', 'PAYMENT_PENDING', 'PAYMENT_CONFIRMED', 'RESTAURANT_PENDING'].includes(mockOrder.status) && (
              <Card>
                <CardContent className="pt-0">
                  <Button variant="danger" className="w-full" onClick={() => {}}>
                    Cancel Order
                  </Button>
                </CardContent>
              </Card>
            )}

            <Card className="bg-blue-50 border-blue-100">
              <CardContent className="pt-0">
                <div className="p-4">
                  <div className="flex items-center gap-3">
                    <Shield className="w-6 h-6 text-blue-600" />
                    <div>
                      <p className="font-medium text-blue-900">Order Support</p>
                      <p className="text-sm text-blue-700">Need help with your order?</p>
                    </div>
                  </div>
                  <Button variant="outline" className="w-full mt-3 border-blue-300 text-blue-700 hover:bg-blue-100">
                    Contact Support
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}