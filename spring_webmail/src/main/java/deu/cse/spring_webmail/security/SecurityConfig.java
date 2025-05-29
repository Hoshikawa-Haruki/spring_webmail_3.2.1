package deu.cse.spring_webmail.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String PARAM_USERID = "userid";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth // 인가 정책
                // 내부 forward 요청도 보안 필터는 통과하되, 인가 검사에서는 허용
                .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                .requestMatchers("/", "/index", "/login.do", "/login_fail", "/css/**", "/js/**", "/img_test", "/get_image/**").permitAll()
                .requestMatchers("/main_menu").hasRole("USER")
                .requestMatchers("/admin_menu", "/add_user", "/add_user.do", "/delete_user", "/delete_user.do").hasRole("ADMIN")
                .anyRequest().authenticated()
        );
        http.formLogin(form -> form // 폼 로그인 필터 연결  
                .loginPage("/") // index.jsp
                .loginProcessingUrl("/login.do") // 이 URL로 POST가 오면 Spring Security가 자동 처리
                .usernameParameter(PARAM_USERID)
                .passwordParameter("passwd")
                .successHandler((request, response, authentication) -> {
                    String userid = authentication.getName();
                    String password = request.getParameter("passwd");  // 직접 받아야 함

                    HttpSession session = request.getSession();
                    session.setAttribute(PARAM_USERID, userid);
                    session.setAttribute("password", password);
                    session.setAttribute("host", "localhost");  // 필요시 설정값 가져와서 바꿔도 됨

                    //2025.05.24 lsh
                    //systemcontroller의 isadmin() 기능 이관
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                    log.info("로그인 성공: 사용자 ID = {}, 권한 = {}\n", userid,
                            isAdmin ? "ROLE_ADMIN" : "ROLE_USER");

                    response.sendRedirect(request.getContextPath() + (isAdmin ? "/admin_menu" : "/main_menu"));
                })
                .failureHandler((request, response, exception) -> { // 로그인 실패
                    request.getSession().setAttribute("loginErrorUserid", request.getParameter(PARAM_USERID));
                    log.warn("로그인 실패: 사용자 ID = {}\n", PARAM_USERID);
                    response.sendRedirect(request.getContextPath() + "/login_fail");
                })
        );
        http.exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String deniedUserid = (String) request.getSession().getAttribute(PARAM_USERID);
                    log.warn("❌ 인가 거부: userid = {}, 요청 URI = {}\n", deniedUserid, request.getRequestURI());
                    response.sendRedirect(request.getContextPath() + "/access_denied");
                })
        );
        http.logout(logout -> logout
                .logoutUrl("/logout") // 사용자가 href 요청 보내면 로그아웃 처리
                .logoutSuccessHandler((request, response, authentication) -> {
                    log.info("로그아웃 핸들러 진입 확인\n");
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        String logoutId = (String) session.getAttribute(PARAM_USERID);
                        log.info("로그아웃 요청됨: userid = {}\n", logoutId);
                        session.invalidate();  // 로그아웃 : 세션 무효화
                    }
                    response.sendRedirect(request.getContextPath() + "/");  // index.jsp로 이동
                })
        );

        return http.build();
    }

    @Bean
    public HttpFirewall allowUrlEncodedDoubleSlashFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowSemicolon(true);
        return firewall;
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return web -> web
//                .httpFirewall(allowUrlEncodedDoubleSlashFirewall())
//                .ignoring()
//                .requestMatchers("/WEB-INF/**");  // 내부 forward 무시
//    }
}
