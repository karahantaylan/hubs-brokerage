package org.example.hubs_brokerage;

import org.example.hubs_brokerage.controller.OrderController;
import org.example.hubs_brokerage.dto.OrderResponse;
import org.example.hubs_brokerage.model.OrderSide;
import org.example.hubs_brokerage.model.OrderStatus;
import org.example.hubs_brokerage.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

/*
    @Test
    void createOrder_shouldReturn201() throws Exception {
        // Given
        OrderResponse mockResponse = new OrderResponse(1L, "cust1", "AAPL",
                OrderSide.BUY, 10, 150.0, OrderStatus.PENDING, LocalDateTime.now());

        //when(orderService.createOrder(any())).thenReturn(mockResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "customerId": "cust1",
                        "assetName": "AAPL",
                        "side": "BUY",
                        "size": 10,
                        "price": 150.0
                    }""")
                        .header("Authorization", "Basic YWRtaW46YWRtaW4xMjM="))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetName").value("AAPL"));
    }

 */
}