package com.smarthas.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.smarthas.R
import com.smarthas.data.api.SmartHasApi
import com.smarthas.data.preferences.TokenManager
import com.smarthas.data.repository.AuthRepository
import com.smarthas.presentation.viewmodel.AuthViewModel
import com.smarthas.presentation.viewmodel.LoginState
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerLink: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        emailInput = view.findViewById(R.id.input_email)
        passwordInput = view.findViewById(R.id.input_password)
        loginButton = view.findViewById(R.id.btn_login)
        registerLink = view.findViewById(R.id.txt_register_link)

        // Initialize ViewModel
        val tokenManager = TokenManager(requireContext())
        val api = SmartHasApi.create { tokenManager.getToken() }
        val repository = AuthRepository(api, tokenManager)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        // Set up listeners
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Observe login state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    LoginState.Loading -> {
                        loginButton.isEnabled = false
                        loginButton.text = "Entrando..."
                    }
                    LoginState.Success -> {
                        Toast.makeText(requireContext(), "Login realizado", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_login_to_home)
                    }
                    is LoginState.Error -> {
                        loginButton.isEnabled = true
                        loginButton.text = getString(R.string.login_button)
                        Toast.makeText(
                            requireContext(),
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    LoginState.Idle -> {
                        loginButton.isEnabled = true
                        loginButton.text = getString(R.string.login_button)
                    }
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(requireContext(), "Email é obrigatório", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(requireContext(), "Senha é obrigatória", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repository) as T
    }
}
