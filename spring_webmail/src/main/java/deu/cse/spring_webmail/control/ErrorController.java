/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author Haruki
 */
@Controller
@Slf4j
public class ErrorController {

    @GetMapping("/access_denied")
    public String accessDenied() {
        log.debug("access_denied 페이지 호출\n");
        return "errorpage/access_denied";  // JSP 뷰 이름
    }
}
