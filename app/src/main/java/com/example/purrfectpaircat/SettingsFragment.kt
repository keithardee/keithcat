package com.example.purrfectpaircat

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button
        val aboutUs: Button = view.findViewById(R.id.aboutUs)
        val logout: Button = view.findViewById(R.id.logOut)
        val changepass: Button = view.findViewById(R.id.changePassword)

        //**Set click listener to navigate to AboutUsActivity**//
        aboutUs.setOnClickListener {
            val intent = Intent(requireContext(), AboutUsActivity::class.java)
            startActivity(intent)
        }

        logout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    // Clear SharedPreferences and navigate to Login Screen
                    val sharedPreferences = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Change Pass and go to LoginActivity
        changepass.setOnClickListener {
            val intent = Intent(requireContext(), ChangepassActivity::class.java)
            startActivity(intent)
        }

    }
}