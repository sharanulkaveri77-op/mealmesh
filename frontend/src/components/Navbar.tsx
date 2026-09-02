import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui';
import { Menu, X, User, LogOut, Bell, Truck, ShoppingCart } from 'lucide-react';
import { useState } from 'react';
import { useCart } from '@/context/CartContext';
import { useAuth } from '@/context/AuthContext';
import { toast } from 'sonner';

export function Navbar() {
  const [isOpen, setIsOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { itemCount } = useCart();
  const { user, isAuthenticated, logout, hasRole } = useAuth();

  const navLinks = [
    { href: '/restaurants', label: 'Restaurants' },
    { href: '/orders', label: 'Orders' },
  ];

  const handleLogout = async () => {
    await logout();
    setUserMenuOpen(false);
    navigate('/');
    toast.success('Logged out successfully');
  };

  return (
    <nav className="bg-white shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="h-16 flex items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 text-xl font-bold text-blue-600">
            <Truck className="w-8 h-8" />
            <span className="hidden sm:inline">MealMesh</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-6">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                to={link.href}
                className={`text-sm font-medium transition-colors ${
                  location.pathname.startsWith(link.href) ? 'text-blue-600' : 'text-gray-600 hover:text-blue-600'
                }`}
              >
                {link.label}
              </Link>
            ))}
            
            {hasRole('RESTAURANT_OWNER') && (
              <Link to="/restaurant/dashboard" className="text-sm font-medium text-gray-600 hover:text-blue-600">
                Restaurant
              </Link>
            )}
            {hasRole('DELIVERY_PARTNER') && (
              <Link to="/delivery/dashboard" className="text-sm font-medium text-gray-600 hover:text-blue-600">
                Delivery
              </Link>
            )}
            {hasRole('ADMIN') && (
              <Link to="/admin/dashboard" className="text-sm font-medium text-gray-600 hover:text-blue-600">
                Admin
              </Link>
            )}
          </div>

          {/* Right side */}
          <div className="hidden md:flex items-center gap-3">
            <Link to="/cart" className="relative p-2 text-gray-600 hover:text-blue-600">
              <ShoppingCart className="w-6 h-6" />
              {itemCount > 0 && (
                <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-600 text-white text-xs rounded-full flex items-center justify-center">
                  {itemCount > 9 ? '9+' : itemCount}
                </span>
              )}
            </Link>

            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-50"
                >
                  <div className="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-medium">
                    {user?.name.charAt(0).toUpperCase()}
                  </div>
                  <span className="text-sm font-medium text-gray-700">{user?.name.split(' ')[0]}</span>
                </button>
                
                {userMenuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-100 py-1 z-50">
                    <div className="px-4 py-2 border-b border-gray-100">
                      <p className="text-sm font-medium text-gray-900">{user?.name}</p>
                      <p className="text-xs text-gray-500">{user?.email}</p>
                    </div>
                    <Link to="/profile" onClick={() => setUserMenuOpen(false)} className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
                      <User className="w-4 h-4" />
                      Profile
                    </Link>
                    <Link to="/notifications" onClick={() => setUserMenuOpen(false)} className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
                      <Bell className="w-4 h-4" />
                      Notifications
                    </Link>
                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 border-t border-gray-100"
                    >
                      <LogOut className="w-4 h-4" />
                      Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <>
                <Link to="/login"><Button variant="ghost" size="sm">Login</Button></Link>
                <Link to="/register"><Button size="sm">Sign Up</Button></Link>
              </>
            )}
          </div>

          {/* Mobile Navigation */}
          <div className="md:hidden flex items-center gap-3">
            <Link to="/cart" className="relative p-2 text-gray-600 hover:text-blue-600">
              <ShoppingCart className="w-6 h-6" />
              {itemCount > 0 && (
                <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-600 text-white text-xs rounded-full flex items-center justify-center">
                  {itemCount > 9 ? '9+' : itemCount}
                </span>
              )}
            </Link>
            <button
              onClick={() => setIsOpen(!isOpen)}
              className="p-2 text-gray-600 hover:text-blue-600"
              aria-label="Toggle menu"
            >
              {isOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {isOpen && (
          <div className="md:hidden py-4 border-t border-gray-100 animate-slide-down">
            <div className="flex flex-col gap-2">
              {navLinks.map((link) => (
                <Link
                  key={link.href}
                  to={link.href}
                  onClick={() => setIsOpen(false)}
                  className={`px-4 py-3 rounded-lg text-base font-medium ${
                    location.pathname.startsWith(link.href)
                      ? 'bg-blue-50 text-blue-600' 
                      : 'text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  {link.label}
                </Link>
              ))}
              {hasRole('RESTAURANT_OWNER') && (
                <Link to="/restaurant/dashboard" onClick={() => setIsOpen(false)} className="px-4 py-3 rounded-lg text-base font-medium text-gray-600 hover:bg-gray-50">
                  Restaurant Dashboard
                </Link>
              )}
              {hasRole('DELIVERY_PARTNER') && (
                <Link to="/delivery/dashboard" onClick={() => setIsOpen(false)} className="px-4 py-3 rounded-lg text-base font-medium text-gray-600 hover:bg-gray-50">
                  Delivery Dashboard
                </Link>
              )}
              {hasRole('ADMIN') && (
                <Link to="/admin/dashboard" onClick={() => setIsOpen(false)} className="px-4 py-3 rounded-lg text-base font-medium text-gray-600 hover:bg-gray-50">
                  Admin Dashboard
                </Link>
              )}
              
              <div className="pt-4 border-t border-gray-100 flex flex-col gap-2">
                {isAuthenticated ? (
                  <>
                    <Link to="/profile" onClick={() => setIsOpen(false)}>
                      <Button variant="outline" className="w-full justify-start gap-2">
                        <User className="w-5 h-5" />
                        Profile
                      </Button>
                    </Link>
                    <Link to="/notifications" onClick={() => setIsOpen(false)}>
                      <Button variant="outline" className="w-full justify-start gap-2">
                        <Bell className="w-5 h-5" />
                        Notifications
                      </Button>
                    </Link>
                    <Button variant="danger" onClick={handleLogout} className="w-full justify-start gap-2">
                      <LogOut className="w-5 h-5" />
                      Logout
                    </Button>
                  </>
                ) : (
                  <>
                    <Link to="/login" onClick={() => setIsOpen(false)}>
                      <Button variant="outline" className="w-full">Login</Button>
                    </Link>
                    <Link to="/register" onClick={() => setIsOpen(false)}>
                      <Button className="w-full">Sign Up</Button>
                    </Link>
                  </>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
}