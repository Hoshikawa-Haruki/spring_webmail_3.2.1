<%-- 
    Document   : sidebar_menu
    Created on : 2022. 6. 10., 오후 3:25:30
    Author     : skylo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="deu.cse.spring_webmail.control.CommandType"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>웹메일 시스템 메뉴</title>
    </head>
    <body>
        <br> <br>

        <span style="color: indigo"> <strong>사용자: <%= session.getAttribute("userid")%> </strong> </span> <br>

        <p> <a href="main_menu"> 메일 읽기 </a> </p>
        <p> <a href="write_mail"> 메일 쓰기 </a> </p>
        <p> <a href="show_addr"> 주소록 </a> </p>
        <!--        <p> <a href="logout"> 로그아웃 </a></p>    -->
        <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline;">
            <sec:csrfInput />
            <button type="submit">로그아웃</button>
        </form>
    </body>
</html>
