package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.repository.AddrbookRepository;
import deu.cse.spring_webmail.service.AddrbookService;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WriteController.class)
class WriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServletContext servletContext;
    @MockBean
    private AddrbookService addrbookService;
    //@MockBean 으로 충돌나는 Bean 직접 막기
    @MockBean
    private AddrbookRepository addrbookRepository;
    //@MockBean 으로 충돌나는 Bean 직접 막기

    private final MockHttpSession session = new MockHttpSession();

    @Test
    void shouldShowWriteMailForm() throws Exception {
        mockMvc.perform(get("/write_mail").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("write_mail/write_mail"));
    }

    @Test
    void shouldSendMailWithoutAttachment() throws Exception {
        session.setAttribute("host", "localhost");
        session.setAttribute("userid", "tester@example.com");

        mockMvc.perform(multipart("/write_mail.do") // ✅ multipart로 변경
                .file(new MockMultipartFile("file1", "", "text/plain", new byte[0]))
                .param("to", "to@example.com")
                .param("cc", "")
                .param("subj", "Test Subject")
                .param("body", "Test Body")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"));
    }

    @Test
    void shouldSendMailWithAttachment() throws Exception {
        session.setAttribute("host", "localhost");
        session.setAttribute("userid", "tester@example.com");

        given(servletContext.getRealPath(any())).willReturn(System.getProperty("java.io.tmpdir"));

        MockMultipartFile file = new MockMultipartFile(
                "file1", "test.txt", "text/plain", "hello".getBytes()
        );

        // 일반적으로 sendMessage()는 내부에 SMTP 서버 연결 실패 등이 있어야 false를 리턴함
        // 지금은 완벽히 제어할 수 없으므로 파일만 보내고 성공 메시지만 체크
        mockMvc.perform(multipart("/write_mail.do")
                .file(file)
                .param("to", "to@example.com")
                .param("cc", "cc@example.com")
                .param("subj", "Test Subject")
                .param("body", "Test Body")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"))
                .andExpect(flash().attribute("msg", "메일 전송이 성공했습니다."));
    }
}
