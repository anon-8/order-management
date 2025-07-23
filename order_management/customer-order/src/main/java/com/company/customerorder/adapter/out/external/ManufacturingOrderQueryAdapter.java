package com.company.customerorder.adapter.out.external;

import com.company.sharedkernel.OrderId;
import com.company.customerorder.domain.port.ManufacturingOrderQueryPort;
import com.company.customerorder.domain.port.ManufacturingOrderQueryPort.ManufacturingOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("customerOrderManufacturingQueryAdapter")
@RequiredArgsConstructor
public class ManufacturingOrderQueryAdapter implements ManufacturingOrderQueryPort {

    private final com.company.manufacturingorder.application.port.ManufacturingOrderQueryPort manufacturingQueryPort;

    @Override
    public Optional<ManufacturingOrderStatus> getOrderStatus(OrderId manufacturingOrderId) {
        return manufacturingQueryPort.getOrderStatus(manufacturingOrderId)
                .map(status -> new ManufacturingOrderStatus(
                        status.orderId(),
                        status.status().name(),
                        status.productCode(),
                        status.quantity()
                ));
    }

    @Override
    public boolean isOrderCompleted(OrderId manufacturingOrderId) {
        return manufacturingQueryPort.isOrderCompleted(manufacturingOrderId);
    }
}