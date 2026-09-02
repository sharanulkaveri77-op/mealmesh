import { Button } from '@/components/ui';
import type { CartItem } from '@/types';
import { Minus, Plus, Trash2 } from 'lucide-react';

interface CartItemProps {
  item: CartItem;
  onUpdateQuantity: (id: string, quantity: number) => void;
  onRemove: (id: string) => void;
}

export function CartItem({ item, onUpdateQuantity, onRemove }: CartItemProps) {
  const { menuItem, quantity } = item;
  const total = menuItem.price * quantity;

  return (
    <div className="flex gap-4 p-4 bg-white rounded-xl border border-gray-100">
      <div className="w-20 h-20 bg-gray-100 rounded-lg flex-shrink-0 overflow-hidden relative">
        {menuItem.imageUrl ? (
          <img src={menuItem.imageUrl} alt={menuItem.name} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
            </svg>
          </div>
        )}
        {!menuItem.isAvailable && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <span className="text-white text-xs font-medium px-2 py-1 bg-red-600 rounded">Unavailable</span>
          </div>
        )}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex justify-between">
          <h4 className="font-medium text-gray-900 truncate">{menuItem.name}</h4>
          <Button variant="ghost" size="sm" onClick={() => onRemove(item.id)} className="text-red-600 hover:bg-red-50 p-1">
            <Trash2 className="w-4 h-4" />
          </Button>
        </div>
        <p className="text-sm text-gray-500 mt-1">₹{menuItem.price.toFixed(2)} each</p>
        <div className="flex items-center gap-2 mt-3">
          <Button variant="outline" size="sm" onClick={() => onUpdateQuantity(item.id, quantity - 1)} disabled={quantity <= 1} className="w-8 h-8 p-0">
            <Minus className="w-4 h-4" />
          </Button>
          <span className="w-10 text-center font-medium">{quantity}</span>
          <Button variant="outline" size="sm" onClick={() => onUpdateQuantity(item.id, quantity + 1)} disabled={!menuItem.isAvailable} className="w-8 h-8 p-0">
            <Plus className="w-4 h-4" />
          </Button>
        </div>
      </div>
      <div className="text-right">
        <p className="font-semibold text-gray-900">₹{total.toFixed(2)}</p>
      </div>
    </div>
  );
}