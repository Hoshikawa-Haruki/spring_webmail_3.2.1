/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package deu.cse.spring_webmail.security;

import deu.cse.spring_webmail.control.SystemController;
import deu.cse.spring_webmail.factory.Pop3AgentFactory;
import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.repository.AddrbookRepository;
import deu.cse.spring_webmail.service.AddrbookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 *
 * @author Haruki
 */
@WebMvcTest(SystemController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Pop3AgentFactory pop3AgentFactory;

    @MockBean
    private Pop3Agent pop3Agent;

    @MockBean
    private AddrbookService addrbookService;

    @MockBean
    private AddrbookRepository addrbookRepository;

    @BeforeEach
    void setup() {
        given(pop3AgentFactory.createFromSession(any())).willReturn(pop3Agent);
        given(pop3Agent.getMessageList()).willReturn("Mocked Message List");

        given(pop3AgentFactory.create(anyString(), anyString(), anyString())).willReturn(pop3Agent);
        given(pop3Agent.validate()).willReturn(true);  // 로그인 성공 시나리오
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void testLoginSuccessAsAdmin() throws Exception {
        mockMvc.perform(get("/admin_menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_menu"))
                .andExpect(model().attributeExists("userList"));
    }

    @WithMockUser(username = "user1", roles = {"USER"})
    @Test
    void testLoginSuccessAsUser() throws Exception {
        mockMvc.perform(get("/main_menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("main_menu"))
                .andExpect(model().attributeExists("messageList"));
    }

    @Test
    void testLoginFail() throws Exception {
        // 실패 시나리오에 맞게 validate()가 false 반환하도록 별도 지정
        given(pop3Agent.validate()).willReturn(false);

        mockMvc.perform(post("/login.do")
                .param("userid", "wrong")
                .param("passwd", "badpw"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_fail"))
                .andExpect(request().sessionAttribute("loginErrorUserid", "wrong"));
    }

    @Test
    void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userid", "user1");
        session.setAttribute("password", "userpw");

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testLoginSuccessAsAdminRedirect() throws Exception { // 실제 아이디가 사용되므로 삭제해야할 가능성 O
        mockMvc.perform(post("/login.do")
                .param("userid", "test@test.com") // ADMIN 권한이 부여되는 아이디
                .param("passwd", "test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin_menu"))
                .andExpect(request().sessionAttribute("userid", "test@test.com"))
                .andExpect(request().sessionAttribute("password", "test"))
                .andExpect(request().sessionAttribute("host", "localhost"));
    }

    @Test
    void testLoginSuccessAsUserRedirect() throws Exception {
        mockMvc.perform(post("/login.do")
                .param("userid", "user")
                .param("passwd", "userpw"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main_menu"))
                .andExpect(request().sessionAttribute("userid", "user"))
                .andExpect(request().sessionAttribute("password", "userpw"))
                .andExpect(request().sessionAttribute("host", "localhost"));
    }

    // @WebMvcTest 같은 경량 컨트롤러 테스트에서는 세션 invalidate 같은 필터(시큐리티 필터) 내부 동작을 실제로 검증하기 어려움
    @Test
    void testLogoutWithSessionInvalidation() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userid", "user");
        session.setAttribute("password", "pw");

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
