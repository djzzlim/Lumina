(() => {
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
    const isNoSH = ua.includes("_NoSH");

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
        if (window.LUMINA_HW_INIT) return;
        window.LUMINA_HW_INIT = true;

        saveOriginals();

        const script = document.createElement('script');
        script.textContent = `
            (() => {
                // Initialize default guess immediately & synchronously
                const isDesktop = ${isDesktopUA};
                const isNoSR = ${isNoSR};
                const isNoSH = ${isNoSH};
                let width = isDesktop ? 1920 : 360;
                let height = isDesktop ? 1080 : 800;
                let dpr = isDesktop ? 1.0 : 3.0;
                let orientationType = width > height ? 'landscape-primary' : 'portrait-primary';

                // Helper to find descriptor on prototype chain
                const getDescriptor = (proto, prop) => {
                    let p = proto;
                    while (p) {
                        const desc = Object.getOwnPropertyDescriptor(p, prop);
                        if (desc) return desc;
                        p = Object.getPrototypeOf(p);
                    }
                    return null;
                };

                // Keep references to originals
                const originalDescriptors = {};
                const props = ['width', 'height', 'availWidth', 'availHeight', 'colorDepth', 'pixelDepth'];
                props.forEach(prop => {
                    const desc = getDescriptor(Screen.prototype, prop);
                    if (desc) {
                        originalDescriptors[prop] = desc;
                    }
                });

                const clientWidthDesc = getDescriptor(Element.prototype, 'clientWidth');
                const clientHeightDesc = getDescriptor(Element.prototype, 'clientHeight');
                const originalClientWidth = clientWidthDesc ? clientWidthDesc.get : null;
                const originalClientHeight = clientHeightDesc ? clientHeightDesc.get : null;

                const navOverrides = {
                    appVersion: "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36",
                    appName: "Netscape",
                    appCodeName: "Mozilla",
                    product: "Gecko",
                    productSub: "20030107",
                    vendor: "Google Inc.",
                    vendorSub: "",
                    buildID: undefined,
                    platform: "Win32",
                    oscpu: undefined,
                    hardwareConcurrency: 8,
                    deviceMemory: 8,
                    language: "en-US",
                    languages: ["en-US"],
                    onLine: true,
                    doNotTrack: null,
                    cookieEnabled: true,
                    maxTouchPoints: 0,
                    webdriver: false,
                    pdfViewerEnabled: true,
                    globalPrivacyControl: true
                };

                const originalNavDescriptors = {};

                // Function to apply the spoof
                const applySpoof = (w, h, d) => {
                    width = w;
                    height = h;
                    dpr = d;
                    orientationType = width > height ? 'landscape-primary' : 'portrait-primary';
                };

                const patchedWindows = new WeakSet();

                // Dynamic patching function for newly created iframes/contexts
                const patchWindow = (win) => {
                    if (!win || patchedWindows.has(win)) return;
                    patchedWindows.add(win);

                    try {
                        const iframeScreenProto = win.Screen.prototype;
                        const iframeScreenOrientationProto = win.ScreenOrientation ? win.ScreenOrientation.prototype : null;
                        const iframeElementProto = win.Element.prototype;
                        const iframeNavigatorProto = win.Navigator.prototype;

                        // Clean up User-Agent
                        const rawUA = win.navigator.userAgent || "";
                        const cleanUA = rawUA.replace(" _NoSR", "").replace(" _NoSH", "");
                        if (rawUA !== cleanUA) {
                            Object.defineProperty(iframeNavigatorProto, 'userAgent', { get: () => cleanUA, configurable: true });
                        }

                        // Apply Navigator overrides for iframe context
                        if (!isNoSH) {
                            Object.keys(navOverrides).forEach(key => {
                                Object.defineProperty(iframeNavigatorProto, key, {
                                    get: () => navOverrides[key],
                                    configurable: true
                                });
                            });

                            if ('DevicePosture' in win || 'devicePosture' in win.navigator) {
                                const postureObj = { type: "continuous" };
                                Object.defineProperty(iframeNavigatorProto, 'devicePosture', {
                                    get: () => postureObj,
                                    configurable: true
                                });
                            }
                        }

                        if (!isNoSR) {
                            // Define Screen properties
                            props.forEach(prop => {
                                Object.defineProperty(iframeScreenProto, prop, {
                                    get: function() {
                                        if (prop === 'colorDepth' || prop === 'pixelDepth') return 24;
                                        if (prop === 'width' || prop === 'availWidth') return width;
                                        return height;
                                    },
                                    configurable: true
                                });
                            });

                            Object.defineProperty(win, 'innerWidth', { get: () => width, configurable: true });
                            Object.defineProperty(win, 'innerHeight', { get: () => height, configurable: true });
                            Object.defineProperty(win, 'outerWidth', { get: () => width, configurable: true });
                            Object.defineProperty(win, 'outerHeight', { get: () => height, configurable: true });
                            Object.defineProperty(win, 'devicePixelRatio', { get: () => dpr, configurable: true });

                            if (iframeScreenOrientationProto) {
                                Object.defineProperty(iframeScreenOrientationProto, 'type', { get: () => orientationType, configurable: true });
                                Object.defineProperty(iframeScreenOrientationProto, 'angle', { get: () => 0, configurable: true });
                            }
                            Object.defineProperty(iframeScreenProto, 'mozOrientation', { get: () => orientationType, configurable: true });
                            Object.defineProperty(iframeScreenProto, 'isExtended', { get: () => false, configurable: true });
                            try {
                                Object.defineProperty(win.screen, 'isExtended', { get: () => false, configurable: true });
                            } catch (e) {}

                            // Viewport clientWidth/clientHeight overrides
                            if (originalClientWidth && originalClientHeight) {
                                Object.defineProperty(iframeElementProto, 'clientWidth', {
                                    get: function() {
                                        const realW = originalClientWidth.call(this);
                                        const realH = originalClientHeight.call(this);
                                        if (this === win.document.documentElement || this === win.document.body) {
                                            return width;
                                        }
                                        const vv = win.visualViewport;
                                        const realVVW = vv ? Math.round(vv.width) : 0;
                                        const realVVH = vv ? Math.round(vv.height) : 0;
                                        if (realW === realVVW && realH === realVVH) {
                                            return width;
                                        }
                                        return realW;
                                    },
                                    configurable: true
                                });

                                Object.defineProperty(iframeElementProto, 'clientHeight', {
                                    get: function() {
                                        const realW = originalClientWidth.call(this);
                                        const realH = originalClientHeight.call(this);
                                        if (this === win.document.documentElement || this === win.document.body) {
                                            return height;
                                        }
                                        const vv = win.visualViewport;
                                        const realVVW = vv ? Math.round(vv.width) : 0;
                                        const realVVH = vv ? Math.round(vv.height) : 0;
                                        if (realW === realVVW && realH === realVVH) {
                                            return height;
                                        }
                                        return realH;
                                    },
                                    configurable: true
                                });
                            }
                        }
                    } catch (e) {
                        console.warn("Lumina: Failed to patch iframe window context", e);
                    }
                };

                // Intercept main page Navigator properties
                if (!isNoSH) {
                    Object.keys(navOverrides).forEach(key => {
                        originalNavDescriptors[key] = Object.getOwnPropertyDescriptor(Navigator.prototype, key);
                        Object.defineProperty(Navigator.prototype, key, {
                            get: () => navOverrides[key],
                            configurable: true
                        });
                    });

                    // devicePosture
                    if ('DevicePosture' in window || 'devicePosture' in navigator) {
                        originalNavDescriptors['devicePosture'] = Object.getOwnPropertyDescriptor(Navigator.prototype, 'devicePosture');
                        const postureObj = { type: "continuous" };
                        Object.defineProperty(Navigator.prototype, 'devicePosture', {
                            get: () => postureObj,
                            configurable: true
                        });
                    }
                }

                // Apply initial patches on main window
                patchWindow(window);

                // Trap iframe contentWindow and contentDocument access
                try {
                    const originalContentWindow = Object.getOwnPropertyDescriptor(HTMLIFrameElement.prototype, 'contentWindow').get;
                    Object.defineProperty(HTMLIFrameElement.prototype, 'contentWindow', {
                        get: function() {
                            const win = originalContentWindow.call(this);
                            if (win) patchWindow(win);
                            return win;
                        },
                        configurable: true
                    });

                    const originalContentDocument = Object.getOwnPropertyDescriptor(HTMLIFrameElement.prototype, 'contentDocument').get;
                    Object.defineProperty(HTMLIFrameElement.prototype, 'contentDocument', {
                        get: function() {
                            const doc = originalContentDocument.call(this);
                            if (doc && doc.defaultView) patchWindow(doc.defaultView);
                            return doc;
                        },
                        configurable: true
                    });
                } catch (e) {
                    console.warn("Lumina: Failed to hook HTMLIFrameElement", e);
                }

                // Hook HTMLFrameElement (legacy frame tags support)
                if (typeof HTMLFrameElement !== 'undefined') {
                    try {
                        const frameWindow = Object.getOwnPropertyDescriptor(HTMLFrameElement.prototype, 'contentWindow').get;
                        Object.defineProperty(HTMLFrameElement.prototype, 'contentWindow', {
                            get: function() {
                                const win = frameWindow.call(this);
                                if (win) patchWindow(win);
                                return win;
                            },
                            configurable: true
                        });
                    } catch (e) {
                        console.warn("Lumina: Failed to hook HTMLFrameElement", e);
                    }
                }

                // Listen for updates from content script
                window.addEventListener('__LuminaHwUpdateConfig__', (e) => {
                    const cfg = e.detail;
                    if (cfg.restore) {
                        // Restore screen properties
                        props.forEach(prop => {
                            if (originalDescriptors[prop]) {
                                Object.defineProperty(Screen.prototype, prop, originalDescriptors[prop]);
                            }
                        });
                        if (originalClientWidth && originalClientHeight) {
                            Object.defineProperty(Element.prototype, 'clientWidth', clientWidthDesc);
                            Object.defineProperty(Element.prototype, 'clientHeight', clientHeightDesc);
                        }
                        delete window.innerWidth;
                        delete window.innerHeight;
                        delete window.outerWidth;
                        delete window.outerHeight;
                        delete window.devicePixelRatio;
                        delete Screen.prototype.isExtended;
                        delete window.screen.isExtended;

                        // Restore original navigator properties if they were overridden
                        if (!isNoSH) {
                            Object.keys(navOverrides).forEach(key => {
                                if (originalNavDescriptors[key]) {
                                    Object.defineProperty(Navigator.prototype, key, originalNavDescriptors[key]);
                                } else {
                                    delete Navigator.prototype[key];
                                }
                            });
                            if (originalNavDescriptors['devicePosture']) {
                                Object.defineProperty(Navigator.prototype, 'devicePosture', originalNavDescriptors['devicePosture']);
                            } else {
                                delete Navigator.prototype.devicePosture;
                            }
                        }
                    } else {
                        applySpoof(cfg.width, cfg.height, cfg.dpr);
                    }
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
        browser.runtime.sendMessage({ type: "getHardwareConfig" }).then(res => {
            if (res) {
                targetRandomizeScreen = res.randomizeScreen || false;
                const targetSpoofHardware = res.spoofHardware || false;
                targetScreenWidth = res.screenWidth || (isDesktopUA ? 1920 : 360);
                targetScreenHeight = res.screenHeight || (isDesktopUA ? 1080 : 800);
                targetDevicePixelRatio = res.devicePixelRatio || (isDesktopUA ? 1.0 : 3.0);

                // If settings require restore
                if (!targetRandomizeScreen && !targetSpoofHardware) {
                    window.dispatchEvent(new CustomEvent('__LuminaHwUpdateConfig__', { detail: { restore: true } }));
                } else {
                    // Update screen settings dynamically if randomizeScreen is active
                    if (targetRandomizeScreen) {
                        window.dispatchEvent(new CustomEvent('__LuminaHwUpdateConfig__', {
                            detail: {
                                width: targetScreenWidth,
                                height: targetScreenHeight,
                                dpr: targetDevicePixelRatio
                            }
                        }));
                    } else {
                        // If only hardware is active, keep hardware spoofing but restore screen
                        window.dispatchEvent(new CustomEvent('__LuminaHwUpdateConfig__', { detail: { restore: true } }));
                    }
                }
            }
        }).catch(() => {});
    };

    sync();
})();
