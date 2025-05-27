<%-- 
    Document   : access_denied
    Created on : 2025. 5. 27., 오전 2:42:20
    Author     : Haruki
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
    <head><title>접근 거부</title></head>
    <body>
        <h2>⚠️ 접근 권한이 없습니다.</h2>
        <p>이 페이지는 관리자만 접근할 수 있습니다.</p>
        <a href="<c:url value='/main_menu' />">메인 메뉴로 돌아가기</a>
    </body>
</html>
