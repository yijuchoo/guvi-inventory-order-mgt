package com.guvi.inventory_order_mgt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guvi.inventory_order_mgt.bootstrap.UserSeeder;
import com.guvi.inventory_order_mgt.dto.OrderItemRequest;
import com.guvi.inventory_order_mgt.dto.OrderItemResponse;
import com.guvi.inventory_order_mgt.dto.OrderRequest;
import com.guvi.inventory_order_mgt.dto.OrderResponse;
import com.guvi.inventory_order_mgt.enums.OrderStatus;
import com.guvi.inventory_order_mgt.security.JwtUtil;
import com.guvi.inventory_order_mgt.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserSeeder userSeeder;

    private OrderResponse mockOrderResponse() {
        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(1L, "Wireless Headphones", 2, 99.99));

        return new OrderResponse(
                1L, 1L, OrderStatus.CONFIRMED,
                LocalDateTime.now(), items);
    }

    @Test
    @WithMockUser(roles = "USER")
    void placeOrder_ShouldReturn201_WhenValidRequest() throws Exception {
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 2)));

        when(jwtUtil.extractUserId(any())).thenReturn(1L);
        when(orderService.placeOrder(eq(1L), any(OrderRequest.class)))
                .thenReturn(mockOrderResponse());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer mockToken")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(199.98));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void placeOrder_ShouldReturn403_WhenAdmin() throws Exception {
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 2)));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer mockToken")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void cancelOrder_ShouldReturn200_WhenValidRequest() throws Exception {
        OrderResponse cancelledResponse = new OrderResponse(
                1L, 1L, OrderStatus.CANCELLED,
                LocalDateTime.now(), List.of());

        when(jwtUtil.extractUserId(any())).thenReturn(1L);
        when(orderService.cancelOrder(eq(1L), eq(1L)))
                .thenReturn(cancelledResponse);

        mockMvc.perform(put("/api/orders/1/cancel")
                        .header("Authorization", "Bearer mockToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyOrders_ShouldReturn200_WithPaginatedOrders() throws Exception {
        Page<OrderResponse> page = new PageImpl<>(List.of(mockOrderResponse()));

        when(jwtUtil.extractUserId(any())).thenReturn(1L);
        when(orderService.getMyOrders(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer mockToken")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getOrderById_ShouldReturn200_WhenOrderBelongsToUser() throws Exception {
        when(jwtUtil.extractUserId(any())).thenReturn(1L);
        when(orderService.getOrderById(eq(1L), eq(1L)))
                .thenReturn(mockOrderResponse());

        mockMvc.perform(get("/api/orders/1")
                        .header("Authorization", "Bearer mockToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getMyOrders_ShouldReturn403_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }
}
