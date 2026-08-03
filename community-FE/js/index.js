import BoardItem from '../component/board/boardItem.js';
import Dialog from '../component/dialog/dialog.js';
import Header from '../component/header/header.js';
import { authCheck, getServerUrl, prependChild, resolveImageUrl } from '../utils/function.js';
import { getPosts, searchPosts } from '../api/indexRequest.js';

const DEFAULT_PROFILE_IMAGE = '../public/image/profile/default.jpg';
const HTTP_NOT_AUTHORIZED = 401;
const SCROLL_THRESHOLD = 0.9;
const DEFAULT_SORT = 'recent';

let currentKeyword = '';
let currentSort = DEFAULT_SORT;

let page = 0; 
let isEnd = false;
let isProcessing = false;

const updateSortVisibility = () => {
    const sortRow = document.querySelector('#searchSortRow');
    if (!sortRow) return;
    const isSearching = currentKeyword.trim().length > 0;
    sortRow.classList.toggle('isHidden', !isSearching);
    sortRow.setAttribute('aria-hidden', String(!isSearching));
};

const getBoardItem = async (pageValue = 0) => {
    // 원래 정의된 순서: keyword, page, sort
    const result =
        currentKeyword.trim() === ''
            ? await getPosts(pageValue) 
            : await searchPosts(
                currentKeyword,
                pageValue,   // page=0
                currentSort, 
            );
    if (!result.ok) {
        throw new Error('Failed to load post list.');
    }
    return result.data;
};

const resetBoardList = () => {
    const boardList = document.querySelector('.boardList');
    if (boardList) {
        boardList.innerHTML = '';
    }
};

const loadBoardItems = async ({ reset = false } = {}) => {
    console.log(
        'loadBoardItems',
        'page=', page,
        'reset=', reset,
        'isProcessing=', isProcessing
    );
    if (isProcessing || (!reset && isEnd)) return;
    isProcessing = true;

    try {
        if (reset) {
            page = 0; 
            isEnd = false;
            resetBoardList();
        }
        
        const items = await getBoardItem(page);
        if (!items || items.length === 0) {
            isEnd = true;
            return;
        }
        
        const boardList = document.querySelector('.boardList');
        if (boardList && items) {
            const itemsHtml = items
                .map(data =>
                    BoardItem(
                        data.id,
                        data.createdAt,
                        data.title,
                        data.viewCount,
                        data.profileImage || null,
                        data.nickname,     
                        data.commentCount,
                        data.likeCount,
                    )
                )
                .join('');
            boardList.innerHTML += ` ${itemsHtml}`;
        }

        page += 1; 
    } catch (error) {
        console.error('Error fetching items:', error);
        isEnd = true;
    } finally {
        isProcessing = false;
    }
};

const addSearchEvent = () => {
    const searchInput = document.querySelector('#searchInput');
    const searchButton = document.querySelector('.searchButton');

    if (!searchInput || !searchButton) return;

    const runSearch = async () => {
        const trimmedKeyword = searchInput.value.trim();

        // 2글자 미만 검색 방지
        if (trimmedKeyword.length > 0 && trimmedKeyword.length < 2) {
            Dialog('검색 실패', '검색어는 2글자 이상 입력해주세요.');
            return;
        }

        // 같은 검색어면 다시 요청하지 않음
        if (currentKeyword === trimmedKeyword) return;

        currentKeyword = trimmedKeyword;
        updateSortVisibility();
        await loadBoardItems({ reset: true });
    };

    // 검색 버튼
    searchButton.addEventListener('click', runSearch);

    // Enter 검색
    searchInput.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            runSearch();
        }
    });

    // 검색어를 모두 지우면 전체 게시글 조회
    searchInput.addEventListener('input', async () => {
        if (searchInput.value.trim() === '' && currentKeyword !== '') {
            currentKeyword = '';
            updateSortVisibility();
            await loadBoardItems({ reset: true });
        }
    });
};

const addSortEvent = () => {
    const sortSelect = document.querySelector('#searchSortSelect');
    if (!sortSelect) return;
    sortSelect.value = currentSort;

    sortSelect.addEventListener('change', async () => {
        currentSort = sortSelect.value || DEFAULT_SORT;
        if (currentKeyword.trim().length === 0) return;
        await loadBoardItems({ reset: true });
    });
    
};

const addInfinityScrollEvent = () => {
    window.addEventListener('scroll', async () => {
        const hasScrolledToThreshold =
            window.scrollY + window.innerHeight >=
            document.documentElement.scrollHeight * SCROLL_THRESHOLD;

        if (hasScrolledToThreshold) {
            await loadBoardItems();
        }
    });
};

const init = async () => {
    try {
        const response = await authCheck();
        const data = await response.json();
        if (response.status === HTTP_NOT_AUTHORIZED) {
            window.location.href = '/html/login.html';
            return;
        }

        const profileImageUrl = resolveImageUrl(
            data.data.profileImageUrl,
            DEFAULT_PROFILE_IMAGE,
        );

        prependChild(
            document.body,
            Header('Community', 0, profileImageUrl),
        );

        updateSortVisibility();
        await loadBoardItems({ reset: true });

        addSearchEvent();
        addSortEvent();
        addInfinityScrollEvent();
    } catch (error) {
        console.error('Initialization failed:', error);
    }
};

init();