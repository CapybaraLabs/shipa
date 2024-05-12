package dev.capybaralabs.shipa.discord.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * [Discord Locales](https://discord.com/developers/docs/reference#locales)
 */
enum class DiscordLocale(@JsonValue val value: String, val languageName: String, val nativeName: String) {
	ARABIC("ar", "Arabic", "العربية"), // not documented, but we see it in Sentry
	HEBREW("he", "Hebrew", "עִברִית"), // not documented, but we see it in Sentry
	INDONESIAN("id", "Indonesian", "Bahasa Indonesia"),
	DANISH("da", "Danish", "Dansk"),
	GERMAN("de", "German", "Deutsch"),
	ENGLISH_GB("en-GB", "English, UK", "English, UK"),
	ENGLISH_US("en-US", "English, US", "English, US"),
	SPANISH("es-ES", "Spanish", "Español"),
	LATIN_AMERICAN_SPANISH("es-419", "Spanish, Latin America", "Español, Latinoamérica"), // not documented, but we see it in Sentry
	FRENCH("fr", "French", "Français"),
	CROATIAN("hr", "Croatian", "Hrvatski"),
	ITALIAN("it", "Italian", "Italiano"),
	LITHUANIAN("lt", "Lithuanian", "Lietuviškai"),
	HUNGARIAN("hu", "Hungarian", "Magyar"),
	DUTCH("nl", "Dutch", "Nederlands"),
	NORWEGIAN("no", "Norwegian", "Norsk"),
	POLISH("pl", "Polish", "Polski"),
	PORTUGUESE_BRAZILIAN("pt-BR", "Portuguese, Brazilian", "Português do Brasil"),
	ROMANIAN("ro", "Romanian, Romania", "Română"),
	FINNISH("fi", "Finnish", "Suomi"),
	SWEDISH("sv-SE", "Swedish", "Svenska"),
	VIETNAMESE("vi", "Vietnamese", "Tiếng Việt"),
	TURKISH("tr", "Turkish", "Türkçe"),
	CZECH("cs", "Czech", "Čeština"),
	GREEK("el", "Greek", "Ελληνικά"),
	BULGARIAN("bg", "Bulgarian", "български"),
	RUSSIAN("ru", "Russian", "Pусский"),
	UKRANIAN("uk", "Ukrainian", "Українська"),
	HINDI("hi", "Hindi", "हिन्दी"),
	THAI("th", "Thai", "ไทย"),
	CHINESE_CHINA("zh-CN", "Chinese, China", "中文"),
	JAPANESE("ja", "Japanese", "日本語"),
	CHINESE_TAIWAN("zh-TW", "Chinese, Taiwan", "繁體中文"),
	KOREAN("ko", "Korean", "한국어"),
}
