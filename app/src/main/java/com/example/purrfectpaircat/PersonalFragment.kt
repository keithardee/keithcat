package com.example.purrfectpaircat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.purrfectpaircat.databinding.FragmentPersonalBinding


class PersonalFragment : Fragment() {

    private var _binding: FragmentPersonalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)

        // Get SharedPreferences data
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val fullname = sharedPreferences.getString("fullname", "N/A")
        val email = sharedPreferences.getString("email", "N/A")
        val contactNumber = sharedPreferences.getString("contact_number", "N/A")
        val facebookName = sharedPreferences.getString("facebook_name", "N/A")
        val homeAddress = sharedPreferences.getString("home_address", "N/A")

        binding.tvFullname.text = fullname
        binding.tvEmail.text = email
        binding.tvContactNumber.text = contactNumber
        binding.tvFacebook.text = facebookName
        binding.tvAddress.text = homeAddress

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}