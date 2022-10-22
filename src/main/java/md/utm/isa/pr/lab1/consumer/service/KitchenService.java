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

    void prepareFood(Food food, Long cookId);

    void addToUnpreparedQueue(Food food, Long cookId);

    void addToUnpreparedQueue(TempFood tempFood);

    TempFood getNextUnpreparedFood(CookingApparatus cookingApparatus);

    String postPreparedOrder(PreparedOrderDto preparedOrderDto);

    void addToPreparedQueue(Food food, Long cookId);

    Food takePreparedFood(Long cookId);

    void register();
}
