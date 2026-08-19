package sn.lhacksrt.firdawsitech_server.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.lhacksrt.firdawsitech_server.domain.Order;
import sn.lhacksrt.firdawsitech_server.domain.OrderItem;
import sn.lhacksrt.firdawsitech_server.domain.OrderStatus;
import sn.lhacksrt.firdawsitech_server.domain.Product;
import sn.lhacksrt.firdawsitech_server.dto.CreateOrderRequest;
import sn.lhacksrt.firdawsitech_server.dto.OrderItemDTO;
import sn.lhacksrt.firdawsitech_server.dto.OrderItemResponse;
import sn.lhacksrt.firdawsitech_server.dto.OrderResponse;
import sn.lhacksrt.firdawsitech_server.repository.OrderRepository;
import sn.lhacksrt.firdawsitech_server.repository.ProductRepository;
import sn.lhacksrt.firdawsitech_server.web.error.NotFoundException;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final OrderMailService orderMail;

    /* ===================== CREATE ===================== */

    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        if (req.items() == null || req.items().isEmpty())
            throw new IllegalArgumentException("Le panier est vide.");

        // Construire les lignes avec vérification des stocks et produits actifs
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO it : req.items()) {
            Product p = products.findByUuid(it.productUuid())
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable"));

            if (Boolean.FALSE.equals(p.getInStock())) {
                throw new IllegalStateException("Stock insuffisant pour " + p.getName());
            }

            BigDecimal unit = p.getPrice();
            BigDecimal line = unit.multiply(BigDecimal.valueOf(it.quantity()));
            items.add(OrderItem.builder()
                    .productUuid(p.getUuid())
                    .productName(p.getName())
                    .unitPrice(unit)
                    .quantity(it.quantity())
                    .lineTotal(line)
                    .build());
            total = total.add(line);

            products.save(p);
        }

        Order order = Order.builder()
                .uuid(UUID.randomUUID())
                .customerName(req.customerName())
                .customerEmail(req.customerEmail())
                .customerPhone(req.customerPhone())
                .orderNumber(generateNumber())
                .status(OrderStatus.NEW)
                .totalAmount(total)
                .build();

        Order saved = orders.save(order);
        Order finalSaved = saved;
        items.forEach(oi -> oi.setOrder(finalSaved));
        saved.setItems(items);
        saved = orders.save(saved);

        OrderResponse response = toResponse(saved);

        try {
            orderMail.sendConfirmation(response, req.customerName(), req.customerEmail());
        } catch (Exception ex) {
            log.warn("Echec d'envoi d'email de confirmation pour commande {}", response.orderNumber(), ex);
        }

        return response;
    }

    private String generateNumber() {
        // EC-YYYY-xxxxx (aléatoire simple — remplaçable par séquence si besoin)
        int n = (int) (Math.random() * 90000) + 10000;
        return "EC-" + Year.now() + "-" + n;
    }

    /* ===================== READ ===================== */

    @Transactional(readOnly = true)
    public OrderResponse getByUuid(UUID uuid) {
        Order o = orders.findByUuid(uuid).orElseThrow(() -> new NotFoundException("Commande introuvable"));
        return toResponse(o);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAll() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listByStatus(OrderStatus status) {
        return orders.findAllByStatusOrderByCreatedAtDesc(status).stream().map(this::toResponse).toList();
    }

    /* ===================== TRANSITIONS ===================== */

    @Transactional
    public OrderResponse markPaid(UUID uuid) {
        Order o = orders.findByUuid(uuid).orElseThrow(() -> new NotFoundException("Commande introuvable"));
        if (o.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Seules les commandes NEW peuvent être payées.");
        }
        o.setStatus(OrderStatus.PAID);
        return toResponse(orders.save(o));
    }

    @Transactional
    public OrderResponse markShipped(UUID uuid) {
        Order o = orders.findByUuid(uuid).orElseThrow(() -> new NotFoundException("Commande introuvable"));
        if (o.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Seules les commandes PAID peuvent être expédiées.");
        }
        o.setStatus(OrderStatus.SHIPPED);
        return toResponse(orders.save(o));
    }

    @Transactional
    public OrderResponse cancel(UUID uuid) {
        Order o = orders.findByUuid(uuid).orElseThrow(() -> new NotFoundException("Commande introuvable"));
        if (o.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Impossible d’annuler une commande déjà expédiée.");
        }

        o.setStatus(OrderStatus.CANCELED);
        return toResponse(orders.save(o));
    }

    /* ===================== MAPPER ===================== */

    private OrderResponse toResponse(Order o) {
        var items = o.getItems().stream().map(oi ->
                new OrderItemResponse(
                        oi.getProductUuid(),
                        oi.getProductName(),
                        oi.getUnitPrice(),
                        oi.getQuantity(),
                        oi.getLineTotal()
                )
        ).toList();

        return new OrderResponse(
                o.getUuid(),
                o.getOrderNumber(),
                o.getCustomerName(),
                o.getCustomerEmail(),
                o.getCustomerPhone(),
                o.getStatus().name(),
                o.getTotalAmount(),
                items,
                o.getCreatedAt().toString(),
                o.getUpdatedAt().toString()
        );
    }
}
