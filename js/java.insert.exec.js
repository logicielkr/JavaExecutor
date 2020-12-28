function evalcode() {
	if(
		$("form#insert input.java_id").val() == ""
	) {
		autoSave(true, function() {evalcode2();});
	} else {
		evalcode2();
	}
}
function evalcode2() {
	$("form#insert textarea.results").val("");
	var formData = null;
	formData = new FormData();
	if($("form#insert input.java_id").val() != "") {
		formData.append("java_id", $("form#insert input.java_id").val());
	}
	if($("form#insert input.java_history_id").val() != "") {
		formData.append("java_history_id", $("form#insert input.java_history_id").val());
	}
	formData.append("contents", $("form#insert textarea.contents").val());
	formData.append("source", $("form#insert textarea.source").val());
	var url = "execute.xml";
	
	$.ajax({
		url: url,
		processData: false,
		contentType: false,
		type: 'POST',
		enctype: 'multipart/form-data',
		data: formData,
		success: function(result) {
			var obj = parse_xml_document(result);
			if(obj.results.err) {
				$("form#insert textarea.results").val(obj.results.err);
			} else if(obj.results.out) {
				$("form#insert textarea.results").val(obj.results.out);
			}
			
		}
	}).always(function() {
		
	});
}
function ctrlenter(e) {
	if((e.keyCode == 13 || e.keyCode == 10) && e.ctrlKey) {
		evalcode();
	}
}
function saveAsNew() {
	check_submit(
		document.getElementById("insert"), 
		'변경사항을 저장하시겠습니까?',
		function() {
			if($("form#insert input[name='parent_id']").val() == "") {
				$("form#insert input[name='parent_id']").val($("form#insert input[name='java_id']").val());
			}
			$("form#insert input[name='java_id']").val("");
		}
	);
}
$(document).ready(function() {
	if($("form#insert input[name='java_id']").val() != "") {
//		$("<button type='button' onclick='saveAsNew()'>새이름으로</button>").insertAfter($("button#insert_submit"));
		$("div.nav.top div.box.center").append($("<button type='button' onclick='saveAsNew()'>새이름으로</button>"));
	}
	$("textarea\.contents").keypress(function(e) {
			ctrlenter(e);
	});
	$("input\.execute").click(function() {
		evalcode();
	});
});
