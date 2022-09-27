package md.utm.isa.pr.lab1.consumer.service;

import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.PreparedOrderDto;
import md.utm.isa.pr.lab1.consumer.dto.TempFood;
import md.utm.isa.pr.lab1.consumer.dto.TempOrder;
import md.utm.isa.pr.lab1.consumer.entity.Food;
import md.utm.isa.pr.lab1.consumer.enums.CookingApparatus;

public interface KitchenService {
    void postOrder(OrderDto orderDto);

    TempOrder getNextOrder();

    TempOrder getTempOrder(Long id);

    Food getNextFoodByComplexity(Long id);

    void addToPrepareQueue(Food food, Long cookId);

    void addToUnpreparedQueue(Food food, Long cookId);

    TempFood getNextUnpreparedFood(CookingApparatus cookingApparatus);

    String postPreparedOrder(PreparedOrderDto preparedOrderDto);
}
