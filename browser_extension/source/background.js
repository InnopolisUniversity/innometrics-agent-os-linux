Date.prototype.toIsoString = function() {
	// from https://stackoverflow.com/questions/17415579/how-to-iso-8601-format-a-date-with-timezone-offset-in-javascript
    var tzo = -this.getTimezoneOffset(),
        dif = tzo >= 0 ? '+' : '-',
        pad = function(num) {
            var norm = Math.floor(Math.abs(num));
            return (norm < 10 ? '0' : '') + norm;
        };
    return this.getFullYear() +
        '-' + pad(this.getMonth() + 1) +
        '-' + pad(this.getDate()) +
        'T' + pad(this.getHours()) +
        ':' + pad(this.getMinutes()) +
        ':' + pad(this.getSeconds()) +
        dif + pad(tzo / 60) +
        ':' + pad(tzo % 60);
}

const cfg = {
	request: "http://127.0.0.1:1487/",
	minTime: 500,
	pollInterval: 30000,
};

let activeTabs = [];

function sendData(data) {
	var req = new XMLHttpRequest();
	req.open("POST", cfg.request, true);
	req.addEventListener("load", function () {
		console.log('resp');
		console.log(req.response);
	});
	req.send(data);
}

async function updateUrl(url, title = '') {
	const time = new Date().toIsoString();

	const data = [];
	for (const tab of activeTabs) {
		// if (time - tab.time >= cfg.minTime) {
		data.push({browser_url: tab.url, browser_title: title, start_time: tab.time, end_time: time});
		// }
	}

	activeTabs = [{url, time, title}];
	console.log(data);
	sendData(JSON.stringify(data));
}

browser.tabs.onUpdated.addListener(async (_, changeinfo, tab) => {
	if (changeinfo.url) await updateUrl(changeinfo.url, tab.title);
});

browser.tabs.onActivated.addListener(async ({tabId}) => {
	const info = await browser.tabs.get(tabId);
	updateUrl(info.url, info.title);
});

function partialSubmit() {
	for (const tab of activeTabs) {
		updateUrl(tab.url, tab.title);
	}
}

// without polling browsers will suspend the extension resulting in data loss
setInterval(partialSubmit, cfg.pollInterval);
