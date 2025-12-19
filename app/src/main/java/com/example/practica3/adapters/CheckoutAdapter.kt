package com.example.practica3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.practica3.R
import com.example.practica3.models.CartItemDto


class CheckoutAdapter(
    private val checkoutItems: List<CartItemDto>
) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.checkout_item, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val item = checkoutItems[position]
        holder.textViewName.text = item.name

        val subtotal = item.price * item.quantity
        holder.textViewPrice.text = "${item.price} €  x${item.quantity}  =  ${"%.2f".format(subtotal)} €"
    }

    override fun getItemCount(): Int = checkoutItems.size
}