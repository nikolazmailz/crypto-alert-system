package com.cryptoalert.account.domain

import java.util.UUID

/**
 * Порт репозитория пользователей.
 * Реализация через DatabaseClient живёт в infrastructure.
 */
interface UserRepository {
    suspend fun save(user: User): User
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun existsByEmail(email: String): Boolean
}
