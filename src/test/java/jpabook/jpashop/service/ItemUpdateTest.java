package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;
    
    @Test
    public void updateTest() throws Exception {
        Book book = em.find(Book.class, 1L);

        // TX 트랜잭션
        book.setName("adsf");

        // 변경감지 == dirty checking
        // TX commit 시 JPA가 update 쿼리를 자동으로 반영한다.
    }
}
