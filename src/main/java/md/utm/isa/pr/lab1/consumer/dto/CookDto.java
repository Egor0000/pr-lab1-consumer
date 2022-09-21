package md.utm.isa.pr.lab1.consumer.dto;

import lombok.Data;

@Data
public class CookDto {
    private Long cookId;
    private int rank;
    private int proficiency;
    private String name;
    private String catchPhrase;
}
