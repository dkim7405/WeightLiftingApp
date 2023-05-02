package com.example.app2

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class WorkoutAdapter(val dataSet: MutableList<ItemWorkout>, dateSelected: String, noteData: String) :
    RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    val databaseCalendarReference = FirebaseDatabase.getInstance().getReference("Calendar")
    val databaseWorkoutTypeReference = FirebaseDatabase.getInstance().getReference("Workout Type")
    val onSelectedDate = dateSelected
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var workoutName: TextView
        var workoutInput: TextView
        val deleteButton: ImageButton

        init {
            // Define click listener for the ViewHolder's View
            workoutName = view.findViewById(R.id.item_textView_workoutName)
            workoutInput = view.findViewById(R.id.item_textView_numberWorkout)
            deleteButton = view.findViewById(R.id.item_imageButton_delete)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_workouts, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val context = viewHolder.workoutName.context
        viewHolder.workoutName.text = "${dataSet[position].workoutType}:"
        viewHolder.workoutInput.setText("${dataSet[position].inputWorkout} lbs")

        viewHolder.deleteButton.setOnClickListener {
            dataSet.removeAt(position)
            notifyDataSetChanged()
            deleteWorkoutData(position)
        }

        viewHolder.workoutName.setOnClickListener{
            openEditDialog(context, viewHolder, position)
        }

        viewHolder.workoutInput.setOnClickListener {
            openEditDialog(context, viewHolder, position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun openEditDialog(context: Context, viewHolder: ViewHolder, position: Int)
    {
        val layoutInflaterAdapter = LayoutInflater.from(context)
        val customLayout = layoutInflaterAdapter.inflate(R.layout.layout_dialog, null)
        var addWorkoutWeight = customLayout.findViewById<EditText>(R.id.addWorkoutDialog_editText_weight)
        var addWorkoutName = customLayout.findViewById<EditText>(R.id.addWorkoutDialog_editText_workoutName)
        addWorkoutName.setText(viewHolder.workoutName.text.dropLast(1))
        addWorkoutWeight.setText(viewHolder.workoutInput.text.dropLast(4))
        val dialog =  AlertDialog.Builder(context, R.style.AddDialogTheme)
            .setTitle("Edit Workout")
            .setView(customLayout)
            .setPositiveButton("OK",  DialogInterface.OnClickListener() { dialogInterface, i ->
                viewHolder.workoutName.text = "${addWorkoutName.text}:"
                viewHolder.workoutInput.text = "${addWorkoutWeight.text.toString().toFloat()} lbs"
                updateWorkoutData()
            })
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    fun updateWorkoutData()
    {
        var updateCalendarItem = hashMapOf<String, Any>()
        for(i in dataSet)
        {
            updateCalendarItem.put(i.workoutType, i.inputWorkout)
        }

        databaseCalendarReference.child(onSelectedDate).child("workoutData").updateChildren(updateCalendarItem)
        //databaseWorkoutTypeReference.updateChildren()

    }

    fun deleteWorkoutData(workoutPosition: Int)
    {
        databaseCalendarReference.child(onSelectedDate).child("workoutData").child(workoutPosition.toString()).removeValue()
        databaseCalendarReference.child(onSelectedDate).child("workoutData").setValue(dataSet)

    }
}