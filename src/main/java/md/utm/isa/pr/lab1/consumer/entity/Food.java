package md.utm.isa.pr.lab1.consumer.entity;

import lombok.Data;
import md.utm.isa.pr.lab1.consumer.enums.CookingApparatus;

@Data
public class Food implements Cloneable{
    private Long id;
    private Long orderId;
    private String name;
    private Long preparationTime;
    private Long complexity;
    private CookingApparatus cookingApparatus;

    @Override
    public Food clone() {
        try {
            Food clone = (Food) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
