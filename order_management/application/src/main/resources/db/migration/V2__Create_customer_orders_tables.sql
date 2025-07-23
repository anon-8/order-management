-- Create customer orders table
CREATE TABLE customer_orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_address TEXT NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL CHECK (total_amount >= 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PLACED', 'CONFIRMED', 'MANUFACTURING_IN_PROGRESS', 'MANUFACTURING_COMPLETED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    placed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    manufacturing_order_id UUID
);

-- Create order items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    customer_order_id UUID NOT NULL,
    product_code VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price > 0),
    currency VARCHAR(3) NOT NULL,
    FOREIGN KEY (customer_order_id) REFERENCES customer_orders(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_customer_orders_customer_id ON customer_orders(customer_id);
CREATE INDEX idx_customer_orders_status ON customer_orders(status);
CREATE INDEX idx_customer_orders_placed_at ON customer_orders(placed_at);
CREATE INDEX idx_customer_orders_manufacturing_order_id ON customer_orders(manufacturing_order_id);
CREATE INDEX idx_order_items_customer_order_id ON order_items(customer_order_id);
CREATE INDEX idx_order_items_product_code ON order_items(product_code);

-- Add foreign key constraint for manufacturing order reference
ALTER TABLE customer_orders 
    ADD CONSTRAINT fk_customer_orders_manufacturing_order 
    FOREIGN KEY (manufacturing_order_id) REFERENCES manufacturing_orders(id);