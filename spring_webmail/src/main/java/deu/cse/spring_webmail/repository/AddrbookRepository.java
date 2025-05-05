/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package deu.cse.spring_webmail.repository;

import deu.cse.spring_webmail.model.Addrbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Haruki
 */
@Repository
public interface AddrbookRepository extends JpaRepository<Addrbook, String> {
    // 기본 CRUD 메서드는 자동으로 제공됨 (save, findAll 등)
    // List<Addrbook> findByUserId(String userId); // 사용자별 주소록 조회용 메서드
}
