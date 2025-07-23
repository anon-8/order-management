-- Add index on manufacturing_order_id for efficient querying during event processing
CREATE INDEX IF NOT EXISTS idx_customer_orders_manufacturing_order_id 
ON customer_orders(manufacturing_order_id);

-- Add comment explaining the purpose
COMMENT ON INDEX idx_customer_orders_manufacturing_order_id IS 
'Index to optimize queries by manufacturing_order_id, especially for cross-module event processing';