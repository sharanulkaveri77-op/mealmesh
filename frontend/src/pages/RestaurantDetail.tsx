import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Button, Card, CardContent, Badge } from '@/components/ui';
import { Star, MapPin, Clock, Truck, ArrowLeft, Plus, Minus } from 'lucide-react';
import type { Restaurant, MenuItem } from '@/types';
import { useCart } from '@/context/CartContext';
import { getRestaurantImage, DEFAULT_FOOD_FALLBACK } from '@/constants/images';

const mockRestaurant: Restaurant = {
  id: '1',
  name: 'Spice Garden',
  description: 'Authentic North Indian cuisine with modern twists. We use fresh ingredients and traditional recipes passed down through generations.',
  address: '123 Main Street, Downtown',
  phone: '+91 98765 43210',
  imageUrl: getRestaurantImage('1'),
  rating: 4.5,
  cuisineTypes: ['North Indian', 'Mughlai', 'Kebabs'],
  isActive: true,
  deliveryFee: 30,
  minOrderAmount: 200,
};

const mockMenuItems: MenuItem[] = [
  { id: '1', restaurantId: '1', categoryId: '1', name: 'Butter Chicken', description: 'Tender chicken in rich tomato butter gravy', price: 320, imageUrl: 'https://images.unsplash.com/photo-1588166524941-3bf61a9c41db?auto=format&fit=crop&w=600&q=80', isVegetarian: false, isAvailable: true },
  { id: '2', restaurantId: '1', categoryId: '1', name: 'Paneer Tikka Masala', description: 'Grilled cottage cheese in spiced tomato gravy', price: 280, imageUrl: 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=600&q=80', isVegetarian: true, isAvailable: true },
  { id: '3', restaurantId: '1', categoryId: '1', name: 'Dal Makhani', description: 'Slow-cooked black lentils with butter and cream', price: 220, imageUrl: 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=600&q=80', isVegetarian: true, isAvailable: true },
  { id: '4', restaurantId: '1', categoryId: '2', name: 'Garlic Naan', description: 'Soft leavened bread with garlic and herbs', price: 60, imageUrl: 'https://images.unsplash.com/photo-1626074353765-517a681e40be?auto=format&fit=crop&w=600&q=80', isVegetarian: true, isAvailable: true },
  { id: '5', restaurantId: '1', categoryId: '2', name: 'Butter Naan', description: 'Classic naan brushed with butter', price: 50, imageUrl: 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80', isVegetarian: true, isAvailable: true },
  { id: '6', restaurantId: '1', categoryId: '3', name: 'Chicken Biryani', description: 'Fragrant basmati rice with spiced chicken', price: 350, imageUrl: 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=600&q=80', isVegetarian: false, isAvailable: true },
  { id: '7', restaurantId: '1', categoryId: '3', name: 'Veg Biryani', description: 'Aromatic rice with mixed vegetables', price: 280, imageUrl: 'https://images.unsplash.com/photo-1645177628172-a94c1f96e6db?auto=format&fit=crop&w=600&q=80', isVegetarian: true, isAvailable: true },
  { id: '8', restaurantId: '1', categoryId: '4', name: 'Gulab Jamun', description: 'Soft milk solids in rose syrup (2 pcs)', price: 100, imageUrl: 'https://images.unsplash.com/photo-1668236543090-82eba5ee5976?auto=format&fit=crop&w=600&q=80', isVegetarian: true, isAvailable: true },
  { id: '9', restaurantId: '1', categoryId: '4', name: 'Kulfi', description: 'Traditional Indian ice cream', price: 120, imageUrl: 'https://images.unsplash.com/photo-1570197788417-0e82375c9371?auto=format&fit=crop&w=600&q=80', isVegetarian: true, isAvailable: true },
];

const categories = [
  { id: '1', name: 'Main Course' },
  { id: '2', name: 'Breads' },
  { id: '3', name: 'Rice & Biryani' },
  { id: '4', name: 'Desserts' },
];

export function RestaurantDetail() {
  const { id } = useParams<{ id: string }>();
  const { cartItems, addItem, updateQuantity, removeItem, clearCart, restaurantId } = useCart();
  const [activeCategory, setActiveCategory] = useState(categories[0].id);

  const isDifferentRestaurant = restaurantId && restaurantId !== id;
  const categoryItems = mockMenuItems.filter(item => item.categoryId === activeCategory);

  const handleAddToCart = (item: MenuItem) => {
    if (isDifferentRestaurant) {
      if (window.confirm('Adding items from a different restaurant will clear your current cart. Continue?')) {
        clearCart();
        addItem(item);
      }
    } else {
      addItem(item);
    }
  };

  const cartTotal = cartItems.reduce((sum, item) => sum + item.menuItem.price * item.quantity, 0);
  const itemCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

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
              <div>
                <h1 className="text-xl font-bold text-gray-900">{mockRestaurant.name}</h1>
                <p className="text-sm text-gray-500">{mockRestaurant.address}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Link to="/cart">
                <Button variant="ghost" size="sm">Cart ({itemCount})</Button>
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Restaurant Info */}
      <section className="bg-white">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Image & Info */}
            <div className="lg:col-span-2 space-y-6">
              <div className="aspect-video bg-gray-100 rounded-xl overflow-hidden relative shadow-sm">
                <img 
                  src={mockRestaurant.imageUrl || getRestaurantImage('1')} 
                  alt={mockRestaurant.name} 
                  className="w-full h-full object-cover" 
                  onError={(e) => {
                    e.currentTarget.onerror = null;
                    e.currentTarget.src = DEFAULT_FOOD_FALLBACK;
                  }}
                />
                {!mockRestaurant.isActive && (
                  <div className="absolute inset-0 bg-black/60 backdrop-blur-[1px] flex items-center justify-center">
                    <Badge variant="danger" className="text-lg px-6 py-3">Currently Closed</Badge>
                  </div>
                )}
              </div>


              <div className="flex flex-wrap items-center gap-4">
                <div className="flex items-center gap-2">
                  <Star className="w-5 h-5 fill-yellow-400 text-yellow-400" />
                  <span className="font-semibold text-lg">{mockRestaurant.rating}</span>
                  <span className="text-gray-500">(248 reviews)</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  <MapPin className="w-5 h-5" />
                  <span>{mockRestaurant.address}</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  <Clock className="w-5 h-5" />
                  <span>₹{mockRestaurant.deliveryFee} delivery</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  <Truck className="w-5 h-5" />
                  <span>Min order ₹{mockRestaurant.minOrderAmount}</span>
                </div>
              </div>

              <div className="flex flex-wrap gap-2">
                {mockRestaurant.cuisineTypes.map((cuisine) => (
                  <Badge key={cuisine} variant="info">{cuisine}</Badge>
                ))}
              </div>
            </div>

            {/* Order Summary Sidebar */}
            <div className="lg:col-span-1">
              <Card className="sticky top-24">
                <CardContent className="p-4">
                  <h3 className="font-semibold text-gray-900 mb-4">Your Order</h3>
                  {cartItems.length === 0 ? (
                    <div className="text-center py-8 text-gray-500">
                      <Truck className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                      <p>Your cart is empty</p>
                      <p className="text-sm">Add items to get started</p>
                    </div>
                  ) : (
                    <div className="space-y-3 max-h-60 overflow-y-auto">
                      {cartItems.map((item) => (
                        <div key={item.id} className="flex items-center justify-between gap-2 py-2">
                          <div className="flex-1 min-w-0">
                            <p className="font-medium text-sm truncate">{item.menuItem.name}</p>
                            <p className="text-sm text-gray-500">₹{item.menuItem.price} × {item.quantity}</p>
                          </div>
                          <div className="flex items-center gap-2">
                            <Button variant="ghost" size="sm" onClick={() => updateQuantity(item.id, item.quantity - 1)} className="p-1">
                              <Minus className="w-4 h-4" />
                            </Button>
                            <span className="w-8 text-center text-sm">{item.quantity}</span>
                            <Button variant="ghost" size="sm" onClick={() => updateQuantity(item.id, item.quantity + 1)} className="p-1">
                              <Plus className="w-4 h-4" />
                            </Button>
                            <Button variant="ghost" size="sm" onClick={() => removeItem(item.id)} className="p-1 text-red-600">
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                            </Button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}

                  {cartItems.length > 0 && (
                    <div className="mt-4 space-y-3 border-t border-gray-100 pt-4">
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Subtotal</span>
                        <span className="font-medium">₹{cartTotal.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Delivery Fee</span>
                        <span className="font-medium">₹{mockRestaurant.deliveryFee}</span>
                      </div>
                      {cartTotal < mockRestaurant.minOrderAmount && (
                        <div className="flex justify-between text-sm text-orange-600">
                          <span>Min order ₹{mockRestaurant.minOrderAmount}</span>
                          <span>Add ₹{(mockRestaurant.minOrderAmount - cartTotal).toFixed(2)} more</span>
                        </div>
                      )}
                      <div className="flex justify-between text-lg font-bold border-t border-gray-100 pt-3">
                        <span>Total</span>
                        <span>₹{(cartTotal + mockRestaurant.deliveryFee).toFixed(2)}</span>
                      </div>
                      <Link to="/checkout">
                        <Button className="w-full" size="lg" disabled={cartTotal < mockRestaurant.minOrderAmount}>
                          Proceed to Checkout
                        </Button>
                      </Link>
                      {cartTotal < mockRestaurant.minOrderAmount && (
                        <p className="text-center text-sm text-orange-600">Add ₹{(mockRestaurant.minOrderAmount - cartTotal).toFixed(2)} more for delivery</p>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </section>

      {/* Menu */}
      <section className="py-8">
        <div className="max-w-7xl mx-auto px-4">
          {/* Category Tabs */}
          <div className="flex gap-2 overflow-x-auto pb-4 mb-6 border-b border-gray-100">
            {categories.map((category) => (
              <button
                key={category.id}
                onClick={() => setActiveCategory(category.id)}
                className={`px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
                  activeCategory === category.id
                    ? 'bg-[#F4511E] text-white shadow-sm'
                    : 'text-gray-600 hover:bg-orange-50 hover:text-orange-900'
                }`}
              >
                {category.name}
              </button>
            ))}
          </div>

          {/* Menu Items */}
          <div className="space-y-6">
            {categoryItems.map((item) => (
              <div key={item.id} className="bg-white rounded-xl border border-gray-100 p-4 flex gap-4">
                <div className="w-24 h-24 bg-gray-100 rounded-lg flex-shrink-0 overflow-hidden relative">
                  <img 
                    src={item.imageUrl || DEFAULT_FOOD_FALLBACK} 
                    alt={item.name} 
                    className="w-full h-full object-cover" 
                    onError={(e) => {
                      e.currentTarget.onerror = null;
                      e.currentTarget.src = DEFAULT_FOOD_FALLBACK;
                    }}
                  />
                  {!item.isAvailable && (
                    <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                      <span className="text-white text-xs font-medium px-2 py-1 bg-red-600 rounded">Unavailable</span>
                    </div>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <div className="flex items-center gap-2">
                        <h4 className="font-semibold text-gray-900">{item.name}</h4>
                        {item.isVegetarian && (
                          <Badge variant="success" className="text-xs">Veg</Badge>
                        )}
                      </div>
                      <p className="text-sm text-gray-500 mt-1 line-clamp-2">{item.description}</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-bold text-lg text-gray-900">₹{item.price.toFixed(2)}</span>
                      <Button 
                        size="sm" 
                        onClick={() => handleAddToCart(item)}
                        disabled={!item.isAvailable || !mockRestaurant.isActive}
                      >
                        Add
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {categoryItems.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gray-500">No items in this category</p>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}