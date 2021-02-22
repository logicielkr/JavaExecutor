# 실시간 Java 실행기 기능 개요 및 주요 화면

이 프로그램은 메모장 프로그램에서 Java 실행 기능을 추가한 것으로서, 외관은 [메모장 프로그램 기능 개요 및 주요 화면](https://github.com/logicielkr/memo/blob/master/introduction.md) 과 거의 유사하다.

메모장 프로그램과의 차이는 입력/수정 화면에서 Java 코드를 실행하고, 그 결과를 확인할 수 있다는 차이가 있다.

여기서는 입력/수정 기능에서 Java 코드를 실행하는 것을 중심으로 살펴보기로 하고, 나머지 기능은 [메모장 프로그램 기능 개요 및 주요 화면](https://github.com/logicielkr/memo/blob/master/introduction.md) 을 참고하면 된다.

## 1. 입력/수정 기능

입력/수정 기능의 기본 화면은 다음과 같다.

<img src="http://graha.kr/static-contents/images/java/insert.png" alt="입력/수정 기능" />

Import 패키지는 다음과 같이 입력한다.

- 여러 개는 줄바꿈을 해서 입력하면 되고,
- 패키지 이름만 입력하면 되지만, 일반적인 java 소스코드의 import 구문을 그대로 가져와도 무방하다.
- 처음의 "import" 나 마지막의 ";" 정도는 알아서 처리한다.

실행할 java 코드는 외부소스 아래의 입력창에 입력하면 된다.

- 미리 정의된 out.println 이라는 함수를 사용하면, 아래쪽에 결과창에 로그를 찍을 수 있다.

java를 실행하기 위해서는 제목 입력창 오른쪽의 execute 버튼을 클릭하거나, Ctrl + Enter을 입력하면 된다.

## 2. 상세보기 기능

상세보기 기능의 기본 화면은 다음과 같다.

<img src="http://graha.kr/static-contents/images/java/detail.png" alt="상세보기 기능" />

메모장 프로그램과 비교하면 다음과 같이 차이가 있다.

- 화면에 Import 패키지와 실행결과를 표시한다.
- 소스코드는 java로 highlight 처리한다.
