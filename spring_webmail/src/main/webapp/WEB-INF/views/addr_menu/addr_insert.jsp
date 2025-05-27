<%-- 
    Document   : addr_insert
    Created on : 2025. 5. 5., 오전 3:26:19
    Author     : Haruki
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="mytags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>

<%-- @taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" --%>


<html html lang="ko" xml:lang="ko">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>주소록 추가 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
    </head>
    <body>
        <script>
            function autoHyphenPhone(str) {
                str = str.replace(/[^0-9]/g, ''); // 숫자 이외 제거
                let result = "";

                if (str.length < 4) {
                    result = str;
                } else if (str.length < 7) {
                    result = str.slice(0, 3) + "-" + str.slice(3);
                } else if (str.length < 11) {
                    result = str.slice(0, 3) + "-" + str.slice(3, 6) + "-" + str.slice(6);
                } else {
                    result = str.slice(0, 3) + "-" + str.slice(3, 7) + "-" + str.slice(7, 11);
                }
                return result;
            }

            function formatPhoneNumber(e) {
                const input = e.target;
                input.value = autoHyphenPhone(input.value);
            }
        </script>

        <%@include file="../header.jspf"%>

        <div id="sidebar">
            <jsp:include page="../sidebar_previous_menu.jsp" />
        </div>

        <div id="main">
            <h1>주소록 정보 입력</h1>
            <form name="addAddr" action="${pageContext.request.contextPath}/jpa/insert_addr" method="post">
                <sec:csrfInput />
                <table border="1" bgcolor="#CCFFCC" cellpadding="5">
                    <tr>
                        <td>이름</td>
                        <td><input type="text" name="name" required /></td>
                    </tr>
                    <tr>
                        <td>이메일</td>
                        <td><input type="email" name="email" required /></td>
                    </tr>
                    <tr>
                        <td>전화번호</td>
                        <td><input type="text" name="phone" required oninput="formatPhoneNumber(event)"/></td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center">
                            <input type="submit" value="주소록 추가" />
                            <input type="reset" value="초기화" />
                        </td>
                    </tr>
                </table>
            </form>
        </div>
        <c:if test="${not empty msg}">
            <script>
                alert("${msg}");
            </script>
        </c:if>

        <%@include file="../footer.jspf"%>
    </body>
</html>