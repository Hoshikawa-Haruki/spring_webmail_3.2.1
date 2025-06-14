<%-- 
    Document   : index
    Created on : 2022. 6. 10., 오후 2:19:43
    Author     : skylo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="deu.cse.spring_webmail.control.CommandType"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>로그인 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
    </head>
    <body>
        <%@include file="header.jspf"%>


        <div id="login_form">
            <%-- 
                Spring Security에서는 login.do?menu=... 같은 파라미터는 필요 없음.
                Spring Security가 /login.do로 POST 요청을 처리하도록 설정되어 있으므로
                form의 action은 반드시 login.do로 설정. (GET 요청 아님!)
                contextPath를 포함시켜야 컨텍스트 경로(/webmail 등)에서도 잘 동작함.
            --%>
            <form method="POST" action="${pageContext.request.contextPath}/login.do">
                <sec:csrfInput />  <!-- 자동으로 CSRF 토큰을 <input type="hidden"> 형태로 삽입-->
                사용자: <input type="email" name="userid" size="20" autofocus> <br />
                암&nbsp;&nbsp;&nbsp;호: <input type="password" name="passwd" size="20"> <br /> <br />
                <input type="submit" value="로그인" name="B1">&nbsp;&nbsp;&nbsp;
                <input type="reset" value="다시 입력" name="B2">
            </form>
        </div>



        <%@include file="footer.jspf"%>
    </body>
</html>
