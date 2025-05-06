<%-- 
    Document   : addrbook
    Created on : 2025. 5. 5., 오전 1:01:20
    Author     : Haruki
--%>
<!--sql, core 등 jstl라이브러리 등록--> 
<%@tag description="JSTL" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<!--.tag를 사용하는 쪽에서 반드시 넘겨줘야 하는 속성들--> 
<%@attribute name="user" required="true" %>
<%@attribute name="password" required="true" %>
<%@attribute name="schema" required="true" %>
<%@attribute name="table" required="true" %>

<%-- any content can be specified here e.g.: --%>
<!--DB 연결 설정-->
<sql:setDataSource var="dataSrc"
                   url="jdbc:mysql://localhost:3306/${schema}?serverTimezone=Asia/Seoul"
                   driver="com.mysql.cj.jdbc.Driver"
                   user="${user}" password="${password}"/>

<sql:query var="rs" dataSource="${dataSrc}">
    SELECT email, name, phone FROM ${table} WHERE userid = ?
    <sql:param value="${sessionScope.userid}" />
</sql:query>

<table border="1">
    <thead>
        <tr>
            <th>이름</th>
            <th>이메일</th>
            <th>전화번호</th>
            <th>삭제하기</th>
            <th>메일쓰기</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="row" items="${rs.rows}">
            <tr>
                <td>${row.name}</td>
                <td>${row.email}</td>
                <td>${row.phone}</td>
                <td>
                    <%-- 삭제 버튼: 이메일 기준으로 삭제 --%>
                    <form action="${pageContext.request.contextPath}/jpa/delete_addr" method="post" style="display:inline;">
                        <input type="hidden" name="del_email" value="${row.email}" />
                        <button type="submit" onclick="return confirm('정말 삭제할까요?')">삭제</button>
                    </form>
                </td>
                <td>
                    <%-- 메일쓰기 버튼: 해당 이메일로 GET 요청 --%>
                    <form action="${pageContext.request.contextPath}/write_mail" method="get" style="display:inline;">
                        <input type="hidden" name="email_to" value="${row.email}" />
                        <button type="submit">메일쓰기</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
