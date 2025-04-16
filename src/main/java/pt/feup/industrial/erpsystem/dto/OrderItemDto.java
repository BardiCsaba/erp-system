package pt.feup.industrial.erpsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemDto {
    @NotNull
    @PositiveOrZero
    @JsonProperty("type")
    private Integer type;

    @NotNull
    @Positive
    @JsonProperty("quantity")
    private Integer quantity;

    @NotNull
    @Positive
    @JsonProperty("dDate")
    private Integer dDate; // Delivery Date offset

    @NotNull
    @PositiveOrZero
    @JsonProperty("penalty")
    private Double penalty;
}