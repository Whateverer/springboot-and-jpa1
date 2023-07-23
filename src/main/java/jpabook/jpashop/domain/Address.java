package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    protected Address() {
    } // JPA에서 리플렉션을 많이 이용하기 때문에 기본 생성자가 필요하다. 그런데 public으로 하기엔 리스크가 있어 protected로 많이 사용한다.

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
