/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

/**
 * 05.18 
 * pop3 객체를 만드는 Factory 클래스
 * @author Haruki
 */
import deu.cse.spring_webmail.model.Pop3Agent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class Pop3AgentFactory {

    public Pop3Agent create(String host, String userid, String password) {
        return new Pop3Agent(host, userid, password);
    }

    /**
     * 세션 정보를 기반으로 Pop3Agent를 생성. 주로 세션에서 host/userid/password만 필요한 경우에 사용됩니다.
     *
     * @param session
     * @return
     */
    public Pop3Agent createFromSession(HttpSession session) {
        return createFromSession(session, null);
    }

    /**
     * 세션과 request를 기반으로 Pop3Agent를 생성. request가 필요한 경우에만 사용되며, request가 null이면
     * 무시됩니다.
     *
     * @param session
     * @param request
     * @return
     */
    public Pop3Agent createFromSession(HttpSession session, HttpServletRequest request) {
        Pop3Agent pop3 = new Pop3Agent();
        pop3.setHost((String) session.getAttribute("host"));
        pop3.setUserid((String) session.getAttribute("userid"));
        pop3.setPassword((String) session.getAttribute("password"));
        if (request != null) {
            pop3.setRequest(request);
        }
        return pop3;
    }
}
