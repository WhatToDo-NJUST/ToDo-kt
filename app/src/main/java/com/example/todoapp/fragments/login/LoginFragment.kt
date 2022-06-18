package com.example.todoapp.fragments.login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentLoginBinding
import com.example.todoapp.utils.makeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    lateinit var navController:NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_login, container, false)
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        navController= activity?.let { Navigation.findNavController(it, R.id.navHostFragment) }!!


        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            binding.SignUpButton.setOnClickListener {
                navController?.navigate(R.id.action_loginFragment_to_signFragment)
            }

            binding.LoginButton.setOnClickListener {
                val telephone = binding.TelePhoneEdit.text.toString()
                val password = binding.PassWordEdit.text.toString()
                login(telephone, password)
            }
        }


        return binding.root
    }


    private fun login(phone: String, password: String) {
        val url = "http://10.0.2.2:10001//user/login"
        val requestBody = FormBody.Builder()
            .add("phone", phone)
            .add("password", password)
            .build()

        //创建request请求对象
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val res = makeRequest(request)

        Log.d("DATA","finish")

        if (res == "error") {
            makeToast("网络故障，请调试网络后重新登录")
            binding.TelePhoneEdit.setText("")
            binding.PassWordEdit.setText("")
            return
        }


        val jsonObject = JSONObject(res)
        val code = jsonObject.getInt("code")
        if(code==200){
            val data = jsonObject.getJSONObject("data")
            saveToken(data.getString("token"))
            makeToast("登录成功")
            navController?.navigate(R.id.action_loginFragment_to_listFragment)
        }
        else{
            Log.d("DATA", "code:$code")
            val msg=jsonObject.getString("msg")
            makeToast(msg)
        }
    }



    fun makeToast(msg: String) {
        val toast=
            Toast.makeText(
            requireContext(),
            msg,
            Toast.LENGTH_SHORT
        )
        val height: Int = requireView().height
        toast.setGravity(height,0,-height/3)
        toast.show()
    }

    fun saveToken(token: String) {
        val sharedPreferences: SharedPreferences = (context?.getSharedPreferences(
            "data",
            Context.MODE_PRIVATE
        ) ?: null) as SharedPreferences
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.apply()
    }

}