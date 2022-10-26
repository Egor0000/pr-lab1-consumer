package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;

@Data
public class ResponseOrderDto {
    private Long orderId;
    private Long restaurantId;
    private String restaurantAddress;
    private Long estimateWaitingTime;
    private Long createdTime;
    private Long registeredTime;
}
