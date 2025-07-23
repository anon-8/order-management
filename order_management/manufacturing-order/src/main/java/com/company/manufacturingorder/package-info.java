/**
 * Manufacturing Order Module
 * 
 * This module is responsible for managing the manufacturing lifecycle of products.
 * It handles the creation, tracking, and completion of manufacturing orders.
 * 
 * Key capabilities:
 * - Manufacturing order creation and management
 * - Order status tracking and transitions
 * - Production timeline management
 * - Integration with customer orders through events
 * 
 * External dependencies:
 * - sharedkernel: Common domain concepts and events
 * 
 * Published events:
 * - ManufacturingOrderCreated: When a new manufacturing order is created
 * - ManufacturingOrderStatusChanged: When order status changes
 * - ManufacturingOrderCompleted: When manufacturing is finished
 * 
 * Consumed events:
 * - CustomerOrderPlaced: To potentially trigger manufacturing
 * - CustomerOrderStatusUpdated: To coordinate with customer order lifecycle
 * - CustomerOrderCancelled: To cancel related manufacturing orders
 */
package com.company.manufacturingorder;