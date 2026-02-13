(() => {
    let targetTz = null;
    let targetOffset = 0;

    const sync = () => {
        browser.runtime.sendMessage({ type: "getTimezone" }).then(res => {
            if (res && res.timezone) {
                targetTz = res.timezone;
                targetOffset = res.offset;
                inject();
            }
        }).catch(() => {});
    };

    const inject = () => {
        if (window.LUMINA_INIT) return;
        window.LUMINA_INIT = true;

        const script = document.createElement('script');
        script.textContent = `
            (() => {
                const tz = "${targetTz || 'UTC'}";
                const offset = ${targetOffset || 0};
                
                const OriginalDate = window.Date;
                const OriginalIntl = window.Intl;
                const originalDateTimeFormat = OriginalIntl.DateTimeFormat;

                /**
                 * When Anti-fingerprinting (RFP) is enabled in Gecko, Date objects are forced to UTC.
                 * We remove the manual 'time' implementation (overriding getHours, etc.) to respect 
                 * the browser's UTC clock, but keep 'timezone' metadata spoofing as it is useful.
                 */

                // Spoof the timezone offset
                OriginalDate.prototype.getTimezoneOffset = function() { 
                    return -offset; 
                };

                // Spoof the default timezone for Intl.DateTimeFormat
                window.Intl.DateTimeFormat = function(l, o) {
                    o = o || {}; 
                    if (!o.timeZone) o.timeZone = tz;
                    return new originalDateTimeFormat(l, o);
                };
                window.Intl.DateTimeFormat.prototype = originalDateTimeFormat.prototype;
                window.Intl.DateTimeFormat.supportedLocalesOf = originalDateTimeFormat.supportedLocalesOf;

                console.log("Lumina: Timezone spoofed to " + tz + " (Offset: " + offset + "). Time remains UTC per RFP.");
            })();
        `;
        (document.head || document.documentElement).appendChild(script);
        script.remove();
    };

    sync();
})();
