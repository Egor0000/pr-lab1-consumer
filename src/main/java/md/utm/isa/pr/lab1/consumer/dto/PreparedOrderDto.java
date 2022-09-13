package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class PreparedOrderDto {
    Long orderId;
    Long tableId;
    Long waiterId;
    List<Long> items;
    Integer priority;
    Long maxWait;
    Timestamp pickUpTime;
    Long cookingTime;
    List<CookingDetailDto> cookingDetails;
}
