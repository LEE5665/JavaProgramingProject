# 자바프로그래밍 프로젝트
Swing으로 개발한 일정 관리 프로그

# 프로젝트 정보
### 1. 제작기간
> 2025.5.20 ~ 2025.7.10
### 2. 참여 인원
> | 이름 | 역할 |
> |---|---|
> |이정재|개발|
> |최건|개발|
`굳이 나누자면.. 디자인 DB는 내가.. 라이브러리 추가 및 DB 조정은 최건..?`

# 사용 기술
- 개발 도구
  - 이클립스
- 사용 프레임워크
  - 자바 스윙
- 사용 라이브러리
  - 애니메이션(triden)
  - UI 관련(flatlaf, miglayout-swing, lgooddatepicker)
  - sqlite 연결(sqlite-jdb)
  - DB 커넥트 풀 조정(HikariCP)

# ERD
<details>
  <summary>ERD</summary>
  <img width="645" height="238" alt="Image" src="https://github.com/user-attachments/assets/e4276d95-c95f-440b-ac48-ce795d76afea" />
</details>

# 기능
로그인 & 회원가입
> sqlite 사용, 아이디는 로컬 저장소에 저장

다크모드
> UIManager LookAndFeel 기능을 이용해 제작

메모장
> 고정, 수정, 삭제 기능

To-Do List
> 날짜별로 Todo 리스트 작성, 체크박스 및 텍스트 추가, Todo 리스트 정렬, 검색 기능

# 스크린샷
<details>
  <summary>펼쳐보기</summary>
    <img width="671" height="432" alt="Image" src="https://github.com/user-attachments/assets/011785c9-f631-4394-a0dc-d3f7f96560c5" />
    <img width="1174" height="785" alt="Image" src="https://github.com/user-attachments/assets/ec55fce1-0034-4579-bff7-9a5726ee39cd" />
    <img width="1170" height="779" alt="Image" src="https://github.com/user-attachments/assets/b03972c5-e28d-410d-ba52-607052b07c83" />
</details>

# 느낀 점
- JAVA Swing의 기본 레이아웃으로는 이쁘게 만들 수가 없지만 라이브러리를 사용한다면 이쁘게 만들 수 있다.
