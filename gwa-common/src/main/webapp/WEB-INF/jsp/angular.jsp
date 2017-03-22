<%@ page
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"
  session="false"
%><%@
  taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><c:set var="request" value="${pageContext.request}" />
<!DOCTYPE html>
<html>
  <head>
    <base href="${request.contextPath}/" />
    <title>Application</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="css/styles.css" rel="stylesheet">

    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css" rel="stylesheet" type="text/css">
   
    <script type="text/javascript" src="https://code.jquery.com/jquery-1.12.1.min.js"></script>
    <script type="text/javascript" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
  </head>

  <body>
    <bcgov-template>
    </bcgov-template>
    <script type="text/javascript" src="https://unpkg.com/core-js/client/shim.min.js"></script>
    <script type="text/javascript" src="https://unpkg.com/zone.js@0.7.4?main=browser"></script>
    <script type="text/javascript" src="js/system.src.js"></script>
    <script type="text/javascript" src="systemjs.config.server.js"></script>
    <script type="text/javascript">
    SystemJS.import('js/main.bundle.js');
    </script>
    
  </body>
</html>
