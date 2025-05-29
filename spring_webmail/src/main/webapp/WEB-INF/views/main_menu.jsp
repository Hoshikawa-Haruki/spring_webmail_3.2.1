<%-- 
    Document   : main_menu
    Created on : 2022. 6. 10., 오후 3:15:45
    Author     : skylo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html>

<!-- 제어기에서 처리하면 로직 관련 소스 코드 제거 가능!
<jsp:useBean id="pop3" scope="page" class="deu.cse.spring_webmail.model.Pop3Agent" />
<%
    pop3.setHost((String) session.getAttribute("host"));
    pop3.setUserid((String) session.getAttribute("userid"));
    pop3.setPassword((String) session.getAttribute("password"));
%>
-->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>주메뉴 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
        <script>
            <c:if test="${!empty msg}">
            alert("${msg}");
            </c:if>
        </script>
    </head>
    <body>
        <%@include file="header.jspf"%>

        <div id="sidebar">
            <jsp:include page="sidebar_menu.jsp" />
        </div>

        <!-- 메시지 삭제 링크를 누르면 바로 삭제되어 실수할 수 있음. 해결 방법은?       </script> -->
        <!-- 2025.05.04 lsh 해결 완료. MessageFormatter 클래스 참고 -->
        <div id="main">
            ${messageList}
        </div>
        <div style="margin-top: 20px; text-align: left; font-size: 18px; padding: 10px;">
            <c:choose>
                <c:when test="${totalPages == 0}">
                    <span>&nbsp;&nbsp;&nbsp;현재 페이지 없음</span>
                </c:when>
                <c:otherwise>
                    <span>&nbsp;&nbsp;&nbsp;페이지 :</span>
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <a href="${pageContext.request.contextPath}/main_menu?page=${i}"
                           style="margin: 0 5px; ${i == currentPage ? 'font-weight:bold; color:red;' : 'color:blue;'}">
                            ${i}
                        </a>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <%@include file="footer.jspf"%>
    </body>
</html>
