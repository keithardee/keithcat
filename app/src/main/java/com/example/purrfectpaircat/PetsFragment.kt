package com.example.purrfectpaircat

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.purrfectpaircat.model.UserData
import com.example.purrfectpaircat.view.UserAdapter
import com.example.purrfectpaircat.viewmodel.PetsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.util.Calendar

class PetsFragment : Fragment() {

    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val petViewModel: PetsViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var imageView: ImageView
    private lateinit var btnImage: View
    private var imageUri: Uri? = null

    // Activity result launcher to pick image from gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val savedImagePath = saveImageToInternalStorage(uri)
            if (savedImagePath != null) {
                imageUri = Uri.parse(savedImagePath) // Store the file path as a URI
                imageView.setImageURI(imageUri)
            } else {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pets, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addsBtn = view.findViewById(R.id.addingBtn)
        recv = view.findViewById(R.id.mRecycler)

        sharedPreferences = requireActivity().getSharedPreferences("pet_data", AppCompatActivity.MODE_PRIVATE)

        loadPetData()

        userAdapter = UserAdapter(requireContext(), petViewModel.petList.value ?: ArrayList(), true) { position ->
            deletePet(position)
        }

        recv.layoutManager = LinearLayoutManager(requireContext())
        recv.adapter = userAdapter

        petViewModel.petList.observe(viewLifecycleOwner) { newList ->
            userAdapter.updateData(newList)
        }

        addsBtn.setOnClickListener { addPet() }
    }

    private fun deletePet(position: Int) {
        val petList = petViewModel.petList.value?.toMutableList() ?: return

        petList.removeAt(position)
        petViewModel.setPetList(ArrayList(petList))

        savePetData(ArrayList(petList))

        userAdapter.notifyItemRemoved(position)
        Toast.makeText(requireContext(), "Pet Deleted", Toast.LENGTH_SHORT).show()
    }

    private fun loadPetData() {
        val savedData = sharedPreferences.getString("pets", "") ?: ""
        val petList = ArrayList<UserData>()

        if (savedData.isNotEmpty()) {
            savedData.split(";").forEach { entry ->
                val parts = entry.split(",")
                if (parts.size >= 8) {
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

    private fun saveImageToInternalStorage(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().filesDir, "pet_${System.currentTimeMillis()}.jpg")

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    private fun addPet() {
        val inflater = LayoutInflater.from(requireContext())
        val v = inflater.inflate(R.layout.activity_additem, null)

        val petName = v.findViewById<EditText>(R.id.petName)
        val petBreed = v.findViewById<EditText>(R.id.breedType)
        val radioMale = v.findViewById<RadioButton>(R.id.radioMale)
        val radioFemale = v.findViewById<RadioButton>(R.id.radioFemale)
        val petAge = v.findViewById<EditText>(R.id.agePet)
        val petAdopt = v.findViewById<EditText>(R.id.adoptType)
        val vaccine = v.findViewById<EditText>(R.id.vaccination)
        val datePicker = v.findViewById<EditText>(R.id.datePicker)

        imageView = v.findViewById(R.id.imageView)
        btnImage = v.findViewById(R.id.btnimage)

        btnImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Apply emoji filter to prevent emojis in input fields
        val emojiFilter = getEmojiFilter()
        petName.filters = arrayOf(emojiFilter)
        petBreed.filters = arrayOf(emojiFilter)
        petAge.filters = arrayOf(emojiFilter)
        petAdopt.filters = arrayOf(emojiFilter)
        vaccine.filters = arrayOf(emojiFilter)
        datePicker.filters = arrayOf(emojiFilter)

        datePicker.setOnClickListener {
            showDatePicker(datePicker)
        }

        // Apply input filters
        val filters = arrayOf(getEmojiFilter())
        listOf(petName, petBreed, petAge, petAdopt, vaccine, datePicker).forEach {
            it.filters = filters
            it.setOnKeyListener(blockEnterKeyListener)
        }

        val addDialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Pet")
            .setView(v)
            .setPositiveButton("Ok") { _, _ ->
                val name = petName.text.toString().trim()
                val breed = petBreed.text.toString().trim()
                val gender = if (radioMale.isChecked) "Male" else if (radioFemale.isChecked) "Female" else "Not Specified"
                val age = petAge.text.toString()
                val adopt = petAdopt.text.toString()
                val vaccination = vaccine.text.toString()
                val adddate = datePicker.text.toString()

                if (name.isNotEmpty() && breed.isNotEmpty() && age.isNotEmpty() && adopt.isNotEmpty() && vaccination.isNotEmpty() && adddate.isNotEmpty()) {
                    val newPet = UserData(name, breed, gender, age, adopt, vaccination, adddate, imageUri?.toString() ?: "")
                    petViewModel.addPet(newPet)
                    savePetData(petViewModel.petList.value ?: ArrayList())
                    Toast.makeText(requireContext(), "Pet Added Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        addDialog.show()
    }

    // InputFilter to prevent emojis and non-breaking spaces (Shift + Space)
    private fun getEmojiFilter(): InputFilter {
        return object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                if (source.isNullOrEmpty()) {
                    return null
                }

                val regex = Regex("[\\p{L}\\p{N}\\p{P}\\p{Z}&&[^\\u00A0]]+")

                if (!source.matches(regex)) {
                    if (source.toString() != "\u00A0") {
                        Toast.makeText(requireContext(), "Emojis/Shift Spaces are not allowed!", Toast.LENGTH_SHORT).show()
                    }
                    return ""
                }
                return null
            }
        }
    }

    private val blockEnterKeyListener = View.OnKeyListener { _, keyCode, event ->
        keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
    }

    private fun showDatePicker(datePicker: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            datePicker.setText("${month + 1}/$day/$year")
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun savePetData(petList: ArrayList<UserData>) {
        sharedPreferences.edit().putString("pets", petList.joinToString(";") {
            "${it.name},${it.breed},${it.gender},${it.age},${it.adopt},${it.vaccination},${it.adddate},${it.imageUri ?: ""}"
        }).apply()
    }
}
