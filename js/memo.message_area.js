function MessageArea() {
}
MessageArea.hide = function() {
	$(window).off("scroll");
	$(window).off("keypress");
	$(window).off("keydown");
	$("div#message_area div.btn div.btns").empty();
	$("div#message_area div.btn div.btns").hide();
	$("div#message_area input.confirm").off("click");
	$("div#message_area input.cancle").off("click");
	$("div#message_area").hide();
};
MessageArea.alert = function(out, callback) {
	$("div#message_area").css("top", $(window).scrollTop());
	$(window).scroll(function() {
			$("div#message_area").css("top", $(window).scrollTop());
	});
	$("div#message_area div.confirm").hide();
	$("div#message_area div.btn div.btns").hide();
	$("div#message_area ul.alert").empty();
	for(var i = 0; i < out.length; i++) {
		$("div#message_area ul.alert").append("<li>" + out[i].msg + "</li>")
	}
	$("div#message_area input.cancle").hide();
	$("div#message_area input.confirm").off("click");
	$("div#message_area input.cancle").off("click");
	$("div#message_area input.confirm").click(function() {
		MessageArea.hide();
		if(callback) {
			callback(out);
		}
	});
	$(window).off("keypress");
	$(window).keypress(function(e) {
		if(e.which == 13 || e.which == 32) {
			MessageArea.hide();
			if(callback) {
				callback(out);
			}
			e.preventDefault();
//			$(window).off("keypress");
			return false;
		}
    });
	$("div#message_area ul.alert").show();
	$("div#message_area").show();
	$("div#message_area input.confirm").focus();
};
MessageArea.confirm = function(msg, callback, cancleCallback) {
	$("div#message_area").css("top", $(window).scrollTop());
	$(window).scroll(function() {
			$("div#message_area").css("top", $(window).scrollTop());
	});
	$("div#message_area ul.alert").hide();
	$("div#message_area div.btn div.btns").hide();
	
	$("div#message_area div.confirm").empty();
	$("div#message_area div.confirm").append(msg);
	
	$("div#message_area input.confirm").off("click");
	$("div#message_area input.confirm").click(function() {
		MessageArea.hide();
		if(callback) {
			callback();
		}
	});
	$(window).off("keypress");
	$(window).keypress(function(e) {
		if(e.which == 13 || e.which == 32) {
			MessageArea.hide();
			if(callback) {
				callback();
			}
			e.preventDefault();
//			$(window).off("keypress");
//			$(window).off("keydown");
			return false;
		}
    });
	$("div#message_area input.cancle").off("click");
	$("div#message_area input.cancle").click(function() {
		MessageArea.hide();
		if(cancleCallback) {
			cancleCallback();
		}
	});
	$(window).off("keydown");
	$(window).keydown(function(e) {
		if(e.keyCode == 27) {
			MessageArea.hide();
			if(cancleCallback) {
				cancleCallback();
			}
			e.preventDefault();
//			$(window).off("keypress");
//			$(window).off("keydown");
			return false;
		}
    });
	$("div#message_area input.cancle").show();
	$("div#message_area input.confirm").show();
	
	$("div#message_area div.confirm").show();
	$("div#message_area").show();
	$("div#message_area input.confirm").focus();
};

MessageArea.message = function(msg, btns) {
	$("div#message_area").css("top", $(window).scrollTop());
	$(window).scroll(function() {
			$("div#message_area").css("top", $(window).scrollTop());
	});
	$("div#message_area ul.alert").hide();
	
	$("div#message_area div.confirm").empty();
	$("div#message_area div.confirm").append(msg);
	
	$("div#message_area input.confirm").off("click");
	$("div#message_area input.cancle").off("click");

	$("div#message_area input.confirm").hide();
	$("div#message_area input.cancle").hide();
	$("div#message_area div.btn div.btns").empty();
	if(btns) {
		for(var i = 0; i < btns.length; i++) {
			var result = $("div#message_area div.btn div.btns").append("<input type=\"button\" value=\"" + btns[i].label + "\" class=\"" + btns[i].className + "\" />");
			if(btns[i].callback) {
				result.children().last().click(
					{
						callback:btns[i].callback
					},
					function(event) {
						event.data.callback();
					}
				);
			}
		}
	}
	$(window).off("keydown");
	$(window).keydown(function(e) {
		if(btns) {
			for(var i = 0; i < btns.length; i++) {
				if(btns[i].shortcut && btns[i].callback) {
					if(Array.isArray(btns[i].shortcut)) {
						for(var x = 0; x < btns[i].shortcut.length; x++) {
							if(MessageArea.shortcut(e, btns[i].shortcut[x])) {
								MessageArea.hide();
								btns[i].callback();
								e.preventDefault();
								return false;
							}
						}
					} else if(MessageArea.shortcut(e, btns[i].shortcut[x])) {
						MessageArea.hide();
						btns[i].callback();
						e.preventDefault();
						return false;
					}
				}
			}
		}
    });
	$("div#message_area div.btn div.btns").show();
	$("div#message_area").show();
};
MessageArea.shortcut = function(event, item) {
	if(
		typeof(item) == "number" &&
		(
			event.keyCode == item ||
			event.keyCode == String.fromCharCode(item).toLowerCase().charCodeAt(0) ||
			event.keyCode == String.fromCharCode(item).toUpperCase().charCodeAt(0)
		)
	) {
		return true;
	} else if(
		typeof(item) == "string" &&
		item.length == 1 &&
		(
			event.keyCode == item.charCodeAt(0) ||
			event.keyCode == item.toLowerCase().charCodeAt(0) ||
			event.keyCode == item.toUpperCase().charCodeAt(0)
		)
	) {
		return true;
	} else if(
		typeof(item) == "string" &&
		item.toLowerCase() == "space" &&
		event.keyCode == 27
	) {
		return true;
	} else if(
		typeof(item) == "string" &&
		item.toLowerCase() == "enter" &&
		event.which == 13
	) {
		return true;
	} else if(
		typeof(item) == "string" &&
		item.toLowerCase() == "esc" &&
		event.which == 32
	) {
		return true;
	}
	return false;
};
