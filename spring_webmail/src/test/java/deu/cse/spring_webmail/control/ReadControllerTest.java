package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Pop3Agent;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

public class ReadControllerTest {

    @Mock
    private ServletContext ctx;
    @Mock
    private HttpSession session;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Pop3AgentFactory pop3AgentFactory;
    @Mock
    private Pop3Agent pop3Agent;
    @Mock
    private Model model;
    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ReadController readController;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        readController = new ReadController();

        inject("ctx", ctx);
        inject("session", session);
        inject("request", request);
        inject("pop3AgentFactory", pop3AgentFactory);
        inject("DOWNLOAD_FOLDER", "mock_download");
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = ReadController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(readController, value);
    }

    @Test
    public void testShowMessage() {
        int msgId = 1;
        String expectedMessage = "Hello message";

        given(pop3AgentFactory.createFromSession(session, request)).willReturn(pop3Agent);
        given(pop3Agent.getMessage(msgId)).willReturn(expectedMessage);
        given(pop3Agent.getSender()).willReturn("me@test.com");
        given(pop3Agent.getSubject()).willReturn("Test Subject");
        given(pop3Agent.getBody()).willReturn("Test Body");

        String viewName = readController.showMessage(msgId, model);

        verify(session).setAttribute("sender", "me@test.com");
        verify(session).setAttribute("subject", "Test Subject");
        verify(session).setAttribute("body", "Test Body");
        verify(model).addAttribute("msg", expectedMessage);

        assertEquals("/read_mail/show_message", viewName);
    }

    @Test
    public void testDeleteMail_success() {
        int msgId = 1;

        given(pop3AgentFactory.createFromSession(session)).willReturn(pop3Agent);
        given(pop3Agent.deleteMessage(msgId, true)).willReturn(true);

        String result = readController.deleteMailDo(msgId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("msg", "메시지 삭제를 성공하였습니다.");
        assertEquals("redirect:main_menu", result);
    }

    @Test
    public void testDeleteMail_failure() {
        int msgId = 1;

        given(pop3AgentFactory.createFromSession(session)).willReturn(pop3Agent);
        given(pop3Agent.deleteMessage(msgId, true)).willReturn(false);

        String result = readController.deleteMailDo(msgId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("msg", "메시지 삭제를 실패하였습니다.");
        assertEquals("redirect:main_menu", result);
    }

    @Test
    public void testDownloadFile() throws Exception {
        String userId = "tester";
        String fileName = "test.txt";

        // 임시 디렉토리 생성
        Path fakeDir = Files.createTempDirectory("mock_download");

        // /mock_download/tester 폴더 생성
        Path userDir = fakeDir.resolve(userId);
        Files.createDirectories(userDir);

        // /mock_download/tester/test.txt 파일 생성
        Path filePath = userDir.resolve(fileName);
        Files.write(filePath, "hello".getBytes());

        // 컨트롤러가 fakeDir 경로를 기준으로 찾게 설정
        given(ctx.getRealPath(any())).willReturn(fakeDir.toString());

        // 실제 메서드 실행
        ResponseEntity<?> response = readController.download(userId, fileName);

        // 검증
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof InputStreamResource);
        assertNotNull(response.getBody());
    }
}
