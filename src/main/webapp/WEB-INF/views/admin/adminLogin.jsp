<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MOVE:ON 관리자</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body class="admin-page">

<%-- 관리자 화면은 우리끼리 PC 로만 쓴다.
     그래서 회원 화면(auth.css)의 휴대폰 폭에 맞추지 않고 따로 간다. --%>
<main class="admin-login">
    <h1>MOVE:ON <small>관리자</small></h1>
    <p class="login-sub">행사를 찾아 등록하고 검수하는 곳입니다.</p>

    <c:if test="${not empty msg}">
        <p class="login-error">${msg}</p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/admin/login">
        <label>
            <span>아이디</span>
            <input type="text" name="loginId" required autofocus autocomplete="username">
        </label>
        <label>
            <span>비밀번호</span>
            <input type="password" name="password" required autocomplete="current-password">
        </label>
        <button type="submit" class="btn-primary">로그인</button>
    </form>
</main>

</body>
</html>
