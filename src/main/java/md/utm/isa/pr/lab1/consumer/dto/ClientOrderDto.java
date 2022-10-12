package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClientOrderDto {
    private Long clientId;
    private List<OrderDto> orders = new ArrayList<>();
}
