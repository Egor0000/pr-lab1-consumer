package md.utm.isa.pr.lab1.consumer.service;

import md.utm.isa.pr.lab1.consumer.dto.*;
import md.utm.isa.pr.lab1.consumer.entity.Food;
import md.utm.isa.pr.lab1.consumer.enums.CookingApparatus;

public interface KitchenService {
    void postOrder(OrderDto orderDto);

    TempOrder getNextOrder();

    TempOrder getTempOrder(Key key);

    Food getNextFoodByComplexity(Long id);

    void prepareFood(Food food, Long cookId);

    void addToUnpreparedQueue(Food food, Long cookId);

    void addToUnpreparedQueue(TempFood tempFood);

    TempFood getNextUnpreparedFood(CookingApparatus cookingApparatus);

    String postPreparedOrder(PreparedOrderDto preparedOrderDto);

    void addToPreparedQueue(Food food, Long cookId);

    Food takePreparedFood(Long cookId);

    void register();

    double getEstimatedTime(OrderDto order);

    PreparedOrderDto getPreparedExternalOrder(Long id);

    ResponseRating getAverageRating(PreparedOrderDto preparedOrderDto);
}
