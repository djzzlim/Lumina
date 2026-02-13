// background.js
let cachedTz = null;
let cachedOffset = 0;
let lastFetch = 0;

const fetchTimezone = async () => {
    const now = Date.now();
    if (cachedTz && (now - lastFetch < 300000)) return { timezone: cachedTz, offset: cachedOffset };

    // Try to get timezone from the native app first
    try {
        console.log("Lumina BG: Requesting timezone from native app...");
        const response = await browser.runtime.sendNativeMessage("lumina", { type: "getTimezone" });
        if (response && response.timezone && response.timezone !== "system") {
            cachedTz = response.timezone;
            cachedOffset = response.offset || 0;
            lastFetch = now;
            console.log(`Lumina BG: Native app provided ${cachedTz} (Offset: ${cachedOffset})`);
            return { timezone: cachedTz, offset: cachedOffset };
        }
    } catch (e) {
        console.warn("Lumina BG: Native messaging failed, falling back to network fetch", e);
    }

    const url = "https://ipwho.is/";
    try {
        console.log("Lumina BG: Fetching from " + url);
        const resp = await fetch(url);
        const data = await resp.json();
        
        if (data.success && data.timezone) {
            cachedTz = data.timezone.id;
            cachedOffset = Math.floor(data.timezone.offset / 60);
            lastFetch = now;
            console.log(`Lumina BG: Detected ${cachedTz} (Offset: ${cachedOffset})`);
            return { timezone: cachedTz, offset: cachedOffset };
        }
    } catch (e) {
        console.warn("Lumina BG: Failed " + url, e);
    }

    return { timezone: "UTC", offset: 0 };
};

browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === "getTimezone") {
        fetchTimezone().then(sendResponse);
        return true; // async
    }
});
