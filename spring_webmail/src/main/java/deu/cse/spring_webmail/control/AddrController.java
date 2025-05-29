/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Addrbook;
import deu.cse.spring_webmail.service.AddrbookService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 주소록 조회/추가 위한 제어기
 *
 * @author Haruki
 */
@Controller
@Slf4j
public class AddrController {

    private static final String REDIRECT_SHOW_ADDR = "redirect:/show_addr";
    private static final String USER_ID = "userid";

    @Autowired
    private AddrbookService addrbookService;

    @PostMapping("/jpa/insert_addr")
    public String jpaInsertAddr(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            HttpSession session, RedirectAttributes attrs) {
        String userId = (String) session.getAttribute(USER_ID);  // 세션에서 로그인 ID 가져오기
        log.info("사용자 [{}] 주소록 추가 시도: name={}, email={}", userId, name, email);
        if (addrbookService.isAlreadyRegistered(userId, email)) {
            log.info("사용자 [{}] 주소록 중복 등록 시도: email={}", userId, email);
            attrs.addFlashAttribute("msg", "이미 등록된 이메일입니다.");
            return REDIRECT_SHOW_ADDR;
        }
        addrbookService.addEntry(userId, name, email, phone);
        log.info("사용자 [{}] 주소록 추가 완료: email={}", userId, email);
        attrs.addFlashAttribute("msg", "주소록에 추가되었습니다.");
        return REDIRECT_SHOW_ADDR;  // 저장 후 목록으로 이동
    }

    @PostMapping("/jpa/delete_addr")
    public String deleteAddr(@RequestParam("del_email") String email, HttpSession session, RedirectAttributes attrs) {
        String userId = (String) session.getAttribute(USER_ID);
        log.info("사용자 [{}] 주소록 삭제 요청: email={}", userId, email);
        addrbookService.deleteEntry(userId, email);
        log.info("사용자 [{}] 주소록 삭제 완료: email={}", userId, email);
        attrs.addFlashAttribute("msg", "주소록에서 삭제되었습니다.");
        return REDIRECT_SHOW_ADDR; // 삭제 후 목록으로 이동
    }

    @GetMapping("/show_addr")
    public String showAddr(HttpSession session, Model model) {
        String userid = (String) session.getAttribute(USER_ID);
        log.info("사용자 [{}] 주소록 조회 요청", userid);
        List<Addrbook> addrList = addrbookService.getAddrList(userid);
        model.addAttribute("addrList", addrList);  // 모델에 데이터 저장
        return "addr_menu/addr_book";
    }

    @GetMapping("/insert_addr")
    public String insertAddr() {
        log.info("주소록 입력 페이지 요청");
        return "addr_menu/addr_insert";
    }

}
