INSERT INTO roles (id, name, description) VALUES
    (uuid_generate_v4(), 'CUSTOMER', 'Regular customer who places orders'),
    (uuid_generate_v4(), 'RESTAURANT_OWNER', 'Restaurant owner who manages restaurant and menu'),
    (uuid_generate_v4(), 'DELIVERY_PARTNER', 'Delivery partner who delivers orders'),
    (uuid_generate_v4(), 'ADMIN', 'System administrator with full access')
ON CONFLICT (name) DO NOTHING;