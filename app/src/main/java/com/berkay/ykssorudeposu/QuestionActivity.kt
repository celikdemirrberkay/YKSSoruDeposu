package com.berkay.ykssorudeposu

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.berkay.ykssorudeposu.databinding.ActivityMainBinding
import com.berkay.ykssorudeposu.databinding.ActivityQuestionBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        database = this.openOrCreateDatabase("Question", MODE_PRIVATE,null)

        registerLauncher()
        val intent = intent
        val info = intent.getStringExtra("info")

        if(info.equals("new")){
            binding.dersText.setText("")
            binding.konuText.setText("")
            binding.tarihText.setText("")
            binding.button.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.select)

        }else{
            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)
            val cursor = database.rawQuery("SELECT * FROM questions WHERE ID = ?", arrayOf(selectedId.toString()))

            val lessonNameIx = cursor.getColumnIndex("lessonname")
            val subjectIx = cursor.getColumnIndex("subjectname")
            val dateIx = cursor.getColumnIndex("date")
            val imageIx = cursor.getColumnIndex("image")

            while(cursor.moveToNext()){
                binding.dersText.setText(cursor.getString(lessonNameIx))
                binding.konuText.setText(cursor.getString(subjectIx))
                binding.tarihText.setText(cursor.getString(dateIx))
                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)


            }
            cursor.close()

        }

    }

    fun saveButtonClick(view: View){

        val lessonText = binding.dersText.text.toString()
        val konuText = binding.konuText.text.toString()
        val tarihText = binding.tarihText.text.toString()

        if(selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try{
                database.execSQL("CREATE TABLE IF NOT EXISTS questions (ID INTEGER PRIMARY KEY, lessonname VARCHAR, subjectname VARCHAR, date VARCHAR, image BLOB ) ")
                val sqlString = "INSERT INTO questions (lessonname,subjectname,date,image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,lessonText)
                statement.bindString(2,konuText)
                statement.bindString(3,tarihText)
                statement.bindBlob(4,byteArray)
                statement.execute()

            }catch(e : Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@QuestionActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)


        }


    }

    private fun makeSmallerBitmap(image : Bitmap,maximumSize : Int) : Bitmap{
       var height = image.height
       var width = image.width
       val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if(bitmapRatio > 1){
        // landscape
            width = maximumSize
            val scaledHeight =width / bitmapRatio
            height = scaledHeight.toInt()

        }else{
        // portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }


    fun saveImage(view : View){

        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Galeriye gitmek için izin vermelisiniz . ",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver",View.OnClickListener {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }else{
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)

        }

    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

        if(result.resultCode == RESULT_OK){
            val intentFromResult = result.data
            if(intentFromResult != null){
                val imageData = intentFromResult.data
                //binding.imageView.setImageURI(imageData)
               if(imageData != null) {
                   try {
                       if(Build.VERSION.SDK_INT >= 28){
                       val source = ImageDecoder.createSource(this@QuestionActivity.contentResolver, imageData)
                        selectedBitmap = ImageDecoder.decodeBitmap(source)
                        binding.imageView.setImageBitmap(selectedBitmap)
                       }else{
                           selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                           binding.imageView.setImageBitmap(selectedBitmap)
                       }

                   } catch (e: Exception) {
                       e.printStackTrace()
                   }
               }
            }

        }

        }

      permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
          if(result){

             val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
             activityResultLauncher.launch(intentToGallery)
          }else{
              Toast.makeText(this@QuestionActivity,"İzin Gerekli ! ",Toast.LENGTH_LONG).show()
          }

      }

    }


}