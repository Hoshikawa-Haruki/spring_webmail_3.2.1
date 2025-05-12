/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.service;

import deu.cse.spring_webmail.model.Addrbook;
import deu.cse.spring_webmail.repository.AddrbookRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Haruki
 */
@Service
public class AddrbookService {

    @Autowired
    private AddrbookRepository addrbookRepo;

    public void addEntry(String userid, String name, String email, String phone) {
        Addrbook entry = new Addrbook();
        entry.setUserIdentifier(userid);
        entry.setName(name);
        entry.setEmail(email);
        entry.setPhone(phone);
        addrbookRepo.save(entry);
    }

    @Transactional
    public void deleteEntry(String userId, String email) {
        addrbookRepo.deleteByUseridAndEmail(userId, email);
    }

//    public List<Addrbook> getUserEntries(String userid) {
//        return addrbookRepo.findByUserId(userid);
//    }
    public List<Addrbook> getAll() {
        return addrbookRepo.findAll();
    }
}
