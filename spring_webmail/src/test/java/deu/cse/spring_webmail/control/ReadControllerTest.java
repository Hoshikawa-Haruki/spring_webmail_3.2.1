package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.repository.AddrbookRepository;
import deu.cse.spring_webmail.service.AddrbookService;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadController.class)
@TestPropertySource(properties = "file.download_folder=/mock_download")
class ReadControllerTest {

    @MockBean
    private AddrbookService addrbookService;

    @MockBean
    private AddrbookRepository addrbookRepository;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Pop3AgentFactory pop3AgentFactory;

    @MockBean
    private Pop3Agent pop3Agent;

    @MockBean
    private ServletContext ctx;

    private MockHttpSession session;

    @BeforeEach
    void setup() {
        session = new MockHttpSession();
    }

    @Test
    void testshowMessage_success() throws Exception {
        int msgId = 1;

        given(pop3AgentFactory.createFromSession(any(), any())).willReturn(pop3Agent);
        given(pop3Agent.getMessage(msgId)).willReturn("Hello message");
        given(pop3Agent.getSender()).willReturn("me@test.com");
        given(pop3Agent.getSubject()).willReturn("테스트 제목");
        given(pop3Agent.getBody()).willReturn("본문");

        mockMvc.perform(get("/show_message")
                .param("msgid", String.valueOf(msgId))
                .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("msg"))
                .andExpect(view().name("/read_mail/show_message"));

        assertThat(session.getAttribute("sender")).isEqualTo("me@test.com");
        assertThat(session.getAttribute("subject")).isEqualTo("테스트 제목");
        assertThat(session.getAttribute("body")).isEqualTo("본문");
    }

    @Test
    void testdeleteMail_success() throws Exception {
        int msgId = 1;

        given(pop3AgentFactory.createFromSession(any())).willReturn(pop3Agent);
        given(pop3Agent.deleteMessage(eq(msgId), eq(true))).willReturn(true);

        mockMvc.perform(get("/delete_mail.do")
                .param("msgid", String.valueOf(msgId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("main_menu"))
                .andExpect(flash().attribute("msg", "메시지 삭제를 성공하였습니다."));
    }

    @Test
    void testdeleteMail_fail() throws Exception {
        int msgId = 1;

        given(pop3AgentFactory.createFromSession(any())).willReturn(pop3Agent);
        given(pop3Agent.deleteMessage(eq(msgId), eq(true))).willReturn(false);

        mockMvc.perform(get("/delete_mail.do")
                .param("msgid", String.valueOf(msgId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("main_menu"))
                .andExpect(flash().attribute("msg", "메시지 삭제를 실패하였습니다."));
    }

    @Test
    void testdownload_success(@TempDir Path tempDir) throws Exception {
        String userId = "tester";
        String fileName = "sample.txt";

        // 1. /mock_download/tester/sample.txt 파일 생성
        Path downloadBase = tempDir.resolve("mock_download").resolve(userId);
        Files.createDirectories(downloadBase);
        Path filePath = downloadBase.resolve(fileName);
        Files.writeString(filePath, "Hello World!");

        // 2. ServletContext.getRealPath() mock
        given(ctx.getRealPath("/mock_download")).willReturn(tempDir.resolve("mock_download").toString());

        // 3. 요청 수행
        MvcResult result = mockMvc.perform(get("/download")
                .param("userid", userId)
                .param("filename", fileName))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andReturn();

        // 4. 응답 내용 확인
        String content = result.getResponse().getContentAsString();
        assertThat(content).isEqualTo("Hello World!");
    }
}
