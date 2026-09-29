package com.awesome.awesomepizza;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.awesome.awesomepizza.controller.OrderController;
import com.awesome.awesomepizza.domain.Order;
import com.awesome.awesomepizza.dto.OrderRequest;
import com.awesome.awesomepizza.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for OrderController.
 * Uses @WebMvcTest to test only the web layer with mocked dependencies.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;
    @MockitoBean private OrderService service;

    /**
     * Tests the POST /api/orders endpoint for creating a new pizza order.
     * Verifies that the controller returns the correct order code in the response.
     * 
     * @throws Exception if the MockMvc request fails
     */
    @Test
    void createOrder() throws Exception {
        when(service.createOrder("Margherita")).thenReturn(new Order("Margherita"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new OrderRequest("Margherita"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").exists());
    }
}
