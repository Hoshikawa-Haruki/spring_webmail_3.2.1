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
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WithMockUser(username = "tester", roles = "USER")
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

        MockMultipartFile emptyFile = new MockMultipartFile("upfiles", "", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/write_mail.do")
                .file(emptyFile) // 빈 파일 전달
                .param("to", "to@example.com")
                .param("cc", "")
                .param("subj", "Test Subject")
                .param("body", "Test Body")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"));
    }

    @Test
    void shouldSendMailWithMultipleAttachments() throws Exception {
        session.setAttribute("host", "localhost");
        session.setAttribute("userid", "tester@example.com");

        given(servletContext.getRealPath(any())).willReturn(System.getProperty("java.io.tmpdir"));

        MockMultipartFile file1 = new MockMultipartFile("upfiles", "file1.txt", "text/plain", "hello".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("upfiles", "file2.txt", "text/plain", "world".getBytes());

        mockMvc.perform(multipart("/write_mail.do")
                .file(file1)
                .file(file2)
                .param("to", "to@example.com")
                .param("cc", "cc@example.com")
                .param("subj", "Test Subject")
                .param("body", "Test Body")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"))
                .andExpect(flash().attribute("msg", "메일 전송이 성공했습니다."));
    }
}
