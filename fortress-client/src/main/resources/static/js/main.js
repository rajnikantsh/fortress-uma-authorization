Resources = {
		checkAccess: function(){
			var path = $("#path").val();
			var method = $("#method").val();
			$.ajax({
				url: "/rpt",
				method: "GET",
				
			}).done(function(data){
				debugger;
				var rpt = data.rptToken;
				var oxdId = data.oxdId
				var url = "https://localhost:9099/api/checkAccess?oxdId="+oxdId+"&path="+path+"&httpMethod="+method+"&rpt="+rpt;
				$.ajax({
					url: url,
					method: "GET",
				}).done(function(data2){
					console.log(data2);
					$("#checkAccessStatus").html(data2);
				});
			});
		},
		checkOxdId: function(){
			var oxdId = $("#oxdId").val();
			$.ajax({
				url: "/check/"+oxdId,
				method: "GET",
			}).done(function(data){
				console.log(data)
				$("#checkOxdId").html(data);
			});
		},
		all: function(){
			$.ajax({
				url: "https://localhost:9099/test/resource",
				method: "GET",
			}).done(function(data){
				console.log(data)
				$("#testResourceLabel").html(data);
			});
		}
}