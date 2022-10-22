package md.utm.isa.pr.lab1.consumer.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class OrderDto {
    private Long orderId;
    private Long tableId;
    private Long waiterId;
    private List<Long> items;
    private Integer priority;
    private Double maxWait;
    @JsonAlias({ "createdTime" })
    private Long pickUpTime;

    //fixme only for debug
    private Long receiveTime;
}
