package com.electrostore.inventory.controller;

import com.electrostore.inventory.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
public class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private com.electrostore.inventory.config.JwtUtil jwtUtil;

    @Test
    void loginTestMap_shouldDeserializeBodyCorrectly() throws Exception {
        String jsonBody = "{\"username\":\"admin\",\"password\":\"adminpass\"}";
        mockMvc.perform(post("/auth/login-test-map")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().is4xxClientError()); 
    }
}
