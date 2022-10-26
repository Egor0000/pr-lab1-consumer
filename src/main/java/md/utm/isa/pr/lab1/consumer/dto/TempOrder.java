package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;
import md.utm.isa.pr.lab1.consumer.entity.Food;

import java.util.ArrayList;
import java.util.List;

@Data
public class TempOrder {
    private Long orderId;
    private OrderDto orderDto;
    private boolean external;
    private List<CookingDetailDto> preparedFoods = new ArrayList<>();

    /**
     * @return true if the order food was added;
     *          false if the order is already prepared;
     * */
    public boolean prepareFood(Food food, Long cookId) {
        CookingDetailDto cookingDetail = new CookingDetailDto();
        cookingDetail.setFoodId(food.getId());
        cookingDetail.setCookId(cookId);

        if (orderDto.getItems().remove(food.getId())) {
             preparedFoods.add(cookingDetail);
        }

        return orderDto.getItems().isEmpty();
    }

    public boolean isPrepared() {
        return orderDto.getItems().isEmpty();
    }
}
