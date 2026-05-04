package com.smarthas.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.smarthas.R
import com.smarthas.data.preferences.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // Wait 2 seconds

            val tokenManager = TokenManager(requireContext())
            if (tokenManager.isLoggedIn()) {
                // User is logged in, go to Home
                findNavController().navigate(R.id.action_splash_to_home)
            } else {
                // User is not logged in, go to Login
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }
    }
}
