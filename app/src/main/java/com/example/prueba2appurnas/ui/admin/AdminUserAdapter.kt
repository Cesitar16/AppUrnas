package com.example.prueba2appurnas.ui.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prueba2appurnas.databinding.ItemUserAdminBinding
import com.example.prueba2appurnas.model.User

class AdminUserAdapter(
    private var users: List<User>,
    private val onStatusChange: (User, Boolean) -> Unit, // Callback para el Switch
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        with(holder.binding) {
            txtUserName.text = user.name
            txtUserEmail.text = user.email
            txtUserRole.text = user.rol.uppercase()

            // Colores según rol
            if (user.rol.equals("admin", ignoreCase = true)) {
                txtUserRole.setTextColor(Color.parseColor("#EAC47F")) // Gold
            } else {
                txtUserRole.setTextColor(Color.parseColor("#CCCCCC")) // Gris
            }

            // Lógica del Switch: Evitar disparar el listener durante el reciclado
            switchActive.setOnCheckedChangeListener(null)

            // Asignar estado actual (Si 'activo' es null, asumimos false o true según tu lógica)
            switchActive.isChecked = user.activo ?: false

            // Efecto visual de opacidad
            root.alpha = if (switchActive.isChecked) 1.0f else 0.5f

            // Asignar listener nuevo
            switchActive.setOnCheckedChangeListener { _, isChecked ->
                // Actualizar visualmente inmediato para feedback
                root.alpha = if (isChecked) 1.0f else 0.5f
                onStatusChange(user, isChecked)
            }

            btnDeleteUser.setOnClickListener { onDeleteClick(user) }
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}