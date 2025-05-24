package deu.cse.spring_webmail.model;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Store;
import jakarta.mail.Flags;
import jakarta.mail.FetchProfile;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Pop3AgentTest {

    @InjectMocks
    private Pop3Agent pop3Agent;

    @Mock
    private Store mockStore;

    @Mock
    private Folder mockFolder;

    @Mock
    private Message mockMessage;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Pop3Agent 인스턴스를 spy로 생성하여 일부 실제 메서드는 실행 가능하게 설정
        pop3Agent = Mockito.spy(new Pop3Agent("mockHost", "mockUser", "mockPass"));

        // Store 객체 주입 (connectToStore 내부에서 생성되는 것을 가정)
        pop3Agent.setStore(mockStore);

        // Store#getFolder("INBOX") 호출 시 mockFolder 리턴
        when(mockStore.getFolder("INBOX")).thenReturn(mockFolder);

        // Folder open() 메서드 모킹 - 아무 동작도 하지 않음
        doNothing().when(mockFolder).open(anyInt());

        // Folder close(true) 메서드 모킹 - 아무 동작도 하지 않음
        doNothing().when(mockFolder).close(true);

        // Store close() 메서드 모킹 - 아무 동작도 하지 않음
        doNothing().when(mockStore).close();

        // validate()에서 호출하는 private connectToStore()는 spy 처리로 대체하여 true 리턴하도록 모킹
        doReturn(true).when(pop3Agent).connectToStore();

        doNothing().when(mockFolder).close(anyBoolean());
    }

    @Test
    void testValidate_success() throws MessagingException {
        // validate()는 내부에서 connectToStore() 호출하므로, spy 처리된 connectToStore()가 true 리턴
        assertTrue(pop3Agent.validate());

        // Store close() 호출 확인
        verify(mockStore).close();
    }

    @Test
    void testDeleteMessage_success() throws Exception {
        int msgId = 1;
        boolean reallyDelete = true;

        // Folder#getMessage() 호출 시 mockMessage 리턴
        when(mockFolder.getMessage(msgId)).thenReturn(mockMessage);

        // Message#setFlag() 모킹 - 아무 동작 안함
        doNothing().when(mockMessage).setFlag(Flags.Flag.DELETED, reallyDelete);

        boolean result = pop3Agent.deleteMessage(msgId, reallyDelete);

        assertTrue(result);

        verify(mockFolder).open(Folder.READ_WRITE);
        verify(mockFolder).close(true);
        verify(mockStore).close();
        verify(mockMessage).setFlag(Flags.Flag.DELETED, reallyDelete);
    }

    @Test
    void testGetMessageList_success() throws Exception {
        Message[] messages = new Message[]{mockMessage};

        when(mockFolder.getMessages()).thenReturn(messages);

        // Folder fetch() 모킹 (void 메서드)
        doNothing().when(mockFolder).fetch(eq(messages), any(FetchProfile.class));

        String result = pop3Agent.getMessageList();

        assertNotNull(result);
        assertFalse(result.contains("POP3 연결이 되지 않아"));

        verify(mockFolder).open(Folder.READ_ONLY);
        verify(mockFolder).fetch(eq(messages), any(FetchProfile.class));
        verify(mockFolder).close(true);
        verify(mockStore).close();
    }

//    @Test
//    void testGetMessage_success() throws Exception {
//        int msgNum = 1;
//
//        when(mockFolder.getMessage(msgNum)).thenReturn(mockMessage);
//        doNothing().when(mockFolder).open(Folder.READ_ONLY);
//        doNothing().when(mockFolder).close(true);  // 명확히 true로 모킹
//
//        String result = pop3Agent.getMessage(msgNum);
//
//        assertNotNull(result);
//        assertFalse(result.contains("POP3 서버 연결이 되지 않아"));
//
//        verify(mockFolder).open(Folder.READ_ONLY);
//        verify(mockFolder).close(true);
//        verify(mockStore).close();
//    }
}
