# springboot-and-jpa1
인프런 실전! 스프링 부트와 JPA 활용1 - 웹 애플리케이션 개발 정리

# 섹션0. 강좌 소개
## 강좌 소개
스프링부트 + JPA
#### 강의 목표 : 실무에서 웹 애플리케이션을 제대로 개발
- 프로젝트 환경설정
- 요구사항 분석
- 도메인과 테이블 설계
- 아키텍처 구성
- 핵심 비즈니스 로직 개발(회원, 상품, 주문)
- 테스트
- 웹 계층 개발

# 섹션1. 프로젝트 환경설정
## 프로젝트 생성
Gradle을 통해 필요한 의존관계들을 가져오게 된다.

## 라이브러리 살펴보기
#### 스프링 부트 라이브러리 살펴보기
- spring-boot-starter-web
  - spring-boot-starter-tomcat: 톰캣(웹서버)
  - spring-webmvc: 스프링 웹 MVC
- spring-boot-starter-thymeleaf: 타임리프 템플릿 엔진(View)
- spring-boot-starter-data-jpa
  - spring-boot-starter-aop
  - spring-boot-starter-jdbc
    - HikariCP 커텍션 풀 (부트 2.0 기본)
  - hibernate + JPA: 하이버네이트 + JPA
  - spring-boot-data-jpa: 스프링 데이터 JPA
- spring-boot-starter(공통): 스프링부트 + 스프링 코어 + 로깅
  - spring-boot
    - spring-core
  - spring-boot-starter-logging
    - logback, slf4j
#### 테스트 라이브러리
- spring-boot-starter-test
  - junit: 테스트 프레임워크
  - mockito: 목 라이브러리
  - assertj: 테스트 코드를 좀 더 편하게 작성하게 도와주는 라이브러리
  - spring-test: 스프링 통합 테스트 진행

- 핵심 라이브러리
  - 스프링 MVC
  - 스프링 ORM
  - JPA, 하이버네이트
  - 스프링 데이터 JPA
- 기타 라이브러리 
  - H2 데이터베이스 클라이언트
  - 커넥션 풀: 부트 기본은 HikariCP
  - WEB(thymeleaf)
  - 로깅 SLF$J & LogBack
  - 테스트

참고: 스프링 데이터 JPA는 스프링과 JPA를 먼저 이해하고 사용해야 하는 응용기술이다.

## View 환경설정
### Thymeleaf 템플릿 엔진
- thymeleaf 공식 사이트: https://www.thymeleaf.org/
- 스프링 공식 튜토리얼: https://spring.io/guides/gs/serving-web-content/
- 스프링부트 메뉴얼: https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/boot-features-developing-web-applications.html#boot-features-spring-mvc-templateengines

- 스프링 부트 thymeleaf viewName 매핑
  - ```resources:templates/``` + {ViewName} + ```.html```

## H2 데이터베이스 설치
개발이나 테스트 용도로 가볍고 편리한 DB, 웹 화면 제공
- 다운로드 및 설치
- 데이터베이스 파일 생성 방법
  - http://localhost:8082 접속
  - ```jdbc:h2:~/jpashop```(최소 한번, 세션키 유지한 상태로 실행)
  - ```~/jpashop.mv.db```파일 생성 확인
  - 이후부터는 ```jdbc:h2:tcp://localhost/~/jpashop``` 이렇게 접속

## JPA와 DB 설정, 동작확인

EntityManager를 통한 모든 데이터 변경은 항상 트랜잭션 안에서 이루어져야 한다.
- Entity, Repository 동작 확인
- jar 빌드해서 동작 확인

> 참고: 스프링 부트를 통해 복잡한 설정이 다 자동화 되었다. ```persistence.xml```도 없고, ```LaclContainerEntityManagerFactoryBean```도 없다.
> 스프링 부터를 통한 추가 설정은 스프링 부트 매뉴얼을 참고하고, 스프링 부트를 사용하지 않고 순수 스프링과 JPA 설정 방법은 자바 ORM 표준 JPA 프로그래밍 책을 참고하자.

### 쿼리 파라미터 로그 남기기
- 로그에 다음을 추가하기 ```org.hibernate.type```: SQL 실행 파라미터를 로그로 남긴다.
- 외부 라이브러리 사용
  - https://github.com/gavlyukovskiy/spring-boot-data-source-decorator
스프링 부트를 사용하면 이 라이브러리만 추가하면 된다.
```
implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:${version}")
```

# 도메인 분석 설계
## 요구사항 분석
#### 기능 목록
- 회원 기능
  - 회원 등록
  - 회원 조회
- 상품 기능
  - 상품 등록
  - 상품 수정
  - 상품 조회
- 주문 기능
  - 상품 주문 
  - 주문 내역 조회
  - 주문 취소
- 기타 요구사항
  - 상품은 재고 관리가 필요하다.
  - 상품의 종류는 도서, 음반, 영화가 있다.
  - 상품을 카테고리로 구분할 수 있다.
  - 상품 주문시 배송 정보를 입력할 수 있다.

## 도메인 모델과 테이블 설계
![img.png](img.png)
**회원, 주문, 상품의 관계**: 회원은 여러 상품을 주문할 수 있다. 그리고 한 번 주문할 때 여러 상품을 선택할 수 있으므로 주문과 상품은 다대다 관계다. 
하지만 이런 다대다 관계는 관계형 데이터베이스는 물론이고 엔티티에서도 거의 사용하지 않는다. 따라서 그림처럼 주문상품이라는 엔티티를 추가해서 다대다 관계를 일대다, 다대일 관계로 풀어냈다.

**상품 분류**: 상품은 도서, 음반, 영화로 구분되는데 상품이라는 공통 속성을 사용하므로 상속 구조로 표현했다.
### 회원 엔티티 분석
![img_1.png](img_1.png)

**회원(Member)**: 이름과 임베디드 타입인 주소(```Address```), 그리고 주문(```orders```)리스트를 가진다.

**주문(Order)**: 한번 주문시 여러 상품을 주문할 수 있으므로 주문과 주문상품(```OrderItem```)은 일대다 관계다.
주문은 상품을 주문한 회원과 배송정보, 주문 날짜, 주문 상태(```status```)를 가지고 있다. 주문 상태는 열거형을 사용했는데 주문(```ORDER```), 취소(```CANCEL```)을 표현할 수 있다.

**주문 상품(Order Item)**: 주문한 상품 정보와 주문 금액(```OrderPrice```), 주문 수량(```count```)정보를 가지고 있다. (보통 ```OrderLine```, ```LineItem```으로 많이 표현한다.)

**상품(Item)**: 이름, 가격, 재고수량(```stockQuantity```)을 가지고 있다. 상품을 주문하면 재고수량이 줄어든다.
상품의 종류로는 도서, 음반, 영화가 있는데 각각은 사용하는 속성이 조금씩 다르다.

**배송(Delivery)**: 주문시 하나의 배송 정보를 생성한다. 주문과 배송의 관계는 일대일 관계다.

**카테고리(Category)**: 상품과 다대다 관계를 맺는다. ```parent```, ```child```로 부모, 자식 카테고리를 연결한다.

**주소(Address)**: 값 타입(임베디드 타입)이다. 회원과 배송(Delivery)에서 사용한다.

> 참고: 회원이 주문을 하기 때문에, 회원이 주문리스트를 가지는 것은 얼핏 보면 잘 설계한 것 같지만, 객체 세상은 실제 세계와는 다르다.
> 실무에서는 회원이 주문을 참조하지 않고, 주문이 회원을 참조하는 것으로 충분하다. 여기서는 일대다, 다대일의 양방향 연관관계를 설명하기 위해서 추가했다.

### 회원 테이블 분석
![img_2.png](img_2.png)
**MEMBER**: 회원 엔티티의 ```Address``` 임베디드 타입 정보가 회원 테이블에 그대로 들어갔따. 이것은 ```Delivery``` 테이블도 마찬가지다.

**ITEM**: 앨범, 도서, 영화 타입을 통합해서 하나의 테이블로 만들었다. ```DTYPE``` 컬럼으로 타입을 구분한다.

> 참고: 테이블명이 ```ORDER```가 아니라 ```ORDERS```인 것은 데이터베이스가 ```order by``` 때문에 예약어로 잡고 있는 경우가 많다. 그래서 관례상 ```ORDERS```를 많이 사용한다.

> 참고: 실제 코드에서는 DB에 소문자 + _(언더스코어) 스타일을 사용하겠다.
> 데이터베이스 테이블명, 컬럼명에 대한 관례는 회사마다 다르다. 보통은 대문자 + _(언더스코어)나 소문자 + _(언더스코어) 방식 중에 하나를 지정해서 일관성 있게 사용한다.
> 강의에서 설명할 때는 객체와 차이를 나타내기 위해 데이터베이스 테이블, 컬럼명은 대문자를 사용했지만, **실제 코드에서는 소문자 + _(언더스코어) 스타일을 사용하겠다.

### 연관관계 매핑 분석
**회원과 주문**: 일대다, 다대일의 양방향 관계다. 따라서 연관관계의 주인을 정해야 하는데, 외래 키가 있는 주문을 연관관계 주인으로 정하는 것이 좋다. 그러므로 ```Order.member```를 ```ORDERS.MEMBER_ID```외래 키와 매핑한다.

**주문상품과 주문**: 다대일 양방향 관계다. 외래 키가 주문상품에 있으므로 주문상품이 연관관계의 주인이다. 그러므로 ```OrderItem.order```를 ```ORDER_ITEM.ORDER_ID```외래 키와 매핑한다.

**주문상품과 상품**: 다대일 단방향 관계다. ```OrderItem.item```을 ```ORDER_ITEM.ITEM_ID``` 외래 키와 매핑 한다.

**주문과 배송**: 일대일 단방향 관계다. ```Order.delivery```를 ```ORDERS.DELIVERY_ID``` 외래 키와 매핑한다.

** 카테고리와 상품**: ```@ManyToMany```를 사용해서 매핑한다. (실무에서 @ManyToMany는 사용하지 말자. 여기서는 다대다 관계를 예제로 보여주기 위해 추가했을 뿐이다.)

> **참고: 외래 키가 있는 곳을 연관관계의 주인으로 정해라.**
> 연관관계의 주인은 단순히 외래 키를 누가 관리하냐의 문제이지 비즈니스상 우위에 있다고 주인으로 정하면 안된다.
> 예를 들어서 자동차와 바퀴가 있으면, 일대다 관계에서 항상 다쪽에 외래 키가 있으므로 외래 키가 있는 바퀴를 연관관계의 주인으로 정하면 된다.
> 물론 자동차를 연관관계의 주인으로 정하는 것이 불가능한 것은 아니지만, 자동차를 연관관계의 주인으로 정하면 자동차가 관리하지 않는 바퀴 테이블의 외래 키 값이 업데이트 되므로 
> 관리와 유지보수가 어렵고, 추가적으로 별도의 업데이트 쿼리가 발생하는 성능 문제도 있다.