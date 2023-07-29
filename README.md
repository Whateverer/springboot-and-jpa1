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

## 엔티티 클래스 개발
- 예제에서는 설명을 쉽게하기 위해 엔티티 클래스에 Getter, Setter를 모두 열고, 최대한 단순하게 설계
- 실무에서는 가급적 Getter는 열어두고, Setter는 꼭 필요한 경우에만 사용하는 것을 추천

> 참고: 이론적으로 Getter, Setter 모두 제공하지 않고, 꼭 필요한 별도의 메서드를 제공하는게 가장 이상적이다. 
> 하지만 실무에서 엔티티의 데이터는 조회할 일이 너무 많으므로, Getter의 경우 모두 열어두는 것이 편리하다.
> Getter는 아무리 호출해도 호출하는 것만으로 어떤 일이 발생하지는 않는다. 하지만 Setter는 문제가 다르다. Setter를 호출하면 데이터가 변한다.
> Setter를 막 열어두면 가까운 미래에 엔티티가 도대체 왜 변경되는지 추적하기 점점 힘들어진다. 
> 그래서 엔티티를 변경할 때는 Setter 대신에 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 한다.

내장 타입을 쓸 때는 해당 컬럼에 ```@Embedded```를 쓰거나 해당 클래스에 ```@Embeddable```을 붙여준다.

양방향 연관관계일 때 연관관계의 주인을 정해줘야 한다.
> 참고: 엔티티의 식별자는 ```id```를 사용하고 PK 컬럼명은 ```member_id```를 사용했다. 엔티티는 타입(여기서는 ```Member```)이 있으므로 ```id```필드만으로 쉽게 구분할 수 있다.
> 테이블은 타입이 없으므로 구분이 어렵다. 그리고 테이블은 관례상 ```테이블명 + id```를 많이 사용한다. 참고로 객체에서 ```id```대신에 ```member_id```를 사용해도 된다.
> 중요한 것은 일관성이다.

연관관계의 주인은 그냥 두고, 주인이 아니면 @OneToMany(mappedBy = "맵핑된 엔티티명")을 써준다.

상속관계 매핑이면 상속관계 전략을 정해줘야 한다.(우리는 싱글테이블 전략)
item 엔티티에 ```@DiscriminatorColumn(name = "dtype")```를 설정해주고 하위엔티티들에 ```@DiscriminatorValue("B")```이렇게 설정해주면 구분 컬럼에 어떻게 데이터가 들어가는지 처리해준다.

Enum 타입은 ```@Enumerated(EnumType.STRING)```애노테이션을 붙여줘야 한다. (EnumType.ORDINAL은 사용하면 안된다!)

OneToOne 관계 - 하나의 주문은 하나의 배송정보만 가져야 한다.
보통 1:1관계일때는 접근을 자주하는 엔티티를 연관관계의 주인으로 설정해준다.

> 참고 : 실무에서는 ```@ManyToMany```를 사용하지 말자.
> ```@ManyToMany```는 편리한 것 같지만, 중간 테이블(```CATEGORY_ITEM```)에 컬럼을 추가할 수 없고, 세밀하게 쿼리를 실행하기 어렵기 때문에 실무에서 사용하기에는 한계가 있다.
> 중간엔티티(```CategoryItem```)를 만들고 ```@ManyToOne```, ```OneToMany```로 매핑해서 사용하자. 
> 정리하면 다대다 매핑을 일대다, 다대일 매핑으로 풀어내서 사용하자.

item은 ```@ManyToMany(mappedBy = "items")```으로 처리해준다.

**주소 값 타입**   
JPA에서 리플렉션을 많이 이용하기 때문에 기본 생성자가 필요하다. 그런데 public으로 하기엔 리스크가 있어 protected로 많이 사용한다.
> 참고: 값 타입은 변경 불가능하게 설계해야 한다.
> ```@Setter```를 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만들자. 
> JPA 스펙상 엔티티나 임베디드 타입(```@Embeddable```)은 자바 기본 생성자(default constructor)를 ```public``` 또는 ```protected```로 설정해야 한다.
> ```public```으로 두는 것 보다는 ```protected```로 설정하는 것이 그나마 더 안전하다.
> JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 리플렉션 같은 기술을 사용할 수 있도록 지원해야 하기 때문이다.

## 엔티티 설계시 주의점
### 엔티티에는 가급적 Setter를 사용하지 말자
Setter가 모두 열려있다. 변경 포인트가 너무 많아서 유지보수가 어렵다.
### 모든 연관관계는 지연로딩으로 설정!
- 즉시로딩(```EAGER```)은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 특히 JPQL을 실행할 때 N+1 문제가 자주 발생한다.
- 실무에서 모든 연관관계는 지연로딩(```LAZY```)으로 설정해야 한다.
- 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
- @XToOne(OneToOne, ManyToOne)관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.

즉시로딩: 한 테이블을 조회할 때 연관된 모든 엔티티를 조회해버리는 것
최악의 경우 N+1의 문제가 발생한다. (쿼리 하나만 날렸을 뿐인데 N개의 쿼리가 같이 날아간다는 뜻)

### 컬렉션은 필드에서 초기화하자.
컬렉션은 필드에서 바로 초기화 하는 것이 안전하다.
- ```null```문제에서 안전하다.
- 하이버네이트는 엔티티를 영속할 때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다. 만약 ```getOrders()```처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다. 따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.
```java
Member member = new Member();
System.out.println(member.getOrders().getClass());
em.persist(team); // 영속성 컨텍스트로 가져오면
System.out.println(member.getOrders().getClass());

// 출력 결과
class java.util.ArrayList
class org.hibernate.collection.internal.PersistentBag
```

### 테이블, 컬럼명 생성 전략
스프링 부트에서 하이버네이트 기본 매핑 전략을 변경해서 실제 테이블명은 다름
하이버네이트 기존 구현: 엔티티의 필드명을 그대로 테이블 명으로 사용
(```SpringPhysicalNamingStrategy```)

스프링 부트 신규 설정(엔티티(필드) -> 테이블(컬럼))
1. 카멜 케이스 -> 언더스코어(memberPoint -> member_point)
2. .(점) -> _(언더스코어)
3. 대문자 -> 소문자

**적용 2단계**
1. 논리명 생성: 명시적으로 컬럼, 테이블명을 직접 적지 않으면 ImplicitNamingStrategy 사용 
```spring.jpa.hibernate.naming,implicit-strategy```: 테이블이나, 컬럼명을 명시하지 않을 때 논리명 적용,
2. 물리명 적용: ```spring.jpa.hibernate.naming.physical-strategy```: 모든 논리명에 적용됨, 실제 테이블에 적용 (username -> usernm 등으로 회사 룰로 바꿀 수 있음)

**스프링 부트 기본 설정**

```spring.jpa.hibernate.naming.implicit-strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy```

```spring.jpa.hibernate.naming.physical-strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy```

# 애플리케이션 구현 준비
## 구현 요구사항
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

**예제를 단순화하기 위해 다음 기능은 구현X**
- 로그인과 권한 관리X
- 파라미터 검증과 예외 처리 단순화
- 상품은 도서만 사용
- 카테고리는 사용X
- 배송 정보는 사용X

## 애플리케이션 아키텍처
![img_3.png](img_3.png)
**계층형 구조 사용**
- controller, web: 웹 계층
- service: 비즈니스 로직, 트랜잭션 처리
- repository: JPA를 직접 사용하는 계층, 엔티티 매니저 사용
- domain: 엔티티가 모여 있는 계층, 모든 계층에서 사용

**패키지 구조**
- jpabook.jpashop
  - domain
  - exception
  - repository
  - service
  - web

**개발 순서: 서비스, 리포지토리 계층을 개발하고, 테스트 케이스를 작성해서 검증, 마지막에 웹 계층 적용**

# 회원 도메인 개발
**구현 기능**
- 회원 등록
- 회원 목록 조회

**순서**
- 회원 엔티티 코드 다시 보기
- 회원 리포지토리 개발
- 회원 서비스 개발
- 회원 기능 테스트

## 회원 리포지토리 개발
### 기술 설명
- ```@Repository```: 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 변환
- ```@PersistenceContext```: 엔티티 매니저(```EntityManager```) 주입
- ```@PersistenceUnit```: 엔티티 매니저 팩토리(```EntityManagerFactory```) 주입
### 기능 설명
- ```save()```
- ```findOne()```
- ```findAll()```
- ```findByName()```

## 회원 서비스 개발
조회만 하는 method의 경우 ```@Transactional(readOnly = true)```를 써준다.

### 기술 설명
- ```@Service```
- ```@Transaction```: 트랜잭션, 영속성 컨텍스트
  - ```readOnly=true```: 데이터의 변경이 없는 읽기 전용 메서드에 사용, 영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상(읽기 전용에는 다 적용)
  - 데이터베이스 드라이버가 지원하면 DB에서 성능 향상
- ```@Autowired```
  - 생성자 Injection 많이 사용, 생성자가 하나라면 생략 가능

### 기능 설명
- ```join()```
- ```findMembers()```
- ```findOne()```

> 참고: 실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 회원 테이블의 회원명 컬럼에 유니크 제약 조건을 추가하는 것이 안전하다.

> 참고: 스프링 필드 주입 대신에 생성자 주입을 사용하자.

#### 필드 주입
```java
public class MemberService {
  @Autowired
  MemberRepository memberRepository;
  ...
}
```
#### 생성자 주입
```java
public class MemberService {
    
    private final MemberRepository memberRepository;
        public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
  ...
}
```
- 생성자 주입 방식을 권장
- 변경 불가능한 안전한 객체 생성 기능
- 생성자가 하나면, ```@Autowired```를 생략할 수 있다.
- ```final``` 키워드를 추가하면 컴파일 시점에 ```memberRespository```를 설정하지 않는 오류를 체크할 수 있다. (보통 기본 생성자를 추가할 때 발견)

> 참고: 스프링 데이터 JPA를 사용하면 ```EntityManger```도 주입 가능

## 회원 기능 테스트
### 테스트 요구사항
- 회원가입을 성공해야 한다.
- 회원가입 할 때 같은 이름이 있으면 예외가 발생해야 한다.

@Transactional 안에서 같은 영속성 컨텍스트에서 pk 값이 똑같으면 서로 같은 객체로 간주한다.

### 기술 설명
- ```@RunWith(SpringRunner.class)```: 스프링과 테스트 통합
- ```@SpringBootTest```: 스프링 부트 띄우고 테스트(이게 없으면 ```@Autowired``` 다 실패)
- ```@Transactional```: 반복 가능한 테스트 지원, 각각의 테스트를 실행할 때마다 트랜잭션을 시작하고 **테스트가 끝나면 트랜잭션을 강제로 롤백** (이 어노테이션이 테스트 케이스에서 사용될 때만 롤백)

### 기능 설명
- 회원가입 테스트
- 중복 회원 예외처리 테스트

> 참고: 테스트 케이스 작성 고수되는 마법: Given, When, Then
> 이 방법이 필수는 아니지만 이 방법을 기본으로 해서 다양하게 응용하는 것을 권장한다.

### 테스트 케이스를 위한 설정
테스트는 케이스 격리된 환경에서 실행하고, 끝나면 데이터를 초기화하는 것이 좋다. 그런 면에서 메모리 DB를 사용하는 것이 가장 이상적이다.
추가로 테스트 케이스를 위한 스프링 환경과, 일반적으로 애플리케이션을 실행하는 환경은 보통 다르므로 설정 파일을 다르게 사용하자.
다음과 같이 간단하게 테스트용 설정 파일을 추가하면 된다.
- ```test/resources/application.yml```
이제 테스트에서 스프링을 실행하면 이 위치에 있는 설정 파일을 읽는다. (이 위치에 없으면 src/resources/application.yml을 읽는다.)

스프링 부트는 datasource 설정이 없으면, 기본적으로 메모리 DB를 사용하고, driver-class도 현재 등록된 라이브러리를 보고 찾아준다.
추가로 ```ddl-auto```도 ```create-drop```모드로 동작한다. 따라서 데이터소스나, JPA 관련된 별도의 추가 설정을 하지 않아도 된다.

# 상품 도메인 개발
**구현 기능**
- 상품 등록
- 상품 목록 조회
- 상품 수정

**순서**
- 상품 엔티티 개발(비즈니스 로직 추가)
- 상품 리포지토리 개발
- 상품 서비스 개발
- 상품 기능 테스트
## 상품 엔티티 개발(비즈니스 로직 추가)
도메인 주도 설계에서 엔티티 자체에서 해결할 수 있는 경우 엔티티 안에 비즈니스 로직을 넣는 것이 좋다.
#### 비즈니스 로직 분석
- ```addStock()```메서드는 파라미터로 넘어온 수만큼 재고를 늘린다. 이 메서드는 재고가 증가하거나 숭품 주문을 취소해서 재고를 다시 늘려야 할 때 사용한다.
- ```removeStock()```메서드는 파라미터로 넘어온 수만큼 재고를 줄인다. 만약 재고가 부족하면 예외가 발생한다. 주로 상품을 주문할 때 사용한다.

## 상품 리포지토리 개발
### 기능 설명
- ```save()```
  - ```id```가 없으면 신규로 보고 ```persist()``` 실행
  - ```id```가 있으면 이미 데이터베이스에 저장된 엔티티를 수정한다고 보고, ```merge()```를 실행, 자세한 내용은 뒤에서 웹에서 설명(그냥 지금은 저장한다 정도로 생각하자)

## 상품 서비스 개발
상품 서비스는 상품 리포지토리에 단순히 위임만 하는 클래스
#### 상품 기능 테스트
상품 테스트는 회원 테스트와 비슷하므로 생략

# 주문 도메인 개발
**구현 기능**
- 상품 주문
- 주문 내역 조회
- 주문 취소

**순서**
- 주문 엔티티, 주문상품 엔티티 개발
- 주문 리포지토리 개발
- 주문 서비스 개발
- 주문 검색 기능 개발
- 주문 기능 테스트
## 주문, 주문상품 엔티티 개발
### 주문 엔티티 개발
### 기능 설명
- **생성 메서드**(```createOrder()```): 주문 엔티티를 생성할 때 사용한다. 주문 회원, 배송정보, 주문상품의 정보를 받아서 실제 주문 엔티티를 생성한다.
- **주문 취소**(```cancel()```): 주문 취소시 사용한다. 주문 상태를 취소로 변경하고 주문상품에 주문 취소를 알린다. 만약 이미 배송을 완료한 상품이면 주문을 취소하지 못하도록 예외를 발생시킨다. 
- **전체 주문 가격 조회**: 주문 시 사용한 전체 주문 가격을 조회한다. 전체 주문 가격을 알려면 각각의 주문상품 가격을 알아야 한다. 로직을 보면 연관된 주문상품들의 가격을 조회해서 더한 값을 반환한다.(실무에서는 주로 주문에 전체 주문 가격 필드를 두고 역정규화 한다.)

## 주문 리포지토리 개발
주문 리포지토리에는 주문 엔티티를 저장하고 검색하는 기능이 있다. 마지막의 ```findAll(OrderSearch orderSearch)```메서드는 조금 뒤에 있는 주문 검색 기능에서 자세히 알아보자.

## 주문 서비스 개발
Cascade를 사용할 수 있는 범위: 참조하는게 private Owner일 때 사용하는 것이 권장된다.   
ex) Order가 Delivery를 관리하고, Order가 OrderItem을 관리한다. Order에서만 Delivery와 OrderItem이 사용된다. 이 경우에만 사용하는 것이 좋다.

엔티티의 상태를 변경시키면 JPA의 더티체킹(변경내역 감지)을 이용해 직접 쿼리에 파라미터로 보내지 않아도 테이블에 데이터가 갱신된다.

주문 서비스는 주문 엔티티와 주문 상품 엔티티의 비즈니스 로직을 활용해서 주문, 주문 취소, 주문 내역 검색 기능을 제공한다.

참고: 예제를 단순화하려고 한 번에 하나의 상품만 주문할 수 있다.

- **주문**(```order()```): 주문하는 회원 식별자, 상품 식별자, 주문 수량 정보를 받아서 실제 주문 엔티티를 생성한 후 저장한다.
- **주문 취소**(```cancelOrder()```): 주문 식별자를 받아서 주문 엔티티를 조회한 후 주문 엔티티에 주문 취소를 요청한다.
- **주문 검색**(```findOrders()```): ```OrderSearch```라는 검색 조건을 가진 객체로 주문 엔티티를 검색한다. 자세한 내용은 다음에 나오는 주문 검색 기능에서 알아보자.

> 참고: 주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다.
> 서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다. 이처럼 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을 도메인 모델 패턴이라 한다.
> 반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분의 비즈니스 로직을 처리하는 것을 트랜잭션 스크립트 패턴이라 한다.

## 주문 기능 테스트
**테스트 요구사항**
- 상품 주문이 성공해야 한다.
- 상품을 주문할 때 재고 수량을 초과하면 안된다.
- 주문 취소가 성공해야 한다.

#### 상품 주문 테스트 코드
상품 주문이 정상 동작하는지 확인하는 테스트다. Given 절에서 테스트를 위한 회원과 상품을 만들고 When 절에서 실제 상품을 주문하고 Then 절에서 주문 가격이 올바른지, 주문 후 재고 수량이 정확히 줄었는지 검증한다.

#### 재고 수량 초과 테스트
재고 수량을 초과해서 상품을 주문해보자. 이때는 ```NotEnoughStockException```예외가 발생해야 한다.

#### 주문 취소 테스트
주문을 취소하면 그만큼 재고가 증가해야 한다.
주문을 취소하려면 먼저 주문을 해야한다. Given 절에서 주문하고 When 절에서 해당 주문을 취소했다. Then 절에서 주문상태가 주문 취소 상태인지(```CANCEL```), 취소한 만큼 재고가 증가했는지 검증한다.

## 주문 검색 기능 개발
JPA에서 **동적 쿼리**를 어떻게 해결해야 하는가?

```findAll(OrderSearch orderSearch)```메서드는 검색 조건에 동적으로 쿼리를 생성해서 주문 엔티티를 조회한다.
### JPQL로 처리
JPQL 쿼리를 문자로 생성하기는 번거롭고, 실수로 인한 버그가 충분히 발생할 수 있다.

### JPA Criteria로 처리
JPA Critertia는 JPA 표준 스펙이지만 실무에서 사용하기에 너무 복잡하다. 결국 다른 대안이 필요하다. 많은 개발자가 비슷한 고민을 했지만, 가장 멋진 해결책은 Querydsl이 제시했다.
Querydsl 소개장에서 간단히 언급하겠다. 

# 웹 계층 개발
## 홈 화면과 레이아웃
#### 스프링 부트 타임리프 기본 설정
- 스프링 부트 타임리프 viewName 매핑
  - ```resources:templates/``` + {ViewName} + ```.html```
  - ```resources:templates/home.html```

반환한 문자(```home```)과 스프링부트 설정 ```prefix```, ```suffix``` 정보를 사용해서 렌더링할 뷰(```html```)를 찾는다.

> 참고: Hierarchical-style layout
> 예제에서는 뷰 템플릿을 최대한 간단하게 설명하려고, ```header```, ```footer```같은 템플릿 파일을 반복해서 포함한다. 다음 링크의 Hierarchical-style layouts을 참고하면 이런 부분도 중복을 제거할 수 있다.
> https://www.thymeleaf.org/doc/articles/layouts.html

## 회원 등록
- 폼 객체를 사용해서 화면 계층과 서비스 계층을 명확하게 분리한다.

## 회원 목록 조회
#### 회원 목록 컨트롤러 추가
- 조회한 상품을 뷰에 전달하기 위해 스프링 MVC가 제공하는 모델(```Model```) 객체에 보관
- 실행할 뷰 이름 반환

**회원 목록 뷰**
> 참고: 타임리프에서 ?를 사용하면 ```null```을 무시한다.

> 참고: 폼 객체 vs 엔티티 직접 사용
> 요구사항이 정말 단순할 때는 폼 객체(```MemberForm```)없이 엔티티(```Member```)를 직접 등록과 수정 화면에서 사용해도 된다.
> 하지만 화면 요구사항이 복잡해지기 시작하면, 엔티티에 화면을 처리하기 위한 기능이 점점 증가한다. 
> 결과적으로 엔티티는 점점 화면에 종속적으로 변하고, 이렇게 화면 기능 때문에 지저분해진 엔티티는 결국 유지보수하기 어려워진다.
> 실무에서 **엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 한다.** 
> 화면이나 API에 맞는 폼 객체나 DTO를 사용하자. 그래서 화면이나 API 요구사항을 이것들로 처리하고, 엔티티는 최대한 순수하게 유지하자.

## 상품 등록
#### 상품 등록
- 상품 등록 폼에서 데이터를 입력하고 Submit 버튼을 클릭하면 ```/items/new```를 POST 방식으로 요청
- 상품 저장이 끝나면 상품 목록 화면(```redirect:/items```)으로 리다이렉트

## 상품 목록
```model```에 담아둔 상품 목록인 ```items```를 꺼내서 상품 정보를 출력

## 상품 수정
#### 상품 수정 폼 이동
1. 수정 버튼을 선택하면 ```/items/{itemId}/edit``` URL을 GET 방식으로 요청
2. 그 결과로 ```updateItemForm()``` 메서드를 실행하는데 이 메서드는 ```itemService.findOne(itemId)```를 호출해서 수정할 상품을 조회
3. 조회 결과를 모델 객체에 담아서 뷰(```items/updateItemForm```)에 전달
#### 상품 수정 실행
상품 수정 폼 HTML에는 상품의 id(hidden), 상품명, 가격, 수량 정보 있음
1. 상품 수정 폼에서 정보를 수정하고 Submit 버튼을 선택
2. ```/items/{itemId}/edit``` URL을 POST 방식으로 요청하고 ```updateItem()``` 메서드를 실행
3. 이때 컨트롤러에 파라미터로 넘어온 ```item``` 엔티티 인스턴스는 현재 준영속 상태다. 따라서 영속성 컨텍스트의 지원을 받을 수 없고 데이터를 수정해도 변경 감지 기능은 동작X

## 변경 감지와 병합(merge)
#### 준영속 엔티티?
영속성 컨텍스트가 더는 관리하지 않는 엔티티를 말한다.
(여기서는 ```itemService.saveItem(book)```)에서 수정을 시도하는 ```Book```객체다. ```Book```객체는 이미 DB에 한번 저장되어서 식별자가 존재한다. 이렇게 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으며 준영속 엔티티로 볼 수 있다.)  
데이터베이스에 한번 갔다와서 JPA가 식별할 수 있는 Id 필드를 가지고 있는 객체 
#### 준영속 엔티티를 수정하는 2가지 방법
- 변경 감지 기능 사용
- 병합(```merge```) 사용
 
### 변경 감지 기능 사용
```java
@Transactional
void update(Item itemParam) {
    Item findItem = em.find(Item.class, item.Param.getId()); // 같은 엔티티를 조회한다.
        findItem.setPrice(itemParam.getPrice()); // 데이터를 수정한다.
        }
```
영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법
트랜잭션 안에서 엔티티를 다시 조회, 변경할 값 선택 -> 트랜잭션 커밋 시점에 변경 감지(Dirty Checking)이 동작해서 데이터베이스에 UPDATE SQL 실행

### 병합 사용
병합은 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능이다.
```java
@Transactional
void update(Item itemParam) {
    Item mergeItem = em.merge(itemParam);
        }
```
**병합**: 기존에 있는 엔티티
#### 병합 동작 방식
1. ```merge()```를 실행한다.
2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
2-1. 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
3. 조회한 영속 엔티티(```mergeMember```)에 ```member``` 엔티티의 값을 채워 넣는다. (member 엔티티의 모든 값을 mergeMember에 밀어 넣는다. 이때 mergeMember의 "회원1"이라는 이름이 "회원명변경"으로 바뀐다.)
4. 영속 상태인 mergeMember를 반환한다.

#### 병합 동작 방식을 간단히 정리
1. 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한다.
2. 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.(병합한다.)
3. 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행

> 주의: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경된다. 병합시 값이 없으면 ```null```로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)

### 상품 리포지토리의 저장 메서드 분석 ```ItemRepository```