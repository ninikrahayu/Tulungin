package com.pemmob.tulungin.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.tulungin.data.user.UserRepositoryProvider
import com.pemmob.tulungin.data.user.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    val repository: UserRepository = UserRepositoryProvider.create(application)
    val snapshot = repository.snapshot
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()
    private val mutableMessage = MutableStateFlow<String?>(null)
    val message = mutableMessage.asStateFlow()

    fun clearMessage() { mutableMessage.value = null }
    fun notify(message: String) { mutableMessage.value = message }

    fun perform(success: String? = null, action: suspend UserRepository.() -> Unit) {
        if (mutableBusy.value) return
        mutableBusy.value = true
        viewModelScope.launch {
            try {
                repository.action()
                if (success != null) mutableMessage.value = success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableMessage.value = e.message ?: "Terjadi kendala. Coba lagi."
            } finally {
                mutableBusy.value = false
            }
        }
    }
}
