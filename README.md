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
- Graha 0.5.1.187 이상
- javac (JVM) 혹은 Eclipse JDT Compilier (ecj) 컴파일러

## 4. Eclipse JDT Compilier (ecj) 컴파일러 사용에 대해서

서버에서 javac 를 찾을 수 없다면,
Eclipse JDT Compilier (ecj) 로 컴파일을 시도하고,
다음과 같이 컴파일러를 명시적으로 Eclipse JDT Compilier (ecj) 로 지정할 수도 있다.

```xml
<query id="execute" funcType="query" label="Java 코드 실행">
	<header>
		<prop name="compiler" value="ecj" />
		<prop name="compiler.ecj.warn" value="-unchecked,-serial,-raw" />
	</header>
	<commands>
		<command name="execute" type="native" class="kr.graha.sample.javaexecutor.JavaExecutorProcessorImpl" />
	</commands>
	<redirect path="/java/list" />
</query>
```

단, rt.jar 가 없어진 Java 9 이후에
Eclipse JDT Compilier (ecj) 버전이 오래되었다면,
다음과 같은 에러가 발생한다.

```
1. ERROR in WEB-INF/javaexecutor/classes/kr/graha/sample/javaexecutor/SampleH117.java (at line 1)
	package kr.graha.sample.javaexecutor;
	^
The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files
```

위와 같은 에러는 Apache Tomcat 7.x 와 8.x 에서 발생할 것으로 예상되는데,
이는 Apache Tomcat 7.x 와 8.x 에서는 ```lib/``` 디렉토리 아래의 ```ecj-*.jar``` 파일을 사용할 것이기 때문이다.

해결방법은 Eclipse JDT Compilier (ecj) 의 최신버전을 ```WEB-INF/lib``` 아래에 넣어 놓으면 된다.

> 시도해 본 것은 아니지만,
> Apache Tomcat 의 [JDTCompiler.java](https://github.com/apache/tomcat/blob/main/java/org/apache/jasper/compiler/JDTCompiler.java) 의 generateClass 메소드의 소스코드를 참고하면
> 조금더 우아하게 해결할 수 있는 방법이 있을 것으로 생각되지만, 이런 방법은 일단 보류하기로 한다.

## 5. 주의사항

이 프로그램을 사용할 때 주의사항은 다음과 같다.

- 이 프로그램은 클라이언트에서 작성한 Java 코드를 실시간으로 서버에서 컴파일한 다음, 실행하는 기능을 포함하고 있으므로, 보안에 취약점이 있으며, 개인적으로 혹은 폐쇠적인 개발팀 내에서만 사용해야 한다.
- out.println 메소드를 사용하면 클라이언트에 결과를 가져올 수 있다.

## 6. 배포하는 곳

* 소스코드 : GitHub JavascriptExecutor 프로젝트 (https://github.com/logicielkr/JavaExecutor)
* 웹사이트 : Graha 홈페이지 (https://graha.kr)