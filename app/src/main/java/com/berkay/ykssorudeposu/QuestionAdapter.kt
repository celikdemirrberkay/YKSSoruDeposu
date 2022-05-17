package com.berkay.ykssorudeposu

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berkay.ykssorudeposu.databinding.RecyclerRowBinding

class QuestionAdapter(val questionList:ArrayList<Question>) : RecyclerView.Adapter<QuestionAdapter.QuestionHolder>(){

    class QuestionHolder(val binding : RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return QuestionHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = questionList.get(position).name
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,QuestionActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",questionList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return questionList.size
    }


}