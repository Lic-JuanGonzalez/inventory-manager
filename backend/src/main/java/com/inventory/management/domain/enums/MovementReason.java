package com.inventory.management.domain.enums;

public enum MovementReason {
    // INBOUND
    PURCHASE,
    RETURN_INBOUND,
    POSITIVE_ADJUSTMENT,
    TRANSFER_INBOUND,
    // OUTBOUND
    SALE,
    LOSS,
    NEGATIVE_ADJUSTMENT,
    TRANSFER_OUTBOUND;

    public MovementType getExpectedType() {
        return switch (this) {
            case PURCHASE, RETURN_INBOUND, POSITIVE_ADJUSTMENT, TRANSFER_INBOUND -> MovementType.INBOUND;
            case SALE, LOSS, NEGATIVE_ADJUSTMENT, TRANSFER_OUTBOUND             -> MovementType.OUTBOUND;
        };
    }
}
