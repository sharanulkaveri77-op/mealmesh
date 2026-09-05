import { Link } from 'react-router-dom';
import { Button } from '@/components/ui';
import { RestaurantCard } from '@/components/RestaurantCard';
import { Search, MapPin, Star, Truck, Clock } from 'lucide-react';
import type { Restaurant } from '@/types';
import { getCuisineImage, getRestaurantImage, DEFAULT_FOOD_FALLBACK } from '@/constants/images';

const mockRestaurants: Restaurant[] = [
  {
    id: '1',
    name: 'Spice Garden',
    description: 'Authentic North Indian cuisine with modern twists',
    address: '123 Main Street, Downtown',
    phone: '+91 98765 43210',
    imageUrl: getRestaurantImage('1'),
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
    imageUrl: getRestaurantImage('2'),
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
    imageUrl: getRestaurantImage('3'),
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
    imageUrl: getRestaurantImage('4'),
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
    imageUrl: getRestaurantImage('5'),
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
    imageUrl: getRestaurantImage('6'),
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
      <section className="relative bg-gradient-to-br from-amber-100 via-orange-200 to-orange-300 text-gray-900 z-10">
        {/* Subtle watercolor texture overlays */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_var(--tw-gradient-stops))] from-amber-300/40 via-orange-400/20 to-transparent" />
          <div className="absolute -top-24 -left-24 w-96 h-96 bg-amber-200/50 rounded-full blur-3xl" />
          <div className="absolute -bottom-24 -right-24 w-96 h-96 bg-orange-400/30 rounded-full blur-3xl" />
        </div>

        <div className="relative max-w-7xl mx-auto px-4 pt-24 pb-16 sm:pt-28 sm:pb-24 lg:pt-32 lg:pb-28">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            {/* Left Content Column */}
            <div className="lg:col-span-7 text-left">
              <span className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider bg-orange-500/15 text-[#D84315] border border-orange-400/30 mb-6 shadow-sm">
                🔥 Hot & Fresh Delivery
              </span>
              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-gray-900 leading-[1.15] mb-6 tracking-tight">
                Food Delivered Fast,<br />
                <span className="text-[#F4511E]">Fresh & Hot</span>
              </h1>
              <p className="text-lg sm:text-xl text-gray-800 mb-8 max-w-2xl font-normal leading-relaxed">
                Discover the best restaurants near you. Order with a tap, track in real-time, 
                and enjoy delicious meals delivered right to your doorstep.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 items-stretch sm:items-center">
                <Link to="/restaurants">
                  <Button size="lg" className="w-full sm:w-auto bg-[#F4511E] hover:bg-[#D84315] text-white font-bold px-8 py-3.5 rounded-xl shadow-lg hover:shadow-orange-500/25 transition-all">
                    Explore Restaurants
                  </Button>
                </Link>
                <Link to="/restaurants">
                  <Button size="lg" variant="outline" className="w-full sm:w-auto bg-white text-[#F4511E] hover:bg-orange-50 font-bold border border-orange-200 shadow-md px-8 py-3.5 rounded-xl transition-all">
                    Order Now
                  </Button>
                </Link>
              </div>
            </div>

            {/* Right Circular Platter Image Column */}
            <div className="lg:col-span-5 hidden lg:flex justify-center relative">
              <div className="relative w-80 h-80 lg:w-96 lg:h-96">
                {/* Glow ring */}
                <div className="absolute inset-0 rounded-full bg-gradient-to-tr from-[#F4511E] to-[#FFB300] blur-xl opacity-40 transform scale-105" />
                {/* Main circular image */}
                <img
                  src="https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=1000&q=80"
                  alt="Delicious Biryani Platter"
                  className="relative z-10 w-full h-full object-cover rounded-full shadow-2xl border-4 border-white/90 transform hover:scale-[1.02] transition-transform duration-500"
                  onError={(e) => {
                    e.currentTarget.onerror = null;
                    e.currentTarget.src = DEFAULT_FOOD_FALLBACK;
                  }}
                />
                {/* Floating badge overlay */}
                <div className="absolute -bottom-2 -left-2 z-20 bg-white/95 backdrop-blur-md px-4 py-2.5 rounded-2xl shadow-xl border border-orange-100 flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center text-[#F4511E] font-bold text-lg">
                    ⚡
                  </div>
                  <div>
                    <div className="text-xs text-gray-500 font-medium">Avg. Delivery</div>
                    <div className="text-sm font-extrabold text-gray-900">25 - 30 Mins</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Search Bar */}
        <div className="relative max-w-7xl mx-auto px-4 -mb-12 sm:-mb-14 z-20">
          <div className="bg-white rounded-2xl shadow-xl p-4 sm:p-6 border border-orange-100/60">
            <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
              <div className="sm:col-span-2 relative">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
                <input
                  type="text"
                  placeholder="Search for restaurants, cuisines, or dishes..."
                  className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-gray-900 placeholder-gray-400"
                />
              </div>
              <div className="relative">
                <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
                <select className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 bg-white appearance-none text-gray-700 font-medium">
                  <option>Current Location</option>
                  <option>Downtown</option>
                  <option>Midtown</option>
                  <option>Uptown</option>
                </select>
              </div>
              <Link to="/restaurants">
                <Button className="w-full bg-[#F4511E] hover:bg-[#D84315] py-3 text-white font-bold rounded-xl shadow-md transition-colors">
                  Find Food
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-7xl mx-auto px-4 pt-20 pb-16 sm:pt-24 sm:pb-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            { icon: Truck, title: 'Fast Delivery', desc: 'Average 30 min delivery to your door' },
            { icon: Star, title: 'Top Rated', desc: 'Curated restaurants with 4.0+ ratings' },
            { icon: Clock, title: '24/7 Support', desc: 'Round-the-clock customer assistance' },
          ].map(({ icon: Icon, title, desc }, i) => (
            <div key={i} className="text-center p-6 bg-white rounded-2xl border border-orange-100/50 shadow-sm hover:shadow-md transition-all hover:-translate-y-0.5">
              <div className="w-16 h-16 mx-auto mb-4 bg-orange-50 rounded-2xl flex items-center justify-center">
                <Icon className="w-8 h-8 text-[#F4511E]" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
              <p className="text-gray-600 text-sm">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Popular Cuisines */}
      <section className="bg-white py-16 border-y border-gray-100">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Popular Cuisines</h2>
              <p className="text-sm text-gray-500 mt-1">Explore top-rated dishes by category</p>
            </div>
            <Link to="/restaurants" className="text-[#F4511E] hover:text-[#E64A19] font-semibold text-sm">
              View All →
            </Link>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {['North Indian', 'Chinese', 'Italian', 'Mexican', 'Japanese', 'American'].map((cuisine) => (
              <Link key={cuisine} to="/restaurants" className="group block">
                <div className="relative aspect-square rounded-2xl overflow-hidden shadow-sm group-hover:shadow-lg transition-all duration-300">
                  <img 
                    src={getCuisineImage(cuisine)} 
                    alt={cuisine} 
                    className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500 ease-out" 
                    onError={(e) => {
                      e.currentTarget.onerror = null;
                      e.currentTarget.src = DEFAULT_FOOD_FALLBACK;
                    }}
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/85 via-orange-950/30 to-transparent" />
                  <div className="absolute inset-0 p-4 flex flex-col justify-end text-left">
                    <span className="text-base font-bold text-white tracking-wide drop-shadow-md">{cuisine}</span>
                    <span className="text-xs text-orange-200 mt-0.5 drop-shadow-sm font-medium">12+ restaurants</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>


      {/* Nearby Restaurants */}
      <section className="py-16 bg-gray-50/50">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-bold text-gray-900">Restaurants Near You</h2>
            <Link to="/restaurants" className="text-[#F4511E] hover:text-[#E64A19] font-semibold text-sm">
              View All →
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
      <section className="py-16 bg-white border-t border-gray-100">
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
                <div className="w-16 h-16 mx-auto mb-4 bg-[#F4511E] text-white rounded-2xl flex items-center justify-center text-2xl font-bold shadow-md">
                  {step}
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
                <p className="text-gray-600 text-sm">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer CTA */}
      <section className="bg-gradient-to-r from-[#F4511E] via-[#FF6D00] to-[#FFB300] py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold text-white mb-4 drop-shadow-sm">Ready to Order?</h2>
          <p className="text-orange-50 mb-8 max-w-2xl mx-auto font-medium">
            Join thousands of happy customers. Download the app or order from the web.
          </p>
          <Link to="/register">
            <Button size="lg" variant="secondary" className="bg-white text-[#F4511E] hover:bg-orange-50 px-8 py-3 font-bold border-none shadow-lg">
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