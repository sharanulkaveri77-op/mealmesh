import { Card, CardContent, Badge } from '@/components/ui';
import type { Restaurant } from '@/types';
import { Star, MapPin, Clock } from 'lucide-react';
import { getRestaurantImage, DEFAULT_FOOD_FALLBACK } from '@/constants/images';

interface RestaurantCardProps {
  restaurant: Restaurant;
  onClick?: () => void;
}

export function RestaurantCard({ restaurant, onClick }: RestaurantCardProps) {
  const imgSrc = restaurant.imageUrl && restaurant.imageUrl.trim().length > 0
    ? restaurant.imageUrl
    : getRestaurantImage(restaurant.id);

  return (
    <Card 
      className="group cursor-pointer hover:shadow-xl hover:-translate-y-1 transition-all duration-300 h-full overflow-hidden border border-gray-100/90 rounded-2xl bg-white" 
      onClick={onClick}
    >
      <div className="aspect-[16/10] bg-gray-100 relative overflow-hidden">
        <img 
          src={imgSrc} 
          alt={restaurant.name} 
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 ease-out" 
          onError={(e) => {
            e.currentTarget.onerror = null;
            e.currentTarget.src = DEFAULT_FOOD_FALLBACK;
          }}
        />
        {/* Top Right White Rating Badge */}
        <div className="absolute top-3 right-3 bg-white/95 backdrop-blur-sm text-gray-900 px-2.5 py-1 rounded-lg text-xs font-bold shadow-md flex items-center gap-1 border border-gray-100">
          <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
          <span>{restaurant.rating.toFixed(1)}</span>
        </div>

        {!restaurant.isActive && (
          <div className="absolute inset-0 bg-black/60 backdrop-blur-[1px] flex items-center justify-center">
            <Badge variant="danger" className="text-sm font-semibold px-3 py-1 shadow-md">Closed</Badge>
          </div>
        )}
      </div>
      <CardContent className="p-4">
        <h3 className="font-bold text-gray-900 group-hover:text-[#F4511E] transition-colors line-clamp-1 text-base mb-1">{restaurant.name}</h3>
        <p className="text-xs text-gray-500 line-clamp-1 mb-2.5">{restaurant.description}</p>
        <div className="flex flex-wrap gap-1.5 mb-3">
          {restaurant.cuisineTypes.slice(0, 3).map((cuisine) => (
            <span key={cuisine} className="bg-orange-50/80 text-orange-800 text-[11px] font-medium px-2 py-0.5 rounded-md border border-orange-100/50">
              {cuisine}
            </span>
          ))}
        </div>
        <div className="flex items-center justify-between text-xs text-gray-500 pt-2.5 border-t border-gray-100">
          <div className="flex items-center gap-1 min-w-0">
            <MapPin className="w-3.5 h-3.5 shrink-0 text-gray-400" />
            <span className="truncate">{restaurant.address}</span>
          </div>
          <div className="flex items-center gap-1 shrink-0 font-medium text-gray-600">
            <Clock className="w-3.5 h-3.5 text-gray-400" />
            <span>30 min • ₹{restaurant.deliveryFee} delivery</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}