package com.aotuding.ding.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aotuding.ding.R
import com.aotuding.ding.data.db.TaskEntity

class TaskAdapter(private var tasks: List<TaskEntity>) :
    RecyclerView.Adapter<TaskAdapter.VH>() {

    interface OnTaskClickListener {
        fun onTaskClick(task: TaskEntity, position: Int)
        fun onTaskLongClick(task: TaskEntity, position: Int)
    }

    private var listener: OnTaskClickListener? = null

    fun setOnTaskClickListener(l: OnTaskClickListener) {
        listener = l
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = tasks[position]
        holder.tv.text = "${position + 1}. ${task.time}"
        holder.itemView.setOnClickListener {
            listener?.onTaskClick(task, position)
        }
        holder.itemView.setOnLongClickListener {
            listener?.onTaskLongClick(task, position)
            true
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun update(newTasks: List<TaskEntity>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}