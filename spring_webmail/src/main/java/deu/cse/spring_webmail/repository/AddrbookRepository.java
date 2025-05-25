/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package deu.cse.spring_webmail.repository;

import deu.cse.spring_webmail.model.Addrbook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Haruki
 */
@Repository
public interface AddrbookRepository extends JpaRepository<Addrbook, String> {

    // 기본 CRUD 메서드는 자동으로 제공됨 (save, findAll 등)
    // JPA의 쿼리 메서드는 엔티티 클래스의 필드명과 정확히 일치해야 함. 대소문자까지
    List<Addrbook> findByUserid(String userId); // 사용자별 주소록 조회용 메서드
    void deleteByUseridAndEmail(String userid, String email); // 주소록 삭제 메서드
    boolean existsByUseridAndEmail(String userid, String email); // 주소록 존재 체크 메서드

}
