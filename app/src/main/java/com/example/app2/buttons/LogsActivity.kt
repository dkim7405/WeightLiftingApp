package com.example.app2.buttons

import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app2.*
import com.example.app2.databinding.ActivityLogsBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat

class LogsActivity : AppCompatActivity() {

    companion object{
        val TAG = "Debugging"
    }

    private lateinit var binding: ActivityLogsBinding
    private lateinit var adapter : WorkoutAdapter

    private lateinit var databaseCalendarReference: DatabaseReference
    private lateinit var databaseWorkoutTypeReference: DatabaseReference

    private lateinit var saveLogData: DataLog
    private var retrievedNoteData : String = ""
    private var retrievedWorkoutData : MutableList<ItemWorkout> = mutableListOf()
    private lateinit var stringDateSelected: String

    private lateinit var workoutList: MutableList<ItemWorkout>
    private lateinit var workoutTypeList: MutableList<String>

    private lateinit var addDialogName: String
    private var addDialogWeight = 0f;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        databaseCalendarReference = FirebaseDatabase.getInstance().getReference("Calendar")
        databaseWorkoutTypeReference = FirebaseDatabase.getInstance().getReference("Workout Type")

        workoutTypeList = mutableListOf(
            "Bench",
            "Deadlift",
            "Squat"
        )

        workoutList = mutableListOf(
            ItemWorkout("Bench", 0.0f),
            ItemWorkout("Deadlift", 0.0f),
            ItemWorkout("Squat", 0.0f)
        )

        var calendarFormat = SimpleDateFormat("yyyy-M-d")
        stringDateSelected = calendarFormat.format(binding.logsCalendarViewCalendar.date)

        retrieveData()




//        binding.logsRecyclerViewWorkoutList.layoutManager = LinearLayoutManager(this)
//        adapter = WorkoutAdapter(defalutWorkoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
//        binding.logsRecyclerViewWorkoutList.adapter = adapter
//
//        databaseReference.child(stringDateSelected).get().addOnSuccessListener {
//            if(it.exists())
//            {
//                var retrievedWorkoutList = it.child("workoutData").value as List<HashMap<String, Any?>>
//                val currentWorkoutList = mutableListOf<ItemWorkout>()
//
//                for(workout in retrievedWorkoutList) {
//                    val item = ItemWorkout("", 0f)
//                    item.setData(workout)
//                    currentWorkoutList.add(item)
//                }
//
//                defalutWorkoutList = currentWorkoutList
//
//                adapter = WorkoutAdapter(currentWorkoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
//                binding.logsRecyclerViewWorkoutList.adapter = adapter
//                binding.logsEditTextNote.setText(it.child("noteData").value.toString())
//            }
//        }
//
//        binding.logsCalendarViewCalendar.setOnDateChangeListener { calendarView, i1, i2, i3 ->
//            stringDateSelected = "${i1}-${i2+1}-${i3}"
//
//            databaseReference.child(stringDateSelected).get().addOnSuccessListener {
//                if(it.exists())
//                {
//                    binding.logsEditTextNote.setText(it.child("noteData").value.toString())
//                    var retrievedWorkoutList = it.child("workoutData").value as List<HashMap<String, Any?>>
//                    val currentWorkoutList = mutableListOf<ItemWorkout>()
//                    for(workout in retrievedWorkoutList) {
//                        val item = ItemWorkout("", 0f)
//                        item.setData(workout)
//                        currentWorkoutList.add(item)
//                    }
//
//                    adapter = WorkoutAdapter(currentWorkoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
//                    binding.logsRecyclerViewWorkoutList.adapter = adapter
//                }
//                else
//                {
//                    binding.logsEditTextNote.setText("")
//                    adapter = WorkoutAdapter(defalutWorkoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
//                    binding.logsRecyclerViewWorkoutList.adapter = adapter
//
//                }
//            }
//        }
//
//        binding.logsImageButtonAddWorkout.setOnClickListener {
//            openAddDialog()
//        }
//
//        binding.logsImageButtonBack.setOnClickListener {
//            val intentMain = Intent(this, MainActivity::class.java)
//            startActivity(intentMain)
//        }
//
//    }
//
//    fun openAddDialog()
//    {
//        val customLayout = layoutInflater.inflate(R.layout.layout_dialog, null)
//        var addWorkoutWeight = customLayout.findViewById<EditText>(R.id.addWorkoutDialog_editText_weight)
//        var addWorkoutName = customLayout.findViewById<EditText>(R.id.addWorkoutDialog_editText_workoutName)
//        val dialog =  AlertDialog.Builder(this, R.style.AddDialogTheme)
//            .setTitle("Add Workout")
//            .setView(customLayout)
//            .setPositiveButton("OK",  DialogInterface.OnClickListener() { dialogInterface, i ->
//                    if(!(addWorkoutName.text.toString() == "" || addWorkoutWeight.text.toString() == "")) {
//                        addDialogName = addWorkoutName.text.toString()
//                        addDialogWeight = addWorkoutWeight.text.toString().toFloat()
//                        adapter.dataSet.add(ItemWorkout(addDialogName, addDialogWeight))
//                        adapter.notifyDataSetChanged()
//                        addWorkoutData()
//                    }
//                })
//            .setNegativeButton("Cancel", null)
//            .create();
//        dialog.show();
//    }
//
//    fun addWorkoutData()
//    {
//        saveLogData = DataLog(adapter.dataSet, binding.logsEditTextNote.text.toString())
//        databaseReference.child(stringDateSelected).setValue(saveLogData)
//    }

    }





    fun updateData()
    {

    }

    fun addData(loggingData: DataLog, workoutTypes: List<String>)
    {
        databaseCalendarReference.child(stringDateSelected).setValue(loggingData)
        databaseWorkoutTypeReference.setValue(workoutTypes)
    }

    fun retrieveData()
    {
        databaseCalendarReference.child(stringDateSelected).get().addOnSuccessListener {
                if(it.exists())
                {
                    retrievedNoteData = it.child("noteData").value.toString()
                    retrievedWorkoutData = convertWorkoutData(it.child("workoutData").value as List<HashMap<String, Any?>>)

                    Log.d(TAG, "retrieveData: ${retrievedNoteData} (WorkoutCalendar)")
                    Log.d(TAG, "retrieveData: ${retrievedWorkoutData} (WorkoutCalendar)")

//                    adapter = WorkoutAdapter(currentWorkoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
//                    binding.logsRecyclerViewWorkoutList.adapter = adapter
                }

                else
                {
//                    binding.logsEditTextNote.setText("")
//                    adapter = WorkoutAdapter(defalutWorkoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
//                    binding.logsRecyclerViewWorkoutList.adapter = adapter

                    Log.d(TAG, "retrieveData: Could not retrieve any data (WorkoutCalendar)")

                }
        }

        databaseWorkoutTypeReference.get().addOnSuccessListener {

            if(it.exists())
            {
                Log.d(TAG, "retrieveData: ${it.value} (WorkoutType)")
            }

            else
            {
                Log.d(TAG, "retrieveData: Could not retrieve any data (WorkoutType)")
            }

        }
    }

    fun convertWorkoutData(retrievedData: List<HashMap<String, Any?>>): MutableList<ItemWorkout>
    {
        var tempWorkoutList = mutableListOf<ItemWorkout>()

        for(workout in retrievedData) {
            val item = ItemWorkout("", 0f)
            item.setData(workout)
            tempWorkoutList.add(item)
        }

        return tempWorkoutList
    }


}