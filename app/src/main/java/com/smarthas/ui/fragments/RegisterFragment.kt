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
import com.smarthas.presentation.viewmodel.RegisterState
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var passwordConfirmInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginLink: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        nameInput = view.findViewById(R.id.input_name)
        emailInput = view.findViewById(R.id.input_email)
        passwordInput = view.findViewById(R.id.input_password)
        passwordConfirmInput = view.findViewById(R.id.input_password_confirm)
        registerButton = view.findViewById(R.id.btn_register)
        loginLink = view.findViewById(R.id.txt_login_link)

        // Initialize ViewModel
        val tokenManager = TokenManager(requireContext())
        val api = SmartHasApi.create { tokenManager.getToken() }
        val repository = AuthRepository(api, tokenManager)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        // Set up listeners
        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val passwordConfirm = passwordConfirmInput.text.toString().trim()

            if (validateInputs(name, email, password, passwordConfirm)) {
                viewModel.register(email, name, password)
            }
        }

        loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        // Observe register state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    RegisterState.Loading -> {
                        registerButton.isEnabled = false
                        registerButton.text = "Criando..."
                    }
                    RegisterState.Success -> {
                        Toast.makeText(requireContext(), "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_register_to_login)
                    }
                    is RegisterState.Error -> {
                        registerButton.isEnabled = true
                        registerButton.text = getString(R.string.register_button)
                        Toast.makeText(
                            requireContext(),
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    RegisterState.Idle -> {
                        registerButton.isEnabled = true
                        registerButton.text = getString(R.string.register_button)
                    }
                }
            }
        }
    }

    private fun validateInputs(name: String, email: String, password: String, passwordConfirm: String): Boolean {
        return when {
            name.isEmpty() -> {
                Toast.makeText(requireContext(), "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                false
            }
            email.isEmpty() -> {
                Toast.makeText(requireContext(), "Email é obrigatório", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(requireContext(), "Senha é obrigatória", Toast.LENGTH_SHORT).show()
                false
            }
            password != passwordConfirm -> {
                Toast.makeText(requireContext(), "As senhas não correspondem", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(requireContext(), "Senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}
