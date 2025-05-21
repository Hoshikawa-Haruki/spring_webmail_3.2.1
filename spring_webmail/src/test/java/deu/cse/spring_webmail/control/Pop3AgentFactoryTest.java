package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Pop3Agent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class Pop3AgentFactoryTest {

    private Pop3AgentFactory factory;

    @BeforeEach
    void setUp() {
        factory = new Pop3AgentFactory();
    }

    @Test
    void testCreateWithParameters() {
        Pop3Agent agent = factory.create("localhost", "user1", "pw123");
        assertEquals("localhost", agent.getHost());
        assertEquals("user1", agent.getUserid());
        assertEquals("pw123", agent.getPassword());
    }

    @Test
    void testCreateFromSession() {
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(session.getAttribute("host")).thenReturn("mail.test.com");
        Mockito.when(session.getAttribute("userid")).thenReturn("tester");
        Mockito.when(session.getAttribute("password")).thenReturn("pass");

        Pop3Agent agent = factory.createFromSession(session);

        assertEquals("mail.test.com", agent.getHost());
        assertEquals("tester", agent.getUserid());
        assertEquals("pass", agent.getPassword());
        assertNull(agent.getRequest());  // request는 null일 테니까
    }

    @Test
    void testCreateFromSessionWithRequest() {
        HttpSession session = Mockito.mock(HttpSession.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(session.getAttribute("host")).thenReturn("mail.test.com");
        Mockito.when(session.getAttribute("userid")).thenReturn("tester");
        Mockito.when(session.getAttribute("password")).thenReturn("pass");

        Pop3Agent agent = factory.createFromSession(session, request);

        assertEquals("mail.test.com", agent.getHost());
        assertEquals("tester", agent.getUserid());
        assertEquals("pass", agent.getPassword());
        assertEquals(request, agent.getRequest());
    }
}
