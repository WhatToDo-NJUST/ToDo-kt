package com.example.todoapp.fragments.sign

import android.R.attr.button
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
import androidx.navigation.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentLoginBinding
import com.example.todoapp.databinding.FragmentSignBinding
import com.example.todoapp.utils.makeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject


class SignFragment : Fragment() {

    private var _binding: FragmentSignBinding? = null
    private val binding get() = _binding!!

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view=inflater.inflate(R.layout.fragment_login, container, false)
        _binding=FragmentSignBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        navController= activity?.let { Navigation.findNavController(it, R.id.navHostFragment) }!!


        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            binding.BackLoginButton.setOnClickListener(View.OnClickListener {
                navController?.navigate(R.id.action_signFragment_to_loginFragment)
            })

            binding.SignUpButton.setOnClickListener(View.OnClickListener {
                val username=binding.UserNameEdit.text.toString()
                val password=binding.PassWordEdit.text.toString()
                val password_again=binding.PassWordAgainEdit.text.toString()
                val telephone=binding.TelephoneEdit.text.toString()
                if(password!=password_again) makeToast("两次输入密码不一致,请重新输入")
                else{
                    register(telephone,password)
                }


            })
        }





        return binding.root
    }


    fun register(phone:String,password:String){
        val url = "http://10.0.2.2:10001//user/register"
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
            return
        }

        val jsonObject = JSONObject(res)
        val code = jsonObject.getInt("code")
        if(code==200){
            makeToast("注册成功,请返回登录")
            navController?.navigate(R.id.action_signFragment_to_loginFragment)
        }
        else{
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




}