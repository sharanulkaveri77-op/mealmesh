import { Link } from 'react-router-dom';
import { Button, Card, CardContent, CardHeader, Badge, EmptyState } from '@/components/ui';
import { CartItem } from '@/components/CartItem';
import { ArrowLeft, Shield, Truck, Clock } from 'lucide-react';
import { useCart } from '@/context/CartContext';

export function Cart() {
  const { cartItems, updateQuantity, removeItem, restaurant } = useCart();
  const cartSubtotal = cartItems.reduce((sum, item) => sum + item.menuItem.price * item.quantity, 0);
  const deliveryFee = restaurant?.deliveryFee || 30;
  const minOrder = restaurant?.minOrderAmount || 200;
  const total = cartSubtotal + deliveryFee;

  if (cartItems.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
        <div className="max-w-md w-full text-center">
          <EmptyState
            icon={
              <svg className="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
            }
            title="Your cart is empty"
            description="Looks like you haven't added any items yet. Start exploring restaurants!"
            action={
              <Link to="/restaurants">
                <Button size="lg">Browse Restaurants</Button>
              </Link>
            }
          />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4">
          <div className="h-16 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/restaurants" className="text-gray-500 hover:text-gray-700">
                <ArrowLeft className="w-6 h-6" />
              </Link>
              <h1 className="text-xl font-bold text-gray-900">Cart ({cartItems.length})</h1>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2 space-y-4">
            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Items</h2>
              </CardHeader>
              <CardContent className="space-y-4">
                {cartItems.map((item) => (
                  <CartItem
                    key={item.id}
                    item={item}
                    onUpdateQuantity={updateQuantity}
                    onRemove={removeItem}
                  />
                ))}
                
                {restaurant && (
                  <div className="pt-4 border-t border-gray-100">
                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
                      <span className="font-medium">{restaurant.name}</span>
                      <Badge variant="info" className="text-xs">Min ₹{restaurant.minOrderAmount}</Badge>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Promo Code */}
            <Card>
              <CardContent className="py-4">
                <div className="flex gap-3">
                  <input
                    type="text"
                    placeholder="Enter promo code"
                    className="flex-1 px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  <Button>Apply</Button>
                </div>
                <p className="text-sm text-gray-500 mt-2">Have a coupon? Enter it here to save on your order.</p>
              </CardContent>
            </Card>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <Card className="sticky top-24">
              <CardHeader>
                <h2 className="text-lg font-semibold">Order Summary</h2>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Subtotal ({cartItems.length} items)</span>
                    <span className="font-medium">₹{cartSubtotal.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600 flex items-center gap-1">
                      <Truck className="w-4 h-4" />
                      Delivery Fee
                    </span>
                    <span className="font-medium">₹{deliveryFee}</span>
                  </div>
                  <div className="flex justify-between text-sm text-green-600">
                    <span className="flex items-center gap-1">
                      <Shield className="w-4 h-4" />
                      Platform Fee
                    </span>
                    <span className="font-medium">₹0 (Free)</span>
                  </div>
                  {cartSubtotal < minOrder && (
                    <div className="flex justify-between text-sm text-orange-600 bg-orange-50 p-2 rounded-lg">
                      <span>Min order ₹{minOrder}</span>
                      <span>Add ₹{(minOrder - cartSubtotal).toFixed(2)}</span>
                    </div>
                  )}
                </div>
                
                <div className="border-t border-gray-100 pt-4">
                  <div className="flex justify-between text-lg font-bold">
                    <span>Total</span>
                    <span>₹{total.toFixed(2)}</span>
                  </div>
                  <p className="text-xs text-gray-500 text-center mt-1">
                    Taxes included. Delivery charges may apply.
                  </p>
                </div>

                <Link to="/checkout">
                  <Button 
                    className="w-full" 
                    size="lg" 
                    disabled={cartSubtotal < minOrder}
                  >
                    Proceed to Checkout
                  </Button>
                </Link>

                {cartSubtotal < minOrder && (
                  <p className="text-center text-sm text-orange-600">
                    Add ₹{(minOrder - cartSubtotal).toFixed(2)} more for delivery
                  </p>
                )}

                <div className="flex items-center justify-center gap-2 text-sm text-gray-500">
                  <Shield className="w-4 h-4" />
                  <span>Secure payment</span>
                </div>
              </CardContent>
            </Card>

            {/* Benefits */}
            <Card className="mt-4">
              <CardContent className="py-4">
                <div className="grid grid-cols-3 gap-4 text-center">
                  <div className="flex flex-col items-center gap-1">
                    <Shield className="w-6 h-6 text-blue-600" />
                    <span className="text-xs text-gray-600">Secure Payment</span>
                  </div>
                  <div className="flex flex-col items-center gap-1">
                    <Truck className="w-6 h-6 text-blue-600" />
                    <span className="text-xs text-gray-600">Fast Delivery</span>
                  </div>
                  <div className="flex flex-col items-center gap-1">
                    <Clock className="w-6 h-6 text-blue-600" />
                    <span className="text-xs text-gray-600">Easy Returns</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}