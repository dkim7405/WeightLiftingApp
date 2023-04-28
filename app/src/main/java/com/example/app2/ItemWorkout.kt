package com.example.app2

data class ItemWorkout(var workoutType: String, var inputWorkout: Float){
    fun setData(dataMap : HashMap<String, Any?>?) {
        if(dataMap != null) {
            workoutType = (dataMap["workoutType"] ?: "") as String
            inputWorkout = try {
                ((dataMap["inputWorkout"] ?: 0L) as Long).toFloat()

            } catch (e: ClassCastException) {
                ((dataMap["inputWorkout"] ?: 0.0) as Double).toFloat()
            }
        } else {
            workoutType = "null datamap"
            inputWorkout = 0.0f
        }
    }
}
