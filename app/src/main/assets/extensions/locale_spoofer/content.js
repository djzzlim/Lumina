(() => {
    // 1. Sniff the User-Agent synchronously to determine the profile guess
    const ua = navigator.userAgent || "";
    const isNoSL = ua.includes("_NoSL");
    
    // Extract the locale code if present
    const match = ua.match(/ _Loc_([a-zA-Z\-]+)/);
    const localeCode = match ? match[1] : null;

    const LOCALES = {
        "en-US": { lang: "en-US", langs: ["en-US", "en"] },
        "en-GB": { lang: "en-GB", langs: ["en-GB", "en"] },
        "de-DE": { lang: "de-DE", langs: ["de-DE", "de", "en-US", "en"] },
        "fr-FR": { lang: "fr-FR", langs: ["fr-FR", "fr", "en-US", "en"] },
        "es-ES": { lang: "es-ES", langs: ["es-ES", "es", "en-US", "en"] },
        "it-IT": { lang: "it-IT", langs: ["it-IT", "it", "en-US", "en"] },
        "pt-BR": { lang: "pt-BR", langs: ["pt-BR", "pt", "en-US", "en"] },
        "nl-NL": { lang: "nl-NL", langs: ["nl-NL", "nl", "en-US", "en"] },
        "pl-PL": { lang: "pl-PL", langs: ["pl-PL", "pl", "en-US", "en"] },
        "ru-RU": { lang: "ru-RU", langs: ["ru-RU", "ru", "en-US", "en"] },
        "ja-JP": { lang: "ja-JP", langs: ["ja-JP", "ja", "en-US", "en"] },
        "zh-CN": { lang: "zh-CN", langs: ["zh-CN", "zh", "en-US", "en"] },
        "ko-KR": { lang: "ko-KR", langs: ["ko-KR", "ko", "en-US", "en"] }
    };

    const selected = (localeCode && LOCALES[localeCode]) ? LOCALES[localeCode] : null;

    const inject = () => {
        if (window.LUMINA_LOCALE_INIT) return;
        window.LUMINA_LOCALE_INIT = true;

        const script = document.createElement('script');
        script.textContent = `
            (() => {
                const isNoSL = ${isNoSL};
                const hasSelected = ${selected !== null};
                const currentLocale = "${selected ? selected.lang : 'en-US'}";
                const currentLanguages = ${JSON.stringify(selected ? selected.langs : ['en-US', 'en'])};

                // Clean up User-Agent if it contains _NoSL or _Loc_ suffixes
                const rawUA = navigator.userAgent || "";
                const cleanUA = rawUA.replace(" _NoSL", "").replace(/ _Loc_[a-zA-Z\\-]+/, "");
                if (rawUA !== cleanUA) {
                    Object.defineProperty(Navigator.prototype, 'userAgent', { get: () => cleanUA, configurable: true });
                }

                const patchedWindows = new WeakSet();

                const patchWindow = (win) => {
                    if (!win || patchedWindows.has(win)) return;
                    patchedWindows.add(win);

                    try {
                        const iframeNavigatorProto = win.Navigator.prototype;

                        // Clean up User-Agent
                        const rawUA = win.navigator.userAgent || "";
                        const cleanUA = rawUA.replace(" _NoSL", "").replace(/ _Loc_[a-zA-Z\\-]+/, "");
                        if (rawUA !== cleanUA) {
                            Object.defineProperty(iframeNavigatorProto, 'userAgent', { get: () => cleanUA, configurable: true });
                        }

                        if (!isNoSL && hasSelected) {
                            Object.defineProperty(iframeNavigatorProto, 'language', {
                                get: () => currentLocale,
                                configurable: true
                            });
                            Object.defineProperty(iframeNavigatorProto, 'languages', {
                                get: () => currentLanguages,
                                configurable: true
                            });

                            // Wrap Intl constructors on iframe context
                            const constructors = ['DateTimeFormat', 'NumberFormat', 'Collator', 'PluralRules', 'RelativeTimeFormat', 'ListFormat'];
                            constructors.forEach(name => {
                                if (win.Intl && win.Intl[name]) {
                                    const OriginalConstructor = win.Intl[name];
                                    win.Intl[name] = function(l, o) {
                                        l = l || currentLocale;
                                        return new OriginalConstructor(l, o);
                                    };
                                    win.Intl[name].prototype = OriginalConstructor.prototype;
                                    if (OriginalConstructor.supportedLocalesOf) {
                                        win.Intl[name].supportedLocalesOf = OriginalConstructor.supportedLocalesOf;
                                    }
                                }
                            });
                        }
                    } catch (e) {}
                };

                if (!isNoSL && hasSelected) {
                    Object.defineProperty(Navigator.prototype, 'language', {
                        get: () => currentLocale,
                        configurable: true
                    });
                    Object.defineProperty(Navigator.prototype, 'languages', {
                        get: () => currentLanguages,
                        configurable: true
                    });

                    // Wrap Intl constructors to force the selected locale if not specified
                    const constructors = ['DateTimeFormat', 'NumberFormat', 'Collator', 'PluralRules', 'RelativeTimeFormat', 'ListFormat'];
                    constructors.forEach(name => {
                        if (typeof Intl !== 'undefined' && Intl[name]) {
                            const OriginalConstructor = Intl[name];
                            Intl[name] = function(l, o) {
                                l = l || currentLocale;
                                return new OriginalConstructor(l, o);
                            };
                            Intl[name].prototype = OriginalConstructor.prototype;
                            if (OriginalConstructor.supportedLocalesOf) {
                                Intl[name].supportedLocalesOf = OriginalConstructor.supportedLocalesOf;
                            }
                        }
                    });
                }

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
                } catch (e) {}

                // Hook HTMLFrameElement
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
                    } catch (e) {}
                }
            })();
        `;
        (document.head || document.documentElement).appendChild(script);
        script.remove();
    };

    inject();
})();
