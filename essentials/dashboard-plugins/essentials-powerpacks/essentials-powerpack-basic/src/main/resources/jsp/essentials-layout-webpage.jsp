<!doctype html>
<%@ taglib prefix="hst" uri="http://www.hippoecm.org/jsp/hst/core" %>
<html lang="en">
<head>
  <meta charset="utf-8"/>

  <link rel="stylesheet" href="<hst:link  path="/css/bootstrap.css"/>" type="text/css"/>
  <hst:headContributions categoryExcludes="scripts" xhtml="true"/>
  <hst:headContributions categoryIncludes="globalJavascripts" xhtml="true"/>
  <hst:headContributions categoryIncludes="componentsJavascripts" xhtml="true"/>
</head>
<body>
<div class="container">
  <div class="row">
    <div class="col-md-6 col-md-offset-3"><hst:include ref="menu"/></div>
  </div>
  <div class="row">
    <hst:include ref="main"/>
  </div>
</div>
</body>
</html>