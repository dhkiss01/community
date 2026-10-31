# 더그아웃 화면 메모

야구 커뮤니티에서 가장 자주 할 일은 글을 읽고, 관심 있는 팀 이야기를 찾고, 내 의견을 남기는 일이라고 보고 화면을 구성했습니다. 실시간 데이터가 없는 경기 점수판이나 순위표를 장식으로 넣지는 않았습니다.

화면은 따뜻한 흰색, 짙은 초록, 회색을 중심으로 구성했습니다. 팀마다 다른 색을 쓰는 대신 구단 이름을 같은 형태의 버튼으로 보여줍니다. 넓은 화면에서는 안내를 오른쪽에 두고, 모바일에서는 글 목록에 집중하도록 안내 영역을 접습니다.

- 검색창과 구단 검색 버튼을 바로 보이게 두었습니다. 사용자가 검색 방법을 기억할 필요를 줄이려는 선택입니다. [NN/g: Recognition Rather Than Recall](https://www.nngroup.com/articles/recognition-and-recall/)
- 로딩, 결과 없음, 서버 오류를 구분하고 다음 행동을 안내합니다. [NN/g: Visibility of System Status](https://www.nngroup.com/articles/visibility-system-status/), [Stack Overflow: Empty states](https://stackoverflow.design/system/components/empty-states/)
- 자동 무한 스크롤 대신 더 보기 버튼을 사용하고, 검색 조건은 URL에 남깁니다. 뒤로 가기와 링크 공유로 탐색을 이어갈 수 있도록 했습니다. 이 부분은 위 원칙을 프로젝트에 적용한 설계 판단입니다.
- 입력에는 레이블, 키보드 포커스 표시, 본문 바로가기, 상태 안내를 넣었습니다. 작성 중인 글은 탭의 sessionStorage에 임시 보관합니다. 실제 사용자 인터뷰나 사용성 실험을 수행한 결과는 아닙니다.

README는 [Discourse](https://github.com/discourse/discourse/blob/main/README.md)의 소개 → 실행/개발 안내 흐름과 [Roblox Foreman](https://github.com/Roblox/foreman/blob/main/README.md)의 짧은 실행 예시 구성을 참고했습니다. 본문은 이 저장소의 실제 명령과 제약에 맞춰 작성했습니다.

검색 구현은 [Elastic Nori 문서](https://www.elastic.co/docs/reference/elasticsearch/plugins/analysis-nori)를 참고했습니다.
