package com.example.app2.buttons

import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app2.*
import com.example.app2.R
import com.example.app2.databinding.ActivityLogsBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

class LogsActivity : AppCompatActivity() {

    companion object{
        val TAG = "Debugging"
    }

    private lateinit var binding: ActivityLogsBinding
    private lateinit var adapter : WorkoutAdapter

    private lateinit var databaseCalendarReference: DatabaseReference
    private lateinit var databaseWorkoutTypeReference: DatabaseReference
    private lateinit var databaseGraphReference: DatabaseReference

    private lateinit var saveLogData: DataLog
    private var retrievedNoteData : String = ""
    private var retrievedWorkoutData : MutableList<ItemWorkout> = mutableListOf()

    private lateinit var stringDateSelected: String
    private lateinit var todayDate: Date
    private lateinit var dateSelected: Date
    private lateinit var calendarFormat : SimpleDateFormat


    private lateinit var workoutList: MutableList<ItemWorkout>
    private lateinit var workoutTypeList: MutableList<String>

    private lateinit var addDialogName: String
    private var addDialogWeight = 0f

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

        todayDate = Calendar.getInstance().time

        calendarFormat = SimpleDateFormat("yyyy-M-d")
        stringDateSelected = calendarFormat.format(binding.logsCalendarViewCalendar.date)

        binding.logsRecyclerViewWorkoutList.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutAdapter(workoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
        binding.logsRecyclerViewWorkoutList.adapter = adapter

        databaseCalendarReference.child(stringDateSelected).get().addOnSuccessListener {
            if (it.exists()) {
                retrieveCalendarData(it)

                workoutList = retrievedWorkoutData
                binding.logsEditTextNote.setText(retrievedNoteData)

                adapter = WorkoutAdapter(workoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
                binding.logsRecyclerViewWorkoutList.adapter = adapter

                Log.d(TAG, "retrieveData: ${workoutTypeList}")
                Log.d(TAG, "retrieveData: hello/ ${workoutList}")
            }

            else {
                binding.logsEditTextNote.setText("")
                adapter = WorkoutAdapter(workoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
                binding.logsRecyclerViewWorkoutList.adapter = adapter

                Log.d(TAG, "retrieveData: Could not retrieve any data (WorkoutCalendar)")

            }
        }

        binding.logsImageButtonBack.setOnClickListener {
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
        }

        binding.logsCalendarViewCalendar.setOnDateChangeListener { calendarView, i1, i2, i3 ->
            stringDateSelected = "${i1}-${i2+1}-${i3}"

            var tempCalendar = Calendar.getInstance()
            tempCalendar.set(Calendar.YEAR, i1)
            tempCalendar.set(Calendar.MONTH, i2)
            tempCalendar.set(Calendar.DATE, i3)
            
            var dateList = mutableListOf<Date>()

            dateSelected = tempCalendar.time

            databaseCalendarReference.get().addOnSuccessListener {
                if(it.exists())
                {
                    databaseCalendarReference.addListenerForSingleValueEvent(object: ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            snapshot.children.forEach{
                                dateList.add(calendarFormat.parse(it.key.toString())!!)
                            }

                            var previousData = getClosestPreviousDate(dateList, dateSelected)
                            Log.d(TAG, "onDataChange: ${previousData}")

                            if(previousData == null)
                            {
                                Log.d(TAG, "onDataChange: Not occuring")
                                workoutList = mutableListOf()

                                for(i in workoutTypeList) {
                                    workoutList.add(ItemWorkout(i, 0f))
                                }

                                adapter = WorkoutAdapter(workoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
                                binding.logsRecyclerViewWorkoutList.adapter = adapter
                            }
                            else
                            {
                                callPreviousDate(dateList, dateSelected)
                                Log.d(TAG, "onDataChange: occuring")
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "onCancelled: $error")
                        }

                    })
                }

            }


        }

        binding.logsImageButtonAddWorkout.setOnClickListener {
            openAddDialog()
        }

    }

    fun openAddDialog()
    {
        val customLayout = layoutInflater.inflate(R.layout.layout_dialog, null)
        var addWorkoutWeight = customLayout.findViewById<EditText>(R.id.addWorkoutDialog_editText_weight)
        var addWorkoutName = customLayout.findViewById<EditText>(R.id.addWorkoutDialog_editText_workoutName)
        val dialog =  AlertDialog.Builder(this, R.style.AddDialogTheme)
            .setTitle("Add Workout")
            .setView(customLayout)
            .setPositiveButton("OK",  DialogInterface.OnClickListener { dialogInterface, i ->
                    if(!(addWorkoutName.text.toString() == "" || addWorkoutWeight.text.toString() == "")) {
                        addDialogName = addWorkoutName.text.toString()
                        addDialogWeight = addWorkoutWeight.text.toString().toFloat()
                        adapter.dataSet.add(ItemWorkout(addDialogName, addDialogWeight))
                        adapter.notifyDataSetChanged()

                        if(!workoutTypeList.contains(addWorkoutName.text.toString()))
                        {
                            workoutTypeList.add(addWorkoutName.text.toString())
                        }

                        addData(DataLog(workoutList, binding.logsEditTextNote.text.toString()), workoutTypeList)
                    }
                })
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }


    fun addData(loggingData: DataLog, workoutTypes: List<String>)
    {
        databaseCalendarReference.child(stringDateSelected).setValue(loggingData)
        databaseWorkoutTypeReference.setValue(workoutTypes)
    }

    fun retrieveCalendarData(it: DataSnapshot)
    {

        retrievedNoteData = it.child("noteData").value.toString()
        retrievedWorkoutData = convertWorkoutData(it.child("workoutData").value as List<HashMap<String, Any?>>)

        Log.d(TAG, "retrieveData: $retrievedNoteData (WorkoutCalendar)")
        Log.d(TAG, "retrieveData: $retrievedWorkoutData (WorkoutCalendar)")

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

    fun callPreviousDate(previousDateList: MutableList<Date>, selectedDate: Date)
    {
        databaseCalendarReference.child(stringDateSelected).get().addOnSuccessListener {
            if(it.exists())
            {

                retrieveCalendarData(it)
                workoutList = retrievedWorkoutData
                binding.logsEditTextNote.setText(retrievedNoteData)

                if(todayDate < dateSelected)
                {

                    var dateWorkoutType = mutableSetOf<String>()

                    for (i in workoutList) {
                        dateWorkoutType.add(i.workoutType)
                    }

                    for (i in workoutTypeList) {
                        if (!dateWorkoutType.contains(i)) {
                            workoutList.add(ItemWorkout(i, 0f))
                        }
                    }
                }
            }
            else
            {

            }

            adapter = WorkoutAdapter(workoutList, stringDateSelected, binding.logsEditTextNote.text.toString())
            binding.logsRecyclerViewWorkoutList.adapter = adapter
        }
    }

    fun getClosestPreviousDate(dates: MutableList<Date>, selectedDate: Date): Date? {

        var minDateCount = Int.MAX_VALUE;
        var result : Date? = null;

        dates.forEach {
            Log.d(TAG, "it: $it")
            Log.d(TAG, "selectedDate: $selectedDate")
            Log.d(TAG, "getClosestPreviousDate: ${it.compareTo(selectedDate)}")
            if(it.compareTo(selectedDate) < 0 && abs(it.compareTo(selectedDate)) < minDateCount)
            {
                
                minDateCount = abs(it.compareTo(selectedDate))
                result = it
            }
        }

        return result;
    }


}