package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class PreparedOrderDto {
    private Long orderId;
    private Long tableId;
    private Long waiterId;
    private List<Long> items;
    private Integer priority;
    private Double maxWait;
    private Long pickUpTime;
    private Long cookingTime;
    private List<CookingDetailDto> cookingDetails;
    private boolean isExternal;
    private int rating;
    private long restaurantId;

    //fixme to delete as it is not in the doc
    private Long sendTime;
}
