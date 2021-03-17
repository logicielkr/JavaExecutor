$(document).ready(function() {
	var ext = location.pathname.substring(location.pathname.lastIndexOf(".") + 1);
	$.ajax({
		url: "autosaved.xml",
		processData: false,
		contentType: false,
		type: 'GET',
		success: function(result){
			var obj = parse_graha_xml_document(result);
			if(obj && obj.rows && obj.rows["java_history"] && obj.rows["java_history"].length > 0 && obj.rows["java_history"][0].java_history_id) {
				MessageArea.confirm(
					"자동저장된 게시물을 확인하시겠습니까?", 
					function() {
						location.href = "insert." + ext + "?java_history_id=" + obj.rows["java_history"][0].java_history_id;
					},
					function() {
						if(isEncrypted) {
							PwdArea.show(initDecrypt);
						}
					}
				);
			}
		}
	});
	if(isEncrypted) {
		PwdArea.show(function() {
			$("table#java td.encrypted_title").each(function() {
				if($(this).text() != "") {
					var encrypted = $(this).text();
					var target = $(this).parent().find("td.title a");
					target.text(PwdArea.decrypt(encrypted));
					target.parent().prepend("<i class='material-icons security'>security</i>");
				}
			});
		}, true);
	}
});
