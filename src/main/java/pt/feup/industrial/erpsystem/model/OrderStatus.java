package pt.feup.industrial.erpsystem.model;

public enum OrderStatus {
    PENDING,      // Newly received, not yet processed for MES
    SENT_TO_MES,  // Information successfully sent to MES
    PROCESSING,   // MES reported starting production
    COMPLETED,    // MES reported production finished
}