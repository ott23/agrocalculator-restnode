<%@tag description="Main Wrapper" pageEncoding="UTF-8" %>
<%@attribute name="title" required="false" %>
<%@attribute name="username" required="false" %>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>${title}</title>

    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">

    <link rel="shortcut icon" type="image/x-icon" href="<c:url value="/images/favicon.ico" />" />

    <link href="<c:url value="/css/bootstrap.min.css" />" rel="stylesheet"/>
    <link href="<c:url value="/css/main.css" />" rel="stylesheet"/>

    <script src="<c:url value="/js/bootstrap.min.js" />" type="text/javascript"></script>

</head>
<body>

<div class="header">
    <div class="container">
        <div class="row">

            <div>
                <a href="<c:url value="/" />">Главная</a>
                <a href="<c:url value="/user" />">Список пользователей</a>
                <a href="<c:url value="/user/add" />">Добавить пользователя</a>
            </div>

            <c:if test="${not empty username}"><div>${username}</div></c:if>
            <c:if test="${empty username}"><div>Вы не авторизованы</div></c:if>

        </div>
    </div>
</div>

<div class="content">
    <div class="container">
        <jsp:doBody/>
    </div>
</div>

<div class="footer">
</div>

</body>
</html>