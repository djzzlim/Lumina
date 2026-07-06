(() => {
    let targetTz = null;
    let targetOffset = 0;

    const sync = () => {
        browser.runtime.sendMessage({ type: "getTimezone" }).then(res => {
            if (res && res.timezone) {
                targetTz = res.timezone;
                targetOffset = res.offset || 0;
                inject();
            }
        }).catch(() => {});
    };

    const inject = () => {
        if (window.LUMINA_TZ_INIT) return;
        window.LUMINA_TZ_INIT = true;

        const script = document.createElement('script');
        script.textContent = `
            (() => {
                const tz = "${targetTz || 'UTC'}";
                const offset = ${targetOffset || 0};
                
                const OriginalDate = window.Date;
                const OriginalIntl = window.Intl;
                const originalDateTimeFormat = OriginalIntl.DateTimeFormat;

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

                console.log("Lumina Timezone: Spoofed to " + tz + " (Offset: " + offset + ").");
            })();
        `;
        (document.head || document.documentElement).appendChild(script);
        script.remove();
    };

    sync();
})();
