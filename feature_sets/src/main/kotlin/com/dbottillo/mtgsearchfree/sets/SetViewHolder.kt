package com.dbottillo.mtgsearchfree.sets

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

class SetViewHolder(val row: View) : RecyclerView.ViewHolder(row) {
    val name: TextView = row.findViewById<TextView>(R.id.set_name)
}
