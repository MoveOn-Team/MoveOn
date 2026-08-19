<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
  하단 탭바 (4개 화면 공통)

  쓰는 쪽에서 active 값을 정해 넘긴다.
      <c:set var="active" value="recommend" />
      <jsp:include page="/WEB-INF/views/common/tabbar.jsp" />

  즉시운동 · 행사 · 내정보 는 아직 화면이 없어 링크를 걸지 않았다.
  없는 주소로 걸어두면 눌렀을 때 404 가 나므로, 만들 때 href 를 채우면 된다.
--%>
<nav class="tabbar" aria-label="주요 화면">

    <a class="tab-item ${active eq 'recommend' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/recommend/recommendList">
        <svg viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="11" cy="13" r="8"/>
            <circle cx="11" cy="13" r="3.4"/>
            <path d="M17 7l4-4M17 3h4v4"/>
        </svg>
        <span>추천</span>
    </a>

    <span class="tab-item is-todo" aria-disabled="true">
        <svg viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="14" cy="4.4" r="2"/>
            <path d="M6 21l3.5-6 3-2.5 1.5-4 3.5 3 3 .6"/>
            <path d="M9.5 15L6 12.5 8 8l4.5-1.5"/>
        </svg>
        <span>즉시운동</span>
    </span>

    <span class="tab-item is-todo" aria-disabled="true">
        <svg viewBox="0 0 24 24" aria-hidden="true">
            <rect x="3.5" y="5" width="17" height="16" rx="3"/>
            <path d="M3.5 10h17M8 3v4M16 3v4"/>
        </svg>
        <span>행사</span>
    </span>

    <span class="tab-item is-todo" aria-disabled="true">
        <svg viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="8" r="4"/>
            <path d="M4.5 20.5c0-4 3.4-6.5 7.5-6.5s7.5 2.5 7.5 6.5"/>
        </svg>
        <span>내정보</span>
    </span>

</nav>
