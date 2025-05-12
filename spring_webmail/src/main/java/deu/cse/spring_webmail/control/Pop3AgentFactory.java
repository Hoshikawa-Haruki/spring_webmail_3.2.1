/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

/**
 *
 * @author Haruki
 */
import deu.cse.spring_webmail.model.Pop3Agent;
import org.springframework.stereotype.Component;

@Component
public class Pop3AgentFactory {

    public Pop3Agent create(String host, String userid, String password) {
        return new Pop3Agent(host, userid, password);
    }
}
