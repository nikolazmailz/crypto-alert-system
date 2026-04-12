package com.cryptoalert.account.domain

/**
 * Порт (Domain интерфейс) для хеширования паролей.
 * Реализация живёт в infrastructure (BCrypt).
 * Домен не знает о конкретном алгоритме.
 */
interface PasswordHasher {
    fun hash(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
