package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.service.AddrbookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WithMockUser(username = "user1", roles = "USER")
@WebMvcTest(AddrController.class)
class AddrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddrbookService addrbookService;

    private MockHttpSession session;

    @BeforeEach
    void setup() {
        session = new MockHttpSession();
        session.setAttribute("userid", "user1");
    }

    @Test
    void testinsertAddr_fail() throws Exception {
        given(addrbookService.isAlreadyRegistered("user1", "test@test.com")).willReturn(true);

        mockMvc.perform(post("/jpa/insert_addr")
                .param("name", "홍길동")
                .param("email", "test@test.com")
                .param("phone", "010-1234-5678")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/show_addr"))
                .andExpect(flash().attribute("msg", "이미 등록된 이메일입니다."));
    }

    @Test
    void testinsertAddr_success() throws Exception {
        given(addrbookService.isAlreadyRegistered("user1", "test@test.com")).willReturn(false);

        mockMvc.perform(post("/jpa/insert_addr")
                .param("name", "홍길동")
                .param("email", "test@test.com")
                .param("phone", "010-1234-5678")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/show_addr"))
                .andExpect(flash().attribute("msg", "주소록에 추가되었습니다."));

        then(addrbookService).should().addEntry("user1", "홍길동", "test@test.com", "010-1234-5678");
    }

    @Test
    void testdeleteAddr_success() throws Exception {
        mockMvc.perform(post("/jpa/delete_addr")
                .param("del_email", "test@test.com")
                .session(session)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/show_addr"))
                .andExpect(flash().attribute("msg", "주소록에서 삭제되었습니다."));

        then(addrbookService).should().deleteEntry("user1", "test@test.com");
    }

    @Test
    void testshowAddr() throws Exception {
        mockMvc.perform(get("/show_addr"))
                .andExpect(status().isOk())
                .andExpect(view().name("addr_menu/addr_book"));
    }

    @Test
    void testeinsertAddr() throws Exception {
        mockMvc.perform(get("/insert_addr"))
                .andExpect(status().isOk())
                .andExpect(view().name("addr_menu/addr_insert"));
    }
}
