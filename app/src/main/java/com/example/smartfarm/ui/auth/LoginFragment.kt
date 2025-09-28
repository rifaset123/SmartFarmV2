package com.example.smartfarm.ui.auth

import android.os.Bundle
import android.transition.Fade
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.databinding.FragmentLoginBinding
import com.example.smartfarm.ui.component.LoadingDialogBar
import com.example.smartfarm.util.UiState
import com.example.smartfarm.util.hide
import com.example.smartfarm.util.isValidEmail
import com.example.smartfarm.util.show
import com.example.smartfarm.util.toast
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class LoginFragment : Fragment() {

    val TAG: String = "RegisterFragment"
    lateinit var binding: FragmentLoginBinding
    val viewModel: AuthViewModel by viewModels()
    lateinit var loadingDialogBar: LoadingDialogBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)

        enterTransition = Fade()
        exitTransition = Fade()
        reenterTransition = Fade()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialogBar = LoadingDialogBar(requireContext())
        observer()
        binding.loginBtn.setOnClickListener {
            if (validation()) {
                viewModel.login(
                    email = binding.emailEt.text.trim().toString(),
                    password = binding.passEt.text.toString()
                )
            }
        }
//
//        binding.forgotPassLabel.setOnClickListener {
//            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
//        }
//
        binding.registerLabel.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    fun observer(){
        viewModel.login.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.loginBtn.setText("")
                    loadingDialogBar.showDialog("Memuat")
                }
                is UiState.Failure -> {
                    binding.loginBtn.setText("Login")
                    loadingDialogBar.hideDialog()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.loginBtn.setText("Login")
                    loadingDialogBar.hideDialog()
                    toast(state.data)
                    findNavController().navigate(R.id.action_loginFragment_to_home_navigation)
                }
            }
        }
    }

    fun validation(): Boolean {
        val email = binding.emailEt.text?.toString()?.trim().orEmpty()
        val password = binding.passEt.text?.toString().orEmpty()

        return when {
            email.isEmpty() -> {
                toast(getString(R.string.enter_email))
                false
            }
            !email.isValidEmail() -> {
                toast(getString(R.string.invalid_email))
                false
            }
            password.isEmpty() -> {
                toast(getString(R.string.enter_password))
                false
            }
            password.length < 8 -> {
                toast(getString(R.string.invalid_password))
                false
            }
            else -> true
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getSession { user ->
            if (user != null){
                findNavController().navigate(R.id.action_loginFragment_to_home_navigation)
            }
        }
    }
}