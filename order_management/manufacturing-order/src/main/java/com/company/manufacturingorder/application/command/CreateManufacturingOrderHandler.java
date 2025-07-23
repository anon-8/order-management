package com.company.manufacturingorder.application.command;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.domain.model.ProductSpecification;
import com.company.manufacturingorder.domain.model.Timeline;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.manufacturingorder.domain.service.ManufacturingOrderDomainService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateManufacturingOrderHandler {
    private static final Logger log = LoggerFactory.getLogger(CreateManufacturingOrderHandler.class);
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    
    private final ManufacturingOrderRepository repository;
    private final ManufacturingOrderDomainService domainService;
    
    @Transactional
    public OrderId handle(CreateManufacturingOrderCommand command) {
        log.info("Starting manufacturing order creation: orderId={}, productCode={}", 
                command.orderId(), command.productCode());
        
        long startTime = System.currentTimeMillis();
        
        try {
            var productSpec = ProductSpecification.of(
                command.productCode(),
                command.description(),
                command.quantity(),
                command.specifications()
            );
            
            var timeline = Timeline.create(
                command.expectedStartDate(),
                command.expectedCompletionDate()
            );
            
            var order = ManufacturingOrder.create(
                command.orderId(),
                productSpec,
                timeline
            );
            
            log.debug("Validating order scheduling for orderId={}", command.orderId());
            domainService.validateOrderScheduling(order);
            
            repository.save(order);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully created manufacturing order: orderId={}, duration={}ms", 
                    command.orderId(), duration);
            
            auditLog.info("MANUFACTURING_ORDER_CREATED orderId={} productCode={} quantity={} user=SYSTEM duration={}ms", 
                    command.orderId(), command.productCode(), command.quantity(), duration);
            
            return order.getId();
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed to create manufacturing order: orderId={}, duration={}ms, error={}", 
                    command.orderId(), duration, e.getMessage(), e);
            
            auditLog.error("MANUFACTURING_ORDER_CREATION_FAILED orderId={} error={} duration={}ms", 
                    command.orderId(), e.getMessage(), duration);
            throw e;
        }
    }
}