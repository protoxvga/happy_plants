package com.pperrin54.happy_plants.home_activity

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.pperrin54.happy_plants.R

class PlantAdapter(private var context: Context, private var ArrayList: ArrayList<PlantModal>): BaseAdapter() {
    // This is the method that will be called to get the plant item at the specified position
    override fun getItem(position: Int): Any {
        return (ArrayList[position])
    }

    // This is the method that will be called to get the position of the item in the array list
    override fun getItemId(position: Int): Long {
        return (position.toLong())
    }

    // This is the method that will be called to get the number of items in the array list
    override fun getCount(): Int {
        return (ArrayList.size)
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.grid_item_list, null)  // This is the view that will be used to display the plant item
        val icons: ImageView = view.findViewById(R.id.plant_image)      // This is the image view that will display the plant icon
        val names: TextView = view.findViewById(R.id.plant_name)        // This is the text view that will display the plant name

        val plantItem: PlantModal = ArrayList[position]  // This is the plant item that will be displayed

        names.text = plantItem.plantName                // This is the plant name that will be displayed
        icons.setImageResource(plantItem.plantImage)     // This is the plant icon that will be displayed

        return view
    }
}