# 화면 구성

[MLBPARK](https://mlbpark.donga.com/)의 분류별 게시판과 [네이버 스포츠](https://sports.naver.com/kbaseball/index)의 메뉴 구분을 참고했습니다. 두 사이트는 실제 브라우저에서 확인했습니다.

더그아웃은 게시글을 읽고 찾는 일을 중심으로 구성했습니다. 넓은 카드와 본문 미리보기 대신 번호, 게시판, 제목, 작성자, 날짜, 조회, 공감을 열로 정렬합니다. 댓글 수는 제목 바로 옆에 표시합니다. 모바일에서는 번호와 일부 열을 숨기고 제목 아래에 작성 정보를 보여줍니다.

화면은 흰색이며, 메뉴에는 남색, 댓글 수와 선택 표시에 붉은색을 조금 사용합니다. 로고는 Gmarket Sans Bold를 사용한 글자 로고입니다. 로그인과 회원가입은 상단에서 같은 텍스트 링크로 표시합니다. 오른쪽에는 로그인과 게시판 이동 기능을 둡니다.

게시판은 저장된 분류로 조회하며 검색도 선택한 게시판 안에서 수행합니다. 검색 초기화 후에도 분류를 유지합니다. 모든 브라우저 탭 제목은 더그아웃으로 고정했습니다.

참고 자료: [NN/g 상태 표시](https://www.nngroup.com/articles/visibility-system-status/), [Stack Overflow 빈 결과 안내](https://stackoverflow.design/system/components/empty-states/).

README 구성은 [Discourse](https://github.com/discourse/discourse/blob/main/README.md)와 [Roblox Foreman](https://github.com/Roblox/foreman/blob/main/README.md)의 개발, 실행 안내 구성을 참고했습니다.

## 로고 서체

[G마켓 공식 배포 페이지](https://corp.gmarket.com/fonts/)의 Gmarket Sans Bold를 사용합니다. 장식 아이콘 없이 더그아웃 네 글자를 원래 글자 폭으로 배치했습니다. 로고는 SVG로 저장해 웹폰트 로딩이나 운영체제의 대체 글꼴에 영향을 받지 않습니다. 브라우저 아이콘도 같은 서체의 더 한 글자를 사용합니다.

폰트 파일 자체는 배포하지 않으며, 출처 기록을 위해 공식 폰트에 포함된 [OFL 라이선스](../community-FE/public/licenses/GmarketSans-OFL.txt)를 함께 보관합니다.
