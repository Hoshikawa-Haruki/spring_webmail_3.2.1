package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.model.UserAdminAgent;
import deu.cse.spring_webmail.repository.AddrbookRepository;
import deu.cse.spring_webmail.service.AddrbookService;
import jakarta.servlet.ServletContext;
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

import java.util.List;

@WebMvcTest(SystemController.class)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Pop3AgentFactory pop3AgentFactory;

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

    @Test
    void testLoginSuccessForNormalUser() throws Exception {
        Pop3Agent agent = mock(Pop3Agent.class);
        given(agent.validate()).willReturn(true);
        given(pop3AgentFactory.create(any(), any(), any())).willReturn(agent);

        session.setAttribute("host", "localhost");

        mockMvc.perform(post("/login.do")
                .param("menu", "91")
                .param("userid", "user1")
                .param("passwd", "pw1234")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"));
    }

    @Test
    void testLoginFail() throws Exception {
        Pop3Agent agent = mock(Pop3Agent.class);
        given(agent.validate()).willReturn(false);
        given(pop3AgentFactory.create(any(), any(), any())).willReturn(agent);

        session.setAttribute("host", "localhost");

        mockMvc.perform(post("/login.do")
                .param("menu", "91")
                .param("userid", "wrong")
                .param("passwd", "fail")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login_fail*"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/login.do")
                .param("menu", "92")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
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
    void testAddUserDo() throws Exception {
        UserAdminAgent agent = mock(UserAdminAgent.class);
        given(agent.addUser(eq("tester"), eq("pw123"))).willReturn(true);
        given(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .willReturn(agent);
        given(servletContext.getRealPath(".")).willReturn(".");

        mockMvc.perform(post("/add_user.do")
                .param("id", "tester")
                .param("password", "pw123")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"));
    }

    @Test
    void testDeleteUserDo() throws Exception {
        UserAdminAgent agent = mock(UserAdminAgent.class);
        given(userAdminAgentFactory.create(any(), anyInt(), any(), any(), any(), any()))
                .willReturn(agent);
        given(servletContext.getRealPath(".")).willReturn(".");

        mockMvc.perform(post("/delete_user.do")
                .param("selectedUsers", "a@test.com", "b@test.com")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"));
    }
}
