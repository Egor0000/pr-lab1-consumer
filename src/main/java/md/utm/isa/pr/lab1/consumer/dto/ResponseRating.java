package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;

@Data
public class ResponseRating {
    private Long restaurantId;
    private Double restaurantAverageRating;
    private Long preparedOrders;
}
