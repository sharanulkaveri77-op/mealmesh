import { Link } from 'react-router-dom';
import { Button } from '@/components/ui';
import { RestaurantCard } from '@/components/RestaurantCard';
import { Search, MapPin, Star, Truck, Clock } from 'lucide-react';
import type { Restaurant } from '@/types';

const mockRestaurants: Restaurant[] = [
  {
    id: '1',
    name: 'Spice Garden',
    description: 'Authentic North Indian cuisine with modern twists',
    address: '123 Main Street, Downtown',
    phone: '+91 98765 43210',
    rating: 4.5,
    cuisineTypes: ['North Indian', 'Mughlai', 'Kebabs'],
    isActive: true,
    deliveryFee: 30,
    minOrderAmount: 200,
  },
  {
    id: '2',
    name: 'Pizza Palace',
    description: 'Wood-fired pizzas with fresh ingredients',
    address: '456 Oak Avenue, Midtown',
    phone: '+91 98765 43211',
    rating: 4.3,
    cuisineTypes: ['Italian', 'Pizza', 'Pasta'],
    isActive: true,
    deliveryFee: 25,
    minOrderAmount: 150,
  },
  {
    id: '3',
    name: 'Dragon Wok',
    description: 'Authentic Chinese and Asian fusion',
    address: '789 Elm Road, Uptown',
    phone: '+91 98765 43212',
    rating: 4.7,
    cuisineTypes: ['Chinese', 'Asian', 'Noodles'],
    isActive: true,
    deliveryFee: 35,
    minOrderAmount: 250,
  },
  {
    id: '4',
    name: 'Burger Barn',
    description: 'Gourmet burgers and crispy fries',
    address: '321 Pine Street, Downtown',
    phone: '+91 98765 43213',
    rating: 4.2,
    cuisineTypes: ['American', 'Burgers', 'Fast Food'],
    isActive: true,
    deliveryFee: 20,
    minOrderAmount: 100,
  },
  {
    id: '5',
    name: 'Sushi Express',
    description: 'Fresh sushi and Japanese delicacies',
    address: '654 Maple Drive, Midtown',
    phone: '+91 98765 43214',
    rating: 4.6,
    cuisineTypes: ['Japanese', 'Sushi', 'Seafood'],
    isActive: false,
    deliveryFee: 40,
    minOrderAmount: 300,
  },
  {
    id: '6',
    name: 'Taco Town',
    description: 'Authentic Mexican street food',
    address: '987 Cedar Lane, Uptown',
    phone: '+91 98765 43215',
    rating: 4.4,
    cuisineTypes: ['Mexican', 'Tacos', 'Burritos'],
    isActive: true,
    deliveryFee: 30,
    minOrderAmount: 180,
  },
];

export function Home() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-800 text-white">
        <div className="absolute inset-0 bg-black/20" />
        <div className="relative max-w-7xl mx-auto px-4 py-20 sm:py-32">
          <div className="max-w-3xl">
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold leading-tight mb-6">
              Food Delivered Fast,<br /> Fresh & Hot
            </h1>
            <p className="text-xl text-blue-100 mb-8 max-w-2xl">
              Discover the best restaurants near you. Order with a tap, track in real-time, 
              and enjoy delicious meals delivered to your doorstep.
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <Link to="/restaurants">
                <Button size="lg" className="w-full sm:w-auto">
                  Explore Restaurants
                </Button>
              </Link>
              <Link to="/restaurants">
                <Button size="lg" variant="outline" className="w-full sm:w-auto border-white text-white hover:bg-white/10">
                  How It Works
                </Button>
              </Link>
            </div>
          </div>
        </div>
        {/* Search Bar */}
        <div className="relative max-w-7xl mx-auto px-4 -mb-10 z-10">
          <div className="bg-white rounded-xl shadow-xl p-4 sm:p-6">
            <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
              <div className="sm:col-span-2 relative">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
                <input
                  type="text"
                  placeholder="Search for restaurants, cuisines, or dishes..."
                  className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-900 placeholder-gray-400"
                />
              </div>
              <div className="relative">
                <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
                <select className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white appearance-none">
                  <option>Current Location</option>
                  <option>Downtown</option>
                  <option>Midtown</option>
                  <option>Uptown</option>
                </select>
              </div>
              <Link to="/restaurants">
                <Button className="w-full bg-blue-600 hover:bg-blue-700 py-3">
                  Find Food
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-7xl mx-auto px-4 py-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            { icon: Truck, title: 'Fast Delivery', desc: 'Average 30 min delivery to your door' },
            { icon: Star, title: 'Top Rated', desc: 'Curated restaurants with 4.0+ ratings' },
            { icon: Clock, title: '24/7 Support', desc: 'Round-the-clock customer assistance' },
          ].map(({ icon: Icon, title, desc }, i) => (
            <div key={i} className="text-center p-6">
              <div className="w-16 h-16 mx-auto mb-4 bg-blue-100 rounded-full flex items-center justify-center">
                <Icon className="w-8 h-8 text-blue-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
              <p className="text-gray-600">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Popular Cuisines */}
      <section className="bg-white py-16">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-bold text-gray-900">Popular Cuisines</h2>
            <Link to="/restaurants" className="text-blue-600 hover:text-blue-700 font-medium">
              View All
            </Link>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {['North Indian', 'Chinese', 'Italian', 'Mexican', 'Japanese', 'American'].map((cuisine) => (
              <Link key={cuisine} to="/restaurants" className="group">
                <div className="aspect-square bg-gray-100 rounded-xl overflow-hidden group-hover:scale-105 transition-transform">
                  <div className="w-full h-full flex flex-col items-center justify-center p-4 text-center">
                    <span className="text-lg font-medium text-gray-900">{cuisine}</span>
                    <span className="text-sm text-gray-500 mt-1">12+ restaurants</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Nearby Restaurants */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-bold text-gray-900">Restaurants Near You</h2>
            <Link to="/restaurants" className="text-blue-600 hover:text-blue-700 font-medium">
              View All
            </Link>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {mockRestaurants.map((restaurant) => (
              <Link key={restaurant.id} to={`/restaurants/${restaurant.id}`}>
                <RestaurantCard restaurant={restaurant} />
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-2xl font-bold text-gray-900 text-center mb-12">How MealMesh Works</h2>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            {[
              { step: '1', title: 'Discover', desc: 'Browse restaurants and menus near you' },
              { step: '2', title: 'Order', desc: 'Add items to cart and checkout securely' },
              { step: '3', title: 'Track', desc: 'Watch your order from kitchen to door' },
              { step: '4', title: 'Enjoy', desc: 'Rate your experience and order again' },
            ].map(({ step, title, desc }) => (
              <div key={step} className="text-center relative">
                <div className="w-16 h-16 mx-auto mb-4 bg-blue-600 text-white rounded-full flex items-center justify-center text-2xl font-bold">
                  {step}
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
                <p className="text-gray-600">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer CTA */}
      <section className="bg-gradient-to-r from-blue-600 to-indigo-700 py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold text-white mb-4">Ready to Order?</h2>
          <p className="text-blue-100 mb-8 max-w-2xl mx-auto">
            Join thousands of happy customers. Download the app or order from the web.
          </p>
          <Link to="/register">
            <Button size="lg" variant="secondary" className="bg-white text-blue-600 hover:bg-gray-100 px-8 py-3">
              Get Started Free
            </Button>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-400 py-12">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
            <div>
              <h3 className="text-white font-bold text-xl mb-4">MealMesh</h3>
              <p className="text-sm">Intelligent, event-driven food delivery & logistics platform.</p>
            </div>
            <div>
              <h4 className="text-white font-semibold mb-3">Company</h4>
              <ul className="space-y-2 text-sm">
                <li><Link to="#" className="hover:text-white">About Us</Link></li>
                <li><Link to="#" className="hover:text-white">Careers</Link></li>
                <li><Link to="#" className="hover:text-white">Press</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="text-white font-semibold mb-3">Support</h4>
              <ul className="space-y-2 text-sm">
                <li><Link to="#" className="hover:text-white">Help Center</Link></li>
                <li><Link to="#" className="hover:text-white">Contact Us</Link></li>
                <li><Link to="#" className="hover:text-white">FAQs</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="text-white font-semibold mb-3">Legal</h4>
              <ul className="space-y-2 text-sm">
                <li><Link to="#" className="hover:text-white">Privacy Policy</Link></li>
                <li><Link to="#" className="hover:text-white">Terms of Service</Link></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-800 pt-8 text-center text-sm">
            <p>© 2024 MealMesh. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}