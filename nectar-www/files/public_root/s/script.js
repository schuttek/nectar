function $(e, d, n) { // get a value by id
	if (n)
		return null;
	var m = document.getElementById(e);
	if (m == null && d)
		return $(d, null, true);
	return m;
}

function rd(l) { // redirect with history
	setTimeout(function() {
		window.location.href = l;
	}, 0)
}

function rp(e, c) { // replace one element's content with another's.
	$(e).innerHTML = $(c).innerHTML;
}

function show(e) { // sets display = block
	$(e).style.display = 'block';
}

function hide(e) { // sets display = none
	$(e).style.display = 'none';
}

function rl(e, c) { // replace one element content with the given crap
	$(e).innerHTML = c;
}

function getRandInt(n) {
	return Math.floor(Math.random() * 100 % (n + 1));
}

var main = {
	load : function(args) {
		main.loadPage(args.page);
	},
	loadPage : function (page) {
		ajax.replace(page,'content');
	}
}


ajax = {
	init: function() {
		var conn;
		if (window.XMLHttpRequest) 
		  conn=new XMLHttpRequest();
	   else
		  conn=new ActiveXObject("Microsoft.XMLHTTP");
		return conn;
	},

	get: function(url,callback) {
		var conn = ajax.init();
		conn.onreadystatechange = function(conn, callback) {
			if (conn.readyState==4 && conn.status==200) {
				callback();
			}
		}
		conn.open("GET",url,true);
		conn.send();
	},
	replace: function(url, targetElement) {
		var conn = ajax.init();
		conn.onreadystatechange = function() {
			if (conn.readyState==4 && conn.status==200) {
				rl(targetElement, conn.responseText);
			}
		}
		conn.open("GET",url,true);
		conn.send();
	},
}

