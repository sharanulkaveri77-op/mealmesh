export interface User {
  id: string;
  email: string;
  name: string;
  role: 'CUSTOMER' | 'RESTAURANT_OWNER' | 'DELIVERY_PARTNER' | 'ADMIN';
}

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  address: string;
  phone: string;
  imageUrl?: string;
  rating: number;
  cuisineTypes: string[];
  isActive: boolean;
  deliveryFee: number;
  minOrderAmount: number;
}

export interface MenuItem {
  id: string;
  restaurantId: string;
  categoryId: string;
  name: string;
  description: string;
  price: number;
  imageUrl?: string;
  isVegetarian: boolean;
  isAvailable: boolean;
}

export interface CartItem {
  id: string;
  menuItem: MenuItem;
  quantity: number;
}

export interface Order {
  id: string;
  customerId: string;
  restaurantId: string;
  items: CartItem[];
  status: OrderStatus;
  totalAmount: number;
  deliveryAddress: string;
  createdAt: string;
  updatedAt: string;
}

export type OrderStatus = 
  | 'CREATED' 
  | 'PAYMENT_PENDING' 
  | 'PAYMENT_CONFIRMED' 
  | 'RESTAURANT_PENDING' 
  | 'RESTAURANT_ACCEPTED' 
  | 'PREPARING' 
  | 'READY_FOR_PICKUP' 
  | 'DELIVERY_PARTNER_ASSIGNED' 
  | 'PICKED_UP' 
  | 'OUT_FOR_DELIVERY' 
  | 'DELIVERED' 
  | 'CANCELLED' 
  | 'PAYMENT_FAILED' 
  | 'RESTAURANT_REJECTED';