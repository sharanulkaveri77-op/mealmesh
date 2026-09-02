import { api } from './api';

export interface Restaurant {
  id: string;
  name: string;
  description: string;
  phone: string;
  email: string;
  imageUrl: string;
  cuisineTypes: string[];
  isActive: boolean;
  isAcceptingOrders: boolean;
  preparationTimeMinutes: number;
  minimumOrderAmount: number;
  deliveryFee: number;
  deliveryRadiusKm: number;
  rating: number;
  totalReviews: number;
  latitude: number;
  longitude: number;
  openingTime: string;
  closingTime: string;
}

export interface MenuItem {
  id: string;
  name: string;
  description: string;
  price: number;
  originalPrice: number;
  imageUrl: string;
  isVegetarian: boolean;
  isVegan: boolean;
  isGlutenFree: boolean;
  spiceLevel: number;
  preparationTimeMinutes: number;
  isAvailable: boolean;
  isFeatured: boolean;
  displayOrder: number;
  tags: string;
}

export interface MenuCategory {
  id: string;
  name: string;
  description: string;
  displayOrder: number;
  imageUrl: string;
  isActive: boolean;
  items: MenuItem[];
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export const restaurantService = {
  async getAll(params?: { page?: number; size?: number }) {
    return api.get<PageResponse<Restaurant>>('/restaurants', { params });
  },

  async getAllList() {
    return api.get<Restaurant[]>('/restaurants/list');
  },

  async getById(id: string) {
    return api.get<Restaurant>(`/restaurants/${id}`);
  },

  async searchNearby(params: { lat: number; lon: number; radius?: number }) {
    return api.get<Restaurant[]>('/restaurants/search', { params });
  },

  async getMyRestaurants() {
    return api.get<Restaurant[]>('/restaurants/my');
  },

  async create(data: Partial<Restaurant>) {
    return api.post<Restaurant>('/restaurants', data);
  },

  async update(id: string, data: Partial<Restaurant>) {
    return api.put<Restaurant>(`/restaurants/${id}`, data);
  },

  async delete(id: string) {
    return api.delete(`/restaurants/${id}`);
  },

  async toggleActive(id: string, active: boolean) {
    return api.patch<Restaurant>(`/restaurants/${id}/active`, null, {
      params: { active },
    });
  },

  async toggleAcceptingOrders(id: string, accepting: boolean) {
    return api.patch<Restaurant>(`/restaurants/${id}/accepting-orders`, null, {
      params: { accepting },
    });
  },

  async getMenu(restaurantId: string) {
    return api.get<MenuCategory[]>(`/restaurants/${restaurantId}/menu/categories/with-items`);
  },

  async getCategories(restaurantId: string) {
    return api.get<MenuCategory[]>(`/restaurants/${restaurantId}/menu/categories`);
  },

  async getItems(restaurantId: string, categoryId?: string) {
    return api.get<MenuItem[]>(`/restaurants/${restaurantId}/menu/items`, {
      params: categoryId ? { categoryId } : {},
    });
  },
};