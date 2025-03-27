package expirydatemanager.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import expirydatemanager.retrofit.ExpiryRepository
import nithra.tamil.calendar.expirydatemanager.retrofit.ExpiryViewModel

class ExpiryViewModelFactory(private val repository: ExpiryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpiryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpiryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
