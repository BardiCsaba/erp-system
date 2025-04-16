package pt.feup.industrial.erpsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemCompletionDto {

    @NotNull
    private Long erpOrderItemId;

    @NotNull
    private LocalDateTime completionTime;

    private Integer actualQuantityProduced;
}