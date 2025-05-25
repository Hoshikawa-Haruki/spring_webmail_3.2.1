package deu.cse.spring_webmail.security;

import deu.cse.spring_webmail.factory.Pop3AgentFactory;
import deu.cse.spring_webmail.model.Pop3Agent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

class Pop3AuthenticationProviderTest {

    @Mock
    private Pop3AgentFactory pop3AgentFactory;

    @Mock
    private Pop3Agent pop3Agent;

    @InjectMocks
    private Pop3AuthenticationProvider provider;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // 테스트용 host 직접 세팅 (필요시)
        provider.host = "localhost";
    }

    @Test
    void authenticate_successWithAdminRole() {
        String adminUser = "test@test.com";
        String password = "password";

        given(pop3AgentFactory.create(anyString(), eq(adminUser), eq(password))).willReturn(pop3Agent);
        given(pop3Agent.validate()).willReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken(adminUser, password);

        Authentication result = provider.authenticate(auth);

        assertNotNull(result);
        assertEquals(adminUser, result.getName());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void authenticate_successWithUserRole() {
        String user = "user1";
        String password = "password";

        given(pop3AgentFactory.create(anyString(), eq(user), eq(password))).willReturn(pop3Agent);
        given(pop3Agent.validate()).willReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, password);

        Authentication result = provider.authenticate(auth);

        assertNotNull(result);
        assertEquals(user, result.getName());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void authenticate_failThrowsBadCredentials() {
        String user = "user1";
        String password = "wrongpassword";

        given(pop3AgentFactory.create(anyString(), eq(user), eq(password))).willReturn(pop3Agent);
        given(pop3Agent.validate()).willReturn(false);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, password);

        assertThrows(BadCredentialsException.class, () -> {
            provider.authenticate(auth);
        });
    }

    @Test
    void supports_returnsTrueForUsernamePasswordAuthenticationToken() {
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void supports_returnsFalseForOtherAuthentication() {
        class DummyAuth implements Authentication {

            @Override
            public String getName() {
                return null;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                // 인증 객체는 외부에서 인증 상태를 변경하지 않도록 불변으로 설계되었기 때문에 이 메서드는 사용하지 않음.
                throw new UnsupportedOperationException("setAuthenticated is not supported.");
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public java.util.Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return null;
            }
        }
        assertFalse(provider.supports(DummyAuth.class));
    }
}
