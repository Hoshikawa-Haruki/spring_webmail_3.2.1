package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.factory.UserAdminAgentFactory;
import deu.cse.spring_webmail.model.UserAdminAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAdminAgentFactoryTest {

    private UserAdminAgentFactory factory;

    @BeforeEach
    void setUp() {
        factory = new UserAdminAgentFactory();
    }

    @Test
    void testCreate() {
        UserAdminAgent agent = factory.create(
                "localhost", 110, "/mail", "root", "adminpw", "admin");

        assertNotNull(agent);  // 객체가 잘 생성되었는지
        assertEquals("localhost", agent.getServer());
        assertEquals(110, agent.getPort());
        assertEquals("root", agent.getRootId());
        assertEquals("adminpw", agent.getRootPw());
        assertEquals("admin", agent.getAdminId());
    }
}
