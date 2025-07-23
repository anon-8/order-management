package com.company.manufacturingorder.application.service;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.application.command.*;
import com.company.manufacturingorder.application.query.FindManufacturingOrderQuery;
import com.company.manufacturingorder.application.query.FindManufacturingOrderHandler;
import com.company.manufacturingorder.application.query.ManufacturingOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManufacturingOrderApplicationService {
    private final CreateManufacturingOrderHandler createHandler;
    private final ChangeOrderStatusHandler changeStatusHandler;
    private final CompleteManufacturingOrderHandler completeHandler;
    private final FindManufacturingOrderHandler findHandler;
    
    public OrderId createOrder(CreateManufacturingOrderCommand command) {
        return createHandler.handle(command);
    }
    
    public void changeOrderStatus(ChangeOrderStatusCommand command) {
        changeStatusHandler.handle(command);
    }
    
    public void completeOrder(CompleteManufacturingOrderCommand command) {
        completeHandler.handle(command);
    }
    
    public Optional<ManufacturingOrderDto> findOrder(OrderId orderId) {
        return findHandler.handle(new FindManufacturingOrderQuery(orderId));
    }
}