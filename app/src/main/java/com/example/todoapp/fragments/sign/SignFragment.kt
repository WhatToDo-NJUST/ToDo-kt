package com.example.todoapp.fragments.sign

import android.R.attr.button
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentLoginBinding
import com.example.todoapp.databinding.FragmentSignBinding


class SignFragment : Fragment() {

    private var _binding: FragmentSignBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view=inflater.inflate(R.layout.fragment_login, container, false)
        _binding=FragmentSignBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.BackLoginButton.setOnClickListener(View.OnClickListener {
            activity?.let { it1 -> Navigation.findNavController(it1,R.id.navHostFragment).navigate(R.id.action_signFragment_to_loginFragment) }
        })

        return binding.root
    }


}