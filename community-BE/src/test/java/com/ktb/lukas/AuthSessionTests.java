package com.ktb.lukas;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSessionTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void rotatingRefreshTokensCannotBeReusedAndLogoutRevokesSession() throws Exception {
        mvc.perform(post("/users").contentType("application/json").content(json.writeValueAsString(
                Map.of("email", "session@example.com", "password", "Baseball1!", "nickname", "세션검증"))))
                .andExpect(status().isCreated());
        var login = mvc.perform(post("/auth").contentType("application/json").content(
                "{\"email\":\"session@example.com\",\"password\":\"Baseball1!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token.expiresIn").value(600))
                .andReturn().getResponse();
        Cookie first = login.getCookie("refreshToken");
        assertThat(first).isNotNull();
        assertThat(first.getMaxAge()).isEqualTo(1209600);
        var refresh = mvc.perform(post("/users/token/refresh").cookie(first))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.expiresIn").value(600))
                .andReturn().getResponse();
        Cookie rotated = refresh.getCookie("refreshToken");
        assertThat(rotated).isNotNull();
        assertThat(rotated.getValue()).isNotEqualTo(first.getValue());
        mvc.perform(post("/users/token/refresh").cookie(first)).andExpect(status().isUnauthorized());
        mvc.perform(delete("/auth").cookie(rotated)).andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0));
        mvc.perform(post("/users/token/refresh").cookie(rotated)).andExpect(status().isUnauthorized());
        mvc.perform(post("/users/token/refresh")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/auth")).andExpect(status().isOk());
    }
}
