package md.utm.isa.pr.lab1.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Key {
    private Long id;
    private boolean external;
}
