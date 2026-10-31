# 화면 구성

상단에는 전체글, 자유게시판, 직관 이야기, 경기 분석을 둡니다. 각 게시판은 게시글에 저장한 분류로 조회하며, 검색도 해당 분류 안에서 수행합니다. 검색 초기화 후에도 선택한 게시판을 유지합니다.

배너, 영문 장식 문구, 가입 권유 카드 없이 게시글과 검색을 바로 보여줍니다. 로그인과 회원가입은 같은 테두리 버튼 스타일을 사용합니다. 남색 헤더와 파란색 강조색을 사용하고, 본문은 흰색 목록으로 구분합니다. 글 작성 화면에서 게시판을 선택할 수 있습니다.

검색 조건을 주소에 남기고 로딩, 빈 결과, 오류를 구분합니다. 키보드 포커스 표시와 본문 바로가기를 제공하며, 글쓰기 초안은 탭에 임시 보관합니다.

참고 자료: [NN/g 상태 표시](https://www.nngroup.com/articles/visibility-system-status/), [Stack Overflow 빈 결과 안내](https://stackoverflow.design/system/components/empty-states/).

README 구성은 [Discourse](https://github.com/discourse/discourse/blob/main/README.md)와 [Roblox Foreman](https://github.com/Roblox/foreman/blob/main/README.md)의 개발, 실행 안내 구성을 참고했습니다.

배색과 메뉴 구분은 [MLB 공식 사이트](https://www.mlb.com/)의 남색 내비게이션을, 게시글 탐색 방식은 [MLBPARK](https://mlbpark.donga.com/)의 분류별 목록과 파란색 강조색을 실제 브라우저로 확인해 참고했습니다. 더그아웃에 필요한 게시판, 검색, 작성 기능에 맞춰 적용했습니다.
