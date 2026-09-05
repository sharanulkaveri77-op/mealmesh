import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Input, Card, CardContent, CardHeader } from '@/components/ui';
import { ArrowLeft, CreditCard, Smartphone, Landmark, Lock, Check, ChevronRight, Truck, MapPin } from 'lucide-react';
import { useCart } from '@/context/CartContext';

export function Checkout() {
  const navigate = useNavigate();
  const { cartItems, restaurant, clearCart } = useCart();
  const [step, setStep] = useState(1);
  const [address, setAddress] = useState('');
  const [phone, setPhone] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<'card' | 'upi' | 'cod'>('cod');
  const [isProcessing, setIsProcessing] = useState(false);

  const cartSubtotal = cartItems.reduce((sum, item) => sum + item.menuItem.price * item.quantity, 0);
  const deliveryFee = restaurant?.deliveryFee || 30;
  const total = cartSubtotal + deliveryFee;

  if (cartItems.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
        <Card className="max-w-md w-full text-center p-8">
          <h2 className="text-xl font-bold text-gray-900 mb-2">Cart is Empty</h2>
          <p className="text-gray-600 mb-6">Add items to your cart before checkout</p>
          <Link to="/restaurants"><Button>Browse Restaurants</Button></Link>
        </Card>
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsProcessing(true);
    await new Promise(resolve => setTimeout(resolve, 2000));
    clearCart();
    setIsProcessing(false);
    navigate('/orders/success');
  };

  const paymentMethods = [
    { id: 'cod', label: 'Cash on Delivery', desc: 'Pay when your order arrives', icon: Landmark },
    { id: 'card', label: 'Credit/Debit Card', desc: 'Secure card payment', icon: CreditCard },
    { id: 'upi', label: 'UPI', desc: 'Pay with Google Pay, PhonePe, etc.', icon: Smartphone },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4">
          <div className="h-16 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/cart" className="text-gray-500 hover:text-gray-700">
                <ArrowLeft className="w-6 h-6" />
              </Link>
              <h1 className="text-xl font-bold text-gray-900">Checkout</h1>
            </div>
            {/* Progress Steps */}
            <div className="flex items-center gap-2 hidden md:flex">
              {[1, 2, 3].map((s) => (
                <div key={s} className="flex items-center">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                    step >= s ? 'bg-[#F4511E] text-white shadow-sm' : 'bg-gray-200 text-gray-500'
                  }`}>
                    {step > s ? <Check className="w-4 h-4" /> : s}
                  </div>
                  {s < 3 && <div className={`w-16 h-0.5 mx-2 ${step > s ? 'bg-[#F4511E]' : 'bg-gray-200'}`} />}
                </div>
              ))}
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Form Steps */}
          <div className="lg:col-span-2 space-y-6">
            {/* Step 1: Address */}
            {step === 1 && (
              <Card>
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-[#F4511E] text-white rounded-full flex items-center justify-center text-sm font-medium shadow-sm">1</div>
                    <h2 className="text-lg font-semibold">Delivery Address</h2>
                  </div>
                </CardHeader>
                <CardContent>
                  <form onSubmit={(e) => { e.preventDefault(); setStep(2); }} className="space-y-4">
                    <div>
                      <label htmlFor="address" className="block text-sm font-medium text-gray-700 mb-1">
                        Full Address
                      </label>
                      <textarea
                        id="address"
                        value={address}
                        onChange={(e) => setAddress(e.target.value)}
                        placeholder="House/Flat No., Building, Street, Area, City, State, PIN"
                        required
                        rows={3}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-gray-900"
                      />
                    </div>
                    <Input
                      label="Phone Number"
                      type="tel"
                      id="phone"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      placeholder="+91 98765 43210"
                      required
                    />
                    <div className="flex justify-end pt-4">
                      <Button type="submit" size="lg" disabled={!address || !phone}>
                        Continue to Payment
                        <ChevronRight className="w-4 h-4 ml-2" />
                      </Button>
                    </div>
                  </form>
                </CardContent>
              </Card>
            )}

            {/* Step 2: Payment */}
            {step === 2 && (
              <Card>
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-[#F4511E] text-white rounded-full flex items-center justify-center text-sm font-medium shadow-sm">1</div>
                    <div className="w-8 h-8 bg-[#F4511E] text-white rounded-full flex items-center justify-center text-sm font-medium shadow-sm">2</div>
                    <h2 className="text-lg font-semibold">Payment Method</h2>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3 mb-6">
                    {paymentMethods.map((method) => (
                      <label key={method.id} className="flex items-center gap-4 p-4 border rounded-xl cursor-pointer hover:bg-orange-50/50 transition-colors">
                        <input
                          type="radio"
                          name="payment"
                          value={method.id}
                          checked={paymentMethod === method.id}
                          onChange={(e) => setPaymentMethod(e.target.value as typeof paymentMethod)}
                          className="w-5 h-5 text-[#F4511E] border-gray-300 focus:ring-orange-500"
                        />
                        <method.icon className="w-6 h-6 text-gray-500" />
                        <div>
                          <p className="font-medium text-gray-900">{method.label}</p>
                          <p className="text-sm text-gray-500">{method.desc}</p>
                        </div>
                      </label>
                    ))}
                  </div>
                  
                  <div className="flex justify-between">
                    <Button variant="outline" size="lg" onClick={() => setStep(1)}>
                      <ChevronRight className="w-4 h-4 mr-2 rotate-180" />
                      Back
                    </Button>
                    <Button size="lg" onClick={() => setStep(3)}>
                      Continue to Review
                      <ChevronRight className="w-4 h-4 ml-2" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Step 3: Review */}
            {step === 3 && (
              <Card>
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-[#F4511E] text-white rounded-full flex items-center justify-center text-sm font-medium shadow-sm">1</div>
                    <div className="w-8 h-8 bg-[#F4511E] text-white rounded-full flex items-center justify-center text-sm font-medium shadow-sm">2</div>
                    <div className="w-8 h-8 bg-[#F4511E] text-white rounded-full flex items-center justify-center text-sm font-medium shadow-sm">3</div>
                    <h2 className="text-lg font-semibold">Review & Confirm</h2>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-6">
                    {/* Address */}
                    <div>
                      <h3 className="font-medium text-gray-900 mb-2 flex items-center gap-2">
                        <MapPin className="w-5 h-5 text-[#F4511E]" />
                        Delivery Address
                      </h3>
                      <p className="text-gray-600">{address}</p>
                      <p className="text-gray-600">{phone}</p>
                      <Button variant="ghost" size="sm" onClick={() => setStep(1)} className="mt-2">
                        Change
                      </Button>
                    </div>

                    {/* Payment */}
                    <div>
                      <h3 className="font-medium text-gray-900 mb-2 flex items-center gap-2">
                        <Lock className="w-5 h-5 text-[#F4511E]" />
                        Payment Method
                      </h3>
                      <p className="text-gray-600 capitalize">{paymentMethods.find(m => m.id === paymentMethod)?.label}</p>
                      <Button variant="ghost" size="sm" onClick={() => setStep(2)} className="mt-2">
                        Change
                      </Button>
                    </div>

                    {/* Items */}
                    <div>
                      <h3 className="font-medium text-gray-900 mb-2">Order Items</h3>
                      <div className="space-y-3 max-h-48 overflow-y-auto">
                        {cartItems.map((item) => (
                          <div key={item.id} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                            <div className="flex-1 min-w-0">
                              <p className="font-medium text-sm truncate">{item.menuItem.name}</p>
                              <p className="text-sm text-gray-500">Qty: {item.quantity} × ₹{item.menuItem.price}</p>
                            </div>
                            <span className="font-medium">₹{(item.menuItem.price * item.quantity).toFixed(2)}</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* Total */}
                    <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Subtotal</span>
                        <span>₹{cartSubtotal.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600 flex items-center gap-1">
                          <Truck className="w-4 h-4" />
                          Delivery
                        </span>
                        <span>₹{deliveryFee}</span>
                      </div>
                      <div className="border-t border-gray-200 pt-2 flex justify-between text-lg font-bold">
                        <span>Total</span>
                        <span>₹{total.toFixed(2)}</span>
                      </div>
                    </div>
                  </div>

                  <form onSubmit={handleSubmit} className="mt-6">
                    <Button type="submit" className="w-full" size="lg" isLoading={isProcessing}>
                      {paymentMethod === 'cod' ? 'Place Order' : 'Pay & Place Order'}
                      <Lock className="w-4 h-4 ml-2" />
                    </Button>
                    <p className="text-center text-xs text-gray-500 mt-3">
                      By placing this order, you agree to our Terms of Service and Privacy Policy
                    </p>
                  </form>
                </CardContent>
              </Card>
            )}
          </div>

          {/* Order Summary Sidebar */}
          <div className="lg:col-span-1">
            <Card className="sticky top-24">
              <CardHeader>
                <h2 className="text-lg font-semibold">Order Summary</h2>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-3 max-h-60 overflow-y-auto">
                  {cartItems.map((item) => (
                    <div key={item.id} className="flex items-center justify-between gap-2 py-2 border-b border-gray-100 last:border-0">
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-sm truncate">{item.menuItem.name}</p>
                        <p className="text-sm text-gray-500">× {item.quantity}</p>
                      </div>
                      <span className="font-medium">₹{(item.menuItem.price * item.quantity).toFixed(2)}</span>
                    </div>
                  ))}
                </div>

                <div className="border-t border-gray-100 pt-4 space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Subtotal</span>
                    <span>₹{cartSubtotal.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600 flex items-center gap-1">
                      <Truck className="w-4 h-4" />
                      Delivery
                    </span>
                    <span>₹{deliveryFee}</span>
                  </div>
                  <div className="flex justify-between text-sm text-green-600">
                    <span className="flex items-center gap-1">
                      <Lock className="w-4 h-4" />
                      Platform Fee
                    </span>
                    <span>Free</span>
                  </div>
                  <div className="border-t border-gray-100 pt-2 flex justify-between text-lg font-bold">
                    <span>Total</span>
                    <span>₹{total.toFixed(2)}</span>
                  </div>
                </div>

                <div className="flex items-center gap-2 text-sm text-gray-500 pt-2">
                  <Lock className="w-4 h-4" />
                  <span>Secure & encrypted payment</span>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}