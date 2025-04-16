package pt.feup.industrial.erpsystem.model;

public enum OrderItemStatus {
    PENDING,        // Waiting to be sent to MES (or initial state if order status is PENDING)
    SENT_TO_MES,    // Request sent to MES
    COMPLETED,      // MES reported completion
    FAILED_TO_SEND  // MES reported failure or cancellation
}
