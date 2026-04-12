package com.cryptoalert.account.infrastructure.persistence

import com.cryptoalert.account.domain.User
import com.cryptoalert.account.domain.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Реализует UserRepository через DatabaseClient + JSONB-паттерн (ADR-009).
 *
 * READ:  SELECT jsonb_build_object(...) → парсинг через ObjectMapper
 * WRITE: нативный INSERT с bind-параметрами
 *
 * Запрещено: ORM, Spring Data R2DBC репозитории, сборка объектов из flat rows.
 */
@Repository
class UserRepositoryImpl(
    private val databaseClient: DatabaseClient,
    private val objectMapper: ObjectMapper,
) : UserRepository {

    override suspend fun save(user: User): User {
        databaseClient.sql(
            """
            INSERT INTO users (id, email, password_hash, created_at)
            VALUES (:id, :email, :passwordHash, :createdAt)
            """.trimIndent(),
        )
            .bind("id", user.id)
            .bind("email", user.email)
            .bind("passwordHash", user.passwordHash)
            .bind("createdAt", user.createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return user
    }

    override suspend fun findByEmail(email: String): User? =
        databaseClient.sql(
            """
            SELECT jsonb_build_object(
                'id',           u.id,
                'email',        u.email,
                'passwordHash', u.password_hash,
                'createdAt',    u.created_at
            ) AS data
            FROM users u
            WHERE u.email = :email
            """.trimIndent(),
        )
            .bind("email", email)
            .fetch()
            .one()
            .map { row -> objectMapper.readValue((row["data"] as Json).asString(), User::class.java) }
            .awaitSingleOrNull()

    override suspend fun findById(id: UUID): User? =
        databaseClient.sql(
            """
            SELECT jsonb_build_object(
                'id',           u.id,
                'email',        u.email,
                'passwordHash', u.password_hash,
                'createdAt',    u.created_at
            ) AS data
            FROM users u
            WHERE u.id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .fetch()
            .one()
            .map { row -> objectMapper.readValue((row["data"] as Json).asString(), User::class.java) }
            .awaitSingleOrNull()

    override suspend fun existsByEmail(email: String): Boolean =
        databaseClient.sql(
            "SELECT COUNT(1) AS user_count FROM users WHERE email = :email",
        )
            .bind("email", email)
            .fetch()
            .one()
            .map { row -> (row["user_count"] as Long) > 0 }
            .awaitSingleOrNull() ?: false
}
