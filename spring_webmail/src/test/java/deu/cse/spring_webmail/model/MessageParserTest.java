package deu.cse.spring_webmail.model;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageParserTest {

    private Message mockMessage;
    private HttpServletRequest mockRequest;
    private MessageParser parser;

    @BeforeEach
    void setUp() throws Exception {
        mockMessage = mock(Message.class);
        mockRequest = mock(HttpServletRequest.class);
        ServletContext mockContext = mock(ServletContext.class);
        when(mockRequest.getServletContext()).thenReturn(mockContext);
        when(mockContext.getRealPath(anyString())).thenReturn(System.getProperty("java.io.tmpdir"));

        parser = new MessageParser(mockMessage, "tester", mockRequest);
    }

    @Test
    void parse_withTextPlain_shouldReadBody() throws Exception {
        when(mockMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@test.com")});
        when(mockMessage.getRecipients(Message.RecipientType.TO)).thenReturn(new Address[]{new InternetAddress("to@test.com")});
        when(mockMessage.getSubject()).thenReturn("Test Subject");
        when(mockMessage.getSentDate()).thenReturn(new Date());

        BodyPart part = new MimeBodyPart();
        ((MimeBodyPart) part).setText("Hello\r\nWorld", "utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(part);
        when(mockMessage.getContent()).thenReturn(multipart);
        when(mockMessage.isMimeType("multipart/*")).thenReturn(true);

        assertTrue(parser.parse(true));
        assertTrue(parser.getBody().contains("<br>"));
    }

    @Test
    void parse_withAttachment_shouldSaveFile() throws Exception {
        when(mockMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("from@test.com")});
        when(mockMessage.getRecipients(Message.RecipientType.TO)).thenReturn(new Address[]{new InternetAddress("to@test.com")});
        when(mockMessage.getSentDate()).thenReturn(new Date());

        BodyPart attachment = new MimeBodyPart() {
            @Override
            public String getDisposition() {
                return Part.ATTACHMENT;
            }

            @Override
            public String getFileName() {
                return "test.txt";
            }

            @Override
            public DataHandler getDataHandler() {
                return new DataHandler("dummy", "text/plain") {
                    @Override
                    public void writeTo(OutputStream os) {
                        // nothing
                    }
                };
            }
        };

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(attachment);
        when(mockMessage.getSubject()).thenReturn("Attach");
        when(mockMessage.getContent()).thenReturn(multipart);
        when(mockMessage.isMimeType("multipart/*")).thenReturn(true);

        assertTrue(parser.parse(true));
        assertEquals("test.txt", parser.getFileName());
    }

    @Test
    void parse_withInvalidMessage_shouldFail() throws Exception {
        Message badMessage = mock(Message.class);
        when(badMessage.getFrom()).thenThrow(new MessagingException("bad"));
        MessageParser errorParser = new MessageParser(badMessage, "tester", mockRequest);
        assertFalse(errorParser.parse(true));
    }

    @Test
    void getAddresses_shouldFormatCorrectly() throws Exception {
        Address[] addresses = new Address[]{
            new InternetAddress("a@test.com"),
            new InternetAddress("b@test.com")
        };
        String result = parser.getAddresses(addresses);
        assertEquals("a@test.com, b@test.com", result.trim());
    }
}
