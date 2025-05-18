/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import deu.cse.spring_webmail.PropertyReader;
import jakarta.activation.DataHandler;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeUtility;
import java.io.File;
import java.io.FileOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author skylo
 */
@Slf4j
@RequiredArgsConstructor
public class MessageParser {
    @NonNull @Getter @Setter private Message message;
    @NonNull @Getter @Setter private String userid;
    @Getter @Setter private String toAddress;
    @Getter @Setter private String fromAddress;
    @Getter @Setter private String ccAddress;
    @Getter @Setter private String sentDate;
    @Getter @Setter private String subject;
    @Getter @Setter private String body;
    @Getter @Setter private String fileName;
    @Getter @Setter private String downloadTempDir = "C:/temp/download/";

    public MessageParser(Message message, String userid, HttpServletRequest request) {
        this(message, userid);
        PropertyReader props = new PropertyReader();
        String downloadPath = props.getProperty("file.download_folder");
        downloadTempDir = request.getServletContext().getRealPath(downloadPath);
        File f = new File(downloadTempDir);
        if (!f.exists()) {
            f.mkdir();
        }
    }

    public boolean parse(boolean parseBody) {
        boolean status = false;

        try {
            getEnvelope(message);
            if (parseBody) {
                getPart(message);
            }
            // 220611 LJM: 필요시 true로 하여 메시지 본문 볼 수 있도록 할 것.
            // printMessage(false);  
            //  예외가 발생하지 않았으므로 정상적으로 동작하였음.
            status = true;
        } catch (Exception ex) {
            log.error("MessageParser.parse() - Exception : {}", ex.getMessage());
            status = false;
        }
        return status; //finally 삭제 후 return만 남김
    }

    private void getEnvelope(Message m) throws Exception {
        fromAddress = message.getFrom()[0].toString();  // 101122 LJM : replaces getMyFrom2()
        toAddress = getAddresses(message.getRecipients(Message.RecipientType.TO));
        Address[] addr = message.getRecipients(Message.RecipientType.CC);
        if (addr != null) {
            ccAddress = getAddresses(addr);
        } else {
            ccAddress = "";
        }
        subject = message.getSubject();
        sentDate = message.getSentDate().toString();
        sentDate = sentDate.substring(0, sentDate.length() - 8);  // 8 for "KST 20XX"
    }

    // ref: http://www.oracle.com/technetwork/java/faq-135477.html#readattach
    /**
     * 메일의 Part 객체를 분석하여 본문 또는 첨부파일을 처리하는 메서드 - 첨부파일은 saveAttachedFile()로 저장 -
     * 텍스트 또는 멀티파트는 재귀적으로 파싱
     */
    private void getPart(Part p) throws Exception {
        if (isAttachment(p)) {
            saveAttachedFile(p);
        } else if (p.isMimeType("text/*")) {
            readText(p);
        } else if (p.isMimeType("multipart/alternative")) {
            readPlainTextInHtmlEmail(p);
        } else if (p.isMimeType("multipart/*")) {
            readEachPart(p);
        }
    }

    /**
     * 첨부파일인지 확인하는 메서드 - Part의 Disposition이 ATTACHMENT 또는 INLINE인 경우 true 반환
     */
    private boolean isAttachment(Part p) throws Exception {
        String disp = p.getDisposition();
        return disp != null
                && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE));
    }

    /**
     * 첨부파일을 서버에 저장하는 메서드 - 파일명을 디코딩하고 사용자 임시 폴더에 저장함
     */
    private void saveAttachedFile(Part p) throws Exception {
        String fileName = MimeUtility.decodeText(p.getFileName());
        if (fileName == null) {
            return;
        }

        String tempUserDir = this.downloadTempDir + File.separator + this.userid;
        File dir = new File(tempUserDir);
        if (!dir.exists()) {
            dir.mkdir();
        }

        String decodedFileName = MimeUtility.decodeText(p.getFileName());
        DataHandler dh = p.getDataHandler();
        try (FileOutputStream fos = new FileOutputStream(tempUserDir + File.separator + decodedFileName)) {
            dh.writeTo(fos);
            fos.flush();
        }
        this.fileName = decodedFileName;
    }

    /**
     * 일반 텍스트 내용을 본문(body)으로 읽어오는 메서드 - text/plain인 경우 개행을 <br>로 변환
     */
    private void readText(Part p) throws Exception {
        body = (String) p.getContent();
        if (p.isMimeType("text/plain")) {
            body = body.replace("\r\n", " <br>");
        }
    }

    /**
     * HTML 이메일에서 text/plain 본문만 선택적으로 읽는 메서드 - multipart/alternative에서
     * text/plain만 재귀적으로 파싱
     */
    private void readPlainTextInHtmlEmail(Part p) throws Exception {
        Multipart mp = (Multipart) p.getContent();
        for (int i = 0; i < mp.getCount(); i++) {
            Part bp = mp.getBodyPart(i);
            if (bp.isMimeType("text/plain")) {
                getPart(bp); // 재귀 호출
            }
        }
    }

    /**
     * multipart 메일의 각 파트를 반복적으로 파싱하는 메서드
     */
    private void readEachPart(Part p) throws Exception {
        Multipart mp = (Multipart) p.getContent();
        for (int i = 0; i < mp.getCount(); i++) {
            getPart(mp.getBodyPart(i)); // 재귀 호출
        }
    }

    private void printMessage(boolean printBody) {
        System.out.println("From: " + fromAddress);
        System.out.println("To: " + toAddress);
        System.out.println("CC: " + ccAddress);
        System.out.println("Date: " + sentDate);
        System.out.println("Subject: " + subject);

        if (printBody) {
            System.out.println("본 문");
            System.out.println("---------------------------------");
            System.out.println(body);
            System.out.println("---------------------------------");
            System.out.println("첨부파일: " + fileName);
        }
    }

    private String getAddresses(Address[] addresses) {
        StringBuilder buffer = new StringBuilder();

        for (Address address : addresses) {
            buffer.append(address.toString());
            buffer.append(", ");
        } // 마지막에 있는 ", " 삭제
        int start = buffer.length() - 2;
        int end = buffer.length() - 1;
        buffer.delete(start, end);
        return buffer.toString();
    }
}
