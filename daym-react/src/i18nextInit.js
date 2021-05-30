import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import Backend from "i18next-xhr-backend";
import LanguageDetector from "i18next-browser-languagedetector";
import translationEN from "./locales/en/translation.json";
import translationSP from "./locales/sp/translation.json";
import translationPR from "./locales/pr/translation.json";

const fallbackLng = ["pr"];
const availableLanguages = ["en", "sp", "pr"];

const resources = {
    en: {
        translation: translationEN
    },
    sp: {
        translation: translationSP
    },
    pr: {
        translation: translationPR
    }
};

i18n
    .use(Backend)
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        resources,
        fallbackLng,

        detection: {
            checkWhitelist: true
        },

        debug: false,

        whitelist: availableLanguages,

        interpolation: {
            escapeValue: false
        }
    });

export default i18n;
