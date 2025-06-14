/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.factory.Pop3AgentFactory;
import deu.cse.spring_webmail.factory.UserAdminAgentFactory;
import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.model.UserAdminAgent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import javax.imageio.ImageIO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 초기 화면과 관리자 기능(사용자 추가, 삭제)에 대한 제어기
 *
 * @author skylo
 */
@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class SystemController {

    //java:S1192 상수 선언
    private static final String PARAM_USERID = "userid";
    private static final String REDIRECT_ADMIN_MENU = "redirect:/admin_menu";

    @Autowired
    private ServletContext ctx;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private Pop3AgentFactory pop3AgentFactory;
    @Autowired
    private UserAdminAgentFactory userAdminAgentFactory;

    @Value("${root.id}")
    private String ROOT_ID;
    @Value("${root.password}")
    private String ROOT_PASSWORD;
    @Value("${admin.id}")
    private String ADMINISTRATOR;  //  = "admin";
    @Value("${james.control.port}")
    private Integer JAMES_CONTROL_PORT;
    @Value("${james.host}")
    private String JAMES_HOST;

    @GetMapping("/")
    public String index() {
        log.debug("index() called...");
        session.setAttribute("host", JAMES_HOST);
        session.setAttribute("debug", "false");

        return "/index";
    }

    /*
    2025.05.24 SpringSecurity 구현 과정에서 삭제됨
    -> security 패키지 내 클래스로 기능들 이관
    @author Haruki
     */
//    @RequestMapping(value = "/login.do", method = {RequestMethod.GET, RequestMethod.POST})
//    public String loginDo(@RequestParam Integer menu, RedirectAttributes redirectAttrs) {
//        String url = "";
//        log.debug("로그인 처리: menu = {}", menu);
//        switch (menu) {
//            case CommandType.LOGIN:
//                String host = (String) request.getSession().getAttribute("host");
//                String userid = request.getParameter(PARAM_USERID);
//                String password = request.getParameter("passwd");
//
//                // Check the login information is valid using <<model>>Pop3Agent.
//                // 기존
//                // Pop3Agent pop3Agent = new Pop3Agent(host, userid, password);
//                // 수정 2025-05-13
//                // 세션에서 가져오는 것이 아니고, 사용자 입력으로부터 직접 가져오는 값
//                Pop3Agent pop3Agent = pop3AgentFactory.create(host, userid, password);
//                boolean isLoginSuccess = pop3Agent.validate();
//
//                // Now call the correct page according to its validation result.
//                if (isLoginSuccess) {
//                    if (isAdmin(userid)) {
//                        // HttpSession 객체에 userid를 등록해 둔다.
//                        session.setAttribute(PARAM_USERID, userid);
//                        // response.sendRedirect("admin_menu.jsp");
//                        url = REDIRECT_ADMIN_MENU;
//                    } else {
//                        // HttpSession 객체에 userid와 password를 등록해 둔다.
//                        session.setAttribute(PARAM_USERID, userid);
//                        session.setAttribute("password", password);
//                        // response.sendRedirect("main_menu.jsp");
//                        url = "redirect:/main_menu";  // URL이 http://localhost:8080/webmail/main_menu 이와 같이 됨.
//                        // url = "/main_menu";  // URL이 http://localhost:8080/webmail/login.do?menu=91 이와 같이 되어 안 좋음
//                    }
//                } else {
//                    // RequestDispatcher view = request.getRequestDispatcher("login_fail.jsp");
//                    // view.forward(request, response);
//                    redirectAttrs.addAttribute(PARAM_USERID, userid); // 실패 화면에 사용될 사용자 ID 전달
//                    url = "redirect:/login_fail";
//                }
//                break;
//            case CommandType.LOGOUT:
//                session.invalidate();
//                url = "redirect:/";  // redirect: 반드시 넣어야만 컨텍스트 루트로 갈 수 있음
//                break;
//            default:
//                break;
//        }
//        return url;
//    }
    @GetMapping("/login_fail")
    public String loginFail() {
        return "login_fail";
    }

    //2025.05.24 lsh
    //isAdmin() 메서드는 더 이상 SystemController에서 사용하지 않고, 대신 Spring Security의 authentication.getAuthorities() 로 역할(권한)을 판단
//    protected boolean isAdmin(String userid) {
//        boolean status = false;
//
//        if (userid.equals(this.ADMINISTRATOR)) {
//            status = true;
//        }
//
//        return status;
//    }
    @GetMapping("/main_menu")
    // 2025.05.28 페이지네이션 구현
    public String mainMenu(@RequestParam(defaultValue = "1") int page, Model model, RedirectAttributes attrs) {
        Pop3Agent pop3 = pop3AgentFactory.createFromSession(session);
        int pageSize = 5;

        int totalCount = pop3.getTotalMessageCount();
        int totalPages = (int) Math.ceil(totalCount / (double) pageSize);

        // 페이지 유효성 검사
        if (totalPages > 0 && (page < 1 || page > totalPages)) {
            attrs.addFlashAttribute("msg", "존재하지 않는 페이지입니다. 1페이지로 이동합니다.");
            return "redirect:/main_menu?page=1";
        }

        String messageList = pop3.getMessageList(page, pageSize);

        model.addAttribute("messageList", messageList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);

        return "main_menu";
    }

    @GetMapping("/admin_menu")
    public String adminMenu(Model model) {
        log.debug("root.id = {}, root.password = {}, admin.id = {}",
                ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

        model.addAttribute("userList", getUserList());
        return "admin/admin_menu";
    }

    @GetMapping("/add_user")
    public String addUser() {
        return "admin/add_user";
    }

    @PostMapping("/add_user.do")
    public String addUserDo(@RequestParam String id, @RequestParam String password,
            RedirectAttributes attrs) {
        log.debug("add_user.do: id = {}, password = {}, port = {}",
                id, password, JAMES_CONTROL_PORT);

        try {
            String cwd = ctx.getRealPath(".");
            // 기존 2025-05-13
            //UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
            //        ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
            // 수정 : 팩토리 메서드 적용
            // 2025-05-13
            UserAdminAgent agent = userAdminAgentFactory.create(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                    ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

            // if (addUser successful)  사용자 등록 성공 팦업창
            // else 사용자 등록 실패 팝업창
            if (agent.addUser(id, password)) {
                attrs.addFlashAttribute("msg", String.format("사용자(%s) 추가를 성공하였습니다.", id));
            } else {
                attrs.addFlashAttribute("msg", String.format("사용자(%s) 추가를 실패하였습니다.", id));
            }
        } catch (Exception ex) {
            log.error("add_user.do: 시스템 접속에 실패했습니다. 예외 = {}", ex.getMessage());
        }

        return REDIRECT_ADMIN_MENU;
    }

    @GetMapping("/delete_user")
    public String deleteUser(Model model) {
        log.debug("delete_user called");
        model.addAttribute("userList", getUserList());
        return "admin/delete_user";
    }

    /**
     *
     * @param selectedUsers <input type=checkbox> 필드의 선택된 이메일 ID. 자료형: String[]
     * @param attrs
     * @return
     */
    @PostMapping("delete_user.do")
    public String deleteUserDo(@RequestParam String[] selectedUsers, RedirectAttributes attrs) {
        log.debug("delete_user.do: selectedUser = {}", List.of(selectedUsers));

        try {
            String cwd = ctx.getRealPath(".");
            // 기존 2025-05-13
            //UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
            //        ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
            // 수정 : 팩토리 메서드 적용
            // 2025-05-13
            UserAdminAgent agent = userAdminAgentFactory.create(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                    ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

            agent.deleteUsers(selectedUsers);  // 수정!!!
        } catch (Exception ex) {
            log.error("delete_user.do : 예외 = {}", ex);
        }

        return REDIRECT_ADMIN_MENU;
    }

    private List<String> getUserList() {
        String cwd = ctx.getRealPath(".");
        // 기존 2025-05-13
        //UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
        //        ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);
        // 수정 : 팩토리 메서드 적용
        // 2025-05-13
        UserAdminAgent agent = userAdminAgentFactory.create(JAMES_HOST, JAMES_CONTROL_PORT, cwd,
                ROOT_ID, ROOT_PASSWORD, ADMINISTRATOR);

        List<String> userList = agent.getUserList();
        log.debug("userList = {}", userList);

        //(주의) root.id와 같이 '.'을 넣으면 안 됨.
        userList.sort((e1, e2) -> e1.compareTo(e2));
        return userList;
    }

    @GetMapping("/img_test")
    public String imgTest() {
        return "img_test/img_test";
    }

    /**
     * https://34codefactory.wordpress.com/2019/06/16/how-to-display-image-in-jsp-using-spring-code-factory/
     *
     * @param imageName
     * @return
     */
    @RequestMapping(value = "/get_image/{imageName}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImage(@PathVariable String imageName) {
        try {
            String folderPath = ctx.getRealPath("/WEB-INF/views/img_test/img");
            return getImageBytes(folderPath, imageName);
        } catch (Exception e) {
            log.error("/get_image 예외: {}", e.getMessage());
        }
        return new byte[0];
    }

    private byte[] getImageBytes(String folderPath, String imageName) {
        ByteArrayOutputStream byteArrayOutputStream;
        BufferedImage bufferedImage;
        byte[] imageInByte;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            bufferedImage = ImageIO.read(new File(folderPath + File.separator + imageName));
            String format = imageName.substring(imageName.lastIndexOf(".") + 1);
            ImageIO.write(bufferedImage, format, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            imageInByte = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return imageInByte;
        } catch (FileNotFoundException e) {
            log.error("getImageBytes 예외: {}", e.getMessage());
        } catch (Exception e) {
            log.error("getImageBytes 예외: {}", e.getMessage());
        }
        return new byte[0];
    }

}
