package deu.cse.spring_webmail.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index", "/login.do", "/login_fail", "/main_menu", "/css/**", "/js/**", "/img_test", "/get_image/**").permitAll()
                .requestMatchers("/admin_menu", "/add_user", "/add_user.do", "/delete_user", "/delete_user.do").hasRole("ADMIN")
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/") // index.jsp
                .loginProcessingUrl("/login.do") // 이 URL로 POST가 오면 Spring Security가 자동 처리
                .usernameParameter("userid")
                .passwordParameter("passwd")
                .successHandler((request, response, authentication) -> {
                    String userid = authentication.getName();
                    String password = request.getParameter("passwd");  // 직접 받아야 함

                    HttpSession session = request.getSession();
                    session.setAttribute("userid", userid);
                    session.setAttribute("password", password);
                    session.setAttribute("host", "localhost");  // 필요시 설정값 가져와서 바꿔도 됨
                    
                    //2025.05.24 lsh
                    //systemcontroller의 isadmin() 기능 이관
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                    response.sendRedirect(request.getContextPath() + (isAdmin ? "/admin_menu" : "/main_menu"));
                })
                .failureHandler((request, response, exception) -> { // 로그인 실패
                    request.getSession().setAttribute("loginErrorUserid", request.getParameter("userid"));
                    response.sendRedirect(request.getContextPath() + "/login_fail");
                })
                )
                .logout(logout -> logout
                .logoutUrl("/logout") // 사용자가 href 요청 보내면 로그아웃 처리
                .logoutSuccessHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
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

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web
                .httpFirewall(allowUrlEncodedDoubleSlashFirewall())
                .ignoring()
                .requestMatchers("/WEB-INF/**");  // 내부 forward 무시
    }
}
