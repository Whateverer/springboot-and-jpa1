package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String name;

    @Embedded // 내장 타입을 쓸 때는
    private Address address;

    @OneToMany(mappedBy = "member") // 읽기 전용이 된다.
    private List<Order> orders = new ArrayList<>();
}
