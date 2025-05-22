package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.model.UserAdminAgent;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemControllerTest {

    private SystemController controller;
    private HttpSession session;
    private HttpServletRequest request;
    private ServletContext ctx;
    private Pop3AgentFactory pop3AgentFactory;
    private UserAdminAgentFactory userAdminAgentFactory;

    @BeforeEach
    void setUp() {
        controller = new SystemController();
        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);
        ctx = mock(ServletContext.class);
        pop3AgentFactory = mock(Pop3AgentFactory.class);
        userAdminAgentFactory = mock(UserAdminAgentFactory.class);

        ReflectionTestUtils.setField(controller, "session", session);
        ReflectionTestUtils.setField(controller, "request", request);
        ReflectionTestUtils.setField(controller, "ctx", ctx);
        ReflectionTestUtils.setField(controller, "pop3AgentFactory", pop3AgentFactory);
        ReflectionTestUtils.setField(controller, "userAdminAgentFactory", userAdminAgentFactory);

        ReflectionTestUtils.setField(controller, "ROOT_ID", "root");
        ReflectionTestUtils.setField(controller, "ROOT_PASSWORD", "pw");
        ReflectionTestUtils.setField(controller, "ADMINISTRATOR", "admin");
        ReflectionTestUtils.setField(controller, "JAMES_CONTROL_PORT", 4555);
        ReflectionTestUtils.setField(controller, "JAMES_HOST", "localhost");
    }

    @Test
    void testIndex() {
        String result = controller.index();
        assertEquals("/index", result);
        verify(session).setAttribute("host", "localhost");
        verify(session).setAttribute("debug", "false");
    }

    @Test
    void testLoginSuccessUser() {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("host")).thenReturn("localhost");
        when(request.getParameter("userid")).thenReturn("user1");
        when(request.getParameter("passwd")).thenReturn("pw");

        Pop3Agent mockAgent = mock(Pop3Agent.class);
        when(mockAgent.validate()).thenReturn(true);
        when(pop3AgentFactory.create("localhost", "user1", "pw")).thenReturn(mockAgent);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        String result = controller.loginDo(CommandType.LOGIN, redirectAttributes);
        assertEquals("redirect:/main_menu", result);
    }

    @Test
    void testMainMenu() {
        Model model = mock(Model.class);
        when(session.getAttribute("host")).thenReturn("localhost");
        when(session.getAttribute("userid")).thenReturn("user1");
        when(session.getAttribute("password")).thenReturn("pw");

        Pop3Agent mockPop3 = mock(Pop3Agent.class);
        when(mockPop3.getMessageList()).thenReturn("dummy messages");
        when(pop3AgentFactory.create(
                anyString(), anyString(), anyString()
        )).thenReturn(mockPop3);

        ReflectionTestUtils.setField(controller, "session", session);
        String result = controller.mainMenu(model);

        assertEquals("main_menu", result);
        verify(model).addAttribute("messageList", "dummy messages");
    }

    @Test
    void testAddUser() {
        String viewName = controller.addUser();
        assertEquals("admin/add_user", viewName);
    }

    @Test
    void testAddUserDo() {
        RedirectAttributes attrs = mock(RedirectAttributes.class);
        UserAdminAgent agent = mock(UserAdminAgent.class);

        when(ctx.getRealPath(".")).thenReturn(".");
        when(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(agent);
        when(agent.addUser("testuser", "pw1234")).thenReturn(true);

        String result = controller.addUserDo("testuser", "pw1234", attrs);
        assertEquals("redirect:/admin_menu", result);
    }

    @Test
    void testDeleteUserDo() {
        RedirectAttributes attrs = mock(RedirectAttributes.class);
        UserAdminAgent agent = mock(UserAdminAgent.class);

        when(ctx.getRealPath(".")).thenReturn(".");
        when(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(agent);

        String[] selected = {"a@test.com", "b@test.com"};
        String result = controller.deleteUserDo(selected, attrs);

        verify(agent).deleteUsers(selected);
        assertEquals("redirect:/admin_menu", result);
    }

    @Test
    void testImgTest() {
        assertEquals("img_test/img_test", controller.imgTest());
    }

    @Test
    void testLoginFail() {
        assertEquals("login_fail", controller.loginFail());
    }

    @Test
    void testLoginDoFail() {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("host")).thenReturn("localhost");
        when(request.getParameter("userid")).thenReturn("user1");
        when(request.getParameter("passwd")).thenReturn("wrongpw");

        Pop3Agent pop3 = mock(Pop3Agent.class);
        when(pop3.validate()).thenReturn(false);
        when(pop3AgentFactory.create("localhost", "user1", "wrongpw")).thenReturn(pop3);

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        String result = controller.loginDo(CommandType.LOGIN, redirectAttributes);
        assertEquals("redirect:/login_fail", result);
    }

    @Test
    void testLoginDoLogout() {
        String result = controller.loginDo(CommandType.LOGOUT, mock(RedirectAttributes.class));
        verify(session).invalidate();
        assertEquals("redirect:/", result);
    }

    @Test
    void testAdminMenu() {
        Model model = mock(Model.class);
        when(ctx.getRealPath(".")).thenReturn(".");

        UserAdminAgent mockAgent = mock(UserAdminAgent.class);
        when(mockAgent.getUserList())
                .thenReturn(new ArrayList<>(List.of("user1", "user2")));

        when(userAdminAgentFactory.create(
                anyString(), anyInt(), anyString(),
                anyString(), anyString(), anyString()
        )).thenReturn(mockAgent);

        ReflectionTestUtils.setField(controller, "ctx", ctx);
        String result = controller.adminMenu(model);

        assertEquals("admin/admin_menu", result);
        verify(model).addAttribute(eq("userList"), any());
    }

    @Test
    void testIsAdmin() {
        assertTrue(controller.isAdmin("admin"));
        assertFalse(controller.isAdmin("notadmin"));
    }
}
