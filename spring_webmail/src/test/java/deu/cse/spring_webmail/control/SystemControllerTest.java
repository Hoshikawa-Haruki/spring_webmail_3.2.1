package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.factory.UserAdminAgentFactory;
import deu.cse.spring_webmail.factory.Pop3AgentFactory;
import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.model.UserAdminAgent;
import deu.cse.spring_webmail.repository.AddrbookRepository;
import deu.cse.spring_webmail.service.AddrbookService;
import jakarta.servlet.ServletContext;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;
import javax.imageio.ImageIO;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "superuser", roles = {"USER", "ADMIN"})
@WebMvcTest(SystemController.class)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Pop3AgentFactory pop3AgentFactory;  // 팩토리 자체는 MockBean 처리

    @Mock
    private Pop3Agent pop3Agent;  // 팩토리에서 생성해주는 객체도 Mock

    @MockBean
    private UserAdminAgentFactory userAdminAgentFactory;

    @MockBean
    private AddrbookService addrbookService;

    @MockBean
    private AddrbookRepository addrbookRepository;

    @MockBean
    private ServletContext servletContext;

    private final MockHttpSession session = new MockHttpSession();

    @Test
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("/index"));
    }

//    @Test
//    void testLoginSuccessForNormalUser() throws Exception {
//        Pop3Agent agent = mock(Pop3Agent.class);
//        given(agent.validate()).willReturn(true);
//        given(pop3AgentFactory.create(any(), any(), any())).willReturn(agent);
//
//        session.setAttribute("host", "localhost");
//
//        mockMvc.perform(post("/login.do")
//                .param("menu", "91")
//                .param("userid", "user1")
//                .param("passwd", "pw1234")
//                .session(session))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/main_menu"));
//    }
//
//    @Test
//    void testLoginSuccessForAdminUser() throws Exception {
//        Pop3Agent agent = mock(Pop3Agent.class);
//        given(agent.validate()).willReturn(true);
//        given(pop3AgentFactory.create(any(), any(), any())).willReturn(agent);
//
//        session.setAttribute("host", "localhost");
//
//        mockMvc.perform(post("/login.do")
//                .param("menu", "91")
//                .param("userid", "test@test.com") // 관리자 id
//                .param("passwd", "test") // 관리자 pw
//                .session(session))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/admin_menu"));
//    }
//
//    @Test
//    void testLoginFail() throws Exception {
//        Pop3Agent agent = mock(Pop3Agent.class);
//        given(agent.validate()).willReturn(false);
//        given(pop3AgentFactory.create(any(), any(), any())).willReturn(agent);
//
//        session.setAttribute("host", "localhost");
//
//        mockMvc.perform(post("/login.do")
//                .param("menu", "91")
//                .param("userid", "wrong")
//                .param("passwd", "fail")
//                .session(session))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrlPattern("/login_fail*"));
//    }
//    @Test
//    void testLogout() throws Exception {
//        mockMvc.perform(post("/logout") // Spring Security의 기본 logout 경로
//                .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/"));
//    }
    @Test
    void testMainMenu() throws Exception {
        int page = 1;
        int pageSize = 5;
        int totalCount = 12;
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        String mockedMessageList = "Mocked Message List";

        given(pop3AgentFactory.createFromSession(any())).willReturn(pop3Agent);
        given(pop3Agent.getMessageList(page, pageSize)).willReturn(mockedMessageList);
        given(pop3Agent.getTotalMessageCount()).willReturn(totalCount);

        mockMvc.perform(get("/main_menu").param("page", String.valueOf(page)).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("main_menu"))
                .andExpect(model().attribute("messageList", mockedMessageList))
                .andExpect(model().attribute("totalCount", totalCount))
                .andExpect(model().attribute("currentPage", page))
                .andExpect(model().attribute("pageSize", pageSize))
                .andExpect(model().attribute("totalPages", totalPages));
    }

    @Test
    void testAdminMenu() throws Exception {
        UserAdminAgent agent = mock(UserAdminAgent.class);
        given(agent.getUserList()).willReturn(new ArrayList<>(List.of("user1", "user2")));  // 가변 리스트로
        given(servletContext.getRealPath(".")).willReturn(".");
        given(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .willReturn(agent);

        mockMvc.perform(get("/admin_menu").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_menu"))
                .andExpect(model().attributeExists("userList"));
    }

    @Test
    void testDeleteUserDo() throws Exception {
        UserAdminAgent agent = mock(UserAdminAgent.class);
        given(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .willReturn(agent);
        given(servletContext.getRealPath(".")).willReturn(".");

        mockMvc.perform(post("/delete_user.do")
                .param("selectedUsers", "a@test.com", "b@test.com")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"));
    }

    @Test
    void testAddUserDo_success() throws Exception {
        UserAdminAgent mockAgent = mock(UserAdminAgent.class);
        given(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .willReturn(mockAgent);
        given(mockAgent.addUser(eq("tester"), eq("pw123"))).willReturn(true);
        given(servletContext.getRealPath(".")).willReturn(".");

        mockMvc.perform(post("/add_user.do")
                .param("id", "tester")
                .param("password", "pw123")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"))
                .andExpect(flash().attribute("msg", "사용자(tester) 추가를 성공하였습니다."));
    }

    @Test
    void testAddUserDo_fail() throws Exception {
        UserAdminAgent mockAgent = mock(UserAdminAgent.class);
        given(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .willReturn(mockAgent);
        given(mockAgent.addUser(eq("tester"), eq("pw123"))).willReturn(false);
        given(servletContext.getRealPath(".")).willReturn(".");

        mockMvc.perform(post("/add_user.do")
                .param("id", "tester")
                .param("password", "pw123")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"))
                .andExpect(flash().attribute("msg", "사용자(tester) 추가를 실패하였습니다."));
    }

    @Test
    void testAddUserDo_exception() throws Exception {
        UserAdminAgent mockAgent = mock(UserAdminAgent.class);
        given(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .willThrow(new RuntimeException("JMX 접속 실패"));
        given(servletContext.getRealPath(".")).willReturn(".");

        mockMvc.perform(post("/add_user.do")
                .param("id", "tester")
                .param("password", "pw123")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"));
        // 예외 상황이므로 flash 메시지 검증은 별도로 안함 (필요시 로그 캡처 가능)
    }

    @Test
    void testDeleteUserPage() throws Exception {
        UserAdminAgent agent = mock(UserAdminAgent.class);
        given(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .willReturn(agent);
        given(servletContext.getRealPath(".")).willReturn(".");
        given(agent.getUserList()).willReturn(new ArrayList<>(List.of("user1", "user2")));

        mockMvc.perform(get("/delete_user").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/delete_user"))
                .andExpect(model().attributeExists("userList"));
    }

    @Test
    void testAddUserPage() throws Exception {
        mockMvc.perform(get("/add_user"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/add_user"));
    }

    @Test
    void testGetImage_success() throws Exception {
        String fileName = "test.jpg";

        // 1. 자바로 진짜 이미지 생성
        Path tempImgDir = Files.createTempDirectory("img_test");
        Path imgFile = tempImgDir.resolve(fileName);
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics(); // 이미지생성
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();
        ImageIO.write(image, "jpg", imgFile.toFile());

        // 2. 서블릿 컨텍스트 mock
        given(servletContext.getRealPath("/WEB-INF/views/img_test/img"))
                .willReturn(tempImgDir.toString());

        // 3. 기대되는 바이트 값
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] expectedBytes = baos.toByteArray();

        // 4. 테스트 실행
        mockMvc.perform(get("/get_image/" + fileName))
                .andExpect(status().isOk())
                .andExpect(content().bytes(expectedBytes));
    }

    @Test
    void testGetImage_nofile() throws Exception {
        String fileName = "nonexistent.jpg";

        // 경로는 존재하지만 해당 파일은 없음
        Path tempImgDir = Files.createTempDirectory("img_test");

        given(servletContext.getRealPath("/WEB-INF/views/img_test/img"))
                .willReturn(tempImgDir.toString());

        mockMvc.perform(get("/get_image/" + fileName))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void testLoginFailPage() throws Exception {
        mockMvc.perform(get("/login_fail"))
                .andExpect(status().isOk())
                .andExpect(view().name("login_fail"));
    }
}
