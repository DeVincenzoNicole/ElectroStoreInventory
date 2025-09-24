package com.electrostore.inventory.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import com.electrostore.inventory.config.JwtAuthFilter;
import com.electrostore.inventory.dto.ProductDTO;
import com.electrostore.inventory.service.InventoryService;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;
    
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void contextLoads() {
        // Test de carga de contexto
    }

    @Test
    void getInventoryByStore_shouldReturnProducts() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("Producto Test");
        when(inventoryService.getInventoryByStore(1L)).thenReturn(Collections.singletonList(product));
        mockMvc.perform(get("/inventory/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].name").value("Producto Test"));
    }

    @Test
    void patchProductStock_shouldUpdateStock() throws Exception {
        when(inventoryService.updateProductStock(1L, 1L, 10)).thenReturn(true);
        String jsonBody = "{\"quantity\":10}";
        mockMvc.perform(patch("/inventory/1/products/1/stock")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody))
            .andExpect(status().isOk())
            .andExpect(content().string("Stock actualizado correctamente."));
    }

    @Test
    void patchProductStock_shouldReturnBadRequestIfNoQuantity() throws Exception {
        String jsonBody = "{}";
        mockMvc.perform(patch("/inventory/1/products/1/stock")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("El campo 'quantity' es requerido."));
    }
}