package deu.cse.spring_webmail.security;

import deu.cse.spring_webmail.factory.Pop3AgentFactory;
import deu.cse.spring_webmail.model.Pop3Agent;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Pop3AuthenticationProvider implements AuthenticationProvider {

    @Resource
    private Pop3AgentFactory pop3AgentFactory;

    @Value("${james.host}")
    private String host;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String userid = authentication.getName(); // = username
        String password = authentication.getCredentials().toString();

        // POP3 로그인 유효성 검사
        Pop3Agent agent = pop3AgentFactory.create(host, userid, password);
        if (!agent.validate()) {
            throw new BadCredentialsException("인증 실패: POP3 서버에서 로그인 거부");
        }

        // 권한 부여: test@test.com 이면 ADMIN, 아니면 USER
        List<GrantedAuthority> authorities
                = userid.equals("test@test.com")
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // 인증 성공 토큰 반환
        return new UsernamePasswordAuthenticationToken(userid, password, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
