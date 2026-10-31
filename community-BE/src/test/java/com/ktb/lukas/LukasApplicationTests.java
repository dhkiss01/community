package com.ktb.lukas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LukasApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    JsonNode call(MockHttpServletRequestBuilder request, int status) throws Exception {
        return json.readTree(mvc.perform(request).andExpect(status().is(status)).andReturn().getResponse().getContentAsString());
    }
    String body(Object value) throws Exception { return json.writeValueAsString(value); }

    @Test
    void publicBrowsingAndOwnerProtectedCrud() throws Exception {
        JsonNode member = call(post("/users").contentType("application/json").content(body(Map.of("email", "fan@example.com", "password", "Baseball1!", "nickname", "야구팬"))), 201);
        long userId = member.at("/data/id").asLong();
        call(post("/users").contentType("application/json").content(body(Map.of("email", "other@example.com", "password", "Baseball1!", "nickname", "다른팬"))), 201);
        String token = call(post("/auth").contentType("application/json").content(body(Map.of("email", "fan@example.com", "password", "Baseball1!"))), 200).at("/data/token/accessToken").asText();
        String otherToken = call(post("/auth").contentType("application/json").content(body(Map.of("email", "other@example.com", "password", "Baseball1!"))), 200).at("/data/token/accessToken").asText();
        String auth = "Bearer " + token;
        String otherAuth = "Bearer " + otherToken;
        mvc.perform(get("/posts").header("Origin", "http://127.0.0.1:3000")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:3000"));
        call(post("/posts").contentType("application/json").content(body(Map.of("title", "한화 직관", "content", "즐거운 야구"))), 401);
        JsonNode post = call(post("/posts").header("Authorization", auth).contentType("application/json").content(body(Map.of("title", "한화 직관", "content", "즐거운 야구".repeat(200), "image", "https://example.com/a.jpg"))), 201);
        long id = post.at("/data/id").asLong();
        call(get("/posts/" + id), 200);
        call(get("/posts/search").param("keyword", "한화").param("size", "1"), 200);
        call(get("/posts").param("page", "-1"), 400);
        call(get("/posts/search").param("keyword", "한화").param("sort", "invalid"), 400);
        call(post("/posts").header("Authorization", auth).contentType("application/json").content("{}"), 400);
        call(patch("/users/" + userId).header("Authorization", otherAuth).contentType("application/json").content("{\"nickname\":\"탈취\"}"), 403);
        call(patch("/users/" + userId).header("Authorization", auth).contentType("application/json").content("{\"nickname\":\"야구좋아\"}"), 200);
        call(delete("/users/" + userId).header("Authorization", otherAuth), 403);
        call(patch("/posts/" + id).header("Authorization", otherAuth).contentType("application/json").content("{\"title\":\"수정\",\"content\":\"수정\"}"), 403);
        call(patch("/posts/" + id).header("Authorization", auth).contentType("application/json").content("{\"title\":\"한화 후기\",\"content\":\"수정된 내용\",\"image\":\"https://example.com/b.jpg\"}"), 200);
        mvc.perform(get("/posts/" + id)).andExpect(jsonPath("$.data.image").value("https://example.com/b.jpg"));
        long commentId = call(post("/posts/" + id + "/comments").header("Authorization", auth).contentType("application/json").content("{\"comment\":\"좋아요\"}"), 201).at("/data/id").asLong();
        call(get("/posts/" + id + "/comments"), 200);
        call(delete("/posts/999/comments/" + commentId).header("Authorization", auth), 404);
        call(post("/posts/" + id + "/likes").header("Authorization", auth), 200);
        call(post("/posts/" + id + "/likes").header("Authorization", auth), 409);
        // Child rows must not prevent post deletion.
        call(delete("/posts/" + id).header("Authorization", auth), 200);
        call(get("/posts/" + id), 404);
    }

    @Test
    void boardCategoriesAreIndependentOfKeywords() throws Exception {
        call(post("/users").contentType("application/json").content(body(Map.of("email", "boards@example.com", "password", "Baseball1!", "nickname", "분류검증"))), 201);
        String token = call(post("/auth").contentType("application/json").content(body(Map.of("email", "boards@example.com", "password", "Baseball1!"))), 200).at("/data/token/accessToken").asText();
        long reviewId = 0;
        for (String category : new String[]{"FREE", "REVIEW", "ANALYSIS"}) {
            long id = call(post("/posts").header("Authorization", "Bearer " + token).contentType("application/json")
                    .content(body(Map.of("title", "동일한 제목", "content", "동일한 본문", "category", category))), 201).at("/data/id").asLong();
            if (category.equals("REVIEW")) reviewId = id;
            mvc.perform(get("/posts").param("category", category)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1)).andExpect(jsonPath("$.data[0].category").value(category));
            mvc.perform(get("/posts/search").param("keyword", "동일한").param("category", category))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
        }
        call(patch("/posts/" + reviewId).header("Authorization", "Bearer " + token).contentType("application/json")
                .content(body(Map.of("title", "동일한 제목", "content", "동일한 본문", "category", "ANALYSIS"))), 200);
        mvc.perform(get("/posts").param("category", "REVIEW")).andExpect(jsonPath("$.data.length()").value(0));
        mvc.perform(get("/posts").param("category", "ANALYSIS")).andExpect(jsonPath("$.data.length()").value(2));
        call(get("/posts").param("category", "unknown"), 400);
    }
}
