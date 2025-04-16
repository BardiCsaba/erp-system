package pt.feup.industrial.erpsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesProductionOrderDto {
    private Long erpOrderId; // Internal Order ID from ERP
    private Long erpOrderItemId; // Internal OrderItem ID from ERP
    private Integer productType; // e.g., 5 for P5
    private Integer quantity;
    private LocalDate dueDate;
}
