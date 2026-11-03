package com.example.kidmathlocker

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

data class AppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var isLocked: Boolean
)

class AppAdapter(
    private val appList: List<AppItem>,
    private val onAppToggled: (AppItem, Boolean) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)
        val appPackage: TextView = view.findViewById(R.id.appPackage)
        val appSwitch: SwitchCompat = view.findViewById(R.id.appSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_toggle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = appList[position]
        holder.appName.text = item.name
        holder.appPackage.text = item.packageName
        holder.appIcon.setImageDrawable(item.icon)

        // Clear listener before updating state to avoid recycled-view trigger bugs
        holder.appSwitch.setOnCheckedChangeListener(null)
        holder.appSwitch.isChecked = item.isLocked

        holder.appSwitch.setOnCheckedChangeListener { _, isChecked ->
            item.isLocked = isChecked
            onAppToggled(item, isChecked)
        }
    }

    override fun getItemCount(): Int = appList.size
}