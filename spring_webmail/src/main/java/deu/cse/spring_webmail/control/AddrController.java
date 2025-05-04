/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 주소록 조회/추가 위한 제어기
 *
 * @author Haruki
 */
@Controller
@Slf4j
public class AddrController {

    @GetMapping("/show_addr")
    public String addrBook() {
        return "addr_menu/addr_book";
    }

    @GetMapping("/insert_addr")
    public String insertAddr() {
        return "addr_menu/addr_insert";
    }

}
