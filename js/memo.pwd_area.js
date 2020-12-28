function PwdArea() {
	
}
PwdArea.hide = function(callback, cancleCallback) {
	$("div#pwd_area").hide();
	$(window).off("scroll");
};
PwdArea.showCancle = function() {
	$("div#pwd_area fieldset.pwd input.cancle").show();
};
PwdArea.hideCancle = function() {
	$("div#pwd_area fieldset.pwd input.cancle").hide();
};
PwdArea.show = function(callback, cancleCallback) {
	if(PwdArea.saved()) {
		try {
			callback();
			PwdArea.hide();
		} catch(error) {
			console.log(error);
			PwdArea.showInternal(callback, cancleCallback);
		}
	} else {
		PwdArea.showInternal(callback, cancleCallback);
	}
};
PwdArea.showInternal = function(callback, cancleCallback) {
	$("div#pwd_area").css("top", $(window).scrollTop());
	$(window).scroll(function() {
			$("div#pwd_area").css("top", $(window).scrollTop());
	});
	$("div#pwd_area fieldset.pwd input.pwd").each(function() {
		if($("div#pwd_area fieldset.pwd input#showpwd").prop("checked")) {	
			$(this).prop("type", "password");
		} else {
			$(this).prop("type", "text");
		}
	});
	if(cancleCallback) {
		PwdArea.attach(callback, cancleCallback);
	} else {
		PwdArea.attach(callback);
	}
	$("div#pwd_area fieldset.pwd fieldset.pwd div.alert").hide();
	$("div#pwd_area").show();
};
PwdArea.attach = function(callback, cancleCallback) {
	$("div#pwd_area fieldset.pwd input.cancle").off("click");
	$(window).off("keydown");
	if(cancleCallback) {
		PwdArea.showCancle();
		$("div#pwd_area fieldset.pwd input.cancle").click(function() {
			PwdArea.hide();
			if(cancleCallback && typeof(cancleCallback) == "function") {
				cancleCallback();
			}
		});
		$(window).keydown(function(e) {
			if(e.keyCode == 27) {
				PwdArea.hide();
				if(cancleCallback && typeof(cancleCallback) == "function") {
					cancleCallback();
				}
				e.preventDefault();
				$(window).off("keypress");
				$(window).off("keydown");
				return false;
			}
		});
	}
	$("div#pwd_area fieldset.pwd input#showpwd").off("click");
	$("div#pwd_area fieldset.pwd input#showpwd").click(function() {
		$("div#pwd_area fieldset.pwd input.pwd").each(function() {
			if($("div#pwd_area fieldset.pwd input#showpwd").prop("checked")) {	
				$(this).prop("type", "password");
			} else {
				$(this).prop("type", "text");
			}
		});
	});
	$("div#pwd_area fieldset.pwd input.confirm").off("click");
	$("div#pwd_area fieldset.pwd input.confirm").click(function() {
		var pwd = $("div#pwd_area fieldset.pwd input#pwd001").val();
		var iv = $("div#pwd_area fieldset.pwd input#pwd002").val();
		if(pwd != "" && iv != "") {
			try {
				callback();
				PwdArea.save();
				PwdArea.hide();
			} catch (error) {
				PwdArea.alert("!!!패스워드가 정확하지 않습니다!!!")
			}
		} else {
			PwdArea.alert("!!!패스워드를 입력해 주세요!!!");
		}
	});
	$(window).off("keypress");
	$(window).keypress(function(e) {
		if(e.which == 13) {
			var pwd = $("div#pwd_area fieldset.pwd input#pwd001").val();
			var iv = $("div#pwd_area fieldset.pwd input#pwd002").val();
			if(pwd != "" && iv != "") {
				try {
					callback();
					PwdArea.save();
					PwdArea.hide();
					$(window).off("keypress");
					$(window).off("keydown");
				} catch (error) {
					PwdArea.alert("!!!패스워드가 정확하지 않습니다!!!")
				}
			} else {
				PwdArea.alert("!!!패스워드를 입력해 주세요!!!");
			}
			e.preventDefault();
			return false;
		}
    });
};
PwdArea.alert = function(msg) {
	$("div#pwd_area fieldset.pwd div.alert").empty();
	$("div#pwd_area fieldset.pwd div.alert").append(msg);
	$("div#pwd_area fieldset.pwd div.alert").show();
};
PwdArea.encrypt = function(plain) {
	return PwdArea.encryptInternal(plain, PwdArea.getKey());
};
PwdArea.encryptInternal = function(plain, key) {
	var prp = new sjcl.cipher.aes(key.pwdHash);
	var encrypted = sjcl.codec.bytes.fromBits(sjcl.mode.gcm.encrypt(prp, sjcl.codec.utf8String.toBits(plain), sjcl.codec.bytes.toBits(key.iv)));
	return base64js.fromByteArray(encrypted);
};
PwdArea.decrypt = function(encrypted) {
	return PwdArea.decryptInternal(encrypted, PwdArea.getKey());
};
PwdArea.decryptInternal = function(encrypted, key) {
	var decoded = null;
	try {
		decoded = base64js.toByteArray(encrypted);
	} catch (error) {
		return encrypted;
	}
	if(!decoded || decoded.length == 0) {
		return encrypted;
	}
	var prp = new sjcl.cipher.aes(key.pwdHash);
	var decrypted = sjcl.mode.gcm.decrypt(prp, sjcl.codec.bytes.toBits(decoded), sjcl.codec.bytes.toBits(key.iv));
	return sjcl.codec.utf8String.fromBits(decrypted);
};
PwdArea.getKey = function() {
	var pwdHash = null;
	var ivHash = null;
	if(PwdArea.saved()) {
		pwdHash = sjcl.codec.bytes.toBits(base64js.toByteArray(PwdArea.getSavedPwdHash()));
		ivHash = sjcl.codec.bytes.toBits(base64js.toByteArray(PwdArea.getSavedIvHash()));
	} else {
		var pwd = $("div#pwd_area fieldset.pwd input#pwd001").val();
		var ivText = $("div#pwd_area fieldset.pwd input#pwd002").val();
		pwdHash = sjcl.hash.sha256.hash(pwd);
		ivHash = sjcl.hash.sha256.hash(ivText);
	}
	var iv = sjcl.codec.bytes.fromBits(ivHash).slice(0, 12);
	var key = {
		pwdHash : pwdHash,
		ivHash : ivHash,
		iv : iv
	};
	return key;
};
PwdArea.save = function() {
	if(window.sessionStorage) {
		var key = PwdArea.getKey();
		window.sessionStorage.setItem("pwdHash", base64js.fromByteArray(sjcl.codec.bytes.fromBits(key.pwdHash)));
		window.sessionStorage.setItem("ivHash", base64js.fromByteArray(sjcl.codec.bytes.fromBits(key.ivHash)));
	}
};
PwdArea.getSavedPwdHash = function() {
	if(window.sessionStorage) {
		return window.sessionStorage.getItem("pwdHash");
	}
	return "";
};
PwdArea.getSavedIvHash = function() {
	if(window.sessionStorage) {
		return window.sessionStorage.getItem("ivHash");
	}
	return "";
};
PwdArea.saved = function() {
	if(
		PwdArea.supportSaved() &&
		PwdArea.getSavedPwdHash() && 
		PwdArea.getSavedIvHash() &&
		PwdArea.getSavedPwdHash() != "" && 
		PwdArea.getSavedIvHash() != ""
	) {
		return true;
	} else {
		return false;
	}
};
PwdArea.supportSaved = function() {
	if(
		window.sessionStorage
	) {
		return true;
	} else {
		return false;
	}
};
