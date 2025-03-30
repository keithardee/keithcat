package com.example.purrfectpaircat.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val onDelete: (Int) -> Unit // Callback for deleting item
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.mcatName)
        val breed: TextView = v.findViewById(R.id.mcatBreed)
        val gender: TextView = v.findViewById(R.id.mcatGender)
        val age: TextView = v.findViewById(R.id.mcatAge)
        val adopt: TextView = v.findViewById(R.id.mcatAdopt)
        val vaccination: TextView = v.findViewById(R.id.mcatVaccination)
        val adddate: TextView = v.findViewById(R.id.mcatDateAvailable)
        val petPhoto: ImageView = v.findViewById(R.id.petPhoto)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.activity_listitem, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = catList[position]
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

        if (isDeletable) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDelete(position)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
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