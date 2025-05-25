/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.factory;

/**
 *
 * @author Haruki
 */
import deu.cse.spring_webmail.model.UserAdminAgent;
import org.springframework.stereotype.Component;

@Component
public class UserAdminAgentFactory {

    public UserAdminAgent create(String host, int port, String cwd, String rootId, String rootPw, String adminId) {
        return new UserAdminAgent(host, port, cwd, rootId, rootPw, adminId);
    }
}
