package com.example.locadine.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.locadine.R
import com.example.locadine.Util
import com.example.locadine.pojos.RestaurantInfo
import org.w3c.dom.Text

class RestaurantListAdapter(private val context: Context, private var restaurantInfo: List<RestaurantInfo>): BaseAdapter() {
    override fun getCount(): Int {
        return restaurantInfo.size
    }

    override fun getItem(position: Int): Any {
        return restaurantInfo[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.adapter_restaurant_list, null)

        val adapterImage = view.findViewById<ImageView>(R.id.adapter_image_box)
        val adapterName = view.findViewById<TextView>(R.id.adapter_restaurant_name)
        val adapterRating = view.findViewById<TextView>(R.id.adapter_restaurant_rating)
        val adapterPrice = view.findViewById<TextView>(R.id.adapter_restaurant_price)
        val adapterOpen = view.findViewById<TextView>(R.id.adapter_restaurant_opening)

        val photoUrl = Util.getPhotoUrl(restaurantInfo[position].photos!![0].photo_reference)
        Glide.with(view)
            .load(photoUrl)
            .into(adapterImage) // load photo into the image box

        adapterName.text = restaurantInfo[position].name
        adapterRating.text = "Rating: ${restaurantInfo[position].rating.toString()}"
        adapterPrice.text = "Price: ${getPrice(restaurantInfo[position].price_level)}"
        if (restaurantInfo[position].opening_hours?.open_now == true) {
            adapterOpen.text = "Open now"
        } else {
            adapterOpen.text = "Currently closed"
        }

        return view
    }

    private fun getPrice(level: Int?): String {
        return when (level) {
            1 -> "$"
            2 -> "$$"
            3 -> "$$$"
            4 -> "$$$$"
            else -> "N/A"
        }
    }

}