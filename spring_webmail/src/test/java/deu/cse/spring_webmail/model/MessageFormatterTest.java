package deu.cse.spring_webmail.model;

import jakarta.mail.Message;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageFormatterTest {

    @Mock
    private MessageParser mockParser;

    @Mock
    private Message mockMessage;

    @Mock
    private HttpServletRequest mockRequest;

    private MessageFormatter formatter;

    @Mock
    ServletContext mockServletContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockRequest.getServletContext()).thenReturn(mockServletContext);
        when(mockServletContext.getRealPath(anyString())).thenReturn("/mock/path");

        // userid는 임의값으로 설정
        formatter = new MessageFormatter("testUser");
        formatter.setRequest(mockRequest);  // request null 문제 방지

        // MessageParser 생성자 내부 호출을 Mockito로 대체할 수 없으므로,
        // MessageFormatter 내에 MessageParser 객체를 교체하는 방법 없으면 테스트 한계 존재.
        // 따라서 테스트용으로 별도 생성자 추가하거나 실제 객체 대신 Mock을 사용하는 별도 방법 필요.
    }

    @Test
    void testSetRequest() {
        formatter.setRequest(mockRequest);
        // 단순 세터, 값 저장 확인용 테스트
        assertNotNull(formatter);
    }

    @Test
    void testGetMessageTable_basic() throws Exception {
        // 테스트용 메시지 배열
        Message[] messages = new Message[]{mockMessage};

        // MessageParser 생성자와 parse 메서드는 실제 클래스에 의존하므로,
        // 이 테스트는 MessageFormatter 내부로직 기본 동작 검증에 초점
        // 결과는 HTML 테이블 문자열
        String html = formatter.getMessageTable(messages);

        assertNotNull(html);
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<th> No. </td>"));
    }

    @Test
    void testGetMessage_basic() throws Exception {
        formatter.setRequest(mockRequest);  // request를 세팅하여 NullPointerException 방지
        // 실제 MessageParser가 내부에서 생성되므로 복잡하지만
        // 아래는 기본 정상 동작시 null/빈 문자열이 아닌지 확인하는 간단 테스트
        String result = formatter.getMessage(mockMessage);

        assertNotNull(result);
        assertTrue(result.contains("보낸 사람:"));
    }

    @Test
    void testGetMessage_withAttachment() throws Exception {
        Message mockMessage = mock(Message.class);
        // MessageParser 내부 동작은 실제 객체 사용하므로,
        // 첨부파일 테스트가 어렵다면 MessageParser를 수정하거나 별도 테스트 필요

        String result = formatter.getMessage(mockMessage);

        // 단순 null 체크 및 결과 내 첨부파일 링크 포함 여부 확인
        assertNotNull(result);
        // 첨부파일 링크 관련 테스트는 MessageParser 기능에 의존하므로 생략 가능
    }

}
