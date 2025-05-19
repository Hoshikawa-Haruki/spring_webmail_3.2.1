package deu.cse.spring_webmail.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import java.lang.reflect.Field;
import java.util.List;
import javax.management.remote.JMXConnector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class UserAdminAgentTest {

    private UserAdminAgent agent;
    private MBeanServerConnection mbscMock;
    private ObjectName objectNameMock;

    @BeforeEach
    void setUp() throws Exception {
        agent = new UserAdminAgent();

        mbscMock = mock(MBeanServerConnection.class);
        objectNameMock = new ObjectName("org.apache.james:type=component,name=usersrepository");

        // 리플렉션으로 내부 필드 주입
        injectPrivateField(agent, "mbsc", mbscMock);
        injectPrivateField(agent, "userRepositoryMBean", objectNameMock);
        injectPrivateField(agent, "ADMIN_ID", "admin@test.com");
    }

    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testAddUser_success() throws Exception {
        given(mbscMock.invoke(any(), eq("addUser"), any(), any())).willReturn(null); // ✅ 고친 부분
        assertTrue(agent.addUser("user@test.com", "1234"));
    }

    @Test
    void testAddUser_failure() throws Exception {
        willThrow(new RuntimeException("fail")).given(mbscMock).invoke(any(), eq("addUser"), any(), any());
        assertFalse(agent.addUser("user@test.com", "1234"));
    }

    @Test
    void testGetUserList_excludesAdmin() throws Exception {
        String[] users = {"admin@test.com", "user1@test.com", "user2@test.com"};
        given(mbscMock.invoke(any(), eq("listAllUsers"), isNull(), isNull())).willReturn(users);

        List<String> result = agent.getUserList();
        assertEquals(2, result.size());
        assertFalse(result.contains("admin@test.com"));
    }

    @Test
    void testDeleteUsers_success() throws Exception {
        given(mbscMock.invoke(any(), eq("deleteUser"), any(), any())).willReturn(null);
        assertTrue(agent.deleteUsers(new String[]{"user1@test.com", "user2@test.com"}));
    }

    @Test
    void testDeleteUsers_failure() throws Exception {
        willThrow(new RuntimeException("fail")).given(mbscMock).invoke(any(), eq("deleteUser"), any(), any());
        assertFalse(agent.deleteUsers(new String[]{"user1@test.com"}));
    }

    @Test
    void testVerify_true() throws Exception {
        given(mbscMock.invoke(any(), eq("contains"), any(), any())).willReturn(true);
        assertTrue(agent.verify("user1@test.com"));
    }

    @Test
    void testVerify_false() throws Exception {
        given(mbscMock.invoke(any(), eq("contains"), any(), any())).willReturn(false);
        assertFalse(agent.verify("user1@test.com"));
    }

    @Test
    void testQuit_success() throws Exception {
        JMXConnector connector = mock(JMXConnector.class);
        injectPrivateField(agent, "connector", connector);
        doNothing().when(connector).close();

        assertTrue(agent.quit());
        verify(connector, times(1)).close();
    }

    @Test
    void testQuit_failure() throws Exception {
        JMXConnector connector = mock(JMXConnector.class);
        injectPrivateField(agent, "connector", connector);
        doThrow(new RuntimeException("close failed")).when(connector).close();

        assertFalse(agent.quit());
    }
}
