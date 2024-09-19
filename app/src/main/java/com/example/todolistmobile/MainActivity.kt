package com.example.todolistmobile

import TaskAdapter
import TodoRepository
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolistmobile.databinding.ActivityMainBinding
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.io.IOException


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding

    lateinit var adapter: TaskAdapter

    private var todoRepository = TodoRepository(this)

    lateinit var editTaskIntent:ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        editTaskIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode== RESULT_OK){
                val whatDo = result.data?.getStringExtra("key").toString()
                val num = result.data?.getStringExtra("num")?.toInt()
                when(whatDo){
                    "delete"->{
                        adapter.deleteTask(num)
                    }
                    "update"->{
                        adapter.editTask(num,result.data?.getStringExtra("text").toString(),result.data?.getStringExtra("status").toBoolean())
                    }
                }

            }
        }

        adapter = TaskAdapter(object : TaskAdapter.OnItemClickListener {
            override fun onButtonClick(task: Task) {
                val intent = Intent(this@MainActivity,TaskEditActivity::class.java)
                intent.putExtra("text",task.tasktext)
                intent.putExtra("status", task.isComplete.toString())
                intent.putExtra("num", task.numInList.toString())
                editTaskIntent.launch(intent)
            }
        })

        init()

        binding.buttonSave.setOnClickListener {
            val taskList = adapter.getTaskList()

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Введите имя файла")

            val input = EditText(this)
            builder.setView(input)

            builder.setPositiveButton("Сохранить") { _, _ ->
                val fileName = input.text.toString()
                todoRepository.saveTodoList(taskList, fileName)
                Toast.makeText(this, "Список сохранен в файл $fileName.json", Toast.LENGTH_SHORT).show()
            }

            builder.setNegativeButton("Отмена", null)
            builder.show()
        }

        binding.buttonLoad.setOnClickListener {
            val availableFiles = todoRepository.getAvailableFiles()

            if (availableFiles.isNotEmpty()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Выберите файл для загрузки")

                builder.setItems(availableFiles.toTypedArray()) { _, which ->
                    val selectedFile = availableFiles[which]
                    adapter.setTaskList(todoRepository.loadTodoList(selectedFile))
                    Toast.makeText(this, "Загружен файл $selectedFile.json", Toast.LENGTH_SHORT).show()
                }

                builder.show()
            } else {
                Toast.makeText(this, "Нет доступных файлов для загрузки", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun init(){
        binding.toDoList.layoutManager = LinearLayoutManager(this)
        binding.apply {
            toDoList.adapter = adapter
            buttonAdd.setOnClickListener {
                //Log.d("a","llll")
                if (!inputToDo.text.toString().isEmpty()) {
                    adapter.addTask(inputToDo.text.toString())
                    inputToDo.setText("")
                }
            }
        }
    }


}