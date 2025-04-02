package com.example.purrfectpaircat

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
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
import com.example.purrfectpaircat.api.RetrofitClient
import com.example.purrfectpaircat.model.PetRequest
import com.example.purrfectpaircat.model.PetResponse
import com.example.purrfectpaircat.model.UserData
import com.example.purrfectpaircat.view.UserAdapter
import com.example.purrfectpaircat.viewmodel.PetsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Pet")
            .setMessage("Are you sure you want to delete this pet?")
            .setPositiveButton("Yes") { _, _ ->
                val petList = petViewModel.petList.value?.toMutableList() ?: return@setPositiveButton

                petList.removeAt(position)
                petViewModel.setPetList(ArrayList(petList))

                savePetData(ArrayList(petList))

                userAdapter.notifyItemRemoved(position)
                Toast.makeText(requireContext(), "Pet Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun loadPetData() {
        val savedData = sharedPreferences.getString("pets", "") ?: ""
        val petList = ArrayList<UserData>()
        val validPets = ArrayList<UserData>()

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
        // Filter expired pets
        val today = Calendar.getInstance()
        val todayString = "${today.get(Calendar.MONTH) + 1}/${today.get(Calendar.DAY_OF_MONTH)}/${today.get(Calendar.YEAR)}"

        for (pet in petList) {
            if (pet.adddate >= todayString) {
                validPets.add(pet)  // Keep valid pets only
            }
        }

        petViewModel.setPetList(validPets)
        savePetData(validPets)  // Update storage by removing expired pets
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

        // Set a click listener for the date picker
        datePicker.setOnClickListener {
            showDatePicker(datePicker)
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

        // Apply input filters to restrict to 2 digits
        val digitFilter = InputFilter { source, _, _, dest, _, _ ->
            if (source.matches(Regex("\\d*")) && (dest.length + source.length) <= 2) {
                source
            } else {
                ""
            }
        }

        petAge.filters = arrayOf(digitFilter)
        vaccine.filters = arrayOf(digitFilter)

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

                // Format the date to "YYYY-MM-DD"
                val formattedDate = formatDate(adddate)


                if (name.isNotEmpty() && breed.isNotEmpty() && age.isNotEmpty() && adopt.isNotEmpty() && vaccination.isNotEmpty() && adddate.isNotEmpty() && formattedDate.isNotEmpty()) {
                    val newPet = UserData(name, breed, gender, age, adopt, vaccination, adddate, imageUri?.toString() ?: "")
                    petViewModel.addPet(newPet)
                    savePetData(petViewModel.petList.value ?: ArrayList())

                    // Call the method to send the new pet to the database
                    addPetToDatabase(newPet)
                } else {
                    Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        addDialog.show()
    }

    // Date format conversion to yyyy-MM-dd
    private fun formatDate(date: String): String {
        if (date.isEmpty()) return "" // Handle empty date

        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) // The format you are getting from the user
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // The format the database expects

        return try {
            val parsedDate = inputFormat.parse(date)
            parsedDate?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            "" // Return empty string if parsing fails
        }
    }

    private fun showDatePicker(datePicker: EditText) {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis // Get current date in milliseconds

        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, day ->
            // Format the date to MM/dd/yyyy
            val formattedDate = "${month + 1}/$day/$year"
            datePicker.setText(formattedDate) // Set the date in the EditText
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // Restrict past dates
        datePickerDialog.datePicker.minDate = today

        datePickerDialog.show()
    }

    private fun addPetToDatabase(newPet: UserData) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        Log.d("SharedPreferences", "Retrieved user_id: $userId")


        if (userId == -1) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Log the formatted date before sending it to the API
        Log.d("PetData", "Sending date to API: ${newPet.adddate}")

        val petRequest = PetRequest(
            user_id = userId,
            name = newPet.name,
            breed = newPet.breed,
            gender = newPet.gender,
            age = newPet.age.toIntOrNull() ?: 0, // Prevent crash if empty
            adopt_status = newPet.adopt,
            vaccination = newPet.vaccination.toIntOrNull() ?: 0, // Prevent crash if empty
            adddate = newPet.adddate,
            imageUri = newPet.imageUri
        )

        Log.d("PetData", "Sending: $petRequest")

        RetrofitClient.petService.addPet(petRequest).enqueue(object : retrofit2.Callback<PetResponse> {
            override fun onResponse(call: Call<PetResponse>, response: Response<PetResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        Log.d("API_SUCCESS", "Pet added: ${responseBody.message}")
                        Toast.makeText(requireContext(), "Pet Added Successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("API_ERROR", "Failed: ${responseBody?.message}")
                        Toast.makeText(requireContext(), "${responseBody?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Response Code: ${response.code()}, Error: $errorBody")
                    Toast.makeText(requireContext(), "Failed: $errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PetResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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


    private fun savePetData(petList: ArrayList<UserData>) {
        sharedPreferences.edit().putString("pets", petList.joinToString(";") {
            "${it.name},${it.breed},${it.gender},${it.age},${it.adopt},${it.vaccination},${it.adddate},${it.imageUri ?: ""}"
        }).apply()
    }
}