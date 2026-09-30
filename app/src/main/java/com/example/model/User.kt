package com.example.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: String = "user", // "admin" or "user"
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean
        get() = role == "admin" || email.equals("ameadmriem@gmail.com", ignoreCase = true)
}
