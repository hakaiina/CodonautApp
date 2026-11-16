package ru.codonaut.app.domain.model

// модель для привязки аккаунта при регистрации (чтобы серверный корректно мог связаться с гостевым)

data class PendingAccountLink(
    val profileId: String,
    val authUid: String,
    val nickname: String
)