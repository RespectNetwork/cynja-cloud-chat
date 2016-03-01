<html>

<head>

<title>Cynja Cloud Chat</title>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

<link rel="stylesheet" href="css/style.css" TYPE="text/css" MEDIA="screen" />

<script type="text/javascript" src="js/jquery-2.0.3.min.js"></script>
<script type="text/javascript" src="js/lodash.compat.min.js"></script>

<script type="text/javascript">

function request() {

	var cloud1 = $("#requestCloud1").val().trim(); if (! cloud1) { alert("Please enter \"Cloud 1\""); return; }
	var cloud1SecretToken = $("#requestCloud1SecretToken").val().trim(); if (! cloud1SecretToken) { alert("Please enter \"Cloud 1 Secret Token\""); return; }
	var cloud2 = $("#requestCloud2").val().trim(); if (! cloud2) { alert("Please enter \"Cloud 2\""); return; }

	$.ajax({
	    url: '/v2/request',
	    type: 'POST',
	    data: 'cloud1=' + encodeURIComponent(cloud1) + '&' + 'cloud1SecretToken=' + encodeURIComponent(cloud1SecretToken) + '&' + 'cloud2=' + encodeURIComponent(cloud2),
	    success: function(data) { alert('success: ' + JSON.stringify(data)); },
	    error: function(msg) { alert('error: ' + JSON.stringify(msg)); }
	});
}

function approve() {

	var cloud = $("#approveCloud").val().trim(); if (! cloud) { alert("Please enter \"Cloud\""); return; }
	var cloudSecretToken = $("#approveCloudSecretToken").val().trim(); if (! cloudSecretToken) { alert("Please enter \"Cloud Secret Token\""); return; }
	var cloud1 = $("#approveCloud1").val().trim(); if (! cloud1) { alert("Please enter \"Cloud 1\""); return; }
	var cloud2 = $("#approveCloud2").val().trim(); if (! cloud2) { alert("Please enter \"Cloud 2\""); return; }

	$.ajax({
	    url: '/v2/approve',
	    type: 'POST',
	    data: 'cloud=' + encodeURIComponent(cloud) + '&' + 'cloudSecretToken=' + encodeURIComponent(cloudSecretToken) + '&' + 'cloud1=' + encodeURIComponent(cloud1) + '&' + 'cloud2=' + encodeURIComponent(cloud2),
	    success: function(data) { alert('success: ' + JSON.stringify(data)); },
	    error: function(msg) { alert('error: ' + JSON.stringify(msg)); }
	});
}

function viewasparent() {

	var parent = $("#viewasparentParent").val().trim(); if (! parent) { alert("Please enter \"Parent\""); return; }
	var parentSecretToken = $("#viewasparentParentSecretToken").val().trim(); if (! parentSecretToken) { alert("Please enter \"Parent Secret Token\""); return; }

	$.ajax({
	    url: '/v2/viewasparent',
	    type: 'POST',
	    data: 'parent=' + encodeURIComponent(parent) + '&' + 'parentSecretToken=' + encodeURIComponent(parentSecretToken),
	    success: function(data) { alert('success: ' + JSON.stringify(data)); },
	    error: function(msg) { alert('error: ' + JSON.stringify(msg)); }
	});
}

function viewascloud() {

	var cloud = $("#viewascloudCloud").val().trim(); if (! cloud) { alert("Please enter \"Cloud\""); return; }
	var cloudSecretToken = $("#viewascloudCloudSecretToken").val().trim(); if (! cloudSecretToken) { alert("Please enter \"Cloud Secret Token\""); return; }

	$.ajax({
	    url: '/v2/viewaschild',
	    type: 'POST',
	    data: 'cloud=' + encodeURIComponent(cloud) + '&' + 'cloudSecretToken=' + encodeURIComponent(cloudSecretToken),
	    success: function(data) { alert('success: ' + JSON.stringify(data)); },
	    error: function(msg) { alert('error: ' + JSON.stringify(msg)); }
	});
}

function logs() {

	var cloud = $("#logsCloud").val().trim(); if (! cloud) { alert("Please enter \"Cloud\""); return; }
	var cloudSecretToken = $("#logsCloudSecretToken").val().trim(); if (! cloudSecretToken) { alert("Please enter \"Cloud Secret Token\""); return; }
	var cloud1 = $("#logsCloud1").val().trim(); if (! cloud1) { alert("Please enter \"Cloud 1\""); return; }
	var cloud2 = $("#logsCloud2").val().trim(); if (! cloud2) { alert("Please enter \"Cloud 2\""); return; }

	$.ajax({
	    url: '/v2/logs',
	    type: 'POST',
	    data: 'cloud=' + encodeURIComponent(cloud) + '&' + 'cloudSecretToken=' + encodeURIComponent(cloudSecretToken) + '&' + 'cloud1=' + encodeURIComponent(cloud1) + '&' + 'cloud2=' + encodeURIComponent(cloud2),
	    success: function(data) { alert('success: ' + JSON.stringify(data)); },
	    error: function(msg) { alert('error: ' + JSON.stringify(msg)); }
	});
}

function block() {

	var cloud = $("#blockCloud").val().trim(); if (! cloud) { alert("Please enter \"Cloud\""); return; }
	var cloudSecretToken = $("#blockCloudSecretToken").val().trim(); if (! cloudSecretToken) { alert("Please enter \"Cloud Secret Token\""); return; }
	var cloud1 = $("#blockCloud1").val().trim(); if (! cloud1) { alert("Please enter \"Cloud 1\""); return; }
	var cloud2 = $("#blockCloud2").val().trim(); if (! cloud2) { alert("Please enter \"Cloud 2\""); return; }

	$.ajax({
	    url: '/v2/block',
	    type: 'POST',
	    data: 'cloud=' + encodeURIComponent(cloud) + '&' + 'cloudSecretToken=' + encodeURIComponent(cloudSecretToken) + '&' + 'cloud1=' + encodeURIComponent(cloud1) + '&' + 'cloud2=' + encodeURIComponent(cloud2),
	    success: function(data) { alert('success: ' + JSON.stringify(data)); },
	    error: function(msg) { alert('error: ' + JSON.stringify(msg)); }
	});
}

function unblock() {

	var cloud = $("#unblockCloud").val().trim(); if (! cloud) { alert("Please enter \"Cloud\""); return; }
	var cloudSecretToken = $("#unblockCloudSecretToken").val().trim(); if (! cloudSecretToken) { alert("Please enter \"Cloud Secret Token\""); return; }
	var cloud1 = $("#unblockCloud1").val().trim(); if (! cloud1) { alert("Please enter \"Cloud 1\""); return; }
	var cloud2 = $("#unblockCloud2").val().trim(); if (! cloud2) { alert("Please enter \"Cloud 2\""); return; }

	$.ajax({
	    url: '/v2/unblock',
	    type: 'POST',
	    data: 'cloud=' + encodeURIComponent(cloud) + '&' + 'cloudSecretToken=' + encodeURIComponent(cloudSecretToken) + '&' + 'cloud1=' + encodeURIComponent(cloud1) + '&' + 'cloud2=' + encodeURIComponent(cloud2),
	    success: function(data) { alert('success: ' + JSON.stringify(data)); },
	    error: function(msg) { alert('error: ' + JSON.stringify(msg)); }
	});
}

function delet() {

	var cloud = $("#deleteCloud").val().trim(); if (! cloud) { alert("Please enter \"Cloud\""); return; }
	var cloudSecretToken = $("#deleteCloudSecretToken").val().trim(); if (! cloudSecretToken) { alert("Please enter \"Cloud Secret Token\""); return; }
	var cloud1 = $("#deleteCloud1").val().trim(); if (! cloud1) { alert("Please enter \"Cloud 1\""); return; }
	var cloud2 = $("#deleteCloud2").val().trim(); if (! cloud2) { alert("Please enter \"Cloud 2\""); return; }

	$.ajax({
	    url: '/v2/delete',
	    type: 'POST',
	    data: 'cloud=' + encodeURIComponent(cloud) + '&' + 'cloudSecretToken=' + encodeURIComponent(cloudSecretToken) + '&' + 'cloud1=' + encodeURIComponent(cloud1) + '&' + 'cloud2=' + encodeURIComponent(cloud2),
	    success: function(data) { alert('success: ' + JSON.stringify(data)); },
	    error: function(msg) { alert('error: ' + JSON.stringify(msg)); }
	});
}

var ws = null;

function chatStart() {

	if (ws) chatStop();

	var cloud1 = $("#chatCloud1").val().trim(); if (! cloud1) { alert("Please enter \"Cloud 1\""); return; }
	var cloud2 = $("#chatCloud2").val().trim(); if (! cloud2) { alert("Please enter \"Cloud 2\""); return; }
	var cloud1SecretToken = "";//$("#chatCloud1SecretToken").val().trim(); if (! cloud1SecretToken) { alert("Please enter \"Cloud 1 Secret Token\""); return; }

	var url = window.location.href.replace("http", "ws") + "v2/chat/" + encodeURIComponent(cloud1) + '/' + encodeURIComponent(cloud2) + '?child1SecretToken=' + cloud1SecretToken;

	ws = new WebSocket(url, ["cynja-chat"]);

	ws.onmessage = function(event) {

		$('#messages').val($('#messages').val() + event.data + "\n");
	};

	ws.onerror = function(event) {

		alert('Chat error: ' + event.data);
	};

	ws.onopen = function(event) {

		alert('Chat opened.');
		$('#messages').val('');
	};

	ws.onclose = function(event) {

		alert('Chat closed: ' + event.code + ' ' + event.reason);
		$('#messages').val('');
	};
}

function chatStop() {

	if (! ws) { alert('No open chat.'); return; }

	ws.close();
	ws = null;
}

function chatMessage() {

	if (! ws) { alert('No open chat.'); return; }

	var chatMessage = $("#chatMessage").val().trim();

	ws.send(chatMessage);
	$("chatMessage").val('');
}

</script>

</head>

<body>

<!-- examples for stub implementation

<pre id="example">
 * CYNJA CLOUD CHAT - example parent/child data - <a href="https://github.com/neustarpc/cynja-cloud-chat">https://github.com/neustarpc/cynja-cloud-chat</a>
 * ================

 * Parents                                                      * Parents
 *   [=]!:uuid:1111 and [=]!:uuid:2222                          *   [=]!:uuid:5555 and [=]!:uuid:6666
 * have the following children                                  * have the following children
 *   [=]!:uuid:3333, [=]!:uuid:4444                             *   [=]!:uuid:7777, [=]!:uuid:8888, [=]!:uuid:9999
 
 * Secret token for all parents and children: abcd
</pre>

-->

<pre id="example">
 * CYNJA CLOUD CHAT - example parent/child data - <a href="https://github.com/neustarpc/cynja-cloud-chat">https://github.com/neustarpc/cynja-cloud-chat</a>
 * ================

 * Parents                                                            * Parents
 *   =cynja1 / [=]!:uuid:24909bdb-8f22-4abe-a244-b042adb32b5d         *   =cynja2 / [=]!:uuid:090fba09-cb57-4822-a1c7-b7987e7d62e5
 * have the following children                                        * have the following children
 *   =cynja1-dep1 / [=]!:uuid:3d80d15d-b22b-4ebd-8f80-dd1fa7fdb858    *   =cynja2-dep1 / [=]!:uuid:960162ce-5fed-481b-878f-f3b4da86a31b
 *   =cynja1-dep2 / [=]!:uuid:1a8c8b52-eeb2-403c-8211-8f2924afff1c    *   =cynja2-dep2 / [=]!:uuid:49a806d8-529a-43ca-96ec-4656e8c7f907
 * Secret token for all parents and children: test@123

 * Parents                                                            * Parents
 *   =cynja3 / [=]!:uuid:cb1f5cc8-53de-4c3c-a942-a4a14c0cb998         *   =cynja4 / [=]!:uuid:fb0d8ee8-fee0-4179-8ba8-059e855ac6dc
 * have the following children                                        * have the following children
 *   =cynja3child1 / [=]!:uuid:3e7a53c8-2b84-4737-bd7b-ee6196efda88   *   =cynja4child1 / [=]!:uuid:371c1bf8-d228-4f34-9750-f848c39120e3
 *   =cynja3child2 / [=]!:uuid:295516cf-e89f-4221-ac0b-668c0fb41d1a   *   =cynja4child2 / [=]!:uuid:4af1a56b-f996-4051-bd93-17cb84b24d2f
 * Secret token for all parents and children: Test@123

</pre>

<div id="chat">
<table>
<tr><td>Cloud 1:</td><td><input type="text" id="chatCloud1"></td></tr>
<tr><td>Cloud 2:</td><td><input type="text" id="chatCloud2"></td></tr>
<!-- <tr><td>Child 1 Secret Token:</td><td><input type="text" id="chatCloud1SecretToken"></td></tr> -->
<tr><td><button onclick="chatStart();">Start Chat</button></td><td><button onclick="chatStop();">Stop Chat</button></td></tr>
</table>
<textarea id="messages"></textarea>
<table>
<tr>
<td>Chat Message:</td>
<td><input type="text" id="chatMessage"></td>
<td><button onclick="chatMessage();">Send Message</button></td>
</tr>
</table>
</div>

<div>
<p class="heading">Request Connection</p>
<table>
<tr><td>Cloud 1</td><td><input type="text" id="requestCloud1"></td></tr>
<tr><td>Cloud 1 Secret Token</td><td><input type="text" id="requestCloud1SecretToken"></td></tr>
<tr><td>Cloud 2</td><td><input type="text" id="requestCloud2"></td></tr>
</table>
<button onclick="request();">Request</button>
</div>

<div>
<p class="heading">Approve Connection</p>
<table>
<tr><td>Cloud</td><td><input type="text" id="approveCloud"></td></tr>
<tr><td>Cloud Secret Token</td><td><input type="text" id="approveCloudSecretToken"></td></tr>
<tr><td>Cloud 1</td><td><input type="text" id="approveCloud1"></td></tr>
<tr><td>Cloud 2</td><td><input type="text" id="approveCloud2"></td></tr>
</table>
<button onclick="approve();">Approve</button>
</div>

<div>
<p class="heading">View Connections As Parent</p>
<table>
<tr><td>Parent</td><td><input type="text" id="viewasparentParent"></td></tr>
<tr><td>Parent Secret Token</td><td><input type="text" id="viewasparentParentSecretToken"></td></tr>
</table>
<button onclick="viewasparent();">View As Parent</button>
</div>

<div>
<p class="heading">View Connections As Cloud</p>
<table>
<tr><td>Cloud</td><td><input type="text" id="viewascloudCloud"></td></tr>
<tr><td>Cloud Secret Token</td><td><input type="text" id="viewascloudCloudSecretToken"></td></tr>
</table>
<button onclick="viewascloud();">View As Cloud</button>
</div>

<div>
<p class="heading">View Connection Log</p>
<table>
<tr><td>Cloud</td><td><input type="text" id="logsCloud"></td></tr>
<tr><td>Cloud Secret Token</td><td><input type="text" id="logsCloudSecretToken"></td></tr>
<tr><td>Cloud 1</td><td><input type="text" id="logsCloud1"></td></tr>
<tr><td>Cloud 2</td><td><input type="text" id="logsCloud2"></td></tr>
</table>
<button onclick="logs();">View Log</button>
</div>

<div>
<p class="heading">Block Connection</p>
<table>
<tr><td>Cloud</td><td><input type="text" id="blockCloud"></td></tr>
<tr><td>Cloud Secret Token</td><td><input type="text" id="blockCloudSecretToken"></td></tr>
<tr><td>Cloud 1</td><td><input type="text" id="blockCloud1"></td></tr>
<tr><td>Cloud 2</td><td><input type="text" id="blockCloud2"></td></tr>
</table>
<button onclick="block();">Block</button>
</div>

<div>
<p class="heading">Unblock Connection</p>
<table>
<tr><td>Cloud</td><td><input type="text" id="unblockCloud"></td></tr>
<tr><td>Cloud Secret Token</td><td><input type="text" id="unblockCloudSecretToken"></td></tr>
<tr><td>Cloud 1</td><td><input type="text" id="unblockCloud1"></td></tr>
<tr><td>Cloud 2</td><td><input type="text" id="unblockCloud2"></td></tr>
</table>
<button onclick="unblock();">Unblock</button>
</div>

<div>
<p class="heading">Delete Connection</p>
<table>
<tr><td>Cloud</td><td><input type="text" id="deleteCloud"></td></tr>
<tr><td>Cloud Secret Token</td><td><input type="text" id="deleteCloudSecretToken"></td></tr>
<tr><td>Cloud 1</td><td><input type="text" id="deleteCloud1"></td></tr>
<tr><td>Cloud 2</td><td><input type="text" id="deleteCloud2"></td></tr>
</table>
<button onclick="delet();">Delete</button>
</div>

</body>

</html>
