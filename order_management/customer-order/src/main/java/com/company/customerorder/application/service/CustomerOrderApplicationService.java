package com.company.customerorder.application.service;

import com.company.sharedkernel.OrderId;
import com.company.customerorder.application.command.*;
import com.company.customerorder.application.query.FindCustomerOrderQuery;
import com.company.customerorder.application.query.FindCustomerOrderHandler;
import com.company.customerorder.application.query.CustomerOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerOrderApplicationService {
    private final PlaceCustomerOrderHandler placeOrderHandler;
    private final UpdateCustomerOrderStatusHandler updateStatusHandler;
    private final CancelCustomerOrderHandler cancelOrderHandler;
    private final FindCustomerOrderHandler findOrderHandler;
    
    public OrderId placeOrder(PlaceCustomerOrderCommand command) {
        return placeOrderHandler.handle(command);
    }
    
    public void updateOrderStatus(UpdateCustomerOrderStatusCommand command) {
        updateStatusHandler.handle(command);
    }
    
    public void cancelOrder(CancelCustomerOrderCommand command) {
        cancelOrderHandler.handle(command);
    }
    
    public Optional<CustomerOrderDto> findOrder(OrderId orderId) {
        return findOrderHandler.handle(new FindCustomerOrderQuery(orderId));
    }
}