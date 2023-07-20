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
