package com.example.purrfectpaircat

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.purrfectpaircat.model.UserData
import com.example.purrfectpaircat.view.UserAdapter
import com.example.purrfectpaircat.viewmodel.PetsViewModel


class HomeFragment : Fragment() {

    private lateinit var recyclerViewPets: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val petViewModel: PetsViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using your fragment_home.xml
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Reference the RecyclerView (mRecycler) from your layout
        recyclerViewPets = view.findViewById(R.id.mRecycler)
        sharedPreferences = requireActivity().getSharedPreferences("pet_data", AppCompatActivity.MODE_PRIVATE)

        // Load pet data from SharedPreferences into the ViewModel
        loadPetData()

        // Initialize the adapter with the current pet list
        userAdapter = UserAdapter(requireContext(), petViewModel.petList.value ?: ArrayList(), false) { position ->
            // Do nothing since delete should be disabled in HomeFragment
        }

        recyclerViewPets.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewPets.adapter = userAdapter

        // Observe changes in the pet list and update the adapter accordingly
        petViewModel.petList.observe(viewLifecycleOwner) { newList ->
            userAdapter.updateData(newList)
        }
    }

    private fun deletePet(position: Int) {
        val petList = petViewModel.petList.value?.toMutableList() ?: return
        petList.removeAt(position)
        petViewModel.setPetList(ArrayList(petList))
        savePetData(ArrayList(petList))
        userAdapter.notifyItemRemoved(position)
    }

    private fun loadPetData() {
        val savedData = sharedPreferences.getString("pets", "") ?: ""
        val petList = ArrayList<UserData>()

        if (savedData.isNotEmpty()) {
            savedData.split(";").forEach { entry ->
                val parts = entry.split(",")
                if (parts.size >= 8) {  // Ensure at least 8 fields
                    val name = parts[0].trim()
                    val breed = parts[1].trim()
                    val gender = parts[2].trim()
                    val age = parts[3].trim()
                    val adopt = parts[4].trim()
                    val vaccination = parts[5].trim()
                    val adddate = parts[6].trim()
                    val imageUri = parts[7].trim().ifEmpty { null }

                    petList.add(UserData(name, breed, gender, age, adopt, vaccination, adddate, imageUri))
                }
            }
        }
        petViewModel.setPetList(petList)
    }

    private fun savePetData(petList: ArrayList<UserData>) {
        val editor = sharedPreferences.edit()
        val dataString = petList.joinToString(";") {
            "${it.name},${it.breed},${it.gender},${it.age},${it.adopt},${it.vaccination},${it.adddate},${it.imageUri ?: ""}"
        }
        editor.putString("pets", dataString)
        editor.apply()
    }

}