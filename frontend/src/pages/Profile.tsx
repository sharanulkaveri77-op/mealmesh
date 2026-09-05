import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Input, Card, CardContent, CardHeader, Badge } from '@/components/ui';
import { ArrowLeft, User, Mail, Phone, Shield, Edit, Save, X, Award, MapPin } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { toast } from 'sonner';

export function Profile() {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [name, setName] = useState(user?.name || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [email, setEmail] = useState(user?.email || '');
  const [isSaving, setIsSaving] = useState(false);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="max-w-md w-full text-center p-8">
          <h2 className="text-xl font-bold mb-2">Please log in</h2>
          <p className="text-gray-600 mb-6">You need to be logged in to view your profile</p>
          <Link to="/login"><Button>Sign In</Button></Link>
        </Card>
      </div>
    );
  }

  const handleSave = async () => {
    setIsSaving(true);
    await new Promise(resolve => setTimeout(resolve, 1000));
    setIsSaving(false);
    setIsEditing(false);
    toast.success('Profile updated successfully');
  };

  const handleLogout = async () => {
    await logout();
    navigate('/');
    toast.success('Logged out successfully');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4">
          <div className="h-16 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link to="/" className="text-gray-500 hover:text-gray-700">
                <ArrowLeft className="w-6 h-6" />
              </Link>
              <h1 className="text-xl font-bold text-gray-900">My Profile</h1>
            </div>
            {!isEditing ? (
              <Button onClick={() => setIsEditing(true)} variant="outline" size="sm">
                <Edit className="w-4 h-4 mr-2" />
                Edit Profile
              </Button>
            ) : (
              <div className="flex gap-2">
                <Button onClick={() => setIsEditing(false)} variant="outline" size="sm">
                  <X className="w-4 h-4 mr-2" />
                  Cancel
                </Button>
                <Button onClick={handleSave} size="sm" isLoading={isSaving}>
                  <Save className="w-4 h-4 mr-2" />
                  Save
                </Button>
              </div>
            )}
          </div>
        </div>
      </header>

      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Profile Card */}
          <div className="lg:col-span-1">
            <Card>
              <CardContent className="p-6 text-center">
                <div className="w-24 h-24 mx-auto mb-4 bg-gradient-to-br from-[#F4511E] to-[#FF6D00] rounded-full flex items-center justify-center text-white text-3xl font-bold shadow-md">
                  {user?.name?.charAt(0).toUpperCase()}
                </div>
                <h2 className="text-xl font-bold text-gray-900">{user?.name}</h2>
                <p className="text-gray-500 text-sm">{user?.email}</p>
                <div className="mt-4 flex flex-wrap gap-2 justify-center">
                  {user?.roles.map((role) => (
                    <Badge key={role} variant="info">{role}</Badge>
                  ))}
                </div>
                <div className="mt-6 pt-6 border-t border-gray-100">
                  <div className="grid grid-cols-3 gap-4 text-center">
                    <div>
                      <p className="text-2xl font-bold text-gray-900">0</p>
                      <p className="text-xs text-gray-500">Orders</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-gray-900">0</p>
                      <p className="text-xs text-gray-500">Reviews</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-gray-900">0</p>
                      <p className="text-xs text-gray-500">Points</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="mt-4">
              <CardContent className="p-0">
                <Link to="/orders" className="flex items-center gap-3 p-4 hover:bg-orange-50/50 transition-colors">
                  <Award className="w-5 h-5 text-[#F4511E]" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">My Orders</p>
                    <p className="text-sm text-gray-500">View order history</p>
                  </div>
                </Link>
                <Link to="/notifications" className="flex items-center gap-3 p-4 hover:bg-orange-50/50 transition-colors border-t border-gray-100">
                  <Mail className="w-5 h-5 text-[#F4511E]" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">Notifications</p>
                    <p className="text-sm text-gray-500">View your notifications</p>
                  </div>
                </Link>
                <Link to="/cart" className="flex items-center gap-3 p-4 hover:bg-orange-50/50 transition-colors border-t border-gray-100">
                  <MapPin className="w-5 h-5 text-[#F4511E]" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">Cart</p>
                    <p className="text-sm text-gray-500">View your cart</p>
                  </div>
                </Link>
              </CardContent>
            </Card>
          </div>

          {/* Profile Form */}
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Personal Information</h2>
              </CardHeader>
              <CardContent className="space-y-4">
                <Input
                  label="Full Name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  disabled={!isEditing}
                  leftIcon={<User className="w-5 h-5 text-gray-400" />}
                />
                <Input
                  label="Email Address"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={!isEditing}
                  leftIcon={<Mail className="w-5 h-5 text-gray-400" />}
                />
                <Input
                  label="Phone Number"
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  disabled={!isEditing}
                  leftIcon={<Phone className="w-5 h-5 text-gray-400" />}
                />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold flex items-center gap-2">
                  <Shield className="w-5 h-5 text-[#F4511E]" />
                  Security
                </h2>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-900">Password</p>
                    <p className="text-sm text-gray-500">Last updated 30 days ago</p>
                  </div>
                  <Button variant="outline" size="sm">Change Password</Button>
                </div>
                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-900">Two-Factor Authentication</p>
                    <p className="text-sm text-gray-500">Add an extra layer of security</p>
                  </div>
                  <Button variant="outline" size="sm">Enable</Button>
                </div>
              </CardContent>
            </Card>

            <Card className="border-red-100 bg-red-50">
              <CardContent className="pt-0">
                <div className="p-4">
                  <h3 className="font-semibold text-red-900">Danger Zone</h3>
                  <p className="text-sm text-red-700 mt-1">Once you log out, you'll need to sign in again.</p>
                  <Button variant="danger" onClick={handleLogout} className="mt-3">
                    Log Out
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}