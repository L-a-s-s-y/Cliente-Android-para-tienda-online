package com.example.practica3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.practica3.R
import com.example.practica3.models.ProductModel

class AdminProductAdapter(
    private var productList: List<ProductModel>,
    private val onDeleteClick: (Long) -> Unit,
    private val onEditClick: (ProductModel) -> Unit,

) : RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder>() {

    inner class AdminProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val buttonRemove: Button = itemView.findViewById(R.id.buttonRemove)
        val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_product_item, parent, false)
        return AdminProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminProductViewHolder, position: Int) {
        val product = productList[position]
        holder.textViewName.text = product.name
        holder.textViewPrice.text = "${product.price}€"

        holder.buttonRemove.setOnClickListener {
            onDeleteClick(product.id)
        }

        holder.buttonEdit.setOnClickListener {
            onEditClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateProducts(newList: List<ProductModel>) {
        productList = newList
        notifyDataSetChanged()
    }
}
