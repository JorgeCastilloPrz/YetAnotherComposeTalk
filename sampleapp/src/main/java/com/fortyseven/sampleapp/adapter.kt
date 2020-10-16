package com.fortyseven.sampleapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// DiffUtil.ItemCallback is stateless thus can be object
object UserDiffUtil : DiffUtil.ItemCallback<User>() {
  override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
    oldItem.id == newItem.id

  override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
    oldItem == newItem
}

val AsyncUserDiffUtil: AsyncDifferConfig<User> =
  AsyncDifferConfig.Builder(UserDiffUtil).build()

class UserAdapter() : ListAdapter<User, UserViewHolder>(AsyncUserDiffUtil) {

  var favListener: (User) -> Unit = {}

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
    UserViewHolder(
      LayoutInflater.from(parent.context).inflate(
        R.layout.viewholder_user,
        parent,
        false
      ),
      favListener
    )

  override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
    holder.bind(getItem(position))
}

class UserViewHolder(itemView: View, private val favListener: (User) -> Unit) :
  RecyclerView.ViewHolder(itemView) {

  private val userId: TextView = itemView.findViewById(R.id.user_id)
  private val userName: TextView = itemView.findViewById(R.id.user_name)
  private val favIcon: ImageView = itemView.findViewById(R.id.favIcon)

  fun bind(user: User): Unit {
    userId.text = user.id.toString()
    userName.text = user.name
    favIcon.setOnClickListener { favListener(user) }
    favIcon.setImageResource(
      if (user.isFavorite) {
        R.drawable.ic_fav_filled
      } else {
        R.drawable.ic_fav_outline
      }
    )
  }
}
