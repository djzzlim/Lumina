// background.js

const LOCALES = {
    "en-US": { lang: "en-US", langs: ["en-US", "en"], header: "en-US,en;q=0.9" },
    "en-GB": { lang: "en-GB", langs: ["en-GB", "en"], header: "en-GB,en;q=0.9" },
    "de-DE": { lang: "de-DE", langs: ["de-DE", "de", "en-US", "en"], header: "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7" },
    "fr-FR": { lang: "fr-FR", langs: ["fr-FR", "fr", "en-US", "en"], header: "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7" },
    "es-ES": { lang: "es-ES", langs: ["es-ES", "es", "en-US", "en"], header: "es-ES,es;q=0.9,en-US;q=0.8,en;q=0.7" },
    "it-IT": { lang: "it-IT", langs: ["it-IT", "it", "en-US", "en"], header: "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7" },
    "pt-BR": { lang: "pt-BR", langs: ["pt-BR", "pt", "en-US", "en"], header: "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7" },
    "nl-NL": { lang: "nl-NL", langs: ["nl-NL", "nl", "en-US", "en"], header: "nl-NL,nl;q=0.9,en-US;q=0.8,en;q=0.7" },
    "pl-PL": { lang: "pl-PL", langs: ["pl-PL", "pl", "en-US", "en"], header: "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7" },
    "ru-RU": { lang: "ru-RU", langs: ["ru-RU", "ru", "en-US", "en"], header: "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7" },
    "ja-JP": { lang: "ja-JP", langs: ["ja-JP", "ja", "en-US", "en"], header: "ja-JP,ja;q=0.9,en-US;q=0.8,en;q=0.7" },
    "zh-CN": { lang: "zh-CN", langs: ["zh-CN", "zh", "en-US", "en"], header: "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7" },
    "ko-KR": { lang: "ko-KR", langs: ["ko-KR", "ko", "en-US", "en"], header: "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7" }
};

// Intercept and replace Accept-Language header based on User-Agent suffix
browser.webRequest.onBeforeSendHeaders.addListener(
    (details) => {
        let localeCode = null;
        for (let header of details.requestHeaders) {
            const name = header.name.toLowerCase();
            if (name === "user-agent") {
                const match = header.value.match(/ _Loc_([a-zA-Z\-]+)/);
                if (match) {
                    localeCode = match[1];
                    header.value = header.value.replace(/ _Loc_[a-zA-Z\-]+/, "");
                }
                header.value = header.value.replace(" _NoSL", "");
                break;
            }
        }
        
        if (localeCode && LOCALES[localeCode]) {
            const selected = LOCALES[localeCode];
            for (let header of details.requestHeaders) {
                if (header.name.toLowerCase() === "accept-language") {
                    header.value = selected.header;
                    break;
                }
            }
        }
        return { requestHeaders: details.requestHeaders };
    },
    { urls: ["<all_urls>"] },
    ["blocking", "requestHeaders"]
);
