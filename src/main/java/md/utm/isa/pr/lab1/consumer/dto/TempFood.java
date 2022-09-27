package md.utm.isa.pr.lab1.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import md.utm.isa.pr.lab1.consumer.entity.Food;

@Data
@AllArgsConstructor
public class TempFood {
    private Food food;
    private Long cookId;
}
