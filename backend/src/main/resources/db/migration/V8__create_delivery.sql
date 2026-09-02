CREATE TABLE delivery_partners (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(50) UNIQUE,
    vehicle_type VARCHAR(50),
    vehicle_number VARCHAR(50),
    license_number VARCHAR(50),
    license_expiry DATE,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    is_available BOOLEAN NOT NULL DEFAULT FALSE,
    current_latitude DECIMAL(10,8),
    current_longitude DECIMAL(11,8),
    last_location_update TIMESTAMP WITH TIME ZONE,
    current_area VARCHAR(100),
    max_delivery_radius_km DECIMAL(5,2) DEFAULT 10,
    max_concurrent_orders INTEGER DEFAULT 3,
    current_active_orders INTEGER DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 5.0,
    total_deliveries INTEGER DEFAULT 0,
    total_earnings DECIMAL(12,2) DEFAULT 0,
    average_delivery_time_minutes INTEGER DEFAULT 30,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_documents JSONB DEFAULT '{}',
    bank_account_details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE delivery_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    delivery_partner_id UUID NOT NULL REFERENCES delivery_partners(id) ON DELETE RESTRICT,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    rejection_reason VARCHAR(500),
    estimated_pickup_time TIMESTAMP WITH TIME ZONE,
    estimated_delivery_time TIMESTAMP WITH TIME ZONE,
    actual_distance_km DECIMAL(8,2),
    actual_duration_minutes INTEGER,
    earnings DECIMAL(10,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE delivery_locations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_assignment_id UUID NOT NULL REFERENCES delivery_assignments(id) ON DELETE CASCADE,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    accuracy_meters DECIMAL(6,2),
    speed_kmph DECIMAL(5,2),
    heading_degrees DECIMAL(5,2),
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delivery_partners_user_id ON delivery_partners(user_id);
CREATE INDEX idx_delivery_partners_is_online ON delivery_partners(is_online);
CREATE INDEX idx_delivery_partners_is_available ON delivery_partners(is_available);
CREATE INDEX idx_delivery_partners_location ON delivery_partners(current_latitude, current_longitude);
CREATE INDEX idx_delivery_assignments_partner_id ON delivery_assignments(delivery_partner_id);
CREATE INDEX idx_delivery_assignments_order_id ON delivery_assignments(order_id);
CREATE INDEX idx_delivery_assignments_status ON delivery_assignments(status);
CREATE INDEX idx_delivery_locations_assignment_id ON delivery_locations(delivery_assignment_id);
CREATE INDEX idx_delivery_locations_recorded_at ON delivery_locations(recorded_at);