package com.inventory.management.domain.enums;

public enum MovementReason {
    // ENTRADAS
    COMPRA,
    DEVOLUCION_ENTRADA,
    AJUSTE_POSITIVO,
    TRANSFERENCIA_ENTRADA,
    // SALIDAS
    VENTA,
    PERDIDA,
    AJUSTE_NEGATIVO,
    TRANSFERENCIA_SALIDA;

    public MovementType getExpectedType() {
        return switch (this) {
            case COMPRA, DEVOLUCION_ENTRADA, AJUSTE_POSITIVO, TRANSFERENCIA_ENTRADA -> MovementType.ENTRADA;
            case VENTA, PERDIDA, AJUSTE_NEGATIVO, TRANSFERENCIA_SALIDA             -> MovementType.SALIDA;
        };
    }
}
