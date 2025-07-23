/**
 * Customer Order Module
 * 
 * This module manages customer orders and their lifecycle from placement to fulfillment.
 * It coordinates with the manufacturing module to ensure orders are produced and delivered.
 * 
 * Key capabilities:
 * - Customer order placement and management
 * - Order item management and pricing
 * - Status tracking and customer communications
 * - Integration with manufacturing through events and queries
 * 
 * External dependencies:
 * - sharedkernel: Common domain concepts and events
 * - manufacturingorder: Query interface for manufacturing status (via ports)
 * 
 * Published events:
 * - CustomerOrderPlaced: When a customer places an order
 * - CustomerOrderStatusUpdated: When order status changes
 * - CustomerOrderCancelled: When an order is cancelled
 * 
 * Consumed events:
 * - ManufacturingOrderCreated: To link manufacturing orders with customer orders
 * - ManufacturingOrderStatusChanged: To update customer order status
 * - ManufacturingOrderCompleted: To notify customers of completion
 */
package com.company.customerorder;