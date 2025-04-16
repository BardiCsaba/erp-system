package pt.feup.industrial.erpsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ClientOrderRequestDto {
    @NotBlank
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("nif")
    private Long nif;

    @NotNull
    @JsonProperty("orderID")
    private Long orderID;

    @NotEmpty
    @Valid
    @JsonProperty("orders")
    private List<OrderItemDto> orders;
}