INSERT INTO roles (id, name, description) VALUES
    (gen_random_uuid(), 'CUSTOMER', 'Regular customer who places orders'),
    (gen_random_uuid(), 'RESTAURANT_OWNER', 'Restaurant owner who manages restaurant and menu'),
    (gen_random_uuid(), 'DELIVERY_PARTNER', 'Delivery partner who delivers orders'),
    (gen_random_uuid(), 'ADMIN', 'System administrator with full access')
ON CONFLICT (name) DO NOTHING;
