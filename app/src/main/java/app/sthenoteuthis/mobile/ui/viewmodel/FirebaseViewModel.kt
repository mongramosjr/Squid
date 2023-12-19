package app.sthenoteuthis.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class FirebaseViewModel: ViewModel() {
    var auth: FirebaseAuth? = null

    // Initialize FirebaseAuth within the ViewModel
    fun initFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }
}