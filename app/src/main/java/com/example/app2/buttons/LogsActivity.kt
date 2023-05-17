package com.example.app2.buttons

import android.content.ClipData
import android.content.ClipData.Item
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
        val TAG = "Debugging"  // Tag for logging messages
    }

    // Views
    private lateinit var binding: ActivityLogsBinding  // View binding for activity_logs.xml

    // Adapter
    private lateinit var adapter : WorkoutAdapter  // Adapter for recycler view

    // Firebase References
    private lateinit var databaseCalendarReference: DatabaseReference  // Reference to "Calendar" node in Firebase database
    private lateinit var databaseWorkoutTypeReference: DatabaseReference  // Reference to "Workout Type" node in Firebase database

    // Data Variables
    private var retrievedNoteData : String = ""  // Variable to store note data retrieved from Firebase
    private var retrievedWorkoutData : MutableList<ItemWorkout> = mutableListOf()  // Variable to store workout data retrieved from Firebase

    private lateinit var stringDateSelected: String  // String representation of currently selected date
    private lateinit var todayDate: Date  // Today's date
    private lateinit var dateSelected: Date  // Date selected by user
    private lateinit var calendarFormat : SimpleDateFormat  // Format for date string

    private lateinit var workoutList: MutableList<ItemWorkout>  // List of workout items
    private lateinit var workoutTypeList: MutableList<String>  // List of workout types

    // Dialog Variables
    private lateinit var addDialogName: String  // Name of workout added using dialog
    private var addDialogWeight = 0f  // Weight of workout added using dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View initialization
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Firebase initialization
        databaseCalendarReference = FirebaseDatabase.getInstance().getReference("Calendar")
        databaseWorkoutTypeReference = FirebaseDatabase.getInstance().getReference("Workout Type")

        // Data initialization
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

        //Date initialization
        todayDate = Calendar.getInstance().time
        calendarFormat = SimpleDateFormat("yyyy-M-d")
        stringDateSelected = calendarFormat.format(binding.logsCalendarViewCalendar.date)

        // Recycler view initialization
        binding.logsRecyclerViewWorkoutList.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutAdapter(workoutList, workoutTypeList, stringDateSelected)
        binding.logsRecyclerViewWorkoutList.adapter = adapter

        // Retrieve workout type list from Firebase
        databaseWorkoutTypeReference.get().addOnSuccessListener {
            if(it.exists()) {
                workoutTypeList = it.value as MutableList<String>
                Log.d(TAG, "onCreate work: $workoutTypeList")
            }
        }

        // Retrieve data from Firebase
        databaseCalendarReference.child(stringDateSelected).get().addOnSuccessListener {
            //COME BACK HERE
            if(it.exists())
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
            else
            {
                databaseCalendarReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var dateList = snapshot.children.mapNotNull { calendarFormat.parse(it.key.toString()) }

                        val previousData = getClosestPreviousDate(dateList as MutableList<Date>, todayDate)

                        if (previousData == null) {
                            workoutList = workoutTypeList.map { ItemWorkout(it, 0f) }.toMutableList()
                        } else {
                            callPreviousDate(previousData)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "onCancelled: $error")
                    }
                })
            }
            adapter = WorkoutAdapter(workoutList, workoutTypeList, stringDateSelected)
            binding.logsRecyclerViewWorkoutList.adapter = adapter
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

            dateSelected = tempCalendar.time

            Log.d(TAG, "onCreate: $stringDateSelected")

            databaseCalendarReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var dateList = snapshot.children.mapNotNull { calendarFormat.parse(it.key.toString()) }

                    val previousData = getClosestPreviousDate(dateList as MutableList<Date>, dateSelected)
                    if (previousData == null) {
                        workoutList = workoutTypeList.map { ItemWorkout(it, 0f) }.toMutableList()
                        Log.d(TAG, "onDataChange, workoutList: $workoutTypeList")
                        adapter = WorkoutAdapter(workoutList, workoutTypeList, stringDateSelected)
                        binding.logsRecyclerViewWorkoutList.adapter = adapter
                    } else {
                        callPreviousDate(previousData)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: $error")
                }
            })
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

    fun callPreviousDate(prevDate: Date)
    {
        databaseCalendarReference.child(calendarFormat.format(prevDate)).get().addOnSuccessListener {
            if(it.exists())
            {
                retrieveCalendarData(it)
                workoutList = retrievedWorkoutData
                binding.logsEditTextNote.setText(retrievedNoteData)

                if(workoutTypeList.size > workoutList.size)
                {
                    var tempList: MutableList<String> = mutableListOf()
                    for(i in workoutList) {
                        tempList.add(i.workoutType)
                    }
                    for(i in workoutTypeList)
                    {
                        if(!tempList.contains(i))
                        {
                            workoutList.add(ItemWorkout(i, 0f))
                        }
                    }
                }
            }

            adapter = WorkoutAdapter(workoutList, workoutTypeList, stringDateSelected)
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

        return result
    }

}