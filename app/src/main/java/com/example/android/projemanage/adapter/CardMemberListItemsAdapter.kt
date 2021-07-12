package com.example.android.projemanage.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.projemanage.R
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.item_card_selected_member.view.*
import kotlinx.android.synthetic.main.item_members.view.*
import models.SelectedMembers
import models.User

//<-------To display the selected member to the recycler View------>

open class CardMemberListItemsAdapter
       (private val context: Context,
         private val List: ArrayList<SelectedMembers>,
       private val assignedMembers: Boolean)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card_selected_member,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = List[position]

        if(holder is MyViewHolder){
            if(position == List.size-1 && assignedMembers){ //If list is Empty
              holder.itemView.iv_add_member.visibility = View.VISIBLE
                holder.itemView.iv_selected_member_image.visibility = View.GONE
            }else{ //If list contains Some member
                holder.itemView.iv_add_member.visibility = View.GONE
                holder.itemView.iv_selected_member_image.visibility = View.VISIBLE

                //<----For adding Image of the Selected User---->
                Glide.with(context)
                        .load(model.image)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(holder.itemView.iv_selected_member_image);
            }
            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick()
                }
            }
        }
    }

    override fun getItemCount(): Int {
      return  List.size
    }
     class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick()
    }

}