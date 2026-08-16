package com.aquadaily.app.core.auth

import java.security.MessageDigest

object PasswordHasher {

    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }
}
