package sn.lhacksrt.firdawsitech_server.domain;

public enum OrderStatus {
    NEW,        // créée après checkout
    PAID,       // payée
    SHIPPED,    // expédiée
    CANCELED    // annulée
}
