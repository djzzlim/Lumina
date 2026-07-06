(() => {
    let targetTz = null;
    let targetOffset = 0;
    let targetRandomizeScreen = false;
    let targetScreenWidth = 1920;
    let targetScreenHeight = 1080;
    let targetDevicePixelRatio = 1.0;
    let originalDescriptors = {};
    let originalWindowProperties = {};
    let originalClientWidth = null;
    let originalClientHeight = null;
    
    // 1. Sniff the User-Agent synchronously to determine the profile guess
    const ua = navigator.userAgent || "";
    const isDesktopUA = ua.includes("Windows NT") || ua.includes("Macintosh") || ua.includes("X11");
    const isNoSR = ua.includes("_NoSR");

    // Guess defaults synchronously based on UA & platform cues
    targetRandomizeScreen = !isNoSR; // Disable if _NoSR is present
    if (isDesktopUA) {
        targetScreenWidth = 1920;
        targetScreenHeight = 1080;
        targetDevicePixelRatio = 1.0;
    } else {
        targetScreenWidth = 360;
        targetScreenHeight = 800;
        targetDevicePixelRatio = 3.0;
    }

    const saveOriginals = () => {
        const props = ['width', 'height', 'availWidth', 'availHeight', 'colorDepth', 'pixelDepth'];
        props.forEach(prop => {
            originalDescriptors[prop] = Object.getOwnPropertyDescriptor(Screen.prototype, prop);
        });
        
        const winProps = ['innerWidth', 'innerHeight', 'outerWidth', 'outerHeight', 'devicePixelRatio'];
        winProps.forEach(prop => {
            originalWindowProperties[prop] = window[prop];
        });

        if (typeof Element !== 'undefined') {
            originalClientWidth = Object.getOwnPropertyDescriptor(Element.prototype, 'clientWidth');
            originalClientHeight = Object.getOwnPropertyDescriptor(Element.prototype, 'clientHeight');
        }
    };

    const inject = () => {
        if (window.LUMINA_INIT) return;
        window.LUMINA_INIT = true;

        saveOriginals();

        const script = document.createElement('script');
        script.textContent = `
            (() => {
                // Initialize default guess immediately & synchronously
                const isDesktop = ${isDesktopUA};
                const isNoSR = ${isNoSR};
                let width = isDesktop ? 1920 : 360;
                let height = isDesktop ? 1080 : 800;
                let dpr = isDesktop ? 1.0 : 3.0;
                let orientationType = width > height ? 'landscape-primary' : 'portrait-primary';

                // Keep references to originals
                const originalDescriptors = {};
                const props = ['width', 'height', 'availWidth', 'availHeight', 'colorDepth', 'pixelDepth'];
                props.forEach(prop => {
                    originalDescriptors[prop] = Object.getOwnPropertyDescriptor(Screen.prototype, prop);
                });

                const originalClientWidth = Object.getOwnPropertyDescriptor(Element.prototype, 'clientWidth').get;
                const originalClientHeight = Object.getOwnPropertyDescriptor(Element.prototype, 'clientHeight').get;

                // Function to apply the spoof
                const applySpoof = (w, h, d) => {
                    width = w;
                    height = h;
                    dpr = d;
                    orientationType = width > height ? 'landscape-primary' : 'portrait-primary';
                };

                // Clean up User-Agent if it contains _NoSR suffix
                const rawUA = navigator.userAgent || "";
                const cleanUA = rawUA.replace(" _NoSR", "");
                if (rawUA !== cleanUA) {
                    Object.defineProperty(Navigator.prototype, 'userAgent', { get: () => cleanUA, configurable: true });
                }

                // Define getters on prototype only if randomizeScreen is active
                if (!isNoSR) {
                    props.forEach(prop => {
                        Object.defineProperty(Screen.prototype, prop, {
                            get: function() {
                                if (prop === 'colorDepth' || prop === 'pixelDepth') return 24;
                                if (prop === 'width' || prop === 'availWidth') return width;
                                return height;
                            },
                            configurable: true
                        });
                    });

                    Object.defineProperty(window, 'innerWidth', { get: () => width, configurable: true });
                    Object.defineProperty(window, 'innerHeight', { get: () => height, configurable: true });
                    Object.defineProperty(window, 'outerWidth', { get: () => width, configurable: true });
                    Object.defineProperty(window, 'outerHeight', { get: () => height, configurable: true });
                    Object.defineProperty(window, 'devicePixelRatio', { get: () => dpr, configurable: true });

                    if (typeof ScreenOrientation !== 'undefined') {
                        Object.defineProperty(ScreenOrientation.prototype, 'type', { get: () => orientationType, configurable: true });
                        Object.defineProperty(ScreenOrientation.prototype, 'angle', { get: () => 0, configurable: true });
                    }
                    Object.defineProperty(Screen.prototype, 'mozOrientation', { get: () => orientationType, configurable: true });

                    Object.defineProperty(Element.prototype, 'clientWidth', {
                        get: function() {
                            if (this === document.documentElement || this === document.body) {
                                return width;
                            }
                            return originalClientWidth.call(this);
                        },
                        configurable: true
                    });

                    Object.defineProperty(Element.prototype, 'clientHeight', {
                        get: function() {
                            if (this === document.documentElement || this === document.body) {
                                return height;
                            }
                            return originalClientHeight.call(this);
                        },
                        configurable: true
                    });
                }

                // Listen for updates from content script
                window.addEventListener('__LuminaUpdateConfig__', (e) => {
                    const cfg = e.detail;
                    if (cfg.restore) {
                        // Restore originals
                        props.forEach(prop => {
                            Object.defineProperty(Screen.prototype, prop, originalDescriptors[prop]);
                        });
                        Object.defineProperty(Element.prototype, 'clientWidth', { get: originalClientWidth, configurable: true });
                        Object.defineProperty(Element.prototype, 'clientHeight', { get: originalClientHeight, configurable: true });
                        delete window.innerWidth;
                        delete window.innerHeight;
                        delete window.outerWidth;
                        delete window.outerHeight;
                        delete window.devicePixelRatio;
                    } else {
                        applySpoof(cfg.width, cfg.height, cfg.dpr);
                    }
                });

                // Apply initial timezone if available (timezone requires Date/Intl overrides)
                window.addEventListener('__LuminaTimezone__', (e) => {
                    const tz = e.detail.timezone || 'UTC';
                    const offset = e.detail.offset || 0;
                    
                    const OriginalDate = window.Date;
                    const OriginalIntl = window.Intl;
                    const originalDateTimeFormat = OriginalIntl.DateTimeFormat;

                    OriginalDate.prototype.getTimezoneOffset = function() { 
                        return -offset; 
                    };

                    window.Intl.DateTimeFormat = function(l, o) {
                        o = o || {}; 
                        if (!o.timeZone) o.timeZone = tz;
                        return new originalDateTimeFormat(l, o);
                    };
                    window.Intl.DateTimeFormat.prototype = originalDateTimeFormat.prototype;
                    window.Intl.DateTimeFormat.supportedLocalesOf = originalDateTimeFormat.supportedLocalesOf;
                    
                    console.log("Lumina: Dynamic timezone applied -> " + tz);
                });
            })();
        `;
        (document.head || document.documentElement).appendChild(script);
        script.remove();
    };

    // Inject guess synchronously at document_start immediately
    inject();

    // Now query the background script asynchronously for exact settings
    const sync = () => {
        browser.runtime.sendMessage({ type: "getTimezone" }).then(res => {
            if (res) {
                targetTz = res.timezone;
                targetOffset = res.offset || 0;
                targetRandomizeScreen = res.randomizeScreen || false;
                targetScreenWidth = res.screenWidth || (isDesktopUA ? 1920 : 360);
                targetScreenHeight = res.screenHeight || (isDesktopUA ? 1080 : 800);
                targetDevicePixelRatio = res.devicePixelRatio || (isDesktopUA ? 1.0 : 3.0);

                // If randomizeScreen is false in native settings, restore originals
                if (!targetRandomizeScreen) {
                    window.dispatchEvent(new CustomEvent('__LuminaUpdateConfig__', { detail: { restore: true } }));
                } else {
                    // Update the page context script with final dimensions
                    window.dispatchEvent(new CustomEvent('__LuminaUpdateConfig__', {
                        detail: {
                            width: targetScreenWidth,
                            height: targetScreenHeight,
                            dpr: targetDevicePixelRatio
                        }
                    }));
                }

                // If timezone spoofing is active, apply it
                if (targetTz) {
                    window.dispatchEvent(new CustomEvent('__LuminaTimezone__', {
                        detail: {
                            timezone: targetTz,
                            offset: targetOffset
                        }
                    }));
                }
            }
        }).catch(() => {});
    };

    sync();
})();
