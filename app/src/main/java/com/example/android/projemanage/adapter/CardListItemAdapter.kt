package com.example.android.projemanage.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.projemanage.R
import com.example.android.projemanage.activities.TaskListActivity
import kotlinx.android.synthetic.main.activity_card_details.view.*
import kotlinx.android.synthetic.main.item_card.view.*
import models.Board
import models.Card
import models.SelectedMembers

open class CardListItemAdapter(private val context: Context,
                               private var list: ArrayList<Card>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
     return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){

            if(model.labelColor.isNotEmpty()){
                holder.itemView.view_label_color.visibility = View.VISIBLE
                holder.itemView.view_label_color
                        .setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.itemView.view_label_color.visibility = View.GONE
            }
            holder.itemView.tv_card_name.text = model.name

            //<--------to display the card member to tasklistActivity------->
            if((context as TaskListActivity)
                            .mAssignedMemberDetailList.size > 0){
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                for(i in context.mAssignedMemberDetailList.indices){
                    for (j in model.assignedTo){
                        if(context.mAssignedMemberDetailList[i].id==j){
                            val selectedMembers = SelectedMembers(
                                    context.mAssignedMemberDetailList[i].id,
                                    context.mAssignedMemberDetailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                //<-----If the assigned member to card is equal to the man who created the board then it will not the image of the user----->
                if (selectedMembersList.size > 0){
                    if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.itemView.rv_card_selected_member_list.visibility = View.GONE
                    }else{
                        holder.itemView.rv_card_selected_member_list.visibility = View.VISIBLE

                        holder.itemView.rv_card_selected_member_list.layoutManager =
                                GridLayoutManager(context,4)

                        val adapter = CardMemberListItemsAdapter(context,selectedMembersList,false)
                        holder.itemView.rv_card_selected_member_list.adapter = adapter
                        adapter.setOnClickListener(
                                object : CardMemberListItemsAdapter.OnClickListener{
                                    override fun onClick() {
                                       if(onClickListener != null){
                                           onClickListener!!.onClick(position)
                                       }
                                    }
                                })

                    }
                }else{
                    holder.itemView.rv_selected_members_list.visibility = View.GONE
                }
            }

          //  <-----Giving onclick event to all the card------->
            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }
        }

    }

    override fun getItemCount(): Int {
       return  list.size
    }

    interface OnClickListener{
        fun onClick(position: Int)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
