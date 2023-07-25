# 글꼬리
## 프로젝트 개요

글을 쓰고 싶으나 주제를 생각하기 어려운 사람들을 위해, 날마다 주제를 추천해주며 해당 주제에 대해 글을 쓸 수 있는 게시판 서비스. 

다른 회원의 글에 좋아요을 누르고 해당 글을 개인 페이지에서 모아 볼 수 있다. 다른 회원을 구독하거나 른 회원이 한 회원을 구독했을 경우 페이지에서 모아 관리할 수 있다
## 팀 구성
<table>
  <tbody>
    <tr>
      <td align="center"><a href=""><img src="width="100px;" alt=""/><br /><sub><b>BE 팀장 : 김륜환</b></sub></a><br /></td>
      <td align="center"><a href=""><img src="" width="100px;" alt=""/><br /><sub><b>BE 팀원 : 최승인</b></sub></a><br /></td>
      <td align="center"><a href=""><img src="" width="100px;" alt=""/><br /><sub><b>BE 팀원 : 김태윤</b></sub></a><br /></td>
      <td align="center"><a href=""><img src="" width="100px;" alt=""/><br /><sub><b>BE 팀원 : 이준호</b></sub></a><br /></td>
      <td align="center"><a href=""><img src="" width="100px;" alt=""/><br /><sub><b>BE 팀원 : 신채안</b></sub></a><br /></td>
      <td align="center"><a href=""><img src="" width="100px;" alt=""/><br /><sub><b>BE 팀원 : 이준호</b></sub></a><br /></td>
    </tr>
  </tbody>
</table>

### 각 역할

김륜환 → 데이터베이스, 스프링 시큐리티, 소셜 로그인, 구독, 좋아요, 

최승인 → 게시글 전반, 댓글, 게시글 텍스트 에디터, 

김태윤 → 게시글 전반, 관리자, 좋아요, 해시태그, 주제, 

신채안 → 회원 전반, 이메일 중복 체크, 회원 가입, 임시 비밀번호, 활동량 잔디 심기, 

이준호 → DB보조, 회원 전반, 관리자, 해시태그, 주제, 신고, 

기술 스택 및 개발 도구
//프로젝트에 사용되는 개발 환경 및 도구 목록

<img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white">


VCS
Git
Git hub
IntelliJ Git

Java 11
Spring boot 2.7.1
Spring Security 5.7

DB Access

JPA
querydsl

DB
H2 Database
MySql

Template Engine
thymeleaf

Email
JavaMailSender

Oauth2 Server
spring-boot-starter-oauth2-client

Web Language
Javascript
Jquery


디렉토리 구조
 ```
   ├── application
    │   ├── EmailDto.java
    │   ├── EmailService.java
    │   ├── comment
    │   ├── follow
    │   ├── post
    │   ├── security
    │   ├── social
    │   └── user
    ├── domain
    │   ├── admin
    │   ├── comment
    │   ├── favorites
    │   ├── follow
    │   ├── hashtag
    │   ├── post
    │   ├── posthashtag
    │   ├── social
    │   ├── topic
    │   └── user
    ├── infrastructure
    │   ├── QuerydslConfiguration.java
    └── web
        ├── admin
        ├── comment
        ├── favorite
        ├── follow
        ├── home
        ├── post
        ├── social
        └── user
```
데이터베이스 스키마
프로젝트의 데이터베이스 스키마 설계 또는 ER 다이어그램
![image](https://github.com/geulkkoli-refactor/geulkkoli/assets/85615666/b1e6e3a8-9e34-49f7-8b56-f59155a02c52)

프로젝트 시연영상 모음
https://www.youtube.com/playlist?list=PLr9nS95erT7m5jrlJ80b1B3WeEYnyu4yq
