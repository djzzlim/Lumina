// background.js

const fetchHardwareConfig = async (luminaId) => {
    try {
        console.log("Hardware Spoofer BG: Requesting config for ID:", luminaId);
        const response = await browser.runtime.sendNativeMessage("lumina", { 
            type: "getTimezone", // Reuse native message handler
            luminaId: luminaId
        });
        console.log("Hardware Spoofer BG: Response:", JSON.stringify(response));
        return {
            randomizeScreen: response ? response.randomizeScreen : false,
            spoofHardware: response ? response.spoofHardware : false,
            screenWidth: response ? response.screenWidth : 1920,
            screenHeight: response ? response.screenHeight : 1080,
            devicePixelRatio: response ? response.devicePixelRatio : 1.0
        };
    } catch (e) {
        console.warn("Hardware Spoofer BG: Native messaging failed", e);
    }
    return {
        randomizeScreen: false,
        spoofHardware: false
    };
};

// Strip the _NoSR and _NoSH suffixes from outgoing User-Agent headers
browser.webRequest.onBeforeSendHeaders.addListener(
    (details) => {
        for (let header of details.requestHeaders) {
            if (header.name.toLowerCase() === "user-agent") {
                header.value = header.value.replace(" _NoSR", "").replace(" _NoSH", "").replace(" _NoSL", "").replace(/ _Loc_[a-zA-Z\-]+/, "");
                break;
            }
        }
        return { requestHeaders: details.requestHeaders };
    },
    { urls: ["<all_urls>"] },
    ["blocking", "requestHeaders"]
);

browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === "getHardwareConfig") {
        const cookieStoreId = sender.tab ? sender.tab.cookieStoreId : "";
        const match = cookieStoreId.match(/lumina_session_(\d+)/);
        const luminaId = match ? parseInt(match[1]) : null;
        
        fetchHardwareConfig(luminaId).then(sendResponse);
        return true; // async
    }
});
