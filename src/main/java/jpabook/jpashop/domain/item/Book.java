package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B") // item 테이블에 어떤 구분자로 들어갈건지
@Getter
@Setter
public class Book extends Item {
    private String author;
    private String isbn;
}
