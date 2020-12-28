var selectedTitle = null;
var selectedContents = null;
var selectedContents2 = null;
function initDecrypt() {
	var encrypted = null;
	if(selectedTitle.parent().parent().parent().prop("id") == "java") {
		if(isEncrypted) {
			encrypted = selectedTitle.text();
			var decrypted = PwdArea.decrypt(encrypted);
			selectedTitle.text(decrypted);
			selectedTitle.prepend("<i class='material-icons security'>security</i>");
			$(document).attr("title", "메모(" + decrypted + ")");
			$("h2.title").text("메모(" + decrypted + ")");
			encrypted = selectedContents.text();
			selectedContents.text(PwdArea.decrypt(encrypted));
		}
		if(isChildEncrypted) {
			decryptChild();
		}
		if(PwdArea.saved()) {
			decryptHistory();
		}
	} else {
		if(selectedTitle.parent().find("td.encrypted_title").text() != "") {
			encrypted = selectedTitle.parent().find("td.encrypted_title").text();
			selectedTitle.text(PwdArea.decrypt(encrypted));
			selectedTitle.prepend("<i class='material-icons security'>security</i>");
		}
		appendButton(selectedTitle);
		if(selectedTitle.parent().find("td.encrypted").text() == "t") {
			encrypted = selectedContents.text();
			selectedContents.text(PwdArea.decrypt(encrypted));
		}
		selectedTitle.parent().find("td.encrypted").text("");
	}
	
	if(isMarked && selectedTitle.parent().parent().parent().prop("id") == "java") {
		mark();
	} else if(selectedTitle.parent().find("td.marked").text() == "t") {
		mark();
	}
}
function decryptChild() {
	$("table#java_child td.encrypted_title").each(function() {
		if($(this).text() != "") {
			var encrypted = $(this).text();
			
			var target = $(this).parent().find("td.title a");
			var source = $(this);
			target.text(PwdArea.decrypt(encrypted));
			source.text("");
			target.parent().prepend("<i class='material-icons security'>security</i>");
		}
	});
}
function decryptHistory() {
	$("table#java_history td.encrypted_title").each(function() {
		if($(this).text() != "") {
			var encrypted = $(this).text();
			var target = $(this).parent().find("td.title");
			var source = $(this);
			target.text(PwdArea.decrypt(encrypted));
			source.text("");
			target.prepend("<i class='material-icons security'>security</i>");
		}
	});
}
function mark() {
	marked.setOptions({
		renderer: new marked.Renderer(),
		highlight: function(code, language) {
			let validLanguage = hljs.getLanguage(language) ? language : 'plaintext';
			return hljs.highlight(validLanguage, code).value;
		},
		pedantic: false,
		gfm: true,
		breaks: false,
		sanitize: false,
		smartLists: true,
		smartypants: false,
		xhtml: false
	});
	var text = marked("```java\n" + selectedContents.text() + "\n```");
	selectedContents.hide();
	selectedContents2.show();
	selectedContents2.html(text);
	selectedContents2.find("code").each(function(i, block) {
		hljs.lineNumbersBlock(block);
	});
}
$(document).ready(function() {
	if(isEncrypted || isChildEncrypted || isHistoryEncrypted) {
		selectedTitle = $("table#java td.title");
		selectedContents = $("table#java td.contents");
		selectedContents2 = $("table#java td.contents2");
		PwdArea.show(initDecrypt, true);
	} else if(isMarked) {
		selectedContents = $("table#java td.contents");
		selectedContents2 = $("table#java td.contents2");
		mark();
	}
	$("table#java_history td.title").each(function() {
		$(this).click(function () {
			if(
				(
					$(this).parent().find("td.marked").text() == "t" &&
					$(this).parent().next().find("td.contents2").is(":visible")
				) || (
					$(this).parent().find("td.marked").text() == "" &&
					$(this).parent().next().find("td.contents").is(":visible")
				)
			) {
				$(this).parent().next().find("td.contents2").hide();
				$(this).parent().next().find("td.contents").hide();
				$(this).parent().next().next().find("td.source").hide();
				$(this).parent().next().next().next().find("td.results").hide();
				$(this).find("i.delete_forever").remove();
				$(this).find("i.history").remove();
			} else {
				$(this).parent().next().find("td.contents").show();
				$(this).parent().next().next().find("td.source").show();
				$(this).parent().next().next().next().find("td.results").show();
				if($(this).parent().find("td.encrypted").text() == "t") {
					selectedTitle = $(this);
					selectedContents = $(this).parent().next().find("td.contents");
					selectedContents2 = $(this).parent().next().find("td.contents2");
					PwdArea.show(
						initDecrypt,
						function() {
							appendButton(selectedTitle);
						}
					);
				} else {
					selectedContents = $(this).parent().next().find("td.contents");
					selectedContents2 = $(this).parent().next().find("td.contents2");
					if($(this).parent().find("td.marked").text() == "t") {
						mark();
					} else {
						selectedContents.show();
					}
					appendButton($(this));
				}
			}
		});
	});
});
function appendButton(target) {
	if(target.find("i").length < 2) {
		var history = $("<i class='material-icons history'>history</i>");
		history.click(function(event) {
			diff(target);
			event.stopPropagation();
		});
		target.append(history);
		var deleteForever = $("<i class='material-icons delete_forever'>delete_forever</i>");
		deleteForever.click(function(event) {
			MessageArea.confirm(
				"변경이력을 삭제하시겠습니까?", 
				function() {
					autoSave(target);
					return;
				}
			);
			event.stopPropagation();
		});
		target.append(deleteForever);
	}
}

function check_submit(form, msg) {
	MessageArea.confirm(
		msg, 
		function() {
			$(form).removeAttr("onsubmit");
			$(form).submit();
		}
	);
	return false;
}
function diff(target) {
	var one = target.parent().next().find("td.contents").text();
	var other = $("table#java td.contents").text();
	$("pre#display").empty();
	var diff = Diff.diffLines(one, other),
	display = document.getElementById("display"),
	fragment = document.createDocumentFragment();
	
	diff.forEach(function(part) {
		var color = part.added ? 'green' :
		part.removed ? 'red' : 'grey';
		span = document.createElement('span');
		span.style.color = color;
		span.appendChild(document.createTextNode(part.value));
		fragment.appendChild(span);
	});
	display.appendChild(fragment);
	$("div#diff pre#display").height($(window).height() - 30);
	$("div#diff pre#display").width($(window).width() - 30);
	$("div#diff").css("top", $(window).scrollTop());
	$("div#diff").show();
	$("div#diff i.close").click(function() {
		$("div#diff").hide();
	});
}
function autoSave(target) {
	var formData = null;
	var url = null;
	if(target.parent().find("td.java_history_id").text() == "") {
		return;
	}
	formData = new FormData();
	formData.append("java_history_id", target.parent().find("td.java_history_id").text());
	url = "deleteAutoSave.xml";
	$.ajax({
		url: url,
		processData: false,
		contentType: false,
		type: 'POST',
		enctype: 'multipart/form-data',
		data: formData,
		success: function(result){
			var obj = parse_xml_document(result);
			if(
				obj && 
				obj.params && 
				obj.params.java_history_id == target.parent().find("td.java_history_id").text()
			) {
				target.parent().next().remove();
				target.parent().remove();
			}
		}
	});
}