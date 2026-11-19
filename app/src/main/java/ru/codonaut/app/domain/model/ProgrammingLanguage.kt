package ru.codonaut.app.domain.model

/** Stable identifiers are persisted; display names may change independently. */
enum class ProgrammingLanguage(
    val id: String,
    val displayName: String
) {
    PYTHON("python", "Python"),
    JAVASCRIPT("javascript", "JavaScript"),
    JAVA("java", "Java");

    companion object {
        fun fromId(id: String): ProgrammingLanguage? =
            entries.firstOrNull { it.id == id }
    }
}
