package sn.lhacksrt.firdawsitech_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.lhacksrt.firdawsitech_server.domain.OrderStatus;
import sn.lhacksrt.firdawsitech_server.dto.CreateOrderRequest;
import sn.lhacksrt.firdawsitech_server.dto.OrderResponse;
import sn.lhacksrt.firdawsitech_server.service.OrderService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders", produces = "application/json")
public class OrderController {

    private final OrderService service;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> list(
            @RequestParam(required = false) OrderStatus status
    ) {
        List<OrderResponse> data = (status == null)
                ? service.listAll()
                : service.listByStatus(status);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<OrderResponse> get(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.getByUuid(uuid));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest req) {
        OrderResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/orders/" + created.uuid())).body(created);
    }

    @PostMapping("/{uuid}/pay")
    public ResponseEntity<OrderResponse> pay(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.markPaid(uuid));
    }

    @PostMapping("/{uuid}/ship")
    public ResponseEntity<OrderResponse> ship(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.markShipped(uuid));
    }

    @PostMapping("/{uuid}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.cancel(uuid));
    }
}
