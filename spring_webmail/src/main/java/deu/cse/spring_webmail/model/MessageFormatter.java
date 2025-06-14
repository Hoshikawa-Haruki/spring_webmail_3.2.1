/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import jakarta.mail.Message;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author skylo
 */
@Slf4j
@RequiredArgsConstructor
public class MessageFormatter {
    @NonNull private String userid;  // 파일 임시 저장 디렉토리 생성에 필요
    private HttpServletRequest request = null;

    // 250507 상수 추가
    private static final String HTML_BREAK = " <br>";
    private static final String HTML_HR = " <hr>";

    // 220612 LJM - added to implement REPLY
    @Getter private String sender;
    @Getter private String subject;
    @Getter private String body;

//   2025.05.28 페이지 네이션 적용 후 사용 X
//   pop3Agent getMessageList로 이관
//    public String getMessageTable(Message[] messages) {
//        StringBuilder buffer = new StringBuilder();
//
//        // 메시지 제목 보여주기
//        buffer.append("<table>");  // table start
//        buffer.append("<tr> "
//                + " <th> No. </td> "
//                + " <th> 보낸 사람 </td>"
//                + " <th> 제목 </td>     "
//                + " <th> 보낸 날짜 </td>   "
//                + " <th> 삭제 </td>   "
//                + " </tr>");
//
//        for (int i = messages.length - 1; i >= 0; i--) {
//            MessageParser parser = new MessageParser(messages[i], userid);
//            parser.parse(false);  // envelope 정보만 필요
//            // 메시지 헤더 포맷
//            // 추출한 정보를 출력 포맷 사용하여 스트링으로 만들기
//            buffer.append("<tr> "
//                    + " <td id=no>" + (i + 1) + " </td> "
//                    + " <td id=sender>" + parser.getFromAddress() + "</td>"
//                    + " <td id=subject> "
//                    + " <a href=show_message?msgid=" + (i + 1) + " title=\"메일 보기\"> "
//                    + parser.getSubject() + "</a> </td>"
//                    + " <td id=date>" + parser.getSentDate() + "</td>"
//                    + " <td id=delete>"
//                    + "<a href='delete_mail.do?msgid=" + (i + 1) + "' onclick=\"return confirm('정말 삭제하시겠습니까?');\">삭제</a>"
//                    + " </tr>");
//        }
//        buffer.append("</table>");
//
//        return buffer.toString();
////        return "MessageFormatter 테이블 결과";
//    }

    public String getMessage(Message message) {
        StringBuilder buffer = new StringBuilder();

        // MessageParser parser = new MessageParser(message, userid);
        MessageParser parser = new MessageParser(message, userid, request);
        parser.parse(true);

        sender = parser.getFromAddress();
        subject = parser.getSubject();
        body = parser.getBody();

        buffer.append("보낸 사람: ").append(parser.getFromAddress()).append(HTML_BREAK)
                .append("받은 사람: ").append(parser.getToAddress()).append(HTML_BREAK)
                .append("Cc &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; : ").append(parser.getCcAddress()).append(HTML_BREAK)
                .append("보낸 날짜: ").append(parser.getSentDate()).append(HTML_BREAK)
                .append("제 &nbsp;&nbsp;&nbsp;  목: ").append(parser.getSubject()).append(HTML_BREAK).append(HTML_HR)
                .append(parser.getBody());

        List<String> attachedFiles = parser.getAttachmentNames();
        if (!attachedFiles.isEmpty()) {
            buffer.append(HTML_BREAK).append(HTML_HR)
                    .append("첨부파일:").append(HTML_BREAK);

            for (String attachedFile : attachedFiles) {
                buffer.append("<a href=download?userid=")
                        .append(this.userid)
                        .append("&filename=")
                        .append(attachedFile.replace(" ", "%20"))
                        .append(" target=_top>")
                        .append(attachedFile)
                        .append("</a>").append(HTML_BREAK);
            }
        }

        return buffer.toString();
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
}
