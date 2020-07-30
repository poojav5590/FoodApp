<%--
  Created by IntelliJ IDEA.
  User: pvasudev
  Date: 4/1/20
  Time: 4:49 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Food Search</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
<h1>Food Search</h1>
<form action="getFood" method="GET">
  <label for="letter">Type food item of interest to search nutrient facts: </label>
  <input type="text" name="searchWord" value="" /><br>
  <input type="submit" value="Submit" />
  <% if (request.getAttribute("resultString") != null ) { %>
  <p>Nutrient Facts: <%= request.getAttribute("resultString")%></p>
  <% } %>
</form>
</body>
</html>
