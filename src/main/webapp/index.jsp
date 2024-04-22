<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>JspCRUD</title>

</head>

<body>
    <h1>Welcome</h1>

<c:if test="${not empty errorMessage}">
    <div style="color: red;">
        <p>Error: ${errorMessage}</p>
    </div>
</c:if>

<c:if test="${not empty successMessage}">
    <div style="color: green;">
        <p>Done: ${successMessage}</p>
    </div>
</c:if>
    <form method="post" action="FormServlet">
        First Name: <input type="text" name="firstName"><br>
        Last Name: <input type="text" name="lastName"><br>
        Phone Number: <input type="number" name="phoneNumber"><br>
        <input type="submit" value="Add Data">
    </form>
<hr>
<form method="get" action="FormServlet">
    Row Per Page : <input type="number" name="rowsPerPage">
    <input type="submit" value="Row Per Page ">
</form>
</br>
<form method="get" action="FormServlet">
    SearchBar: <input type="text" name="searchQuery">
    <input type="submit" value="Search">
</form>

</br>
<form method="get" action="FormServlet">
    <select name="sortOrder">
        <option value="ASC">Ascending</option>
        <option value="DESC">Descending</option>
    </select>
    <input type="submit" value="Sort">
</form>

    <h2>User Data:</h2>
    <table border="1">
        <tr>
            <th>ID</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Phone Number</th>
             <th>Action</th> <!-- Add a new column for action -->
        </tr>
         <c:forEach var="user" items="${userData}">
                   <tr>
                       <td>${user.id}</td>
                       <td>${user.firstName}</td>
                       <td>${user.lastName}</td>
                       <td>${user.phoneNumber}</td>
                       <td>
                       <form method="post" action="DeleteServlet">
                       <!-- Hidden input field to send user ID -->
                       <input type="hidden" name="id" value="${user.id}">
                       <input type="submit" value="Delete">
                       </form>
                       </td>
                   </tr>
               </c:forEach>
    </table>
 <div>
         Page:
         <c:forEach var="page" begin="1" end="${totalPages}">
             <c:url value="${requestScope.contextPath}" var="url">
                 <c:param name="page" value="${page}"/>
                 <c:param name="searchQuery" value="${param.searchQuery}"/>
                 <c:param name="sortOrder" value="${param.sortOrder}"/>
                 <c:param name="rowsPerPage" value="${param.rowsPerPage}"/>
             </c:url>
             <c:choose>
                 <c:when test="${page eq currentPage}">
                     <b>${page}</b>
                 </c:when>
                 <c:otherwise>
                     <a href="${url}">${page}</a>
                 </c:otherwise>
             </c:choose>
         </c:forEach>
     </div>
</body>
</html>
