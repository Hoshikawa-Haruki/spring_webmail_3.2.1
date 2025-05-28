/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deu.cse.spring_webmail.model;

import com.sun.mail.smtp.SMTPMessage;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author jongmin
 */
@Slf4j
public class SmtpAgent {

    @Getter @Setter  protected String host = null;
    @Getter @Setter  protected String userid = null;
    @Getter @Setter protected String to = null;
    @Getter @Setter protected String cc = null;
    @Getter @Setter protected String subj = null;
    @Getter @Setter protected String body = null;
    @Getter @Setter protected String file1 = null;

    private List<String> attachments = new ArrayList<>();

    public SmtpAgent(String host, String userid) {
        this.host = host;
        this.userid = userid;
    }

    public void addAttachment(String path) {
        attachments.add(path);
    }

    public boolean sendMessage() {
        boolean status = false;

        Properties props = System.getProperties();
        props.put("mail.debug", false);
        props.put("mail.smtp.host", this.host);
        log.debug("SMTP host : {}", props.get("mail.smtp.host"));

        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(false);

        try {
            SMTPMessage msg = new SMTPMessage(session);

            msg.setFrom(new InternetAddress(this.userid));

            if (this.to.contains(";")) {
                this.to = this.to.replace(";", ",");
            }
            msg.setRecipients(Message.RecipientType.TO, this.to);

            if (this.cc != null && this.cc.length() > 1) {
                if (this.cc.contains(";")) {
                    this.cc = this.cc.replace(";", ",");
                }
                msg.setRecipients(Message.RecipientType.CC, this.cc);
            }

            msg.setSubject(this.subj);
            msg.setHeader("User-Agent", "LJM-WM/0.1");

            Multipart mp = new MimeMultipart();

            // 본문
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setText(this.body);
            mp.addBodyPart(mbp);

            // 첨부파일 처리
            for (String filePath : attachments) {
                MimeBodyPart attachPart = new MimeBodyPart();
                DataSource src = new FileDataSource(filePath);
                attachPart.setDataHandler(new DataHandler(src));
                String fileName = new File(filePath).getName();
                attachPart.setFileName(MimeUtility.encodeText(fileName, "UTF-8", "B"));
                mp.addBodyPart(attachPart);
            }

            msg.setContent(mp);

            Transport.send(msg);

            // 전송 후 첨부파일 삭제
            for (String filePath : attachments) {
                File f = new File(filePath);
                if (!f.delete()) {
                    log.error("{}: 파일 삭제 실패", filePath);
                }
            }

            status = true;
        } catch (Exception ex) {
            log.error("sendMessage() error: {}", ex);
        }

        return status;
    }
}
