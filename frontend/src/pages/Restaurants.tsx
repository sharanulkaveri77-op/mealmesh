import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button, Card, Badge, Skeleton } from '@/components/ui';
import { RestaurantCard } from '@/components/RestaurantCard';
import { Search, Filter, MapPin, X } from 'lucide-react';
import type { Restaurant } from '@/types';

const mockRestaurants: Restaurant[] = [
  { id: '1', name: 'Spice Garden', description: 'Authentic North Indian cuisine with modern twists', address: '123 Main Street, Downtown', phone: '+91 98765 43210', imageUrl: undefined, rating: 4.5, cuisineTypes: ['North Indian', 'Mughlai', 'Kebabs'], isActive: true, deliveryFee: 30, minOrderAmount: 200 },
  { id: '2', name: 'Pizza Palace', description: 'Wood-fired pizzas with fresh ingredients', address: '456 Oak Avenue, Midtown', phone: '+91 98765 43211', imageUrl: undefined, rating: 4.3, cuisineTypes: ['Italian', 'Pizza', 'Pasta'], isActive: true, deliveryFee: 25, minOrderAmount: 150 },
  { id: '3', name: 'Dragon Wok', description: 'Authentic Chinese and Asian fusion', address: '789 Elm Road, Uptown', phone: '+91 98765 43212', imageUrl: undefined, rating: 4.7, cuisineTypes: ['Chinese', 'Asian', 'Noodles'], isActive: true, deliveryFee: 35, minOrderAmount: 250 },
  { id: '4', name: 'Burger Barn', description: 'Gourmet burgers and crispy fries', address: '321 Pine Street, Downtown', phone: '+91 98765 43213', imageUrl: undefined, rating: 4.2, cuisineTypes: ['American', 'Burgers', 'Fast Food'], isActive: true, deliveryFee: 20, minOrderAmount: 100 },
  { id: '5', name: 'Sushi Express', description: 'Fresh sushi and Japanese delicacies', address: '654 Maple Drive, Midtown', phone: '+91 98765 43214', imageUrl: undefined, rating: 4.6, cuisineTypes: ['Japanese', 'Sushi', 'Seafood'], isActive: false, deliveryFee: 40, minOrderAmount: 300 },
  { id: '6', name: 'Taco Town', description: 'Authentic Mexican street food', address: '987 Cedar Lane, Uptown', phone: '+91 98765 43215', imageUrl: undefined, rating: 4.4, cuisineTypes: ['Mexican', 'Tacos', 'Burritos'], isActive: true, deliveryFee: 30, minOrderAmount: 180 },
  { id: '7', name: 'Curry House', description: 'Traditional South Indian flavors', address: '147 Birch Street, Downtown', phone: '+91 98765 43216', imageUrl: undefined, rating: 4.4, cuisineTypes: ['South Indian', 'Dosa', 'Idli'], isActive: true, deliveryFee: 25, minOrderAmount: 150 },
  { id: '8', name: 'Mediterranean Grill', description: 'Fresh Mediterranean and Greek cuisine', address: '258 Spruce Avenue, Midtown', phone: '+91 98765 43217', imageUrl: undefined, rating: 4.5, cuisineTypes: ['Mediterranean', 'Greek', 'Grill'], isActive: true, deliveryFee: 35, minOrderAmount: 200 },
  { id: '9', name: 'Noodle Bar', description: 'Hand-pulled noodles and Asian soups', address: '369 Willow Road, Uptown', phone: '+91 98765 43218', imageUrl: undefined, rating: 4.3, cuisineTypes: ['Asian', 'Noodles', 'Soup'], isActive: true, deliveryFee: 30, minOrderAmount: 180 },
];

const cuisines = ['North Indian', 'South Indian', 'Chinese', 'Italian', 'Mexican', 'Japanese', 'American', 'Mediterranean', 'Asian', 'Fast Food'];

export function Restaurants() {
  const [search, setSearch] = useState('');
  const [selectedCuisines, setSelectedCuisines] = useState<string[]>([]);
  const [showFilters, setShowFilters] = useState(false);
  const isLoading = false;

  const filteredRestaurants = mockRestaurants.filter((r) => {
    const matchesSearch = r.name.toLowerCase().includes(search.toLowerCase()) ||
      r.cuisineTypes.some(c => c.toLowerCase().includes(search.toLowerCase()));
    const matchesCuisine = selectedCuisines.length === 0 || 
      selectedCuisines.some(c => r.cuisineTypes.includes(c));
    return matchesSearch && matchesCuisine && r.isActive;
  });

  const toggleCuisine = (cuisine: string) => {
    setSelectedCuisines(prev => 
      prev.includes(cuisine) ? prev.filter(c => c !== cuisine) : [...prev, cuisine]
    );
  };

  const clearFilters = () => {
    setSearch('');
    setSelectedCuisines([]);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4">
          <div className="h-16 flex items-center justify-between">
            <Link to="/" className="text-xl font-bold text-blue-600">MealMesh</Link>
            <div className="flex items-center gap-4">
              <Link to="/cart">
                <Button variant="ghost" size="sm">Cart (0)</Button>
              </Link>
              <Link to="/login">
                <Button variant="outline" size="sm">Login</Button>
              </Link>
              <Link to="/register">
                <Button size="sm">Sign Up</Button>
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Search & Filters */}
      <section className="bg-white border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <div className="relative max-w-3xl mx-auto mb-6">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search restaurants, cuisines, or dishes..."
              className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div className="flex flex-wrap items-center gap-3">
            <Button variant="outline" size="sm" onClick={() => setShowFilters(!showFilters)}>
              <Filter className="w-4 h-4 mr-2" />
              Filters
              {selectedCuisines.length > 0 && (
                <Badge variant="info" className="ml-2">{selectedCuisines.length}</Badge>
              )}
            </Button>
            
            {selectedCuisines.length > 0 && (
              <Button variant="ghost" size="sm" onClick={clearFilters}>
                <X className="w-4 h-4 mr-1" />
                Clear All
              </Button>
            )}
          </div>
        </div>

        {/* Cuisine Filters */}
        {showFilters && (
          <div className="max-w-7xl mx-auto px-4 pb-6">
            <div className="flex flex-wrap gap-2">
              {cuisines.map((cuisine) => (
                <button
                  key={cuisine}
                  onClick={() => toggleCuisine(cuisine)}
                  className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                    selectedCuisines.includes(cuisine)
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  {cuisine}
                </button>
              ))}
            </div>
          </div>
        )}
      </section>

      {/* Results */}
      <section className="py-8">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-gray-900">
              {filteredRestaurants.length} {filteredRestaurants.length === 1 ? 'Restaurant' : 'Restaurants'} Found
            </h2>
            <div className="flex items-center gap-2 text-sm text-gray-600">
              <MapPin className="w-4 h-4" />
              <span>Current Location</span>
            </div>
          </div>

          {isLoading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1,2,3,4,5,6].map(i => (
                <Card key={i} className="h-80">
                  <Skeleton className="aspect-video w-full" />
                  <div className="p-4 space-y-3">
                    <Skeleton className="h-5 w-3/4" />
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-1/2" />
                    <Skeleton className="h-8 w-full" />
                  </div>
                </Card>
              ))}
            </div>
          ) : filteredRestaurants.length === 0 ? (
            <div className="text-center py-16">
              <div className="text-gray-400 mb-4">
                <svg className="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">No restaurants found</h3>
              <p className="text-gray-500 mb-4">Try adjusting your search or filters</p>
              <Button variant="outline" onClick={clearFilters}>Clear Filters</Button>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredRestaurants.map((restaurant) => (
                <Link key={restaurant.id} to={`/restaurants/${restaurant.id}`}>
                  <RestaurantCard restaurant={restaurant} />
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}