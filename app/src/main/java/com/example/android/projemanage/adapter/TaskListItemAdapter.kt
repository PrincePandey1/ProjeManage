package com.example.android.projemanage.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.projemanage.R
import com.example.android.projemanage.activities.TaskListActivity
import kotlinx.android.synthetic.main.item_task.view.*
import models.Task
import java.util.*
import kotlin.collections.ArrayList

open class TaskListItemAdapter(private val context: Context,
                               private var list: ArrayList<Task>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//<----------Created two global variable for drag and drop element----------->
    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_task,parent,false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width*0.5).toInt() , LinearLayout.LayoutParams.WRAP_CONTENT) // we define the height and width
        //in which we want to the the data to displayed in particular that width and height

        layoutParams.setMargins(                           // we define the margin inside the function LayoutParams
            (15.toDp().toPx()),0,(40.toDp()).toPx(),0)
        view.layoutParams    //Passes that layout to view

        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            if (position==list.size-1){  //If list is Empty
                holder.itemView.tv_add_task_list.visibility = View.VISIBLE
                holder.itemView.ll_task_item.visibility = View.GONE
            }else{ //Not Empty
                holder.itemView.tv_add_task_list.visibility = View.GONE
                holder.itemView.ll_task_item.visibility = View.VISIBLE
            }
            holder.itemView.tv_task_list_title.text = model.title  // Adding Clickable function to the item of task list activity
            holder.itemView.tv_add_task_list.setOnClickListener {
                holder.itemView.tv_add_task_list.visibility = View.GONE
                holder.itemView.cv_add_task_list_name.visibility = View.VISIBLE
            }
            holder.itemView.ib_done_list_name.setOnClickListener {
                holder.itemView.tv_add_task_list.visibility = View.GONE
                holder.itemView.cv_add_task_list_name.visibility = View.VISIBLE
            }
            holder.itemView.ib_close_list_name.setOnClickListener {
                holder.itemView.tv_add_task_list.visibility = View.VISIBLE
                holder.itemView.cv_add_task_list_name.visibility = View.GONE
            }
            holder.itemView.ib_done_list_name.setOnClickListener {
                val listName = holder.itemView.et_task_list_name.text.toString()
                if (listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context,"Please Enter the List Name.",Toast.LENGTH_LONG).show()
                }
            }
            holder.itemView.ib_edit_list_name.setOnClickListener {
                holder.itemView.et_edit_task_list_name.setText(model.title)
                holder.itemView.ll_title_view.visibility = View.GONE
                holder.itemView.cv_edit_task_list_name.visibility = View.VISIBLE
            }
            holder.itemView.ib_close_editable_view.setOnClickListener {
                holder.itemView.ll_title_view.visibility = View.VISIBLE
                holder.itemView.cv_edit_task_list_name.visibility = View.GONE
            }

            holder.itemView.ib_done_edit_list_name.setOnClickListener {
                val listName = holder.itemView.et_edit_task_list_name.text.toString()

                if (listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.updateTaskList(position, listName, model)
                    }
                }else{
                    Toast.makeText(context,"Please Enter the List Name.", Toast.LENGTH_LONG).show()
                }
            }

            holder.itemView.ib_delete_list.setOnClickListener {
                alertDialogForDeleteList(position,model.title)
            }
            holder.itemView.tv_add_card.setOnClickListener {

                holder.itemView.cv_add_card.visibility = View.VISIBLE
                holder.itemView.tv_add_card.visibility = View.GONE
            }

            holder.itemView.ib_close_card_name.setOnClickListener {
                holder.itemView.cv_add_card.visibility = View.GONE
                holder.itemView.tv_add_card.visibility = View.VISIBLE
            }


            holder.itemView.ib_done_card_name.setOnClickListener {
                val cardName = holder.itemView.et_card_name.text.toString() //Getting the cardName From the UI

                if (cardName.isNotEmpty()){
                    if(context is TaskListActivity){
                     context.addCardToTaskList(position, cardName)
                    }
                }else{
                    Toast.makeText(context,"Please Enter a Card Name.", Toast.LENGTH_LONG).show()
                }
            }
//<---------Load the cards in CardRecycler View------->
            holder.itemView.rv_card_list.layoutManager = LinearLayoutManager(context)
            holder.itemView.rv_card_list.setHasFixedSize(true)

           val adapter = CardListItemAdapter(context , model.cards) // Passing value to CardListItemAdapter
            holder.itemView.rv_card_list.adapter = adapter // Passing adapter to recyclerView

        //    <-------here gets position of the task from onBindViewHolder Of TaskListAdapter & Under that we get the object Which  override the  onclick Function where we get the position of the card From CardListItemAdapter
            adapter.setOnClickListener( //We get position from TaskListItemAdapter from Binfer function
                object: CardListItemAdapter.OnClickListener{
                    override fun onClick(Cardposition: Int) {  //And cardPosition from CardItemAdapter
                        if(context is TaskListActivity){
                            context.cardDetails(position,Cardposition)
                        }
                    }
                }
            )
            //<-------==== Adding drag and drop feature---==============>
              val dividerItemDecoration = DividerItemDecoration(context,
                      DividerItemDecoration.VERTICAL)
            holder.itemView.rv_card_list.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(
                 object :  ItemTouchHelper.SimpleCallback(
                         ItemTouchHelper.UP or ItemTouchHelper.DOWN , 0
                 ){
                     override fun onMove(recyclerView: RecyclerView, dragged: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                         val draggedPosition = dragged.adapterPosition //The position of the item from where the drag has been started
                         val targetPosition =   target.adapterPosition //The position where the item is dropped

                         if(mPositionDraggedFrom == -1){  //If dragged item is empty then pass pass the position of the drag item
                             mPositionDraggedFrom = draggedPosition
                         }
                         mPositionDraggedTo = targetPosition
                         //<==========Here we pass the list ,draggedPosition and targetPosition to the Swap function to perform the following feature-=======>
                         Collections.swap(list[position].cards , draggedPosition , targetPosition)

                         adapter.notifyItemMoved(draggedPosition,targetPosition)
                         return false

                     }

                     override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                     }

                     override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                         super.clearView(recyclerView, viewHolder)
                          // Checks if the elements are swapped
                         if (mPositionDraggedFrom !=-1 && mPositionDraggedTo !=-1 && mPositionDraggedFrom != mPositionDraggedTo){
                             (context as TaskListActivity).updateCardsInTaskList(position,list[position].cards)
                         }
                         //After drag update the value
                         mPositionDraggedTo = -1
                         mPositionDraggedFrom = -1
                     }
                 }
            )
            helper.attachToRecyclerView(holder.itemView.rv_card_list)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun Int.toDp(): Int = (this/ Resources.getSystem().displayMetrics.density).toInt() // It allow to get the screen dP and convert it into integer
    // (In how much area of the screen we want to show data
    // in mobile we can manage using this function)
    // it gives Density to Pixel
    private fun Int.toPx(): Int = (this *Resources.getSystem().displayMetrics.density).toInt()   // Px means Pixel || it gives Pixel to density

    private fun alertDialogForDeleteList(position: Int, title: String){  // Created Alter Dialog

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")

        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(R.drawable.ic_warning)

        builder.setPositiveButton("Yes"){ dialogInterface, which->
            dialogInterface.dismiss()

            if (context is TaskListActivity){
                context.deleteTaskList(position)
            }

        }
        builder.setNegativeButton("No"){dialogInterface, which->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

}