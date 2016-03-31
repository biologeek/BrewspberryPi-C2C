<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@page import="net.brewspberry.business.beans.Actioner"%>
<%@page import="java.util.List"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>

<head>
<title>Suivi en temps réél</title>
<!-- Bootstrap -->
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet"
	media="screen">
<link href="bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet"
	media="screen">
<link href="assets/styles.css" rel="stylesheet" media="screen">
<link href="vendors/jGrowl/jquery.jgrowl.css" rel="stylesheet"
	media="screen">

<!--/.fluid-container-->
<link href="vendors/datepicker.css" rel="stylesheet" media="screen">
<link href="vendors/uniform.default.css" rel="stylesheet" media="screen">
<link href="vendors/chosen.min.css" rel="stylesheet" media="screen">
<link href="css/additional.css" rel="stylesheet" media="screen">

<link href="vendors/wysiwyg/bootstrap-wysihtml5.css" rel="stylesheet"
	media="screen">

<script src="vendors/jquery-1.9.1.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
<script src="vendors/jquery.uniform.min.js"></script>
<script src="vendors/chosen.jquery.min.js"></script>
<script src="vendors/bootstrap-datepicker.js"></script>

<script src="vendors/wysiwyg/wysihtml5-0.3.0.js"></script>
<script src="vendors/wysiwyg/bootstrap-wysihtml5.js"></script>

<script src="vendors/wizard/jquery.bootstrap.wizard.min.js"></script>

<script type="text/javascript"
	src="vendors/jquery-validation/dist/jquery.validate.min.js"></script>
<script src="assets/form-validation.js"></script>

<script src="assets/scripts.js"></script>
<script>
	jQuery(document).ready(function() {
		FormValidation.init();
	});

	$(function() {
		$(".datepicker").datepicker();
		$(".uniform_on").uniform();
		$(".chzn-select").chosen();
		$('.textarea').wysihtml5();

		$('#rootwizard').bootstrapWizard(
				{
					onTabShow : function(tab, navigation, index) {
						var $total = navigation.find('li').length;
						var $current = index + 1;
						var $percent = ($current / $total) * 100;
						$('#rootwizard').find('.bar').css({
							width : $percent + '%'
						});
						// If it's the last tab then hide the last button and show the finish instead
						if ($current >= $total) {
							$('#rootwizard').find('.pager .next').hide();
							$('#rootwizard').find('.pager .finish').show();
							$('#rootwizard').find('.pager .finish')
									.removeClass('disabled');
						} else {
							$('#rootwizard').find('.pager .next').show();
							$('#rootwizard').find('.pager .finish').hide();
						}
					}
				});
		$('#rootwizard .finish').click(function() {
			alert('Finished!, Starting over!');
			$('#rootwizard').find("a[href*='tab1']").trigger('click');
		});
	});
</script>

<script type="text/javascript">

function activate_actioner(uuid) {

	$.ajax({
        url: 'Actionner?type=activate&uuid='+uuid,
        type: 'get',
        dataType: 'html',
        success: function(data) {
        	
        	document.getElementById(uuid).className = 'btn btn-success btn-mini';
        	
        }
    });
}
function deactivate_actioner(uuid) {

	$.ajax({
        url: 'Actionner?type=deactivate&uuid='+uuid,
        type: 'get',
        dataType: 'html',
        success: function(data) {
        	
        	document.getElementById(uuid).className = 'btn btn-danger btn-mini';
        	
        }
    });
}

</script>

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
            <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->
<script src="vendors/modernizr-2.6.2-respond-1.1.0.min.js"></script>

<script type="text/javascript" src="js/lib/soap/jquery.soap.js"></script>
<script type="text/javascript" src="js/lib/d3js/d3.js">
	
</script>
<!-- <script src="js/graph_builder.js"></script>

 -->
</head>

<body class="wysihtml5-supported">

	<%
		List<Actioner> brewList = (List<Actioner>) request
				.getAttribute("actioners");
	%>

	<jsp:include page="tpl/header.jsp"></jsp:include>


	<div class="container-fluid">

		<div class="row-fluid">
			<jsp:include page="tpl/sidebar.jsp"></jsp:include>
			<!--/span-->
			<div class="span9" id="content">

				<div class="row-fluid">
					<!-- block -->
					<div class="block">
						<div class="navbar navbar-inner block-header">
							<div class="muted pull-left">Résumé des bières</div>
						</div>
						<div class="block-content collapse in">
							<div class="span12">
								<table class="table">
									<thead>
										<tr>
											<th>#</th>
											<th>UUID</th>
											<th>Label</th>
											<th>Type</th>
											<th>Activé ?</th>
										</tr>
									</thead>
									<tbody>

										<c:forEach items="${actioners}" var="brew">
											<tr>
												<td>${brew.getAct_id()}</td>
												<td>${brew.getAct_uuid()}</td>
												<td>${brew.getAct_nom()}</td>
												<td>${brew.getAct_type()}</td>
												<td><c:if test="${!brew.getAct_activated()}">
														
															<button id="${brew.getAct_uuid()}" onclick="activate_actioner('${brew.getAct_uuid()}')" class="btn btn-danger btn-mini">Activate</button>
														
													</c:if> <c:if test="${brew.getAct_activated()}">

														<button id="${brew.getAct_uuid()}" onclick="deactivate_actioner('${brew.getAct_uuid()}')" class="btn btn-danger btn-mini">Deactivate</button>
														

													</c:if></td>
											</tr>
										</c:forEach>
									</tbody>
								</table>

							</div>
						</div>

						<div class="block-content collapse in">
							<div class="span12"></div>
						</div>
					</div>
				</div>
			</div>
			<jsp:include page="tpl/footer.jsp"></jsp:include>
</body>
</html>