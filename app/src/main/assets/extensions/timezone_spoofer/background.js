// background.js
let cachedNetworkTz = null;
let cachedNetworkOffset = 0;
let lastNetworkFetch = 0;
let cachedTz = null;
let cachedOffset = 0;

// Fallback options for privacy (America or other common places)
const FALLBACK_ZONES = [
    { tz: "America/New_York", offset: -300 },
    { tz: "America/Los_Angeles", offset: -480 },
    { tz: "America/Chicago", offset: -360 },
    { tz: "Europe/London", offset: 0 },
    { tz: "Europe/Berlin", offset: 60 },
    { tz: "Asia/Singapore", offset: 480 }
];

const getFallback = (excludeTz) => {
    // Pick a stable fallback for this session if not already cached
    if (cachedTz && cachedTz !== excludeTz) return { timezone: cachedTz, offset: cachedOffset };
    
    const pool = FALLBACK_ZONES.filter(z => z.tz !== excludeTz);
    const choice = pool.length > 0 
        ? pool[Math.floor(Math.random() * pool.length)]
        : FALLBACK_ZONES[0]; // Fallback to first if all excluded (unlikely)
        
    cachedTz = choice.tz;
    cachedOffset = choice.offset;
    return { timezone: choice.tz, offset: choice.offset };
};

const fetchTimezone = async (luminaId) => {
    try {
        console.log("Lumina BG: Requesting timezone from native app for ID:", luminaId);
        const response = await browser.runtime.sendNativeMessage("lumina", { 
            type: "getTimezone",
            luminaId: luminaId
        });
        console.log("Lumina BG: Native app response:", JSON.stringify(response));
        
        if (response && response.timezone === "disabled") {
            console.log("Lumina BG: Timezone spoofing is disabled for this session.");
            return {
                timezone: null
            }; 
        }

        // If app explicitly requested fallback (or Tor is off and user wants to hide local)
        if (response && response.timezone === "fallback") {
            const fallback = getFallback(response.systemTimezone);
            console.log("Lumina BG: Explicit fallback requested. Selected:", JSON.stringify(fallback));
            return {
                timezone: fallback.timezone,
                offset: fallback.offset
            };
        }

        // If Tor is ON and provided a location, use it.
        if (response && response.timezone && response.timezone !== "system") {
            console.log("Lumina BG: Using Tor/Native provided TZ:", response.timezone);
            return {
                timezone: response.timezone,
                offset: response.offset || 0
            };
        }

        // If Tor is OFF (system), detect if a VPN is active by checking the network IP location.
        const url = "https://ipwho.is/";
        const now = Date.now();
        
        // Use cache if network check was performed recently
        if (cachedNetworkTz && (now - lastNetworkFetch < 300000)) {
            console.log("Lumina BG: Using cached network TZ:", cachedNetworkTz);
            return {
                timezone: cachedNetworkTz,
                offset: cachedNetworkOffset
            };
        }

        console.log("Lumina BG: Tor off/System mode. Checking network location via " + url);
        try {
            const resp = await fetch(url);
            const data = await resp.json();
            console.log("Lumina BG: ipwho.is data:", JSON.stringify(data));
            
            if (data.success && data.timezone) {
                const networkTz = data.timezone.id;
                const systemTz = response.systemTimezone; 

                console.log(`Lumina BG: Compare -> Network: ${networkTz}, System: ${systemTz}`);

                if (networkTz === systemTz) {
                    console.log("Lumina BG: Network matches System (Real location exposed). Spoofing...");
                    const fallback = getFallback(networkTz);
                    cachedNetworkTz = fallback.timezone;
                    cachedNetworkOffset = fallback.offset;
                    lastNetworkFetch = now;
                    return {
                        timezone: fallback.timezone,
                        offset: fallback.offset
                    };
                } else {
                    console.log("Lumina BG: Network differs from System (VPN active). Using Network TZ.");
                    cachedNetworkTz = networkTz;
                    cachedNetworkOffset = Math.floor(data.timezone.offset / 60);
                    lastNetworkFetch = now;
                    return {
                        timezone: cachedNetworkTz,
                        offset: cachedNetworkOffset
                    };
                }
            } else {
                console.error("Lumina BG: ipwho.is failed or returned success=false");
            }
        } catch (e) {
            console.error("Lumina BG: Network fetch failed", e);
        }

    } catch (e) {
        console.warn("Lumina BG: Native messaging failed", e);
    }

    // Default hard fallback for privacy
    console.log("Lumina BG: Final fallback.");
    const fallback = getFallback();
    return {
        timezone: fallback.timezone,
        offset: fallback.offset
    };
};

browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === "getTimezone") {
        const cookieStoreId = sender.tab ? sender.tab.cookieStoreId : "";
        const match = cookieStoreId.match(/lumina_session_(\d+)/);
        const luminaId = match ? parseInt(match[1]) : null;
        
        fetchTimezone(luminaId).then(sendResponse);
        return true; // async
    }
});
