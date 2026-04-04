package com.guvi.inventory_order_mgt.controller;

import com.guvi.inventory_order_mgt.dto.OrderRequest;
import com.guvi.inventory_order_mgt.dto.OrderResponse;
import com.guvi.inventory_order_mgt.security.JwtUtil;
import com.guvi.inventory_order_mgt.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
Endpoints
`POST /api/orders`, `PUT /api/orders/{id}/cancel`, `GET /api/orders`
*/
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    // POST /api/orders
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            // request must contain a JSON body, JSON will be converted into an OrderRequest object
            /* Example request:
            {
              "productId": 1,
              "quantity": 2
            }
            */
            @Valid @RequestBody OrderRequest request,
            // This extracts the Authorization header from the request
            // Eg. Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
            @RequestHeader("Authorization") String authHeader) {

        // Extract User ID
        // Taking the token from the header
        // Extracting the user ID (probably from a JWT); so you know who is placing order
        Long userId = extractUserId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                // userId → who is ordering
                // request → what they are ordering
                .body(orderService.placeOrder(userId, request));
    }

    // PUT /api/orders/{id}/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(orderService.cancelOrder(userId, id));
    }

    // GET /api/orders?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getMyOrders(userId, pageable));
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(orderService.getOrderById(userId, id));
    }

    // Helper — extracts userId from Bearer token
    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
