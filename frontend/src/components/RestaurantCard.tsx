import { Card, CardContent, Badge } from '@/components/ui';
import type { Restaurant } from '@/types';
import { Star, MapPin, Clock } from 'lucide-react';

interface RestaurantCardProps {
  restaurant: Restaurant;
  onClick?: () => void;
}

export function RestaurantCard({ restaurant, onClick }: RestaurantCardProps) {
  return (
    <Card className="cursor-pointer hover:shadow-md transition-shadow h-full" onClick={onClick}>
      <div className="aspect-video bg-gray-100 relative overflow-hidden">
        {restaurant.imageUrl ? (
          <img src={restaurant.imageUrl} alt={restaurant.name} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}
        {!restaurant.isActive && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <Badge variant="danger" className="text-lg px-4 py-2">Closed</Badge>
          </div>
        )}
      </div>
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="font-semibold text-gray-900 line-clamp-1">{restaurant.name}</h3>
          <div className="flex items-center gap-1 text-yellow-500">
            <Star className="w-4 h-4 fill-current" />
            <span className="font-medium">{restaurant.rating.toFixed(1)}</span>
          </div>
        </div>
        <p className="text-sm text-gray-500 line-clamp-2 mb-3">{restaurant.description}</p>
        <div className="flex flex-wrap gap-2 mb-3">
          {restaurant.cuisineTypes.slice(0, 3).map((cuisine) => (
            <Badge key={cuisine} variant="info" className="text-xs">{cuisine}</Badge>
          ))}
        </div>
        <div className="flex items-center gap-4 text-sm text-gray-500">
          <div className="flex items-center gap-1">
            <MapPin className="w-4 h-4" />
            <span>{restaurant.address}</span>
          </div>
          <div className="flex items-center gap-1">
            <Clock className="w-4 h-4" />
            <span>₹{restaurant.deliveryFee} delivery</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}