function MessageArea() {
}
MessageArea.hide = function(callback, cancleCallback) {
	$("div#message_area").hide();
	$(window).off("scroll");
};
MessageArea.alert = function(out, callback) {
	$("div#message_area").css("top", $(window).scrollTop());
	$(window).scroll(function() {
			$("div#message_area").css("top", $(window).scrollTop());
	});
	$("div#message_area div.confirm").hide();
	$("div#message_area ul.alert").empty();
	for(var i = 0; i < out.length; i++) {
		$("div#message_area ul.alert").append("<li>" + out[i].msg + "</li>")
	}
	$("div#message_area input.cancle").hide();
	$("div#message_area input.confirm").off("click");
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
			$(window).off("keypress");
			return false;
		}
    });
	$("div#message_area ul.alert").show();
	$("div#message_area").show();
};
MessageArea.confirm = function(msg, callback, cancleCallback) {
	$("div#message_area").css("top", $(window).scrollTop());
	$(window).scroll(function() {
			$("div#message_area").css("top", $(window).scrollTop());
	});
	$("div#message_area ul.alert").hide();
	
	$("div#message_area div.confirm").empty();
	$("div#message_area div.confirm").append(msg)
	
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
			$(window).off("keypress");
			$(window).off("keydown");
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
			$(window).off("keypress");
			$(window).off("keydown");
			return false;
		}
    });
	$("div#message_area input.cancle").show();
	
	$("div#message_area div.confirm").show();
	$("div#message_area").show();
};