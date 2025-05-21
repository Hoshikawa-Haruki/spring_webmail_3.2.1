package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.model.Addrbook;
import deu.cse.spring_webmail.repository.AddrbookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddrbookServiceTest {

    @Mock
    private AddrbookRepository addrbookRepo;

    @InjectMocks
    private AddrbookService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addEntryCorrectFields() {
        String userid  = "user1";
        String name    = "홍길동";
        String email   = "hong@test.com";
        String phone   = "010-1234-5678";
        ArgumentCaptor<Addrbook> captor = ArgumentCaptor.forClass(Addrbook.class);

        service.addEntry(userid, name, email, phone);

        verify(addrbookRepo, times(1)).save(captor.capture());
        Addrbook saved = captor.getValue();
        assertEquals(userid,  saved.getUserid());
        assertEquals(name,    saved.getName());
        assertEquals(email,   saved.getEmail());
        assertEquals(phone,   saved.getPhone());
    }

    @Test
    void deleteEntry() {
        String userid = "user1";
        String email  = "hong@test.com";

        service.deleteEntry(userid, email);

        verify(addrbookRepo, times(1))
            .deleteByUseridAndEmail(userid, email);
    }

    @Test
    void ReturnExists() {
        String userid = "user1";
        String email  = "hong@test.com";
        when(addrbookRepo.existsByUseridAndEmail(userid, email))
            .thenReturn(true);

        boolean exists = service.isAlreadyRegistered(userid, email);

        assertTrue(exists);
        verify(addrbookRepo).existsByUseridAndEmail(userid, email);
    }

    @Test
    void ReturnNotExists() {
        String userid = "user1";
        String email  = "none@test.com";
        when(addrbookRepo.existsByUseridAndEmail(userid, email))
            .thenReturn(false);

        boolean exists = service.isAlreadyRegistered(userid, email);

        assertFalse(exists);
        verify(addrbookRepo).existsByUseridAndEmail(userid, email);
    }

    @Test
    void ReturnAllRepository() {
        List<Addrbook> list = Arrays.asList(new Addrbook(), new Addrbook());
        when(addrbookRepo.findAll()).thenReturn(list);

        List<Addrbook> result = service.getAll();

        verify(addrbookRepo).findAll();
        assertSame(list, result);
    }
}
