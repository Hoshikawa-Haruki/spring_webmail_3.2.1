<%-- 
    Document   : addr
    Created on : 2025. 5. 5., 오전 3:32:38
    Author     : Haruki
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="mytags" %>

<!DOCTYPE html>

<%-- @taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" --%>


<html lang="ko" xml:lang="ko">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>주소록 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
    </head>
    <body>
        <%@include file="../header.jspf"%>

        <div id="sidebar">
            <jsp:include page="../sidebar_previous_menu.jsp" />
        </div>

        <div id="main">
            <h1>주소록</h1>
            <hr/>
            <%--
            2025.05.25 jpa사용으로 인해 삭제
            <c:catch var="errorReason">
                <mytags:addrbook user="jdbctester" password="12345"
                                 schema="webmail" table="addressbook"/>
            </c:catch>
            --%>
            <mytags:addrbook />
            ${empty errorReason ? "<noerror>" : errorReason} <!-- 오류 원인 출력 -->
            <br/>
            <a href="${pageContext.request.contextPath}/insert_addr">주소록 추가</a>
        </div>
        <c:if test="${not empty msg}">
            <script>
                alert("${msg}");
            </script>
        </c:if>
        <%@include file="../footer.jspf"%>
    </body>
</html>
