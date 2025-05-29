/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import java.util.Properties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author skylo
 */
@Slf4j
@NoArgsConstructor        // 기본 생성자 생성
public class Pop3Agent {

    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private String userid;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private Store store;
    @Getter
    @Setter
    private String excveptionType;
    @Getter
    @Setter
    private HttpServletRequest request;

    // 220612 LJM - added to implement REPLY
    @Getter
    private String sender;
    @Getter
    private String subject;
    @Getter
    private String body;

    //상수 선언
    private static final String INBOX_FOLDER = "INBOX";
    private static final String PROP_TRUE = "true";
    private static final String PROP_FALSE = "false";

    public Pop3Agent(String host, String userid, String password) {
        this.host = host;
        this.userid = userid;
        this.password = password;
    }

    public boolean validate() {
        boolean status = false;

        try {
            status = connectToStore();
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.validate() error : " + ex);
            status = false;  // for clarity
        }
        return status;
    }

    public boolean deleteMessage(int msgid, boolean really_delete) {
        boolean status = false;

        if (!connectToStore()) {
            return status;
        }

        try {
            // Folder 설정
//            Folder folder = store.getDefaultFolder();
            Folder folder = store.getFolder(INBOX_FOLDER);
            folder.open(Folder.READ_WRITE);

            // Message에 DELETED flag 설정
            Message msg = folder.getMessage(msgid);
            msg.setFlag(Flags.Flag.DELETED, really_delete);

            // 폴더에서 메시지 삭제
            // Message [] expungedMessage = folder.expunge();
            // <-- 현재 지원 안 되고 있음. 폴더를 close()할 때 expunge해야 함.
            folder.close(true);  // expunge == true
            store.close();
            status = true;
        } catch (Exception ex) {
            log.error("deleteMessage() error: {}", ex.getMessage());
        }
        return status;
    }

    /*
     2025.05.28 페이지네이션 구현 lsh
     메일 목록을 페이지 단위로 HTML 문자열로 반환
     전체 메시지를 받아와서 start~end만 잘라서 HTML 조립
     page: 현재 페이지 번호 (1부터 시작)
     pageSize: 한 페이지에 보여줄 메일 수
     */
    public String getMessageList(int page, int pageSize) {
        String result = "";
        Message[] messages = null;

        if (!connectToStore()) {
            log.error("POP3 connection failed!");
            return "POP3 연결이 되지 않아 메일 목록을 볼 수 없습니다.";
        }

        try {
            Folder folder = store.getFolder(INBOX_FOLDER);
            folder.open(Folder.READ_ONLY);

            messages = folder.getMessages();  // 오래된 메일이 앞쪽

            int total = messages.length;

            // 최신순으로 잘라오기 위해 뒤에서부터 잘라옴
            int start = total - (page * pageSize);
            int end = total - ((page - 1) * pageSize);
            if (start < 0) {
                start = 0;
            }

            Message[] pageMessages = Arrays.copyOfRange(messages, start, end);  // 최신순

            // 최신이 위로 오게 순서 반전
            Message[] reversed = new Message[pageMessages.length];
            for (int i = 0; i < pageMessages.length; i++) {
                reversed[i] = pageMessages[pageMessages.length - 1 - i];
            }

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(reversed, fp);

            StringBuilder sb = new StringBuilder();
            sb.append("<table border='1'>");
            sb.append("<tr><th>No.</th><th>보낸 사람</th><th>제목</th><th>날짜</th><th>삭제</th></tr>");

            for (int i = 0; i < reversed.length; i++) {
                Message msg = reversed[i];

                // 메일의 실제 index 계산 → 가장 오래된 메일이 1번
                int actualIndexInMessages = total - ((page - 1) * pageSize) - i - 1;
                int number = actualIndexInMessages + 1;

                String from = msg.getFrom()[0].toString();
                String subject = msg.getSubject();
                String sentDate = msg.getSentDate().toString();
                int msgid = msg.getMessageNumber();

                sb.append("<tr>");
                sb.append("<td>").append(number).append("</td>");
                sb.append("<td>").append(from).append("</td>");
                sb.append("<td><a href=\"show_message?msgid=").append(msgid).append("\">")
                        .append(subject).append("</a></td>");
                sb.append("<td>").append(sentDate).append("</td>");
                sb.append("<td><a href=\"delete_mail.do?msgid=")
                        .append(msgid)
                        .append("\" onclick=\"return confirm('정말로 삭제하시겠습니까?');\">삭제</a></td>");

                sb.append("</tr>");
            }

            sb.append("</table>");

            result = sb.toString();

            folder.close(true);
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList() : exception = {}", ex.getMessage());
            result = "Pop3Agent.getMessageList() : exception = " + ex.getMessage();
        }

        return result;
    }

    /*
    전체 받은 메일 개수를 반환 (페이지 수 계산에 필요)
    2025.05.28 페이지네이션 구현 lsh
     */
    public int getTotalMessageCount() {
        try {
            if (!connectToStore()) {
                return 0;
            }
            Folder folder = store.getFolder(INBOX_FOLDER);
            folder.open(Folder.READ_ONLY);
            int count = folder.getMessageCount();
            folder.close();
            store.close();
            return count;
        } catch (Exception ex) {
            log.error("getTotalMessageCount() 예외: {}", ex.getMessage());
            return 0;
        }
    }

    public String getMessage(int n) {
        String result = "POP3  서버 연결이 되지 않아 메시지를 볼 수 없습니다.";

        if (!connectToStore()) {
            log.error("POP3 connection failed!");
            return result;
        }

        try {
            Folder folder = store.getFolder(INBOX_FOLDER);
            folder.open(Folder.READ_ONLY);

            Message message = folder.getMessage(n);

            MessageFormatter formatter = new MessageFormatter(userid);
            formatter.setRequest(request);  // 210308 LJM - added
            result = formatter.getMessage(message);
            sender = formatter.getSender();  // 220612 LJM - added
            subject = formatter.getSubject();
            body = formatter.getBody();

            folder.close(true);
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList() : exception = {}", ex);
            result = "Pop3Agent.getMessage() : exception = " + ex;
        }
        return result;
    }

    boolean connectToStore() {
        boolean status = false;
        Properties props = System.getProperties();
        // https://jakarta.ee/specifications/mail/2.1/apidocs/jakarta.mail/jakarta/mail/package-summary.html
        props.setProperty("mail.pop3.host", host);
        props.setProperty("mail.pop3.user", userid);
        props.setProperty("mail.pop3.apop.enable", PROP_FALSE);
        props.setProperty("mail.pop3.disablecapa", PROP_TRUE);  // 200102 LJM - added cf. https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
        props.setProperty("mail.debug", PROP_FALSE);
        props.setProperty("mail.pop3.debug", PROP_FALSE);

        Session session = Session.getInstance(props);
        session.setDebug(false);

        try {
            store = session.getStore("pop3");
            store.connect(host, userid, password);
            status = true;
        } catch (Exception ex) {
            log.error("connectToStore 예외: {}", ex.getMessage());
        }
        return status;
    }

}
