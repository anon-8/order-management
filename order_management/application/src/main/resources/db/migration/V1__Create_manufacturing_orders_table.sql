-- Create manufacturing orders table
CREATE TABLE manufacturing_orders (
    id UUID PRIMARY KEY,
    product_code VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    specifications TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    expected_start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    expected_completion_date TIMESTAMP WITH TIME ZONE NOT NULL,
    actual_start_date TIMESTAMP WITH TIME ZONE,
    actual_completion_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_manufacturing_orders_status ON manufacturing_orders(status);
CREATE INDEX idx_manufacturing_orders_product_code ON manufacturing_orders(product_code);
CREATE INDEX idx_manufacturing_orders_expected_completion ON manufacturing_orders(expected_completion_date);
CREATE INDEX idx_manufacturing_orders_created_at ON manufacturing_orders(created_at);

-- Add constraints
ALTER TABLE manufacturing_orders 
    ADD CONSTRAINT chk_completion_after_start 
    CHECK (expected_completion_date > expected_start_date);

ALTER TABLE manufacturing_orders 
    ADD CONSTRAINT chk_actual_completion_after_start 
    CHECK (actual_completion_date IS NULL OR actual_start_date IS NULL OR actual_completion_date >= actual_start_date);