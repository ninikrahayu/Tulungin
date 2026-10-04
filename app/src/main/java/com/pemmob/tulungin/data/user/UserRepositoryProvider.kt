package com.pemmob.tulungin.data.user

import android.content.Context

object UserRepositoryProvider {
    fun create(context: Context): UserRepository = FirestoreUserRepository(context)
}
