# [Graha 응용프로그램]실시간 Java 실행기

## 0. notice

최근에 Graha 소스코드는 새롭게 작성되었고,
이를 반영하기 위한 약간의 수정이 있었다.

## 1. about

Graha를 활용한 실시간 Java 실행기 프로그램이다.  

## 2. 기능

Java 실행기 프로그램은 메모장 프로그램에 실시간 Java 실행 기능을 추가한 것이다.

## 3. 실행환경

이 프로그램은 다음과 같은 환경에서 개발되고 테스트 되었다.

- Apache Tomcat 7.x 이상
- PostgreSQL 9.1.4 이상
- JDBC 4.0 이상
- Graha 0.5.0.6 이상

## 4. 주의사항

이 프로그램을 사용할 때 주의사항은 다음과 같다.

- 이 프로그램은 클라이언트에서 작성한 Java 코드를 실시간으로 서버에서 컴파일한 다음, 실행하는 기능을 포함하고 있으므로, 보안에 취약점이 있으며, 개인적으로 혹은 폐쇠적인 개발팀 내에서만 사용해야 한다.
- out.println 메소드를 사용하면 클라이언트에 결과를 가져올 수 있다.

## 5. 배포하는 곳

* 소스코드 : GitHub JavascriptExecutor 프로젝트 (https://github.com/logicielkr/JavaExecutor)
* 웹사이트 : Graha 홈페이지 (https://graha.kr)