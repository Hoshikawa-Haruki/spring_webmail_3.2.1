/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.service.AddrbookService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Haruki
 */
@Controller
@Slf4j
public class JPAController {

    @Autowired
    private AddrbookService addrbookService;

    @PostMapping("/jpa/insert_addr")
    public String jpaInsertAddr(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            HttpSession session) {
        String userId = (String) session.getAttribute("userid");  // 세션에서 로그인 ID 가져오기
        addrbookService.addEntry(userId, name, email, phone);
        return "redirect:/show_addr";  // 저장 후 목록으로 이동
    }

    @PostMapping("/jpa/delete_addr")
    public String deleteAddr(@RequestParam("del_email") String email, HttpSession session) {
        String userId = (String) session.getAttribute("userid");
        addrbookService.deleteEntry(userId, email);
        return "redirect:/show_addr"; // 삭제 후 목록으로 이동
    }
}
