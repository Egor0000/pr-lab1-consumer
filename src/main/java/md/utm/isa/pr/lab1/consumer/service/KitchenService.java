package md.utm.isa.pr.lab1.consumer.service;

import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.PreparedOrderDto;
import md.utm.isa.pr.lab1.consumer.dto.TempOrder;
import md.utm.isa.pr.lab1.consumer.entity.Food;

public interface KitchenService {
    void postOrder(OrderDto orderDto);

    TempOrder getNextOrder();

    TempOrder getTempOrder(Long id);

    Food getNextFoodByComplexity(Long id);

    void addToPrepared(Food food, Long cookId);

    String postPreparedOrder(PreparedOrderDto preparedOrderDto);
}
