package com.berkay.ykssorudeposu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.berkay.ykssorudeposu.databinding.ActivityMainBinding
import com.berkay.ykssorudeposu.databinding.ActivityQuestionBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var questionList : ArrayList<Question>
    private lateinit var questionAdapter : QuestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        questionList = ArrayList<Question>()
        questionAdapter = QuestionAdapter(questionList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = questionAdapter

        try {
            val database = this.openOrCreateDatabase("Question", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM questions",null)
            val lessonNameIx = cursor.getColumnIndex("lessonname")
            val idIx = cursor.getColumnIndex("ID")

            while (cursor.moveToNext()){
                val name = cursor.getString(lessonNameIx)
                val id = cursor.getInt(idIx)
                val question = Question(name,id)
                questionList.add(question)

            }
            questionAdapter.notifyDataSetChanged()
                cursor.close()
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.question_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.add_question_item){
            val intent = Intent(this@MainActivity,QuestionActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }


}