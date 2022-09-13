package md.utm.isa.pr.lab1.consumer.service;

import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.PreparedOrderDto;

public interface KitchenService {
    void postOrder(OrderDto orderDto);

    OrderDto getNextOrder();

    String postPreparedOrder(PreparedOrderDto preparedOrderDto);
}
