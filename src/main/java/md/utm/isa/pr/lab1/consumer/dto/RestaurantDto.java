package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;
import md.utm.isa.pr.lab1.consumer.entity.Food;

import java.util.List;

@Data
public class RestaurantDto {
    private Long restaurantId;
    private String name;
    private String address;
    private Long menuItems;
    private List<Food> menu;
    private Double rating;
}
