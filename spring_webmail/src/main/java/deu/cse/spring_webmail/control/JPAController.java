/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.service.AddrbookService;
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
            @RequestParam String phone) {
        addrbookService.addEntry(name, email, phone);
        return "redirect:/show_addr"; // 저장 후 목록 페이지로 이동
    }
}
