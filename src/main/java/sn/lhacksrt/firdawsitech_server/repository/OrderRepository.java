package sn.lhacksrt.firdawsitech_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.lhacksrt.firdawsitech_server.domain.Order;
import sn.lhacksrt.firdawsitech_server.domain.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUuid(UUID uuid);
    boolean existsByOrderNumber(String orderNumber);
    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
}
