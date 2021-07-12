package com.example.android.projemanage.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.projemanage.R
import com.example.android.projemanage.adapter.LabelColorListItemsAdapter
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class LabelColorListDialog(
        context: Context,
        private var list: ArrayList<String>,
        private val title: String = " ",
        private val mSelectedColor: String = " "
): Dialog(context) {

    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(
                R.layout.dialog_list,null)

        setContentView(view)
        setCanceledOnTouchOutside(true) // By clicking outside the dialog will be cancelled
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View){
        view.tvTitle.text = title
        view.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context , list, mSelectedColor)
        view.rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)

            }

        }
    }

    protected abstract fun onItemSelected(color: String)


}