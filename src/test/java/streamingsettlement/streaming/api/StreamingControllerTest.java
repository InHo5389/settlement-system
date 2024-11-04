package streamingsettlement.streaming.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import streamingsettlement.auth.config.SecurityConfig;
import streamingsettlement.auth.handler.OAuth2SuccessHandler;
import streamingsettlement.auth.service.CustomOauth2UserService;
import streamingsettlement.streaming.api.dto.StreamingRequest;
import streamingsettlement.streaming.domain.StreamingService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(StreamingController.class)
class StreamingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StreamingService streamingService;

    @MockBean
    private CustomOauth2UserService customOauth2UserService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Test
    @DisplayName("동영상 재생 API가 올바른 요청을 받으면 성공 응답을 반환한다")
    void watch() throws Exception {
        //given
        Long streamingId = 1L;
        StreamingRequest.Watch request = new StreamingRequest.Watch(null, "192.0.0.4");
        //when
        //then
        mockMvc.perform(post("/streamings/{streamingId}/watch",streamingId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("영상 정보를 응답합니다."));
    }

    @Test
    @DisplayName("재생 시점 API가 올바른 요청을 받으면 성공 응답을 반환한다")
    void updatePlayTime() throws Exception {
        //given
        StreamingRequest.UpdatePlayTime request = new StreamingRequest.UpdatePlayTime(1L, 150);
        //when
        //then
        mockMvc.perform(post("/streamings/update-time")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}