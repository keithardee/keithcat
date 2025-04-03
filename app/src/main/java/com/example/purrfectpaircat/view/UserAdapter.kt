package com.example.purrfectpaircat.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.purrfectpaircat.R
import com.example.purrfectpaircat.model.UserData
import org.w3c.dom.Text

class UserAdapter(
    private val context: Context,
    private var catList: ArrayList<UserData>,
    private val isDeletable: Boolean, // NEW PARAMETER TO CONTROL DELETE BUTTON VISIBILITY
    private val onDelete: (Int) -> Unit // Callback for deleting itemz
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val userFullName: TextView = v.findViewById(R.id.userFullName)
        val name: TextView = v.findViewById(R.id.mcatName)
        val breed: TextView = v.findViewById(R.id.mcatBreed)
        val gender: TextView = v.findViewById(R.id.mcatGender)
        val age: TextView = v.findViewById(R.id.mcatAge)
        val adopt: TextView = v.findViewById(R.id.mcatAdopt)
        val vaccination: TextView = v.findViewById(R.id.mcatVaccination)
        val adddate: TextView = v.findViewById(R.id.mcatDateAvailable)
        val petPhoto: ImageView = v.findViewById(R.id.petPhoto)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
        val btnUserInfo: Button = v.findViewById(R.id.userInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.activity_listitem, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = catList[position]

        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val ownerFullname = sharedPreferences.getString("fullname", "N/A") ?: "N/A"

        holder.userFullName.text = "Owner: $ownerFullname"

        holder.name.text = "Pets Name: ${user.name}"
        holder.breed.text = "Pets Breed: ${user.breed}"
        holder.gender.text = "Pets Gender: ${user.gender}"
        holder.age.text = "Pets Age: ${user.age}"
        holder.adopt.text = "Adopt Status: ${user.adopt}"
        holder.vaccination.text = "Pets Vaccination Count: ${user.vaccination}"
        holder.adddate.text = "Available Date Until: ${user.adddate}"

        // SAFE IMAGE HANDLING
        if (!user.imageUri.isNullOrEmpty()) {
            try {
                holder.petPhoto.setImageURI(Uri.parse(user.imageUri))
            } catch (e: Exception) {
                holder.petPhoto.setImageResource(R.drawable.ic_circle_account) // Fallback if invalid URI
            }
        } else {
            holder.petPhoto.setImageResource(R.drawable.ic_circle_account)
        }

        // ADD CLICK LISTENER TO USER INFO BUTTON
        holder.btnUserInfo.setOnClickListener {
            showUserPopup(holder.itemView.context, user)
        }

        if (isDeletable) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDelete(position)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    // FUNCTION TO SHOW USER INFO POPUP
    private fun showUserPopup(context: Context, user: UserData) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.popup_user_info, null)
        val dialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val user = UserData (
            name = "", breed = "", gender = "", age = "", adopt = "",
            vaccination =  "", adddate = "", imageUri = null,

            fullname = sharedPreferences.getString("fullname", "N/A"),
            email = sharedPreferences.getString("email", "N/A"),
            contactNumber = sharedPreferences.getString("contact_number", "N/A"),
            facebookName = sharedPreferences.getString("facebook_name", "N/A"),
            address = sharedPreferences.getString("home_address", "N/A")
        )

        // BIND DATA TO POPUP FIELDS
        dialogView.findViewById<TextView>(R.id.tvFullname).text = user.fullname
        dialogView.findViewById<TextView>(R.id.tvEmail).text = user.email
        dialogView.findViewById<TextView>(R.id.tvContactNumber).text = user.contactNumber
        dialogView.findViewById<TextView>(R.id.tvFacebook).text = user.facebookName
        dialogView.findViewById<TextView>(R.id.tvAddress).text = user.address

        // CLOSE BUTTON
        dialogView.findViewById<Button>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    override fun getItemCount(): Int {
        return catList.size
    }

    // Update RecyclerView data
    fun updateData(newList: ArrayList<UserData>) {
        catList = newList
        notifyDataSetChanged() // Ensure UI updates
    }
}