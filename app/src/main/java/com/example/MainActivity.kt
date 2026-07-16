package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

// ============================================================================
// 1. DATA MODELS & SEED DATA
// ============================================================================

enum class Role(val id: String, val title: String, val subtitle: String, val icon: ImageVector) {
    PARENT("parent", "Parent", "Child safety & academics", Icons.Default.Favorite),
    TEACHER("teacher", "Teacher", "Attendance, exams & lessons", Icons.Default.Edit),
    ADMIN("admin", "Administrator", "School-wide operations", Icons.Default.Settings),
    CONDUCTOR("conductor", "Conductor", "Bus boarding & safety", Icons.Default.Place),
    BUS_DRIVER("bus_driver", "Bus Driver", "Route & trip management", Icons.Default.PlayArrow),
    SECURITY("security", "Security Guard", "Gate & early pickup", Icons.Default.Lock)
}

data class User(
    val username: String,
    val role: String,
    val displayName: String,
    val subtitle: String = ""
)

data class Student(
    val id: String,
    val name: String,
    val rollNo: Int,
    val admissionNo: String,
    val section: String,
    val attendancePct: Int,
    val gender: String,
    val fatherName: String,
    val motherName: String,
    val address: String,
    val phone: String = "9876543210",
    val penNo: String = "PEN982314",
    val aadharNo: String = "4512-9843-1280",
    val email: String = "archana@gmail.com",
    val uid: String = "UID-100234",
    val dateOfAdmission: String = "15/06/2021",
    val profilePic: String = ""
)

data class Homework(
    val id: String,
    val subject: String,
    val title: String,
    val description: String,
    val section: String,
    val dueDate: String,
    val assignedBy: String
)

data class Notice(
    val id: String,
    val title: String,
    val body: String,
    val category: String, // e.g., "Urgent", "Academic", "Event"
    val targetRole: String,
    val createdAt: String
)

data class Incident(
    val id: String,
    val studentName: String,
    val type: String,
    val notes: String,
    val section: String,
    val severity: String, // "High", "Medium", "Low"
    val reportedBy: String,
    val createdAt: String
)

data class ExamResult(
    val id: String,
    val studentId: String,
    val examName: String,
    val subject: String,
    val marks: Int,
    val maxMarks: Int
)

data class TimetableSlot(
    val id: String,
    val section: String,
    val dayOfWeek: String,
    val period: Int,
    val subject: String,
    val startTime: String,
    val endTime: String
)

data class PickupRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val section: String,
    val reason: String,
    val time: String,
    val status: String, // "Pending", "Approved", "Rejected"
    val visitorName: String = "Rajesh Nair",
    val relationship: String = "Guardian",
    val visitorPhoto: String = ""
)

data class VisitorLog(
    val id: String,
    val name: String,
    val purpose: String,
    val inTime: String,
    val outTime: String = ""
)

data class BusEvent(
    val id: String,
    val studentName: String,
    val action: String, // "Boarded Bus", "Reached School", "Left School", "Reached Home"
    val timestamp: String,
    val busNo: String = "Bus 1"
)

// ============================================================================
// 2. VIEWMODEL
// ============================================================================

class SchoolViewModel : ViewModel() {
    private var _currentScreen = mutableStateOf<String>("landing")
    var currentScreen: String
        get() = _currentScreen.value
        set(value) {
            _currentScreen.value = value
            saveState()
        }

    private var _selectedRole = mutableStateOf<Role?>(null)
    var selectedRole: Role?
        get() = _selectedRole.value
        set(value) {
            _selectedRole.value = value
            saveState()
        }

    private var _currentUser = mutableStateOf<User?>(null)
    var currentUser: User?
        get() = _currentUser.value
        set(value) {
            _currentUser.value = value
            saveState()
        }

    // Bus Tracking Shared State
    var busTripActive = mutableStateOf(false)
    var busRouteStatus = mutableStateOf("Idle at Yard")
    var busLatitude = mutableStateOf(8.5241)
    var busLongitude = mutableStateOf(76.9366)

    // School State Databases
    var students = mutableStateListOf<Student>()
    var attendanceMap = mutableStateMapOf<String, Boolean>() // studentId -> isPresent
    var homeworkList = mutableStateListOf<Homework>()
    var notices = mutableStateListOf<Notice>()
    var incidents = mutableStateListOf<Incident>()
    var examResults = mutableStateListOf<ExamResult>()
    var timetable = mutableStateListOf<TimetableSlot>()
    var pickupRequests = mutableStateListOf<PickupRequest>()
    var visitorLogs = mutableStateListOf<VisitorLog>()
    var busEvents = mutableStateListOf<BusEvent>()

    private var sharedPref: SharedPreferences? = null

    init {
        seedInitialData()
    }

    fun initPrefs(context: Context) {
        if (sharedPref == null) {
            sharedPref = context.applicationContext.getSharedPreferences("edushield_cache", Context.MODE_PRIVATE)
            loadState()
        }
    }

    fun saveState() {
        val prefs = sharedPref ?: return
        try {
            val editor = prefs.edit()

            editor.putString("currentScreen", currentScreen)
            editor.putString("selectedRole", selectedRole?.name)
            
            currentUser?.let { user ->
                val uObj = JSONObject().apply {
                    put("username", user.username)
                    put("role", user.role)
                    put("displayName", user.displayName)
                    put("subtitle", user.subtitle)
                }
                editor.putString("currentUser", uObj.toString())
            } ?: editor.remove("currentUser")

            val studentsArr = JSONArray().apply {
                students.forEach { s ->
                    put(JSONObject().apply {
                        put("id", s.id)
                        put("name", s.name)
                        put("rollNo", s.rollNo)
                        put("admissionNo", s.admissionNo)
                        put("section", s.section)
                        put("attendancePct", s.attendancePct)
                        put("gender", s.gender)
                        put("fatherName", s.fatherName)
                        put("motherName", s.motherName)
                        put("address", s.address)
                        put("phone", s.phone)
                        put("penNo", s.penNo)
                        put("aadharNo", s.aadharNo)
                        put("email", s.email)
                        put("uid", s.uid)
                        put("dateOfAdmission", s.dateOfAdmission)
                        put("profilePic", s.profilePic)
                    })
                }
            }
            editor.putString("students", studentsArr.toString())

            val attObj = JSONObject().apply {
                attendanceMap.forEach { (k, v) ->
                    put(k, v)
                }
            }
            editor.putString("attendanceMap", attObj.toString())

            val hwArr = JSONArray().apply {
                homeworkList.forEach { hw ->
                    put(JSONObject().apply {
                        put("id", hw.id)
                        put("subject", hw.subject)
                        put("title", hw.title)
                        put("description", hw.description)
                        put("section", hw.section)
                        put("dueDate", hw.dueDate)
                        put("assignedBy", hw.assignedBy)
                    })
                }
            }
            editor.putString("homeworkList", hwArr.toString())

            val noticesArr = JSONArray().apply {
                notices.forEach { n ->
                    put(JSONObject().apply {
                        put("id", n.id)
                        put("title", n.title)
                        put("body", n.body)
                        put("category", n.category)
                        put("targetRole", n.targetRole)
                        put("createdAt", n.createdAt)
                    })
                }
            }
            editor.putString("notices", noticesArr.toString())

            val incidentsArr = JSONArray().apply {
                incidents.forEach { inc ->
                    put(JSONObject().apply {
                        put("id", inc.id)
                        put("studentName", inc.studentName)
                        put("type", inc.type)
                        put("notes", inc.notes)
                        put("section", inc.section)
                        put("severity", inc.severity)
                        put("reportedBy", inc.reportedBy)
                        put("createdAt", inc.createdAt)
                    })
                }
            }
            editor.putString("incidents", incidentsArr.toString())

            val examResultsArr = JSONArray().apply {
                examResults.forEach { er ->
                    put(JSONObject().apply {
                        put("id", er.id)
                        put("studentId", er.studentId)
                        put("examName", er.examName)
                        put("subject", er.subject)
                        put("marks", er.marks)
                        put("maxMarks", er.maxMarks)
                    })
                }
            }
            editor.putString("examResults", examResultsArr.toString())

            val timetableArr = JSONArray().apply {
                timetable.forEach { tt ->
                    put(JSONObject().apply {
                        put("id", tt.id)
                        put("section", tt.section)
                        put("dayOfWeek", tt.dayOfWeek)
                        put("period", tt.period)
                        put("subject", tt.subject)
                        put("startTime", tt.startTime)
                        put("endTime", tt.endTime)
                    })
                }
            }
            editor.putString("timetable", timetableArr.toString())

            val pickupRequestsArr = JSONArray().apply {
                pickupRequests.forEach { pr ->
                    put(JSONObject().apply {
                        put("id", pr.id)
                        put("studentId", pr.studentId)
                        put("studentName", pr.studentName)
                        put("section", pr.section)
                        put("reason", pr.reason)
                        put("time", pr.time)
                        put("status", pr.status)
                        put("visitorName", pr.visitorName)
                        put("relationship", pr.relationship)
                        put("visitorPhoto", pr.visitorPhoto)
                    })
                }
            }
            editor.putString("pickupRequests", pickupRequestsArr.toString())

            val visitorLogsArr = JSONArray().apply {
                visitorLogs.forEach { vl ->
                    put(JSONObject().apply {
                        put("id", vl.id)
                        put("name", vl.name)
                        put("purpose", vl.purpose)
                        put("inTime", vl.inTime)
                        put("outTime", vl.outTime)
                    })
                }
            }
            editor.putString("visitorLogs", visitorLogsArr.toString())

            val busEventsArr = JSONArray().apply {
                busEvents.forEach { be ->
                    put(JSONObject().apply {
                        put("id", be.id)
                        put("studentName", be.studentName)
                        put("action", be.action)
                        put("timestamp", be.timestamp)
                        put("busNo", be.busNo)
                    })
                }
            }
            editor.putString("busEvents", busEventsArr.toString())

            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadState() {
        val prefs = sharedPref ?: return
        try {
            val savedScreen = prefs.getString("currentScreen", null)
            if (savedScreen != null) {
                currentScreen = savedScreen
            }
            
            val savedRoleStr = prefs.getString("selectedRole", null)
            if (savedRoleStr != null) {
                selectedRole = try { Role.valueOf(savedRoleStr) } catch(e: Exception) { null }
            }

            val savedUserStr = prefs.getString("currentUser", null)
            if (savedUserStr != null) {
                val uObj = JSONObject(savedUserStr)
                currentUser = User(
                    uObj.getString("username"),
                    uObj.getString("role"),
                    uObj.getString("displayName"),
                    uObj.optString("subtitle", "")
                )
            }

            val savedStudents = prefs.getString("students", null)
            if (savedStudents != null) {
                val arr = JSONArray(savedStudents)
                students.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    students.add(
                        Student(
                            o.getString("id"),
                            o.getString("name"),
                            o.getInt("rollNo"),
                            o.getString("admissionNo"),
                            o.getString("section"),
                            o.getInt("attendancePct"),
                            o.getString("gender"),
                            o.getString("fatherName"),
                            o.getString("motherName"),
                            o.getString("address"),
                            o.optString("phone", "9876543210"),
                            o.optString("penNo", "PEN982314"),
                            o.optString("aadharNo", "4512-9843-1280"),
                            o.optString("email", "archana@gmail.com"),
                            o.optString("uid", "UID-100234"),
                            o.optString("dateOfAdmission", "15/06/2021"),
                            o.optString("profilePic", "")
                        )
                    )
                }
            }

            val savedAttendance = prefs.getString("attendanceMap", null)
            if (savedAttendance != null) {
                val o = JSONObject(savedAttendance)
                attendanceMap.clear()
                o.keys().forEach { k ->
                    attendanceMap[k] = o.getBoolean(k)
                }
            }

            val savedHw = prefs.getString("homeworkList", null)
            if (savedHw != null) {
                val arr = JSONArray(savedHw)
                homeworkList.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    homeworkList.add(
                        Homework(
                            o.getString("id"),
                            o.getString("subject"),
                            o.getString("title"),
                            o.getString("description"),
                            o.getString("section"),
                            o.getString("dueDate"),
                            o.getString("assignedBy")
                        )
                    )
                }
            }

            val savedNotices = prefs.getString("notices", null)
            if (savedNotices != null) {
                val arr = JSONArray(savedNotices)
                notices.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    notices.add(
                        Notice(
                            o.getString("id"),
                            o.getString("title"),
                            o.getString("body"),
                            o.getString("category"),
                            o.getString("targetRole"),
                            o.getString("createdAt")
                        )
                    )
                }
            }

            val savedIncidents = prefs.getString("incidents", null)
            if (savedIncidents != null) {
                val arr = JSONArray(savedIncidents)
                incidents.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    incidents.add(
                        Incident(
                            o.getString("id"),
                            o.getString("studentName"),
                            o.getString("type"),
                            o.getString("notes"),
                            o.getString("section"),
                            o.getString("severity"),
                            o.getString("reportedBy"),
                            o.getString("createdAt")
                        )
                    )
                }
            }

            val savedExamResults = prefs.getString("examResults", null)
            if (savedExamResults != null) {
                val arr = JSONArray(savedExamResults)
                examResults.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    examResults.add(
                        ExamResult(
                            o.getString("id"),
                            o.getString("studentId"),
                            o.getString("examName"),
                            o.getString("subject"),
                            o.getInt("marks"),
                            o.getInt("maxMarks")
                        )
                    )
                }
            }

            val savedTimetable = prefs.getString("timetable", null)
            if (savedTimetable != null) {
                val arr = JSONArray(savedTimetable)
                timetable.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    timetable.add(
                        TimetableSlot(
                            o.getString("id"),
                            o.getString("section"),
                            o.getString("dayOfWeek"),
                            o.getInt("period"),
                            o.getString("subject"),
                            o.getString("startTime"),
                            o.getString("endTime")
                        )
                    )
                }
            }

            val savedPickup = prefs.getString("pickupRequests", null)
            if (savedPickup != null) {
                val arr = JSONArray(savedPickup)
                pickupRequests.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    pickupRequests.add(
                        PickupRequest(
                            o.getString("id"),
                            o.getString("studentId"),
                            o.getString("studentName"),
                            o.getString("section"),
                            o.getString("reason"),
                            o.getString("time"),
                            o.getString("status"),
                            o.optString("visitorName", "Rajesh Nair"),
                            o.optString("relationship", "Guardian"),
                            o.optString("visitorPhoto", "")
                        )
                    )
                }
            }

            val savedVisitor = prefs.getString("visitorLogs", null)
            if (savedVisitor != null) {
                val arr = JSONArray(savedVisitor)
                visitorLogs.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    visitorLogs.add(
                        VisitorLog(
                            o.getString("id"),
                            o.getString("name"),
                            o.getString("purpose"),
                            o.getString("inTime"),
                            o.optString("outTime", "")
                        )
                    )
                }
            }

            val savedBus = prefs.getString("busEvents", null)
            if (savedBus != null) {
                val arr = JSONArray(savedBus)
                busEvents.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    busEvents.add(
                        BusEvent(
                            o.getString("id"),
                            o.getString("studentName"),
                            o.getString("action"),
                            o.getString("timestamp"),
                            o.optString("busNo", "Bus 1")
                        )
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seedInitialData() {
        // Students
        students.addAll(
            listOf(
                Student("s1", "ARCHANA S", 1, "271808221006008", "Class 10A", 94, "Female", "SUNDAR S", "MEENA S", "House 12, Sector 4, Trivandrum", "9876543210", "PEN982314", "4512-9843-1280", "archana@gmail.com", "UID-100234", "15/06/2021", "avatar1"),
                Student("s2", "ESHITA K S", 2, "271808221006126", "Class 10A", 88, "Female", "KRISHNA K", "SUNITA K", "House 45, Sector 8, Trivandrum", "9447102030", "PEN120934", "9823-1402-9831", "eshita@gmail.com", "UID-102943", "20/06/2021", "avatar2"),
                Student("s3", "ADITHYAN P", 3, "271808221006180", "Class 10A", 92, "Male", "PARAMESH S", "LATHA P", "House 77, Pattom, Trivandrum", "9048123456", "PEN883210", "7712-4091-8823", "adithyan@gmail.com", "UID-330412", "18/06/2021", ""),
                Student("s4", "GAUTHAM S", 4, "271808221006200", "Class 10A", 85, "Male", "SURESH KUMAR", "MINI S", "House 102, Peroorkada, Trivandrum", "9562410293", "PEN440931", "1102-9843-7721", "gautham@gmail.com", "UID-209841", "12/06/2021", ""),
                Student("s5", "NIVEDITHA R", 5, "271808221006310", "Class 10A", 96, "Female", "RAVEENDRAN G", "SOBHA R", "House 5, Kowdiar, Trivandrum", "9845120394", "PEN559021", "3324-9812-4402", "niveditha@gmail.com", "UID-409183", "21/06/2021", "avatar3"),
Student("s_10c_1", "ARCHANA S", 1, "271808221006008", "Class 10C", 83, "Male", "ARCHANA S", "ARCHANA DEVI", "House 1, Sector 2, Trivandrum"),
                Student("s_10c_2", "ESHITA K S", 2, "271808221006126", "Class 10C", 84, "Male", "ESHITA S", "ESHITA DEVI", "House 2, Sector 3, Trivandrum"),
                Student("s_10c_3", "MAHESWARAN RANJITH", 3, "271808222006451", "Class 10C", 85, "Male", "MAHESWARAN S", "MAHESWARAN DEVI", "House 3, Sector 4, Trivandrum"),
                Student("s_10c_4", "AMANYA M S", 4, "271808222006435", "Class 10C", 86, "Male", "AMANYA S", "AMANYA DEVI", "House 4, Sector 5, Trivandrum"),
                Student("s_10c_5", "SHRAVAN SANDEEP SHELAR", 5, "271808223006748", "Class 10C", 87, "Male", "SHRAVAN S", "SHRAVAN DEVI", "House 5, Sector 6, Trivandrum"),
                Student("s_10c_6", "KEERTHANA C A", 6, "271808224007072", "Class 10C", 88, "Female", "KEERTHANA S", "KEERTHANA DEVI", "House 6, Sector 7, Trivandrum"),
                Student("s_10c_7", "SREEDATH P", 7, "271808224007157", "Class 10C", 89, "Male", "SREEDATH S", "SREEDATH DEVI", "House 7, Sector 8, Trivandrum"),
                Student("s_10c_8", "SUBHRANSHU SEKHAR MAHAKHUD", 8, "271808225007477", "Class 10C", 90, "Male", "SUBHRANSHU S", "SUBHRANSHU DEVI", "House 8, Sector 9, Trivandrum"),
                Student("s_10c_9", "AKASH KRISHNA A S", 9, "271808225007345", "Class 10C", 91, "Male", "AKASH S", "AKASH DEVI", "House 9, Sector 10, Trivandrum"),
                Student("s_10c_10", "LEKSHMIPRIYA M S", 10, "271808225007312", "Class 10C", 92, "Male", "LEKSHMIPRIYA S", "LEKSHMIPRIYA DEVI", "House 10, Sector 1, Trivandrum"),
                Student("s_10c_11", "ADWAIDH S R", 11, "271808219005356", "Class 10C", 93, "Male", "ADWAIDH S", "ADWAIDH DEVI", "House 11, Sector 2, Trivandrum"),
                Student("s_10c_12", "S DHANUSHKA", 12, "271808219005429", "Class 10C", 94, "Female", "S S", "S DEVI", "House 12, Sector 3, Trivandrum"),
                Student("s_10c_13", "GIYA BLAZE", 13, "271808219005447", "Class 10C", 95, "Female", "GIYA S", "GIYA DEVI", "House 13, Sector 4, Trivandrum"),
                Student("s_10c_14", "ESWAR NARAYAN", 14, "271808217004653", "Class 10C", 96, "Male", "ESWAR S", "ESWAR DEVI", "House 14, Sector 5, Trivandrum"),
                Student("s_10c_15", "VAIGA N R", 15, "271808218004938", "Class 10C", 82, "Male", "VAIGA S", "VAIGA DEVI", "House 15, Sector 6, Trivandrum"),
                Student("s_10c_16", "ANUVINDHA D SUKUMAR", 16, "271808218004985", "Class 10C", 83, "Male", "ANUVINDHA S", "ANUVINDHA DEVI", "House 16, Sector 7, Trivandrum"),
                Student("s_10c_17", "S S AKSHARA", 17, "271808218004983", "Class 10C", 84, "Female", "S S", "S DEVI", "House 17, Sector 8, Trivandrum"),
                Student("s_10c_18", "ARDHRA SANTHOSH", 18, "271808218004971", "Class 10C", 85, "Male", "ARDHRA S", "ARDHRA DEVI", "House 18, Sector 9, Trivandrum"),
                Student("s_10c_19", "SIVAGANGA N S", 19, "271808217004350", "Class 10C", 86, "Male", "SIVAGANGA S", "SIVAGANGA DEVI", "House 19, Sector 10, Trivandrum"),
                Student("s_10c_20", "SANA D S", 20, "271808217004351", "Class 10C", 87, "Male", "SANA S", "SANA DEVI", "House 20, Sector 1, Trivandrum"),
                Student("s_10c_21", "DEVIKA.A.P", 21, "271808217004360", "Class 10C", 88, "Female", "DEVIKA.A.P S", "DEVIKA.A.P DEVI", "House 21, Sector 2, Trivandrum"),
                Student("s_10c_22", "VEDHA SHRI", 22, "271808217004362", "Class 10C", 89, "Female", "VEDHA S", "VEDHA DEVI", "House 22, Sector 3, Trivandrum"),
                Student("s_10c_23", "SURYADEV S S", 23, "271808217004369", "Class 10C", 90, "Male", "SURYADEV S", "SURYADEV DEVI", "House 23, Sector 4, Trivandrum"),
                Student("s_10c_24", "OLIVIA.S", 24, "271808217004370", "Class 10C", 91, "Female", "OLIVIA.S S", "OLIVIA.S DEVI", "House 24, Sector 5, Trivandrum"),
                Student("s_10c_25", "MANORANJINI M R", 25, "271808217004371", "Class 10C", 92, "Male", "MANORANJINI S", "MANORANJINI DEVI", "House 25, Sector 6, Trivandrum"),
                Student("s_10c_26", "AATHIF MUHAMMED S", 26, "271808217004373", "Class 10C", 93, "Male", "AATHIF S", "AATHIF DEVI", "House 26, Sector 7, Trivandrum"),
                Student("s_10c_27", "DEVIKA S S", 27, "271808217004375", "Class 10C", 94, "Female", "DEVIKA S", "DEVIKA DEVI", "House 27, Sector 8, Trivandrum"),
                Student("s_10c_28", "ABHINAV RAJESH", 28, "271808217004381", "Class 10C", 95, "Male", "ABHINAV S", "ABHINAV DEVI", "House 28, Sector 9, Trivandrum"),
                Student("s_10c_29", "HARSHITH S KURUP", 29, "271808217004412", "Class 10C", 96, "Male", "HARSHITH S", "HARSHITH DEVI", "House 29, Sector 10, Trivandrum"),
                Student("s_10c_30", "DHIYA GRACE AJISH", 30, "271808217004418", "Class 10C", 82, "Female", "DHIYA S", "DHIYA DEVI", "House 30, Sector 1, Trivandrum"),
                Student("s_10c_31", "AARDHRA T S", 31, "271808217004424", "Class 10C", 83, "Male", "AARDHRA S", "AARDHRA DEVI", "House 31, Sector 2, Trivandrum"),
                Student("s_10c_32", "HASNA.S", 32, "271808217004364", "Class 10C", 84, "Male", "HASNA.S S", "HASNA.S DEVI", "House 32, Sector 3, Trivandrum"),
                Student("s_10c_33", "MITHRA A S", 33, "271808217004426", "Class 10C", 85, "Male", "MITHRA S", "MITHRA DEVI", "House 33, Sector 4, Trivandrum"),
                Student("s_10c_34", "DIYA P", 34, "271808217004519", "Class 10C", 86, "Male", "DIYA S", "DIYA DEVI", "House 34, Sector 5, Trivandrum"),
                Student("s_10c_35", "GOKUL G", 35, "271808217004494", "Class 10C", 87, "Male", "GOKUL S", "GOKUL DEVI", "House 35, Sector 6, Trivandrum"),
                Student("s_10c_36", "R.MEGHNA", 36, "271808217004468", "Class 10C", 88, "Female", "R.MEGHNA S", "R.MEGHNA DEVI", "House 36, Sector 7, Trivandrum"),
                Student("s_10c_37", "GOVIND M.R.", 37, "271808217004474", "Class 10C", 89, "Male", "GOVIND S", "GOVIND DEVI", "House 37, Sector 8, Trivandrum"),
                Student("s_10c_38", "AMIYA ANNA BINUSH", 38, "271808217004566", "Class 10C", 90, "Female", "AMIYA S", "AMIYA DEVI", "House 38, Sector 9, Trivandrum"),
                Student("s_10c_39", "SHIGA PRASAD", 39, "271808217004563", "Class 10C", 91, "Male", "SHIGA S", "SHIGA DEVI", "House 39, Sector 10, Trivandrum"),
                Student("s_10c_40", "SAI VARSHA S J", 40, "271808217004615", "Class 10C", 92, "Male", "SAI S", "SAI DEVI", "House 40, Sector 1, Trivandrum"),
                Student("s_10c_41", "AARABHI S S", 41, "271808217004640", "Class 10C", 93, "Male", "AARABHI S", "AARABHI DEVI", "House 41, Sector 2, Trivandrum"),
                Student("s_10c_42", "LISHO SANTHOSH", 42, "271808217004644", "Class 10C", 94, "Male", "LISHO S", "LISHO DEVI", "House 42, Sector 3, Trivandrum"),
                Student("s_10c_43", "RACHEL L JOHNSON", 43, "271808217004646", "Class 10C", 95, "Male", "RACHEL S", "RACHEL DEVI", "House 43, Sector 4, Trivandrum"),
                Student("s_10c_44", "ABHITH ABHILASH", 44, "271808217004671", "Class 10C", 96, "Male", "ABHITH S", "ABHITH DEVI", "House 44, Sector 5, Trivandrum"),
                Student("s_10c_45", "ANAGHA T S", 45, "271808217004674", "Class 10C", 82, "Male", "ANAGHA S", "ANAGHA DEVI", "House 45, Sector 6, Trivandrum"),
                Student("s_10c_46", "REMETA YESUDAS", 46, "271808217004678", "Class 10C", 83, "Male", "REMETA S", "REMETA DEVI", "House 46, Sector 7, Trivandrum"),
                Student("s_10c_47", "M.S.VIHAN", 47, "271808216004059", "Class 10C", 84, "Male", "M.S.VIHAN S", "M.S.VIHAN DEVI", "House 47, Sector 8, Trivandrum"),
                Student("s_10c_48", "NEELAMBARI T", 48, "271808226007780", "Class 10C", 85, "Female", "NEELAMBARI S", "NEELAMBARI DEVI", "House 48, Sector 9, Trivandrum"),
                Student("s_11a_1", "DEVANANDA D S", 1, "271808221006122", "Class 11A", 83, "Male", "DEVANANDA S", "DEVANANDA DEVI", "House 1, Sector 2, Trivandrum"),
                Student("s_11a_2", "PRATIK R S", 2, "271808221006058", "Class 11A", 84, "Male", "PRATIK S", "PRATIK DEVI", "House 2, Sector 3, Trivandrum"),
                Student("s_11a_3", "SIDHARTH S", 3, "271808220005756", "Class 11A", 85, "Male", "SIDHARTH S", "SIDHARTH DEVI", "House 3, Sector 4, Trivandrum"),
                Student("s_11a_4", "VYGA JEEVAN", 4, "271808220005707", "Class 11A", 86, "Male", "VYGA S", "VYGA DEVI", "House 4, Sector 5, Trivandrum"),
                Student("s_11a_5", "NILA PRASANTH R", 5, "271808220005593", "Class 11A", 87, "Male", "NILA S", "NILA DEVI", "House 5, Sector 6, Trivandrum"),
                Student("s_11a_6", "ANAND R SHENOY", 6, "271808222006484", "Class 11A", 88, "Male", "ANAND S", "ANAND DEVI", "House 6, Sector 7, Trivandrum"),
                Student("s_11a_7", "S R KRITIKA", 7, "271808223006647", "Class 11A", 89, "Female", "S S", "S DEVI", "House 7, Sector 8, Trivandrum"),
                Student("s_11a_8", "SAHIL ASHARAF", 8, "271808222006412", "Class 11A", 90, "Male", "SAHIL S", "SAHIL DEVI", "House 8, Sector 9, Trivandrum"),
                Student("s_11a_9", "SHOBHIT KUMAR", 9, "271808222006445", "Class 11A", 91, "Male", "SHOBHIT S", "SHOBHIT DEVI", "House 9, Sector 10, Trivandrum"),
                Student("s_11a_10", "VEDHA RATHEESH", 10, "271808223006777", "Class 11A", 92, "Male", "VEDHA S", "VEDHA DEVI", "House 10, Sector 1, Trivandrum"),
                Student("s_11a_11", "ASHWIN AJITH", 11, "271808223006758", "Class 11A", 93, "Male", "ASHWIN S", "ASHWIN DEVI", "House 11, Sector 2, Trivandrum"),
                Student("s_11a_12", "PARVATHY S", 12, "271808223006823", "Class 11A", 94, "Male", "PARVATHY S", "PARVATHY DEVI", "House 12, Sector 3, Trivandrum"),
                Student("s_11a_13", "S PAVITHRA", 13, "271808223006832", "Class 11A", 95, "Female", "S S", "S DEVI", "House 13, Sector 4, Trivandrum"),
                Student("s_11a_14", "ANUBHUTI SINGH", 14, "271808224007034", "Class 11A", 96, "Male", "ANUBHUTI S", "ANUBHUTI DEVI", "House 14, Sector 5, Trivandrum"),
                Student("s_11a_15", "G DHARSSHINI", 15, "271808224007093", "Class 11A", 82, "Female", "G S", "G DEVI", "House 15, Sector 6, Trivandrum"),
                Student("s_11a_16", "ABHINAV KRISHNA M", 16, "271808225007418", "Class 11A", 83, "Male", "ABHINAV S", "ABHINAV DEVI", "House 16, Sector 7, Trivandrum"),
                Student("s_11a_17", "GOUTHAM THEJAS P", 17, "271808218004794", "Class 11A", 84, "Male", "GOUTHAM S", "GOUTHAM DEVI", "House 17, Sector 8, Trivandrum"),
                Student("s_11a_18", "JAHNVI ANIL", 18, "271808218005068", "Class 11A", 85, "Female", "JAHNVI S", "JAHNVI DEVI", "House 18, Sector 9, Trivandrum"),
                Student("s_11a_19", "NABIN MUHANNAD B", 19, "271808219005455", "Class 11A", 86, "Female", "NABIN S", "NABIN DEVI", "House 19, Sector 10, Trivandrum"),
                Student("s_11a_20", "ADITHYAN P M", 20, "271808219005364", "Class 11A", 87, "Male", "ADITHYAN S", "ADITHYAN DEVI", "House 20, Sector 1, Trivandrum"),
                Student("s_11a_21", "ADITYA RAJ.S.R", 21, "271808216004319", "Class 11A", 88, "Male", "ADITYA S", "ADITYA DEVI", "House 21, Sector 2, Trivandrum"),
                Student("s_11a_22", "AMRUTHA A RAJ", 22, "271808217004488", "Class 11A", 89, "Male", "AMRUTHA S", "AMRUTHA DEVI", "House 22, Sector 3, Trivandrum"),
                Student("s_11a_23", "BODDADA HARSHITA", 23, "271808216004300", "Class 11A", 90, "Female", "BODDADA S", "BODDADA DEVI", "House 23, Sector 4, Trivandrum"),
                Student("s_11a_24", "ADITH NARAYAN.S", 24, "271808216004301", "Class 11A", 91, "Male", "ADITH S", "ADITH DEVI", "House 24, Sector 5, Trivandrum"),
                Student("s_11a_25", "THEJA S R", 25, "271808216004247", "Class 11A", 92, "Male", "THEJA S", "THEJA DEVI", "House 25, Sector 6, Trivandrum"),
                Student("s_11a_26", "ANANTH.A", 26, "271808216004206", "Class 11A", 93, "Female", "ANANTH.A S", "ANANTH.A DEVI", "House 26, Sector 7, Trivandrum"),
                Student("s_11a_27", "LAKSHMI PRADEEP", 27, "271808216004214", "Class 11A", 94, "Male", "LAKSHMI S", "LAKSHMI DEVI", "House 27, Sector 8, Trivandrum"),
                Student("s_11a_28", "SHREYA G.S", 28, "271808216004241", "Class 11A", 95, "Male", "SHREYA S", "SHREYA DEVI", "House 28, Sector 9, Trivandrum"),
                Student("s_11a_29", "DEVIN.K.SHINE", 29, "271808216004196", "Class 11A", 96, "Male", "DEVIN.K.SHINE S", "DEVIN.K.SHINE DEVI", "House 29, Sector 10, Trivandrum"),
                Student("s_11a_30", "THANMAYA BINUKUMAR", 30, "271808216004079", "Class 11A", 82, "Male", "THANMAYA S", "THANMAYA DEVI", "House 30, Sector 1, Trivandrum"),
                Student("s_11a_31", "B.GOWRI PRARTHANA", 31, "271808216004083", "Class 11A", 83, "Female", "B.GOWRI S", "B.GOWRI DEVI", "House 31, Sector 2, Trivandrum"),
                Student("s_11a_32", "B.HARIPRASIDH", 32, "271808216004084", "Class 11A", 84, "Male", "B.HARIPRASIDH S", "B.HARIPRASIDH DEVI", "House 32, Sector 3, Trivandrum"),
                Student("s_11a_33", "MANYU M SABU", 33, "271808216004052", "Class 11A", 85, "Male", "MANYU S", "MANYU DEVI", "House 33, Sector 4, Trivandrum"),
                Student("s_11a_34", "MIDHUN.M", 34, "271808216004145", "Class 11A", 86, "Male", "MIDHUN.M S", "MIDHUN.M DEVI", "House 34, Sector 5, Trivandrum"),
                Student("s_11a_35", "VAISHNAV. K.V", 35, "271808216004162", "Class 11A", 87, "Male", "VAISHNAV. S", "VAISHNAV. DEVI", "House 35, Sector 6, Trivandrum"),
                Student("s_11a_36", "SANJAY SANTHOSH", 36, "271808216004164", "Class 11A", 88, "Male", "SANJAY S", "SANJAY DEVI", "House 36, Sector 7, Trivandrum"),
                Student("s_11a_37", "ABHINAV.T.NAIR", 37, "271808216004166", "Class 11A", 89, "Male", "ABHINAV.T.NAIR S", "ABHINAV.T.NAIR DEVI", "House 37, Sector 8, Trivandrum"),
                Student("s_11a_38", "SHIVRAM P KUMAR", 38, "271808216004169", "Class 11A", 90, "Male", "SHIVRAM S", "SHIVRAM DEVI", "House 38, Sector 9, Trivandrum"),
                Student("s_11a_39", "SIVADEV.S.S.", 39, "271808216004101", "Class 11A", 91, "Male", "SIVADEV.S.S. S", "SIVADEV.S.S. DEVI", "House 39, Sector 10, Trivandrum"),
                Student("s_11a_40", "VAGEESWARY.S.S", 40, "271808216004105", "Class 11A", 92, "Male", "VAGEESWARY.S.S S", "VAGEESWARY.S.S DEVI", "House 40, Sector 1, Trivandrum"),
                Student("s_11a_41", "BHADRA KIRAN", 41, "271808216004109", "Class 11A", 93, "Male", "BHADRA S", "BHADRA DEVI", "House 41, Sector 2, Trivandrum"),
                Student("s_11a_42", "ANSH BIJU", 42, "271808216004113", "Class 11A", 94, "Male", "ANSH S", "ANSH DEVI", "House 42, Sector 3, Trivandrum"),
                Student("s_11a_43", "VAIGA.R.KRISHNA", 43, "271808216004120", "Class 11A", 95, "Female", "VAIGA.R.KRISHNA S", "VAIGA.R.KRISHNA DEVI", "House 43, Sector 4, Trivandrum"),
                Student("s_11a_44", "VANDANA.B.S", 44, "271808216004122", "Class 11A", 96, "Male", "VANDANA.B.S S", "VANDANA.B.S DEVI", "House 44, Sector 5, Trivandrum"),
                Student("s_11a_45", "NIYA S DEVI", 45, "271808216004130", "Class 11A", 82, "Female", "NIYA S", "NIYA DEVI", "House 45, Sector 6, Trivandrum"),
                Student("s_11a_46", "AADIDEV MEPPURATHU RATISH", 46, "271808216004004", "Class 11A", 83, "Male", "AADIDEV S", "AADIDEV DEVI", "House 46, Sector 7, Trivandrum"),
                Student("s_11a_47", "ADITHYA NARAYAN .C", 47, "271808215003747", "Class 11A", 84, "Male", "ADITHYA S", "ADITHYA DEVI", "House 47, Sector 8, Trivandrum"),
                Student("s_11a_48", "MOHAMMED RAYYAN S", 48, "271808215003754", "Class 11A", 85, "Male", "MOHAMMED S", "MOHAMMED DEVI", "House 48, Sector 9, Trivandrum"),
                Student("s_11a_49", "AFSAL S", 49, "271808216004170", "Class 11A", 86, "Male", "AFSAL S", "AFSAL DEVI", "House 49, Sector 10, Trivandrum"),
                Student("s_11a_50", "ATHULJITH A P", 50, "271808216004131", "Class 11A", 87, "Male", "ATHULJITH S", "ATHULJITH DEVI", "House 50, Sector 1, Trivandrum")
            )
        )

        // Attendance Defaults
        students.forEach { attendanceMap[it.id] = true }

        // Homework
        homeworkList.addAll(
            listOf(
                Homework("hw1", "Mathematics", "Quadratic Equations", "Solve problems 1 to 10 from Chapter 4 exercise 2.", "Class 10A", "2026-07-15", "JAYASREE SREEKUMAR"),
                Homework("hw2", "Computer Science", "Networking notes", "Write a summary on OSI Model layers in your homework copy.", "Class 10A", "2026-07-14", "AMBILY KRISHNAN")
            )
        )

        // Notices
        notices.addAll(
            listOf(
                Notice("n1", "Orange Alert rain holiday", "Due to heavy rains and orange alert in Trivandrum, the school will remain closed tomorrow.", "Urgent", "all", "2026-07-11"),
                Notice("n2", "Annual Athletic Meet", "The annual athletic meet registration starts today. Contact your class teacher.", "Event", "parent", "2026-07-10")
            )
        )

        // Incidents
        incidents.addAll(
            listOf(
                Incident("i1", "ADITHYAN P", "Late Arrival", "Arrived 20 mins late due to traffic blockage.", "Class 10A", "Low", "PADMAREKHA A K", "2026-07-10"),
                Incident("i2", "GAUTHAM S", "Discipline", "Found using mobile phone in classroom.", "Class 10A", "Medium", "AMBILY KRISHNAN", "2026-07-09")
            )
        )

        // Exam Results
        examResults.addAll(
            listOf(
                ExamResult("e1", "s1", "Unit Test 1", "Mathematics", 48, 50),
                ExamResult("e2", "s1", "Unit Test 1", "Physics", 44, 50),
                ExamResult("e3", "s1", "Unit Test 1", "Computer Science", 47, 50),
                ExamResult("e4", "s2", "Unit Test 1", "Mathematics", 38, 50),
                ExamResult("e5", "s2", "Unit Test 1", "Physics", 41, 50)
            )
        )

        // Timetable
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        days.forEach { day ->
            timetable.add(TimetableSlot("t_${day}_1", "Class 10A", day, 1, "Mathematics", "08:30 AM", "09:15 AM"))
            timetable.add(TimetableSlot("t_${day}_2", "Class 10A", day, 2, "Physics", "09:15 AM", "10:00 AM"))
            timetable.add(TimetableSlot("t_${day}_3", "Class 10A", day, 3, "Computer Science", "10:15 AM", "11:00 AM"))
            timetable.add(TimetableSlot("t_${day}_4", "Class 10A", day, 4, "Chemistry", "11:00 AM", "11:45 AM"))
        }

        // Bus Events
        busEvents.addAll(
            listOf(
                BusEvent("b1", "ARCHANA S", "Boarded Bus", "07:42 AM"),
                BusEvent("b2", "ARCHANA S", "Reached School", "08:15 AM")
            )
        )

        // Visitor Logs
        visitorLogs.addAll(
            listOf(
                VisitorLog("v1", "Mr. Rajesh Nair", "Parent Enquiry regarding admission", "09:10 AM", "09:40 AM"),
                VisitorLog("v2", "Suresh Kumar", "Authorized early pick up of Gautham S", "10:15 AM")
            )
        )
    }

    fun addHomework(hw: Homework) {
        homeworkList.add(0, hw)
        saveState()
    }

    fun addNotice(n: Notice) {
        notices.add(0, n)
        saveState()
    }

    fun addIncident(inc: Incident) {
        incidents.add(0, inc)
        saveState()
    }

    fun updateStudentDetails(
        id: String,
        name: String,
        section: String,
        fatherName: String,
        motherName: String,
        address: String,
        phone: String,
        penNo: String,
        aadharNo: String,
        email: String,
        uid: String,
        dateOfAdmission: String,
        profilePic: String = ""
    ) {
        students.replaceAll {
            if (it.id == id) {
                it.copy(
                    name = name,
                    section = section,
                    fatherName = fatherName,
                    motherName = motherName,
                    address = address,
                    phone = phone,
                    penNo = penNo,
                    aadharNo = aadharNo,
                    email = email,
                    uid = uid,
                    dateOfAdmission = dateOfAdmission,
                    profilePic = if (profilePic.isNotEmpty()) profilePic else it.profilePic
                )
            } else it
        }
        saveState()
    }

    fun updateStudentProfilePic(id: String, profilePic: String) {
        students.replaceAll {
            if (it.id == id) {
                it.copy(profilePic = profilePic)
            } else it
        }
        saveState()
    }

    fun updatePickupRequest(reqId: String, newStatus: String) {
        pickupRequests.replaceAll {
            if (it.id == reqId) it.copy(status = newStatus) else it
        }
        saveState()
    }

    fun addPickupRequest(req: PickupRequest) {
        pickupRequests.add(0, req)
        saveState()
    }

    fun addVisitor(v: VisitorLog) {
        visitorLogs.add(0, v)
        saveState()
    }

    fun checkoutVisitor(vId: String, time: String) {
        visitorLogs.replaceAll {
            if (it.id == vId) it.copy(outTime = time) else it
        }
        saveState()
    }

    fun triggerBusEvent(studentName: String, action: String, time: String) {
        busEvents.add(0, BusEvent(UUID.randomUUID().toString(), studentName, action, time))
        saveState()
    }
}

// ============================================================================
// 3. UI STYLE CONFIG & THEMES
// ============================================================================

object ThemeConfig {
    val darkBlueGradient = Brush.verticalGradient(listOf(Color(0xFF040B18), Color(0xFF0C2454), Color(0xFF0F2A5C)))

    fun getGradientForRole(role: Role): Brush {
        return when (role) {
            Role.ADMIN -> Brush.verticalGradient(listOf(Color(0xFF0F2A5C), Color(0xFF1E3A8A), Color(0xFFFF7A2E)))
            Role.TEACHER -> Brush.verticalGradient(listOf(Color(0xFF0F2A5C), Color(0xFF2563EB), Color(0xFFFF7A2E)))
            Role.PARENT -> Brush.verticalGradient(listOf(Color(0xFF0F2A5C), Color(0xFF1D4ED8), Color(0xFFFF7A2E)))
            Role.CONDUCTOR -> Brush.verticalGradient(listOf(Color(0xFF0F2A5C), Color(0xFFF59E0B), Color(0xFFFF7A2E)))
            Role.BUS_DRIVER -> Brush.verticalGradient(listOf(Color(0xFF0F2A5C), Color(0xFF059669), Color(0xFFFF7A2E)))
            Role.SECURITY -> Brush.verticalGradient(listOf(Color(0xFF0F2A5C), Color(0xFFDC2626), Color(0xFFFF7A2E)))
        }
    }

    fun getAccentColor(role: Role): Color {
        return when (role) {
            Role.ADMIN -> Color(0xFFFFC864)
            Role.TEACHER -> Color(0xFFFF7A2E)
            Role.PARENT -> Color(0xFF2D62CD)
            Role.CONDUCTOR -> Color(0xFFF59E0B)
            Role.BUS_DRIVER -> Color(0xFF10B981)
            Role.SECURITY -> Color(0xFFEF4444)
        }
    }

    fun getBgForRole(role: Role): Color {
        return Color(0xFFF1F5F9)
    }
}

// ============================================================================
// 4. MAIN ACTIVITY ENTRY
// ============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF060F24)
                ) {
                    val viewModel: SchoolViewModel = viewModel()
                    val context = LocalContext.current
                    viewModel.initPrefs(context)
                    MainAppShell(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppShell(viewModel: SchoolViewModel) {
    AnimatedContent(
        targetState = viewModel.currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "AppScreenNavigation"
    ) { screen ->
        when (screen) {
            "landing" -> LandingScreen(viewModel)
            "login" -> LoginScreen(viewModel)
            "portal_parent" -> ParentPortal(viewModel)
            "portal_teacher" -> TeacherPortal(viewModel)
            "portal_admin" -> AdminPortal(viewModel)
            "portal_conductor" -> ConductorPortal(viewModel)
            "portal_bus_driver" -> BusDriverPortal(viewModel)
            "portal_security" -> SecurityPortal(viewModel)
        }
    }
}

// ============================================================================
// 5. LANDING SCREEN (ROLE SELECTOR)
// ============================================================================

@Composable
fun LandingScreen(viewModel: SchoolViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_beacon")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_beacon"
    )

    val educationalQuotes = listOf(
        "तत् त्वं पूषन् अपावृणु (O Sun, please uncover the truth) — KVS Motto",
        "Shaping minds, securing futures, building a resilient nation. — PM SHRI School KV Pattom",
        "Safety and security are the vital foundations of quality education. — CBSE Board",
        "Let us remember: One book, one pen, one child, and one teacher can change the world. — Malala Yousafzai",
        "Education is the movement from darkness to light. — Allan Bloom",
        "Every child safe. Every parent informed. — KVS EduShield AI Sentry"
    )
    
    var currentQuoteIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            currentQuoteIndex = (currentQuoteIndex + 1) % educationalQuotes.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeConfig.darkBlueGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 30.dp, bottom = 40.dp)
        ) {
            // Header / Brand
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = beaconAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE AI CAMPUS SENTRY ACTIVE",
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                
                Text(
                    text = "KVS EduShield AI",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Kendriya Vidyalaya Sangathan · Pattom",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Hero Banner Illustration
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, Color(0xFFFF7A2E).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "KVS School Safety Shield Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient Overlay for readability and premium look
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Every Child Safe. Every Parent Informed.",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Kendriya Vidyalaya's premier AI guardian suite",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Dynamic Inspirational Educational Quotes Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Quote Icon",
                            tint = Color(0xFFFFC864),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "QUOTE OF THE DAY",
                                color = Color(0xFFFFC864),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            AnimatedContent(
                                targetState = currentQuoteIndex,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                                },
                                label = "quote_carousel"
                            ) { idx ->
                                Text(
                                    text = educationalQuotes[idx],
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Mini Stats Grid
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "6" to "Roles Active",
                        "100+" to "Students Secure",
                        "5" to "Active Buses",
                        "AI Shield" to "Sentry Guard"
                    ).forEach { (value, label) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            Text(
                                text = label,
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Divider Title
            item {
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                    Text(
                        text = "SELECT ACCESS PORTAL",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Role selector grid
            items(Role.entries) { role ->
                RoleCard(role) {
                    viewModel.selectedRole = role
                    viewModel.currentScreen = "login"
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Footer
            item {
                Spacer(modifier = Modifier.height(20.dp))
                NovaThinkFooter(lightTheme = false)
            }
        }
    }
}

@Composable
fun RoleCard(role: Role, onClick: () -> Unit) {
    val accentColor = ThemeConfig.getAccentColor(role)
    val roleBg = accentColor.copy(alpha = 0.05f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("role_button_${role.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color.White, roleBg)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = role.icon,
                    contentDescription = role.title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = role.title,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SECURE",
                            color = accentColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Text(
                    text = role.subtitle,
                    color = Color(0xFF475569),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ============================================================================
// 6. LOGIN SCREEN
// ============================================================================

@Composable
fun LoginScreen(viewModel: SchoolViewModel) {
    val role = viewModel.selectedRole ?: Role.PARENT
    val accentColor = ThemeConfig.getAccentColor(role)
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Hints for testing login easily
    val hint = when (role) {
        Role.ADMIN -> "Hint: Username: admin, Password: admin"
        Role.TEACHER -> "Hint: Username: teacher, Password: teacher"
        Role.PARENT -> "Hint: Enter Admission No (e.g. 4350), Password: parent"
        Role.CONDUCTOR -> "Hint: Username: conductor, Password: conductor"
        Role.BUS_DRIVER -> "Hint: Username: driver, Password: driver"
        Role.SECURITY -> "Hint: Username: security, Password: security"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeConfig.getGradientForRole(role))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 30.dp, bottom = 40.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.currentScreen = "landing" },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("KVS EduShield AI", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Pattom Portal", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(role.icon, contentDescription = role.title, tint = Color.White, modifier = Modifier.size(42.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(role.title + " Portal", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(role.subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SIGN IN",
                            color = Color(0xFF1E293B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Username field
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(if (role == Role.PARENT) "Admission Number" else "Username / ID") },
                            placeholder = { Text(if (role == Role.PARENT) "e.g. 271808221006008" else "e.g. Employee Code") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Account") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Lock else Icons.Default.Lock,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hints Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.08f))
                                .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Hint", tint = accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = hint,
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (username.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Please enter login credentials.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                // Simple mock validation to navigate to appropriate screens
                                val userDisplay = if (role == Role.PARENT) "Archana S" else username.uppercase()
                                viewModel.currentUser = User(username, role.id, userDisplay, "Class 10A")
                                Toast.makeText(context, "Sign-in successful!", Toast.LENGTH_SHORT).show()
                                viewModel.currentScreen = "portal_${role.id}"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 7. PARENT PORTAL
@Composable
fun ParentPortal(viewModel: SchoolViewModel) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("Dashboard") }
    var userQuestion by remember { mutableStateOf("") }
    var aiChatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() } // question/reply, isUser
    var isThinking by remember { mutableStateOf(false) }
    var showAiSuiteByTab by remember { mutableStateOf(false) }

    var activeParentDialogState by remember { mutableStateOf<String?>(null) }
    var parentChatTeacherMessage by remember { mutableStateOf("") }
    val parentChatHistory = remember { mutableStateListOf<Pair<String, Boolean>>(
        "Hello Mr. Sundar! Archana is doing great in computer science. Do you have any questions or do you need to submit early pickup?" to false
    ) }
    var earlyLeaveTime by remember { mutableStateOf("01:30 PM") }
    var earlyLeaveReason by remember { mutableStateOf("Doctor Consultation Appointment") }

    // Retrieve live student s1 (Archana S)
    val student = viewModel.students.find { it.id == "s1" } ?: Student(
        "s1", "ARCHANA S", 1, "271808221006008", "Class 10A", 94, "Female", 
        "SUNDAR S", "MEENA S", "House 12, Sector 4, Trivandrum", "9876543210", 
        "PEN982314", "4512-9843-1280", "archana@gmail.com", "UID-100234", "15/06/2021", "avatar1"
    )

    // Init with welcome
    if (aiChatHistory.isEmpty()) {
        aiChatHistory.add("Hello! I am your KVS EduShield AI assistant. Ask me anything about your ward Archana S's safety, attendance, timetable, or exam marks." to false)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf(
                    "Dashboard" to Icons.Default.Home,
                    "My Child" to Icons.Default.Face,
                    "Bus Tracker" to Icons.Default.Place,
                    "AI Assistant" to Icons.Default.AccountCircle
                ).forEach { (tab, icon) ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        icon = { Icon(icon, contentDescription = tab) },
                        label = { Text(tab, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ThemeConfig.getAccentColor(Role.PARENT),
                            selectedTextColor = ThemeConfig.getAccentColor(Role.PARENT)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar using KVS Common Header Component
                KvsPmShriHeaderBar(
                    title = "Parent: Sundar S",
                    subtitle = "Ward: Archana S · Class 10A",
                    onBackClick = { viewModel.currentScreen = "landing" },
                    role = Role.PARENT,
                    profileLetter = "A",
                    profilePic = student.profilePic
                )

                // Main content switcher
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        "Dashboard" -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // 1. REAL-TIME SECURITY SENTRY EARLY PICKUP APPROVALS LOG
                                val pendingPickup = viewModel.pickupRequests.find { 
                                    it.studentId == "s1" && (it.status == "Pending" || it.status == "Pending Notification") 
                                }
                                if (pendingPickup != null) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                            border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Warning, contentDescription = "Alert", tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("🚨 SECURITY GATE VISITOR ALERT", color = Color(0xFF991B1B), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "A visitor is currently at the school gate requesting early pickup of Archana S in school hours. Parents must confirm the identity of the visitor before release.",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF7F1D1D)
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                                ) {
                                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        // Show captured image or avatar placeholder
                                                        Box(
                                                            modifier = Modifier
                                                                .size(56.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFE2E8F0)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.AccountCircle, contentDescription = "Visitor", tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text("Visitor: ${pendingPickup.visitorName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            Text("Relationship: ${pendingPickup.relationship}", fontSize = 11.sp, color = Color.Gray)
                                                            Text("Reason: ${pendingPickup.reason}", fontSize = 11.sp, color = Color.Gray)
                                                            Text("Time: ${pendingPickup.time}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF7A2E))
                                                        }
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updatePickupRequest(pendingPickup.id, "Approved")
                                                            Toast.makeText(context, "Identity confirmed! Visitor release approved.", Toast.LENGTH_LONG).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Confirm & Approve", color = Color.White, fontSize = 11.sp)
                                                    }
                                                    Button(
                                                        onClick = {
                                                            viewModel.updatePickupRequest(pendingPickup.id, "Rejected")
                                                            Toast.makeText(context, "Alert: Gate release rejected. Security has been notified.", Toast.LENGTH_LONG).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Deny / Alarm", color = Color.White, fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. SECURITY ACTIVE SCORECARD
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFECFDF5))
                                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("CAMPUS SECURITY STATUS: 100% SECURE", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF065F46))
                                            Text("Sentry, RFID scanners, GPS tracking & CCTV live.", fontSize = 9.sp, color = Color(0xFF047857))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFD1FAE5))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ACTIVE", color = Color(0xFF065F46), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // 3. COLOURFUL QUICK LAUNCH ACTION HUB (9 WIDGET BUTTONS)
                                item {
                                    Text("My Parent Quick-Launch Tools", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                }

                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Exit Pass", "Early leave", Icons.Default.Lock, Color(0xFFEF4444)) {
                                                    activeParentDialogState = "early_leave"
                                                }
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Timetable", "Class schedule", Icons.Default.List, Color(0xFF3B82F6)) {
                                                    activeParentDialogState = "timetable"
                                                }
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Grades", "CBSE Marksheet", Icons.Default.Star, Color(0xFFF59E0B)) {
                                                    activeParentDialogState = "grades"
                                                }
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Bus Live", "GPS Route 4", Icons.Default.Place, Color(0xFF10B981)) {
                                                    activeParentDialogState = "bus_telemetry"
                                                }
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Health", "Medical index", Icons.Default.Favorite, Color(0xFFEC4899)) {
                                                    activeParentDialogState = "health"
                                                }
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Canteen", "Diet wallet", Icons.Default.ShoppingCart, Color(0xFF8B5CF6)) {
                                                    activeParentDialogState = "canteen"
                                                }
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Medals", "Achievements", Icons.Default.CheckCircle, Color(0xFF6366F1)) {
                                                    activeParentDialogState = "achievements"
                                                }
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Teacher", "Secure chat", Icons.Default.Call, Color(0xFF14B8A6)) {
                                                    activeParentDialogState = "chat"
                                                }
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                ParentQuickActionCard("Circulars", "KV Calendar", Icons.Default.Info, Color(0xFFF97316)) {
                                                    activeParentDialogState = "circulars"
                                                }
                                            }
                                        }
                                    }
                                }

                                // 4. Live security logs
                                item {
                                    Text("Student Safety & Gate Logs", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                }

                                if (viewModel.busEvents.isEmpty()) {
                                    item {
                                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                                Text("No entry/exit logs recorded today yet.", color = Color.Gray, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else {
                                    items(viewModel.busEvents.reversed()) { ev ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(if (ev.action.contains("Boarded")) Color(0xFFDCF2FE) else Color(0xFFE0F2FE)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        if (ev.action.contains("Boarded")) Icons.Default.Place else Icons.Default.Home,
                                                        contentDescription = "Event",
                                                        tint = Color(0xFF0284C7)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(ev.action, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Tracker: ${ev.busNo}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Text(ev.timestamp, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                            }
                                        }
                                    }
                                }

                                item {
                                    NovaThinkFooter(lightTheme = true)
                                }
                            }
                        }

                        "My Child" -> {
                            var showCameraPreview by remember { mutableStateOf(false) }
                            var cameraCountdown by remember { mutableStateOf(3) }
                            var cameraFlashActive by remember { mutableStateOf(false) }

                            if (showCameraPreview) {
                                LaunchedEffect(showCameraPreview) {
                                    cameraCountdown = 3
                                    while (cameraCountdown > 0) {
                                        kotlinx.coroutines.delay(1000)
                                        cameraCountdown--
                                    }
                                    cameraFlashActive = true
                                    kotlinx.coroutines.delay(150)
                                    cameraFlashActive = false
                                    // Update to a new cute camera photo avatar
                                    val index = viewModel.students.indexOfFirst { it.id == "s1" }
                                    if (index != -1) {
                                        viewModel.students[index] = student.copy(profilePic = "avatar3")
                                        viewModel.saveState()
                                    }
                                    showCameraPreview = false
                                }

                                AlertDialog(
                                    onDismissRequest = { showCameraPreview = false },
                                    title = { Text("AI Sentry Camera Viewfinder", fontWeight = FontWeight.Bold, color = Color(0xFF0F2A5C)) },
                                    text = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(240.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (cameraFlashActive) Color.White else Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!cameraFlashActive) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.Face, contentDescription = "Camera Shutter", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text("SMILE FOR THE CAMERA!", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                                    Text("Capturing Student Profile Photo in", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                                    Text("$cameraCountdown", color = Color(0xFFFFC864), fontSize = 48.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = { showCameraPreview = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                                            Text("Cancel", color = Color.White)
                                        }
                                    }
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // 1. PROFILE PHOTO MANAGEMENT UPLOADER
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Set Student Profile Photo", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F2A5C))
                                            Text("This helps teachers and security guards easily identify your child.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                                            
                                            Spacer(modifier = Modifier.height(14.dp))

                                            // Dynamic Photo Preview
                                            Box(
                                                modifier = Modifier
                                                    .size(90.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (student.profilePic) {
                                                            "avatar1" -> Color(0xFFF59E0B)
                                                            "avatar2" -> Color(0xFF10B981)
                                                            "avatar3" -> Color(0xFF8B5CF6)
                                                            "avatar4" -> Color(0xFFEC4899)
                                                            "avatar5" -> Color(0xFF06B6D4)
                                                            "avatar6" -> Color(0xFF3B82F6)
                                                            else -> Color(0xFF3B82F6)
                                                        }
                                                    )
                                                    .border(3.dp, Color(0xFFFFC864), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (student.profilePic) {
                                                        "avatar1" -> Icons.Default.Face
                                                        "avatar2" -> Icons.Default.Favorite
                                                        "avatar3" -> Icons.Default.Star
                                                        "avatar4" -> Icons.Default.ThumbUp
                                                        "avatar5" -> Icons.Default.Check
                                                        else -> Icons.Default.AccountCircle
                                                    },
                                                    contentDescription = "Profile Photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(54.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("Choose Student Profile Accent Avatar:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Avatar Selectors
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                listOf(
                                                    "avatar1" to Color(0xFFF59E0B),
                                                    "avatar2" to Color(0xFF10B981),
                                                    "avatar3" to Color(0xFF8B5CF6),
                                                    "avatar4" to Color(0xFFEC4899),
                                                    "avatar5" to Color(0xFF06B6D4),
                                                    "avatar6" to Color(0xFF3B82F6)
                                                ).forEach { (picName, color) ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                            .border(
                                                                width = if (student.profilePic == picName) 2.5.dp else 0.dp,
                                                                color = if (student.profilePic == picName) Color(0xFFFFC864) else Color.Transparent,
                                                                shape = CircleShape
                                                            )
                                                            .clickable {
                                                                viewModel.updateStudentProfilePic("s1", picName)
                                                                Toast.makeText(context, "Profile Photo updated successfully!", Toast.LENGTH_SHORT).show()
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = when (picName) {
                                                                "avatar1" -> Icons.Default.Face
                                                                "avatar2" -> Icons.Default.Favorite
                                                                "avatar3" -> Icons.Default.Star
                                                                "avatar4" -> Icons.Default.ThumbUp
                                                                "avatar5" -> Icons.Default.Check
                                                                else -> Icons.Default.AccountCircle
                                                            },
                                                            contentDescription = picName,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = { showCameraPreview = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A2E)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Camera", tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Capture Real Profile Picture", fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }

                                // 2. MINUTE-BY-MINUTE DYNAMIC SCHOOL ACTIVITIES LOG
                                item {
                                    class SentryActivity(val time: String, val details: String, val icon: ImageVector, val color: Color)

                                    val activities = listOf(
                                        SentryActivity("07:42 AM", "Boarded school Bus 1 at Pattom Junction stop (Verified by Conductor)", Icons.Default.Place, Color(0xFF16A34A)),
                                        SentryActivity("08:15 AM", "Arrived safely at KVS Pattom Gate. (RFID Registered entry)", Icons.Default.CheckCircle, Color(0xFF16A34A)),
                                        SentryActivity("08:30 AM", "Entered Classroom 10A (Intelligent Bluetooth Sentry Sensed)", Icons.Default.Home, Color(0xFF2563EB)),
                                        SentryActivity("09:15 AM", "Attendance marked: PRESENT by Class Teacher Ambily Krishnan", Icons.Default.AccountBox, Color(0xFF2563EB)),
                                        SentryActivity("10:45 AM", "Computer Science Lab login - Working on OSI Model assignment", Icons.Default.Edit, Color(0xFF8B5CF6)),
                                        SentryActivity("12:15 PM", "KVS Hot Lunch session recorded - Opted for nutritious school menu", Icons.Default.Favorite, Color(0xFFEC4899)),
                                        SentryActivity("01:45 PM", "PM SHRI Science Club Exhibition - Displayed active robotic prototype", Icons.Default.Star, Color(0xFFFF7A2E)),
                                        SentryActivity("02:45 PM", "Regular school dismissal. Checked out of class gate successfully", Icons.Default.ExitToApp, Color(0xFF64748B))
                                    )

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.DateRange, contentDescription = "Timeline", tint = Color(0xFF0F2A5C), modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Minute-by-Minute Activity Timeline", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                                            }
                                            Text("Today's real-time physical & academic telemetry logs", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                                            Spacer(modifier = Modifier.height(16.dp))

                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                activities.forEachIndexed { index, act ->
                                                    Row(verticalAlignment = Alignment.Top) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(24.dp)
                                                                    .clip(CircleShape)
                                                                    .background(act.color.copy(alpha = 0.12f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(act.icon, contentDescription = "Log icon", tint = act.color, modifier = Modifier.size(14.dp))
                                                            }
                                                            if (index < activities.size - 1) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .width(2.dp)
                                                                        .height(30.dp)
                                                                        .background(Color(0xFFE2E8F0))
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(act.time, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = act.color)
                                                            Text(act.details, fontSize = 11.sp, color = Color(0xFF1E293B))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. STUDENT'S MINUTE DETAILS & ACTIVITIES LIST
                                item {
                                    Text("Archana's Official Registry Details", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F2A5C))
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(
                                                "PEN Number" to student.penNo,
                                                "Aadhar Number" to student.aadharNo,
                                                "UID Number" to student.uid,
                                                "Date of Admission" to student.dateOfAdmission,
                                                "Full Name" to student.name,
                                                "Admission No" to student.admissionNo,
                                                "Roll No" to student.rollNo.toString(),
                                                "Class Section" to student.section,
                                                "Email Address" to student.email,
                                                "Phone Number" to student.phone,
                                                "Residential Address" to student.address,
                                                "Father Name" to student.fatherName,
                                                "Mother Name" to student.motherName
                                            ).forEach { (field, valText) ->
                                                Row(modifier = Modifier.fillMaxWidth()) {
                                                    Text(field, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(130.dp))
                                                    Text(valText, fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                                }
                                                Divider(color = Color(0xFFF1F5F9))
                                            }
                                        }
                                    }
                                }

                                item {
                                    NovaThinkFooter(lightTheme = true)
                                }
                            }
                        }

                        "Bus Tracker" -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // 1. TRIP MAP & COORDINATES CARD
                                item {
                                    val isTripActive = viewModel.busTripActive.value
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Place, contentDescription = "Bus", tint = Color(0xFFFF7A2E), modifier = Modifier.size(24.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("LIVE BUS ROUTE 1 TRACKER", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                                                Spacer(modifier = Modifier.weight(1f))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (isTripActive) Color(0xFFDCF2FE) else Color(0xFFF1F5F9))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isTripActive) "LIVE TRACKING" else "IDLE",
                                                        color = if (isTripActive) Color(0xFF0284C7) else Color.Gray,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            // Mock Visual GPS Map
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE2E8F0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Map Pin", tint = Color(0xFF0F2A5C), modifier = Modifier.size(32.dp))
                                                    Text("KVS PATTON - BUS 1 LIVE NAV", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF0F2A5C))
                                                    Text(
                                                        text = if (isTripActive) "📍 GPS Coords: ${viewModel.busLatitude.value}° N, ${viewModel.busLongitude.value}° E" else "📍 Bus parked in school transit yard.",
                                                        fontSize = 10.sp,
                                                        color = Color.DarkGray,
                                                        fontStyle = FontStyle.Italic
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("Trip Status: ${viewModel.busRouteStatus.value}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Estimated ETA to Home: " + (if (isTripActive) "14 mins" else "N/A"), fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                // 2. INTERACTIVE ROUTE PROGRESS STEPS
                                item {
                                    Text("Bus 1 Stop-by-Stop Progress", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            val stops = listOf(
                                                "PM SHRI KV Pattom campus" to "Departed at 02:40 PM",
                                                "Kesavadasapuram Stop" to "Passed at 02:45 PM",
                                                "Kowdiar Junction stop" to "Bus is approaching stop now",
                                                "Peroorkada Stop" to "Scheduled 02:55 PM",
                                                "Home Stop" to "Scheduled 03:02 PM"
                                            )
                                            stops.forEachIndexed { idx, (stop, desc) ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (idx < 2) Color(0xFF16A34A) 
                                                                else if (idx == 2 && viewModel.busTripActive.value) Color(0xFFFF7A2E) 
                                                                else Color(0xFFE2E8F0)
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text((idx + 1).toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(stop, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text(desc, fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    NovaThinkFooter(lightTheme = true)
                                }
                            }
                        }

                        "AI Assistant" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Sub headers to toggle Suite
                                TabRow(
                                    selectedTabIndex = if (showAiSuiteByTab) 1 else 0,
                                    containerColor = Color.White,
                                    contentColor = ThemeConfig.getAccentColor(Role.PARENT)
                                ) {
                                    Tab(
                                        selected = !showAiSuiteByTab,
                                        onClick = { showAiSuiteByTab = false },
                                        text = { Text("Ward Query Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                    )
                                    Tab(
                                        selected = showAiSuiteByTab,
                                        onClick = { showAiSuiteByTab = true },
                                        text = { Text("PM SHRI KVS AI Suite", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }

                                if (showAiSuiteByTab) {
                                    // Direct render of PM SHRI KVS AI Suite
                                    Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                                        KvsAiSuite(role = Role.PARENT)
                                    }
                                } else {
                                    // Ward Query Chat layout
                                    Column(modifier = Modifier.weight(1f)) {
                                        LazyColumn(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            contentPadding = PaddingValues(vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(aiChatHistory) { chat ->
                                                val isUser = chat.second
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .widthIn(max = 280.dp)
                                                            .clip(RoundedCornerShape(
                                                                topStart = 16.dp,
                                                                topEnd = 16.dp,
                                                                bottomStart = if (isUser) 16.dp else 0.dp,
                                                                bottomEnd = if (isUser) 0.dp else 16.dp
                                                            ))
                                                            .background(if (isUser) ThemeConfig.getAccentColor(Role.PARENT) else Color.White)
                                                            .padding(12.dp)
                                                    ) {
                                                        Text(
                                                            text = chat.first,
                                                            color = if (isUser) Color.White else Color(0xFF1E293B),
                                                            fontSize = 13.sp,
                                                            lineHeight = 18.sp
                                                        )
                                                    }
                                                }
                                            }

                                            if (isThinking) {
                                                item {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                                        Card(
                                                            shape = RoundedCornerShape(12.dp),
                                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                                        ) {
                                                            Text("Thinking...", modifier = Modifier.padding(12.dp), fontStyle = FontStyle.Italic, fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Query Input Row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = userQuestion,
                                                onValueChange = { userQuestion = it },
                                                placeholder = { Text("Ask about school safety / grades...", fontSize = 12.sp) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(ThemeConfig.getAccentColor(Role.PARENT))
                                                    .clickable {
                                                        if (userQuestion.isNotBlank()) {
                                                            val q = userQuestion
                                                            aiChatHistory.add(q to true)
                                                            userQuestion = ""
                                                            isThinking = true

                                                            // Simulate response
                                                            val responseText = when {
                                                                q.contains("attendance", ignoreCase = true) ->
                                                                    "Archana S currently has 94% attendance which meets KVS guidelines. She was present today and boarded Bus 1."
                                                                q.contains("marks", ignoreCase = true) || q.contains("grade", ignoreCase = true) ->
                                                                    "Her latest test scores are excellent: Mathematics 48/50, Physics 44/50, and Computer Science 47/50."
                                                                q.contains("safety", ignoreCase = true) || q.contains("bus", ignoreCase = true) ->
                                                                    "Archana boarded Bus 1 at 07:42 AM and safely reached Kendriya Vidyalaya Pattom campus at 08:15 AM."
                                                                else -> "KVS EduShield AI: Archana's overall status is great! She is on-track for academics and arrived safely at class today."
                                                            }

                                                            // Delay to feel natural
                                                            Timer().schedule(object : TimerTask() {
                                                                override fun run() {
                                                                    isThinking = false
                                                                    aiChatHistory.add(responseText to false)
                                                                }
                                                            }, 1200)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Dialog Overlays for Parent Quick Actions
            if (activeParentDialogState != null) {
                AlertDialog(
                    onDismissRequest = { activeParentDialogState = null },
                    confirmButton = {
                        TextButton(onClick = { activeParentDialogState = null }) {
                            Text("Dismiss", color = ThemeConfig.getAccentColor(Role.PARENT), fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        val title = when (activeParentDialogState) {
                            "early_leave" -> "Pre-Approve Early Exit Pass"
                            "timetable" -> "Weekly Class Timetable"
                            "grades" -> "CBSE Exam Grades Card"
                            "bus_telemetry" -> "Route 4 GPS Telemetry"
                            "health" -> "Campus Health Registry"
                            "canteen" -> "Canteen Wallet & Diet Plan"
                            "achievements" -> "Academic & Sport Medals"
                            "chat" -> "Secure Teacher Chatroom"
                            "circulars" -> "KV Activities Circulars"
                            else -> "Details"
                        }
                        Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            when (activeParentDialogState) {
                                "early_leave" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Publish pre-approved gate passes to bypass gate alarms.", fontSize = 11.sp, color = Color.Gray)
                                        OutlinedTextField(
                                            value = earlyLeaveTime,
                                            onValueChange = { earlyLeaveTime = it },
                                            label = { Text("Departure Time") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = earlyLeaveReason,
                                            onValueChange = { earlyLeaveReason = it },
                                            label = { Text("Reason for Exit") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(
                                            onClick = {
                                                val req = PickupRequest(
                                                    UUID.randomUUID().toString(),
                                                    "s1", "ARCHANA S", "Class 10A",
                                                    earlyLeaveReason, earlyLeaveTime, "Pending",
                                                    "Sundar S", "Father"
                                                )
                                                viewModel.addPickupRequest(req)
                                                Toast.makeText(context, "Early leave request submitted to Security Gate!", Toast.LENGTH_LONG).show()
                                                activeParentDialogState = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Publish Exit Request", color = Color.White)
                                        }
                                    }
                                }
                                "timetable" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(
                                            "09:00 AM - 09:45 AM" to "📐 Mathematics (Trigonometry)",
                                            "09:45 AM - 10:30 AM" to "🔬 Physics (Electromagnetism)",
                                            "10:30 AM - 11:15 AM" to "🧪 Chemistry (Organic Compounds)",
                                            "11:15 AM - 11:30 AM" to "☕ Recess Break",
                                            "11:30 AM - 12:15 PM" to "💻 Computer Science (Kotlin UI)",
                                            "12:15 PM - 01:00 PM" to "📚 English Literature (Macbeth)"
                                        ).forEach { (time, subject) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(time, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                                Text(subject, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                            }
                                        }
                                    }
                                }
                                "grades" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("CBSE Term-1 Progress Card (Archana S)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        listOf(
                                            "Computer Science" to 0.96f,
                                            "Mathematics" to 0.88f,
                                            "Physics" to 0.91f,
                                            "Chemistry" to 0.85f,
                                            "English" to 0.92f
                                        ).forEach { (subject, score) ->
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(subject, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                    Text("${(score * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                                }
                                                LinearProgressIndicator(
                                                    progress = score,
                                                    color = Color(0xFFF59E0B),
                                                    trackColor = Color(0xFFFEF3C7),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 4.dp)
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                                "bus_telemetry" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(
                                            "Vehicle Registered" to "KL-01-CB-4011 (Route 4)",
                                            "Current Speed" to "38 km/h (Safe Range)",
                                            "Logistics Occupancy" to "18 / 30 Students Onboard",
                                            "Bus Cabin Temp" to "24.5°C",
                                            "Seatbelt Alarm" to "0 Warnings (All Buckled)",
                                            "Driver Name" to "Anil Kumar (Contact: +91 9447012345)"
                                        ).forEach { (label, value) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(label, fontSize = 11.sp, color = Color.Gray)
                                                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                            }
                                        }
                                    }
                                }
                                "health" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(
                                            "Height" to "154 cm",
                                            "Weight" to "46 kg",
                                            "BMI Index" to "19.4 (Normal / Healthy)",
                                            "Blood Group" to "O+ Positive",
                                            "Vaccinations" to "Up-to-date",
                                            "Last Clinic Visit" to "05/06/2026 (Routine)"
                                        ).forEach { (label, value) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(label, fontSize = 11.sp, color = Color.Gray)
                                                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                                            }
                                        }
                                    }
                                }
                                "canteen" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Balance: ₹450.00", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF8B5CF6))
                                        listOf(
                                            "Dietary Plan" to "Low-sodium Vegetarian",
                                            "Meal Pre-ordered" to "KV Meal Package - Chapati, Paneer, Apple",
                                            "Total Calories" to "580 kcal",
                                            "Auto-Replenish" to "Disabled"
                                        ).forEach { (label, value) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(label, fontSize = 11.sp, color = Color.Gray)
                                                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                                            }
                                        }
                                    }
                                }
                                "achievements" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        listOf(
                                            "🥇 KVS Coding Hackathon 2026" to "First Prize Winner (Trivandrum Region)",
                                            "🥈 PM SHRI Inter-School Chess" to "Silver Medalist",
                                            "🎖️ CBSE Srimati Award" to "Outstanding Student in Computer Application"
                                        ).forEach { (award, desc) ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFEEF2F6), RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(award, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                                                Text(desc, fontSize = 10.sp, color = Color.DarkGray)
                                            }
                                        }
                                    }
                                }
                                "chat" -> {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                items(parentChatHistory) { msgPair ->
                                                    val isUs = msgPair.second
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = if (isUs) Arrangement.End else Arrangement.Start
                                                    ) {
                                                        Card(
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = if (isUs) Color(0xFFD9F99D) else Color.White
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            border = BorderStroke(1.dp, if (isUs) Color(0xFFA3E635) else Color(0xFFE2E8F0)),
                                                            modifier = Modifier.widthIn(max = 200.dp)
                                                        ) {
                                                            Text(
                                                                text = msgPair.first,
                                                                fontSize = 11.sp,
                                                                modifier = Modifier.padding(8.dp),
                                                                color = Color(0xFF1E293B)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = parentChatTeacherMessage,
                                                onValueChange = { parentChatTeacherMessage = it },
                                                placeholder = { Text("Ask teacher...", fontSize = 11.sp) },
                                                modifier = Modifier.weight(1f),
                                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Button(
                                                onClick = {
                                                    if (parentChatTeacherMessage.isNotBlank()) {
                                                        val txt = parentChatTeacherMessage
                                                        parentChatHistory.add(txt to true)
                                                        parentChatTeacherMessage = ""
                                                        
                                                        Timer().schedule(object : TimerTask() {
                                                            override fun run() {
                                                                val reply = when {
                                                                    txt.contains("exit", ignoreCase = true) || txt.contains("leave", ignoreCase = true) ->
                                                                        "Noted, Mr. Sundar. I will inform the security kiosk that Archana has a pre-approved departure pass at the requested time. She will be ready!"
                                                                    txt.contains("exam", ignoreCase = true) || txt.contains("marks", ignoreCase = true) ->
                                                                        "Her performance is remarkable. She is leading the class in computer science!"
                                                                    else ->
                                                                        "Thanks Mr. Sundar, I have noted this. I will coordinate with Archana. Rest assured she is in good hands."
                                                                }
                                                                parentChatHistory.add(reply to false)
                                                            }
                                                        }, 1500)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.PARENT)),
                                                contentPadding = PaddingValues(horizontal = 10.dp)
                                            ) {
                                                Text("Send", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                                "circulars" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(
                                            "July 20, 2026" to "🏃 PM SHRI School Annual Athletic Meet",
                                            "August 05, 2026" to "🔬 Regional Science Exhibition & AI Expo",
                                            "August 15, 2026" to "🇮🇳 Independence Day Celebrations & Flag Hoisting"
                                        ).forEach { (date, event) ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFFFF7ED), RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(6.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(date, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                                Text(event, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
fun ParentQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 8.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

// ============================================================================
// 8. TEACHER PORTAL
// ============================================================================

@Composable
fun TeacherPortal(viewModel: SchoolViewModel) {
    var activeMenu by remember { mutableStateOf("Main") }
    var selectedStudentForEdit by remember { mutableStateOf<Student?>(null) }

    // AI Lesson Planner States
    var lessonTopic by remember { mutableStateOf("") }
    var lessonChapter by remember { mutableStateOf("") }
    var lessonPlanResult by remember { mutableStateOf<String?>(null) }
    var isGeneratingLessonPlan by remember { mutableStateOf(false) }

    // Marks Registry States
    val studentMarksCS = remember { mutableStateMapOf<String, String>() }
    val studentMarksScience = remember { mutableStateMapOf<String, String>() }

    // Forms states
    var hwSubject by remember { mutableStateOf("") }
    var hwTitle by remember { mutableStateOf("") }
    var hwDesc by remember { mutableStateOf("") }
    var hwDue by remember { mutableStateOf("") }

    var noticeTitle by remember { mutableStateOf("") }
    var noticeBody by remember { mutableStateOf("") }
    var noticeCategory by remember { mutableStateOf("Academic") }

    var incStudent by remember { mutableStateOf("") }
    var incType by remember { mutableStateOf("Late Arrival") }
    var incNotes by remember { mutableStateOf("") }
    var incSeverity by remember { mutableStateOf("Medium") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            KvsPmShriHeaderBar(
                title = "Teacher: Ambily Krishnan",
                subtitle = "Class Teacher · Class 10A · PGT CS",
                onBackClick = {
                    if (selectedStudentForEdit != null) {
                        selectedStudentForEdit = null
                    } else if (activeMenu != "Main") {
                        activeMenu = "Main"
                    } else {
                        viewModel.currentScreen = "landing"
                    }
                },
                role = Role.TEACHER,
                profileLetter = "A"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
        ) {
            Column {

                when (activeMenu) {
                    "Main" -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val items = listOf(
                                Triple("Attendance", "Manage safety list", Icons.Default.CheckCircle),
                                Triple("Homework Assigner", "Publish CBSE tasks", Icons.Default.Edit),
                                Triple("Notice Board", "Sentry broadcast", Icons.Default.Info),
                                Triple("Report Incident", "Log gate events", Icons.Default.Warning),
                                Triple("Student Directory", "Update PEN & profiles", Icons.Default.AccountCircle),
                                Triple("Class Diary", "Class annals", Icons.Default.List),
                                Triple("CBSE AI Lesson Planner", "AI syllabus planner", Icons.Default.Build),
                                Triple("Marks Registry", "Grade term exam", Icons.Default.Star),
                                Triple("PTA Meeting Slots", "Parent bookings", Icons.Default.Face),
                                Triple("Sitting Chart", "Bench allocations", Icons.Default.Home)
                            )
                            items(items) { (label, subtitle, icon) ->
                                val accentColor = ThemeConfig.getAccentColor(Role.TEACHER)
                                Card(
                                    modifier = Modifier
                                        .height(115.dp)
                                        .clickable { activeMenu = label }
                                        .testTag("teacher_menu_${label.lowercase().replace(" ", "_")}"),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.horizontalGradient(listOf(Color.White, accentColor.copy(alpha = 0.02f))))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(accentColor.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(label, fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.Center, color = Color(0xFF0F172A), maxLines = 1)
                                        Text(subtitle, fontSize = 8.sp, textAlign = TextAlign.Center, color = Color(0xFF64748B), maxLines = 1, modifier = Modifier.padding(top = 1.dp))
                                    }
                                }
                            }
                        }
                    }

                    "Attendance" -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("Mark Attendance - Class 10A", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text("Default is present. Tap to toggle status.", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(viewModel.students) { s ->
                                    val isPresent = viewModel.attendanceMap[s.id] ?: true
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(s.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("Roll No: ${s.rollNo} · Adm: ${s.admissionNo}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Switch(
                                                checked = isPresent,
                                                onCheckedChange = { viewModel.attendanceMap[s.id] = it },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = ThemeConfig.getAccentColor(Role.TEACHER),
                                                    checkedTrackColor = ThemeConfig.getAccentColor(Role.TEACHER).copy(alpha = 0.5f)
                                                ),
                                                modifier = Modifier.testTag("attendance_switch_${s.id}")
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Attendance saved and uploaded to security gate!", Toast.LENGTH_SHORT).show()
                                    activeMenu = "Main"
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.TEACHER))
                            ) {
                                Text("Submit Attendance Report", color = Color.White)
                            }
                        }
                    }

                    "Homework Assigner" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            item {
                                Text("Assign New Homework", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            item {
                                OutlinedTextField(value = hwSubject, onValueChange = { hwSubject = it }, label = { Text("Subject") }, placeholder = { Text("e.g. Computer Science") }, modifier = Modifier.fillMaxWidth())
                            }
                            item {
                                OutlinedTextField(value = hwTitle, onValueChange = { hwTitle = it }, label = { Text("Topic Title") }, placeholder = { Text("e.g. OSI Model Diagram") }, modifier = Modifier.fillMaxWidth())
                            }
                            item {
                                OutlinedTextField(value = hwDesc, onValueChange = { hwDesc = it }, label = { Text("Description / Task") }, placeholder = { Text("Describe the task completely...") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                            }
                            item {
                                OutlinedTextField(value = hwDue, onValueChange = { hwDue = it }, label = { Text("Due Date") }, placeholder = { Text("YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
                            }
                            item {
                                Button(
                                    onClick = {
                                        if (hwSubject.isBlank() || hwTitle.isBlank()) {
                                            Toast.makeText(context, "Subject and Title are required.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        viewModel.addHomework(Homework(UUID.randomUUID().toString(), hwSubject, hwTitle, hwDesc, "Class 10A", hwDue, "AMBILY KRISHNAN"))
                                        Toast.makeText(context, "Homework assigned and parents notified!", Toast.LENGTH_SHORT).show()
                                        activeMenu = "Main"
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.TEACHER))
                                ) {
                                    Text("Assign Homework", color = Color.White)
                                }
                            }
                        }
                    }

                    "Notice Board" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            item {
                                Text("Publish New Notice", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            item {
                                OutlinedTextField(value = noticeTitle, onValueChange = { noticeTitle = it }, label = { Text("Notice Title") }, modifier = Modifier.fillMaxWidth())
                            }
                            item {
                                OutlinedTextField(value = noticeBody, onValueChange = { noticeBody = it }, label = { Text("Detailed Notice Body") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                            }
                            item {
                                Button(
                                    onClick = {
                                        if (noticeTitle.isBlank() || noticeBody.isBlank()) {
                                            Toast.makeText(context, "All notice fields are required.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        viewModel.addNotice(Notice(UUID.randomUUID().toString(), noticeTitle, noticeBody, noticeCategory, "all", "2026-07-12"))
                                        Toast.makeText(context, "Notice published school-wide!", Toast.LENGTH_SHORT).show()
                                        activeMenu = "Main"
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.TEACHER))
                                ) {
                                    Text("Publish Notice", color = Color.White)
                                }
                            }
                        }
                    }

                    "Report Incident" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            item {
                                Text("Log Student Incident", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            item {
                                OutlinedTextField(value = incStudent, onValueChange = { incStudent = it }, label = { Text("Student Name") }, placeholder = { Text("e.g. ADITHYAN P") }, modifier = Modifier.fillMaxWidth())
                            }
                            item {
                                OutlinedTextField(value = incNotes, onValueChange = { incNotes = it }, label = { Text("Description / Remarks") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                            }
                            item {
                                Button(
                                    onClick = {
                                        if (incStudent.isBlank() || incNotes.isBlank()) {
                                            Toast.makeText(context, "Student Name and description are required.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        viewModel.addIncident(Incident(UUID.randomUUID().toString(), incStudent, incType, incNotes, "Class 10A", incSeverity, "AMBILY KRISHNAN", "2026-07-12"))
                                        Toast.makeText(context, "Incident report submitted to Admin!", Toast.LENGTH_SHORT).show()
                                        activeMenu = "Main"
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.TEACHER))
                                ) {
                                    Text("Submit Incident Report", color = Color.White)
                                }
                            }
                        }
                    }

                    "Student Directory" -> {
                        if (selectedStudentForEdit != null) {
                            val s = selectedStudentForEdit!!
                            var editName by remember(s.id) { mutableStateOf(s.name) }
                            var editRollNo by remember(s.id) { mutableStateOf(s.rollNo.toString()) }
                            var editSection by remember(s.id) { mutableStateOf(s.section) }
                            var editFatherName by remember(s.id) { mutableStateOf(s.fatherName) }
                            var editMotherName by remember(s.id) { mutableStateOf(s.motherName) }
                            var editAddress by remember(s.id) { mutableStateOf(s.address) }
                            var editPhone by remember(s.id) { mutableStateOf(s.phone) }
                            var editPenNo by remember(s.id) { mutableStateOf(s.penNo) }
                            var editAadharNo by remember(s.id) { mutableStateOf(s.aadharNo) }
                            var editEmail by remember(s.id) { mutableStateOf(s.email) }
                            var editUid by remember(s.id) { mutableStateOf(s.uid) }
                            var editDateOfAdmission by remember(s.id) { mutableStateOf(s.dateOfAdmission) }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // 1. Hero Card containing Student Summary
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2A5C)),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFF7A2E)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (editName.isNotEmpty()) editName.take(1).uppercase() else "S",
                                                    color = Color.White,
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = editName,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    SuggestionChip(
                                                        onClick = {},
                                                        label = { Text("Roll: $editRollNo", color = Color.White, fontSize = 11.sp) },
                                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                                        border = null
                                                    )
                                                    SuggestionChip(
                                                        onClick = {},
                                                        label = { Text("Class: $editSection", color = Color.White, fontSize = 11.sp) },
                                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                                        border = null
                                                    )
                                                    SuggestionChip(
                                                        onClick = {},
                                                        label = { Text("Adm: ${s.admissionNo}", color = Color.White, fontSize = 11.sp) },
                                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                                        border = null
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. Section: Academic Registry Details
                                item {
                                    Text(
                                        text = "Academic & Registry Info",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F2A5C),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editPenNo,
                                                onValueChange = { editPenNo = it },
                                                label = { Text("PEN (Permanent Education Number)") },
                                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_pen_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )

                                            OutlinedTextField(
                                                value = editAadharNo,
                                                onValueChange = { editAadharNo = it },
                                                label = { Text("Aadhaar Number") },
                                                leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_aadhar_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )

                                            OutlinedTextField(
                                                value = editUid,
                                                onValueChange = { editUid = it },
                                                label = { Text("UID Number") },
                                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_uid_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )

                                            OutlinedTextField(
                                                value = editDateOfAdmission,
                                                onValueChange = { editDateOfAdmission = it },
                                                label = { Text("Date of Admission") },
                                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_doa_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )
                                        }
                                    }
                                }

                                // 3. Section: Contact & Personal Details
                                item {
                                    Text(
                                        text = "Contact & Residential Info",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F2A5C),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editEmail,
                                                onValueChange = { editEmail = it },
                                                label = { Text("Email Address") },
                                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_email_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )

                                            OutlinedTextField(
                                                value = editPhone,
                                                onValueChange = { editPhone = it },
                                                label = { Text("Phone Number") },
                                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_phone_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )

                                            OutlinedTextField(
                                                value = editAddress,
                                                onValueChange = { editAddress = it },
                                                label = { Text("Residential Address") },
                                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_address_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )
                                        }
                                    }
                                }

                                // 4. Section: Parent Details
                                item {
                                    Text(
                                        text = "Parental Info",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F2A5C),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editFatherName,
                                                onValueChange = { editFatherName = it },
                                                label = { Text("Father's Name") },
                                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_father_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )

                                            OutlinedTextField(
                                                value = editMotherName,
                                                onValueChange = { editMotherName = it },
                                                label = { Text("Mother's Name") },
                                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFF7A2E)) },
                                                modifier = Modifier.fillMaxWidth().testTag("edit_mother_${s.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0F2A5C),
                                                    focusedLabelColor = Color(0xFF0F2A5C)
                                                )
                                            )
                                        }
                                    }
                                }

                                // 5. Action Buttons (Save / Cancel)
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedStudentForEdit = null },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                            border = BorderStroke(1.5.dp, Color(0xFFDC2626))
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Cancel")
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (editName.isBlank() || editRollNo.isBlank()) {
                                                    Toast.makeText(context, "Name and Roll No cannot be blank.", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                viewModel.updateStudentDetails(
                                                    id = s.id,
                                                    name = editName,
                                                    section = editSection,
                                                    fatherName = editFatherName,
                                                    motherName = editMotherName,
                                                    address = editAddress,
                                                    phone = editPhone,
                                                    penNo = editPenNo,
                                                    aadharNo = editAadharNo,
                                                    email = editEmail,
                                                    uid = editUid,
                                                    dateOfAdmission = editDateOfAdmission
                                                )
                                                Toast.makeText(context, "Student profile updated successfully!", Toast.LENGTH_SHORT).show()
                                                selectedStudentForEdit = null
                                            },
                                            modifier = Modifier
                                                .weight(1.5f)
                                                .height(48.dp)
                                                .testTag("save_profile_button"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Save Changes", color = Color.White)
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    Text("Student Directory - Class 10A", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF0F2A5C))
                                    Text("Click any student to edit PEN, Aadhaar, phone, UID, email & admission date", fontSize = 11.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                items(viewModel.students) { s ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedStudentForEdit = s },
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (s.profilePic) {
                                                            "avatar1" -> Color(0xFFF59E0B)
                                                            "avatar2" -> Color(0xFF10B981)
                                                            "avatar3" -> Color(0xFF8B5CF6)
                                                            "avatar4" -> Color(0xFFEC4899)
                                                            "avatar5" -> Color(0xFF06B6D4)
                                                            "avatar6" -> Color(0xFF3B82F6)
                                                            else -> Color(0xFF3B82F6)
                                                        }
                                                    )
                                                    .border(1.5.dp, Color(0xFFFFC864), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (s.profilePic) {
                                                        "avatar1" -> Icons.Default.Face
                                                        "avatar2" -> Icons.Default.Favorite
                                                        "avatar3" -> Icons.Default.Star
                                                        "avatar4" -> Icons.Default.ThumbUp
                                                        "avatar5" -> Icons.Default.Check
                                                        else -> Icons.Default.AccountCircle
                                                    },
                                                    contentDescription = "Profile Photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(s.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F2A5C))
                                                Text("Admission No: ${s.admissionNo} · Roll No: ${s.rollNo}", fontSize = 11.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Father: ${s.fatherName} · Phone: ${s.phone}", fontSize = 11.sp, color = Color.DarkGray)
                                            }
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFFFF7A2E), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                item {
                                    NovaThinkFooter(lightTheme = true)
                                }
                            }
                        }
                    }

                    "Class Diary" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Text("Recent Notices & Broadcasts", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            items(viewModel.notices) { notice ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFFEE2E2))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(notice.category, color = Color(0xFFDC2626), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(notice.createdAt, fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(notice.body, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    "CBSE AI Lesson Planner" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            item {
                                Text("CBSE AI Micro-Lesson Planner", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Text("Powered by Sentry AI engine. Instantly builds a complete syllabus lesson plan.", fontSize = 11.sp, color = Color.Gray)
                            }
                            item {
                                OutlinedTextField(
                                    value = lessonTopic,
                                    onValueChange = { lessonTopic = it },
                                    label = { Text("Topic Name") },
                                    placeholder = { Text("e.g. Arrays and Loops in Kotlin") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = lessonChapter,
                                    onValueChange = { lessonChapter = it },
                                    label = { Text("Chapter Number / Section") },
                                    placeholder = { Text("e.g. Chapter 4.2") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Button(
                                    onClick = {
                                        if (lessonTopic.isNotBlank()) {
                                            isGeneratingLessonPlan = true
                                            lessonPlanResult = null
                                            Timer().schedule(object : TimerTask() {
                                                override fun run() {
                                                    isGeneratingLessonPlan = false
                                                    lessonPlanResult = """
                                                        📚 CBSE CLASS X LESSON PLAN OUTLINE
                                                        =================================
                                                        Chapter: $lessonChapter | Topic: $lessonTopic
                                                        Syllabus Standard: NCERT Computer Science
                                                        
                                                        1. LEARNING OBJECTIVES:
                                                           - Define the core architecture of $lessonTopic.
                                                           - Apply real-world application structures.
                                                           - Debug common structural syntax exceptions.
                                                           
                                                        2. CLASSROOM SCHEDULE (45 Mins):
                                                           - 0-10 Min: Concept briefing & blackboard diagram.
                                                           - 10-25 Min: Live execution of Kotlin syntax compiler.
                                                           - 25-40 Min: Student pair-programming lab task.
                                                           - 40-45 Min: Gate Sentry log system explanation.
                                                           
                                                        3. EXTENSION HOMEWORK WORKSHEET:
                                                           - Implement 3 variations of $lessonTopic and document.
                                                    """.trimIndent()
                                                }
                                            }, 1000)
                                        } else {
                                            Toast.makeText(context, "Please enter a topic first!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.TEACHER)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (isGeneratingLessonPlan) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text("Generate CBSE Syllabus Lesson Plan", color = Color.White)
                                    }
                                }
                            }
                            if (lessonPlanResult != null) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF10B981))
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("AI GENERATION SUCCESS", fontWeight = FontWeight.Black, fontSize = 10.sp, color = Color(0xFF047857))
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = lessonPlanResult!!,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF334155),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Marks Registry" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Text("CBSE Board Marks & Exams Entry", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text("Class: 10A · Subjects: CS & Science", color = Color.Gray, fontSize = 11.sp)
                            }
                            items(viewModel.students) { student ->
                                val csMark = studentMarksCS[student.id] ?: "92"
                                val scMark = studentMarksScience[student.id] ?: "88"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Roll No: ${student.rollNo} · PEN: ${student.id}", fontSize = 10.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = csMark,
                                                onValueChange = { studentMarksCS[student.id] = it },
                                                label = { Text("Comp Sci Mark", fontSize = 10.sp) },
                                                modifier = Modifier.weight(1f),
                                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                            )
                                            OutlinedTextField(
                                                value = scMark,
                                                onValueChange = { studentMarksScience[student.id] = it },
                                                label = { Text("Science Mark", fontSize = 10.sp) },
                                                modifier = Modifier.weight(1f),
                                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "CBSE Grades synchronized successfully!", Toast.LENGTH_LONG).show()
                                        activeMenu = "Main"
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.TEACHER)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Submit All Student Marks to CBSE Portal", color = Color.White)
                                }
                            }
                        }
                    }

                    "PTA Meeting Slots" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Text("Parent-Teacher Conference Scheduler", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text("Synchronized with active Parent portal bookings.", color = Color.Gray, fontSize = 11.sp)
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("🕒 03:00 PM - BOOKED", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                                        Text("Parent: Sundar S (Father of Archana)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Topic: Early departure medical checkup coordination", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("🕒 03:20 PM - BOOKED", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                                        Text("Parent: Priya Nair (Mother of Rahul)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Topic: Computer Science scholarship options", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("🕒 03:40 PM - AVAILABLE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                                                Text("Open for bookings", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Button(
                                                onClick = {
                                                    Toast.makeText(context, "Slot locked for guest parent!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text("Reserve Slot", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Sitting Chart" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            item {
                                Text("Classroom Sitting Chart Arranger", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text("Drag & drop style sitting matrix assignments.", color = Color.Gray, fontSize = 11.sp)
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("FRONT BENCHES (Near Blackboard)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFF7A2E))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(modifier = Modifier.weight(1f).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(10.dp)) {
                                                Text("Desk 1 Left:\nArchana S", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                            }
                                            Box(modifier = Modifier.weight(1f).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(10.dp)) {
                                                Text("Desk 1 Right:\nRahul M", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("MIDDLE BENCHES", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFF7A2E))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(modifier = Modifier.weight(1f).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(10.dp)) {
                                                Text("Desk 2 Left:\nDiya K", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                            }
                                            Box(modifier = Modifier.weight(1f).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(10.dp)) {
                                                Text("Desk 2 Right:\nArjun P", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Sitting chart layout synchronized with CBSE classroom monitor!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.TEACHER))
                                ) {
                                    Text("Broadcast Layout Update", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 9. ADMIN PORTAL
// ============================================================================

@Composable
fun AdminPortal(viewModel: SchoolViewModel) {
    var activeMenu by remember { mutableStateOf("Dashboard") }
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
        ) {
            Column {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeConfig.getGradientForRole(Role.ADMIN))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (activeMenu != "Dashboard") activeMenu = "Dashboard" else viewModel.currentScreen = "landing"
                        }
                    ) {
                        Icon(if (activeMenu == "Dashboard") Icons.Default.ExitToApp else Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ADMINISTRATOR PORTAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Schoolwide operations dashboard", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }

                when (activeMenu) {
                    "Dashboard" -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // High level metrics
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Buses on route", color = Color.Gray, fontSize = 10.sp)
                                            Text("2 Active", color = Color(0xFF10B981), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Incidents Week", color = Color.Gray, fontSize = 10.sp)
                                            Text("2 Open", color = Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Quick trigger for emergency schoolwide broadcasts
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = Color(0xFFDC2626))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Trigger Emergency School Broadcast", color = Color(0xFF991B1B), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                        }
                                        Text(
                                            "Instantly broadcast severe warning notifications to all parent and staff portals.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF7F1D1D),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                viewModel.addNotice(
                                                    Notice(
                                                        UUID.randomUUID().toString(),
                                                        "URGENT: Severe Rainfall School Holiday",
                                                        "Following government directive on heavy flooding warnings, Kendriya Vidyalaya Pattom will remain closed tomorrow.",
                                                        "Urgent", "all", "2026-07-12"
                                                    )
                                                )
                                                Toast.makeText(context, "Emergency warning broadcasted successfully!", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                        ) {
                                            Text("Broadcast Extreme Orange Alert", color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // Quick Menu links
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Management Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Button(
                                                onClick = { activeMenu = "Fleet" },
                                                colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.ADMIN))
                                            ) {
                                                Text("Fleet Mgr", color = Color.White)
                                            }
                                            Button(
                                                onClick = { activeMenu = "Incidents" },
                                                colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.ADMIN))
                                            ) {
                                                Text("Incidents", color = Color.White)
                                            }
                                            Button(
                                                onClick = { activeMenu = "Pickups" },
                                                colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.ADMIN))
                                            ) {
                                                Text("Pickups", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Fleet" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                Text("Vehicle Fleet Logistics", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            listOf(
                                "Bus 1" to "Route: Pattom - Peroorkada - Kowdiar",
                                "Bus 2" to "Route: Pattom - Medical College - Kesavadasapuram"
                            ).forEach { (bus, route) ->
                                item {
                                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Place, contentDescription = "Bus", tint = Color(0xFF0F2A5C))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(bus, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(route, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Incidents" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                Text("Incident Resolution Desk", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            items(viewModel.incidents) { inc ->
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row {
                                            Text(inc.type, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(inc.severity, color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        Text("Student: ${inc.studentName} · reported by ${inc.reportedBy}", fontSize = 11.sp, color = Color.Gray)
                                        Text(inc.notes, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    "Pickups" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                Text("Parent Early Departure Approvals", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }

                            if (viewModel.pickupRequests.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                        Text("No active early pickup requests submitted.", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                items(viewModel.pickupRequests) { req ->
                                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row {
                                                Text(req.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text(req.status, fontWeight = FontWeight.ExtraBold, color = if (req.status == "Approved") Color(0xFF16A34A) else Color(0xFFD97706))
                                            }
                                            Text("Reason: ${req.reason} at ${req.time}", fontSize = 11.sp, color = Color.Gray)
                                            if (req.status == "Pending") {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = { viewModel.updatePickupRequest(req.id, "Approved") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                                    ) {
                                                        Text("Approve", color = Color.White)
                                                    }
                                                    Button(
                                                        onClick = { viewModel.updatePickupRequest(req.id, "Rejected") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                                    ) {
                                                        Text("Reject", color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 10. CONDUCTOR PORTAL (STUDENT BOARDING SCANNER)
// ============================================================================

@Composable
fun ConductorPortal(viewModel: SchoolViewModel) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            KvsPmShriHeaderBar(
                title = "Conductor: Rajesh Nair",
                subtitle = "RFID Boarding Scanner · Bus 1 Route",
                onBackClick = { viewModel.currentScreen = "landing" },
                role = Role.CONDUCTOR,
                profileLetter = "R"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tap to Simulate Student RFID Smartcard Entry", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F2A5C))
                        Spacer(modifier = Modifier.height(16.dp))

                        viewModel.students.forEach { std ->
                            Button(
                                onClick = {
                                    val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                    viewModel.triggerBusEvent(std.name, "Boarded Bus", now)
                                    Toast.makeText(context, "${std.name} safety event broadcast to parents!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("simulate_board_${std.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getBgForRole(Role.CONDUCTOR)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = "Board", tint = ThemeConfig.getAccentColor(Role.CONDUCTOR))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simulate: ${std.name} Boarded", color = Color.Black)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                NovaThinkFooter(lightTheme = true)
            }
        }
    }
}

// ============================================================================
// 11. BUS DRIVER PORTAL
// ============================================================================

@Composable
fun BusDriverPortal(viewModel: SchoolViewModel) {
    var isTripActive by remember { mutableStateOf(viewModel.busTripActive.value) }
    var travelStatus by remember { mutableStateOf(viewModel.busRouteStatus.value) }

    // Dynamic GPS shifting loop
    LaunchedEffect(isTripActive) {
        if (isTripActive) {
            var step = 0
            while (isTripActive) {
                kotlinx.coroutines.delay(2000)
                // Shift coordinates slightly
                viewModel.busLatitude.value = 8.535 + (step * 0.0012)
                viewModel.busLongitude.value = 76.942 + (step * 0.0008)
                step = (step + 1) % 15
            }
        } else {
            viewModel.busLatitude.value = 8.535
            viewModel.busLongitude.value = 76.942
        }
    }

    Scaffold(
        topBar = {
            KvsPmShriHeaderBar(
                title = "Driver: Mohan Lal",
                subtitle = "KV Pattom Route 1 Bus (KL-01-CB-4011)",
                onBackClick = { viewModel.currentScreen = "landing" },
                role = Role.BUS_DRIVER,
                profileLetter = "M"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Route: Bus 1 (KVS Pattom - Kowdiar)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Sentry Tracking Status: $travelStatus", color = ThemeConfig.getAccentColor(Role.BUS_DRIVER), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isTripActive) {
                            Text(
                                "📡 LIVE GPS Broadcast: ${String.format(Locale.US, "%.5f", viewModel.busLatitude.value)}° N, ${String.format(Locale.US, "%.5f", viewModel.busLongitude.value)}° E",
                                color = Color(0xFF16A34A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text("GPS telemetry is offline. Trip idle in transit yard.", color = Color.Gray, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                isTripActive = !isTripActive
                                travelStatus = if (isTripActive) "Trip Started - Tracking Active" else "Trip Ended"
                                viewModel.busTripActive.value = isTripActive
                                viewModel.busRouteStatus.value = travelStatus
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTripActive) Color(0xFFDC2626) else ThemeConfig.getAccentColor(Role.BUS_DRIVER)
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isTripActive) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = "Toggle Trip",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTripActive) "End Current Trip" else "Start Trip Navigation", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Conductor/Driver notes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Sentry Transit Guidelines:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("1. Confirm all students are scanned in via Conductor RFID before departure.", fontSize = 11.sp, color = Color.Gray)
                        Text("2. Keep active route navigation enabled to broadcast telemetry to parents.", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                NovaThinkFooter(lightTheme = true)
            }
        }
    }
}

// ============================================================================
// 12. SECURITY GUARD PORTAL
// ============================================================================

@Composable
fun SecurityPortal(viewModel: SchoolViewModel) {
    var activeSubTab by remember { mutableStateOf("Gate Log") }
    
    // Gate Log inputs
    var visitorName by remember { mutableStateOf("") }
    var visitorPurpose by remember { mutableStateOf("") }
    
    // Early Pickup inputs
    var pickupStudentName by remember { mutableStateOf("Archana S") }
    var pickupVisitorName by remember { mutableStateOf("") }
    var pickupRelation by remember { mutableStateOf("") }
    var pickupReason by remember { mutableStateOf("") }
    var isPhotoCaptured by remember { mutableStateOf(false) }
    
    // Camera simulation state
    var showCameraViewfinder by remember { mutableStateOf(false) }
    var cameraCountdown by remember { mutableStateOf(3) }
    var cameraFlashActive by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    if (showCameraViewfinder) {
        LaunchedEffect(showCameraViewfinder) {
            cameraCountdown = 3
            while (cameraCountdown > 0) {
                kotlinx.coroutines.delay(1000)
                cameraCountdown--
            }
            cameraFlashActive = true
            kotlinx.coroutines.delay(150)
            cameraFlashActive = false
            isPhotoCaptured = true
            showCameraViewfinder = false
            Toast.makeText(context, "Visitor profile picture captured securely!", Toast.LENGTH_SHORT).show()
        }

        AlertDialog(
            onDismissRequest = { showCameraViewfinder = false },
            title = { Text("Gate Security AI Camera", fontWeight = FontWeight.Bold, color = Color(0xFF0F2A5C)) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (cameraFlashActive) Color.White else Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (!cameraFlashActive) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Face, contentDescription = "Sentry Lens", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("SENTRY GATE CAMERA ACTIVE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Taking snapshot in", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("$cameraCountdown", color = Color(0xFFFFC864), fontSize = 42.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showCameraViewfinder = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                KvsPmShriHeaderBar(
                    title = "Sentry: Saji Kumar",
                    subtitle = "KV Pattom Shift 2 Main Gate",
                    onBackClick = { viewModel.currentScreen = "landing" },
                    role = Role.SECURITY,
                    profileLetter = "S"
                )
                TabRow(
                    selectedTabIndex = if (activeSubTab == "Gate Log") 0 else 1,
                    containerColor = Color.White,
                    contentColor = ThemeConfig.getAccentColor(Role.SECURITY)
                ) {
                    Tab(
                        selected = activeSubTab == "Gate Log",
                        onClick = { activeSubTab = "Gate Log" },
                        text = { Text("Gate Entrance Sentry", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeSubTab == "Early Pickup",
                        onClick = { activeSubTab = "Early Pickup" },
                        text = { Text("Early Pickup Dispatch", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
        ) {
            if (activeSubTab == "Gate Log") {
                // Routine Visitor Log View
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Record Routine Campus Entry", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                                Text("Log general visitors entering school campus hours", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                                
                                OutlinedTextField(
                                    value = visitorName, 
                                    onValueChange = { visitorName = it }, 
                                    label = { Text("Visitor Name") }, 
                                    placeholder = { Text("e.g. Manoj Sharma") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = visitorPurpose, 
                                    onValueChange = { visitorPurpose = it }, 
                                    label = { Text("Purpose of Visit") }, 
                                    placeholder = { Text("e.g. Parent-Teacher Meet, Office work") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        if (visitorName.isBlank() || visitorPurpose.isBlank()) {
                                            Toast.makeText(context, "Please enter visitor name and purpose.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                        viewModel.addVisitor(VisitorLog(UUID.randomUUID().toString(), visitorName, visitorPurpose, now))
                                        visitorName = ""
                                        visitorPurpose = ""
                                        Toast.makeText(context, "Sentry visitor gate check-in recorded!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeConfig.getAccentColor(Role.SECURITY)),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Log", tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Log Visitor Gate Entrance", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Today's Campus Visitor Logs", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                    }

                    if (viewModel.visitorLogs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No visitors logged entering today.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(viewModel.visitorLogs) { vis ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDCF2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AccountBox, contentDescription = "Visitor", tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(vis.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                                        Text(vis.purpose, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text(vis.inTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }

                    item {
                        NovaThinkFooter(lightTheme = true)
                    }
                }
            } else {
                // Early Pickup Dispatch View
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("New Early Pickup Parent Broadcast", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                                Text("Secures early release of child during school hours", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                                OutlinedTextField(
                                    value = pickupStudentName, 
                                    onValueChange = { pickupStudentName = it }, 
                                    label = { Text("Student Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = pickupVisitorName, 
                                    onValueChange = { pickupVisitorName = it }, 
                                    label = { Text("Visitor / Guardian Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = pickupRelation, 
                                    onValueChange = { pickupRelation = it }, 
                                    label = { Text("Relationship with Student") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = pickupReason, 
                                    onValueChange = { pickupReason = it }, 
                                    label = { Text("Reason for Early Pickup") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                // Camera action row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = { showCameraViewfinder = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A2E)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Face, contentDescription = "Snapshot", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isPhotoCaptured) "Retake Photo (Done)" else "Capture Visitor Photo", fontSize = 11.sp, color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (pickupVisitorName.isBlank() || pickupRelation.isBlank() || pickupReason.isBlank()) {
                                            Toast.makeText(context, "Please fill in all early pickup fields.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (!isPhotoCaptured) {
                                            Toast.makeText(context, "Sentry rules REQUIRE capturing a photo of the visitor first!", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                        viewModel.addPickupRequest(
                                            PickupRequest(
                                                id = UUID.randomUUID().toString(),
                                                studentId = "s1",
                                                studentName = pickupStudentName,
                                                section = "Class 10A",
                                                reason = pickupReason,
                                                time = now,
                                                status = "Pending",
                                                visitorName = pickupVisitorName,
                                                relationship = pickupRelation,
                                                visitorPhoto = "captured"
                                            )
                                        )
                                        pickupVisitorName = ""
                                        pickupRelation = ""
                                        pickupReason = ""
                                        isPhotoCaptured = false
                                        Toast.makeText(context, "Pickup Request broadcast to Parents in real-time!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Broadcast", tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Broadcast Request to Parents", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Active Early Pickup Status Board", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                    }

                    if (viewModel.pickupRequests.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No early pickup requests broadcast today.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(viewModel.pickupRequests) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = when (req.status) {
                                        "Approved" -> Color(0xFF16A34A)
                                        "Rejected" -> Color(0xFFDC2626)
                                        else -> Color(0xFFD97706)
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(req.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                                        Spacer(modifier = Modifier.weight(1f))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (req.status) {
                                                        "Approved" -> Color(0xFFDCF2FE)
                                                        "Rejected" -> Color(0xFFFEE2E2)
                                                        else -> Color(0xFFFEF3C7)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = when (req.status) {
                                                    "Approved" -> "APPROVED - SAFE TO RELEASE"
                                                    "Rejected" -> "BLOCKED - DENIED BY PARENT"
                                                    else -> "PENDING PARENT CONFIRM"
                                                },
                                                color = when (req.status) {
                                                    "Approved" -> Color(0xFF0369A1)
                                                    "Rejected" -> Color(0xFF991B1B)
                                                    else -> Color(0xFFB45309)
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Visitor: ${req.visitorName} (${req.relationship})", fontSize = 11.sp, color = Color.DarkGray)
                                    Text("Reason: ${req.reason} · Time: ${req.time}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    item {
                        NovaThinkFooter(lightTheme = true)
                    }
                }
            }
        }
    }
}

// ============================================================================
// 13. KVS BRANDED COMMON UI COMPONENTS
// ============================================================================

@Composable
fun NovaThinkFooter(lightTheme: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Created by Team NovaThink (PM SHRI KV Pattom Shift 2)",
                color = if (lightTheme) Color(0xFF0F2A5C) else Color(0xFFFFC864),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun KvsPmShriHeaderBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    role: Role,
    profileLetter: String = "A",
    profilePic: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ThemeConfig.getGradientForRole(role))
    ) {
        // PM SHRI & KVS LOGOS ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: KVS Logo Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC864)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("KV", color = Color(0xFF0F2A5C), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("KVS PATTOM", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("SHIFT 2", color = Color(0xFFFFC864), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Right: PM SHRI Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF7A2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = "PM SHRI", tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("PM SHRI", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("SCHOOLS", color = Color(0xFFFF7A2E), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

        // Portal Title & Profile Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            // Student Profile photo or letter
            if (profilePic.isNotEmpty()) {
                val avatarColor = when (profilePic) {
                    "avatar1" -> Color(0xFFF59E0B)
                    "avatar2" -> Color(0xFF10B981)
                    "avatar3" -> Color(0xFF8B5CF6)
                    else -> Color(0xFF3B82F6)
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(avatarColor)
                        .border(1.5.dp, Color(0xFFFFC864), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (profilePic) {
                            "avatar1" -> Icons.Default.Face
                            "avatar2" -> Icons.Default.Favorite
                            "avatar3" -> Icons.Default.Star
                            else -> Icons.Default.AccountCircle
                        },
                        contentDescription = "Profile Photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(profileLetter, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KvsAiSuite(role: Role) {
    var selectedFeature by remember { mutableStateOf("Main") }
    
    // Feature States
    var quickQuery by remember { mutableStateOf("") }
    var quickResponse by remember { mutableStateOf("") }
    var isQuickLoading by remember { mutableStateOf(false) }

    var thinkingQuery by remember { mutableStateOf("") }
    var thinkingResponse by remember { mutableStateOf("") }
    var isThinkingLoading by remember { mutableStateOf(false) }
    val thinkingSteps = remember { mutableStateListOf<String>() }

    var videoPrompt by remember { mutableStateOf("") }
    var videoRatio by remember { mutableStateOf("16:9") }
    var generatedVideoUrl by remember { mutableStateOf("") }
    var isVideoLoading by remember { mutableStateOf(false) }

    var isRecording by remember { mutableStateOf(false) }
    var transcriptionResult by remember { mutableStateOf("") }
    var isTranscribing by remember { mutableStateOf(false) }

    var isLiveVoiceCallActive by remember { mutableStateOf(false) }
    var liveVoiceText by remember { mutableStateOf("Tap mic to start Live Voice Call...") }

    var isVideoAnalyzing by remember { mutableStateOf(false) }
    var videoAnalysisResult by remember { mutableStateOf("") }
    var selectedCameraFeed by remember { mutableStateOf("Gate Camera 1") }

    val accentColor = ThemeConfig.getAccentColor(role)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (selectedFeature != "Main") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedFeature = "Main" }
                    .padding(bottom = 12.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to PM SHRI KVS AI Suite", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        when (selectedFeature) {
            "Main" -> {
                Text(
                    text = "PM SHRI KVS AI Suite",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F2A5C)
                )
                Text(
                    text = "Powered by Google Gemini Models",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFeature = "quick" },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5F7EE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Flash Lite", tint = Color(0xFF1FA971), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Low-Latency School Query", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("gemini-3.1-flash-lite · Fast answers", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFeature = "thinking" },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE4EEFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Pro Thinking", tint = Color(0xFF2A6FDB), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Academic Deep Thinking Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("gemini-3.1-pro-preview · HIGH thinking", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFeature = "video" },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Veo 3.1", tint = Color(0xFFE8A317), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Veo Educational Video Gen", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("veo-3.1-fast-generate-preview · 16:9 / 9:16", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFeature = "voice" },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Live Voice", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Gemini Live Voice Assistant", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("gemini-3.1-flash-live-preview · Live audio", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFeature = "audio" },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3E8FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Audio Transcription", tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Audio Transcription Engine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("gemini-3.5-flash · Speech-to-text safety", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFeature = "safety" },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Video Analysis", tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Video Security Intelligence", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("gemini-3.1-pro-preview · Multi-Modal AI", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            "quick" -> {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Lightning-Fast School Assistant", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                        Text("Powered by gemini-3.1-flash-lite (ultra low latency)", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = quickQuery,
                            onValueChange = { quickQuery = it },
                            placeholder = { Text("Ask about school rules, timings...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                isQuickLoading = true
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        isQuickLoading = false
                                        quickResponse = "⚡ [gemini-3.1-flash-lite Response - 110ms]\nKendriya Vidyalaya Pattom Shift 2 strictly follows CBSE curriculum and maintains school hours from 08:30 AM to 02:40 PM. Transport routes are checked in real-time."
                                    }
                                }, 400)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1FA971)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isQuickLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Query Instantly", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        if (quickResponse.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE5F7EE)).padding(10.dp).clip(RoundedCornerShape(6.dp))) {
                                Text(quickResponse, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            "thinking" -> {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Academic Deep Thinking Planner", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                        Text("Model: gemini-3.1-pro-preview (ThinkingLevel: HIGH)", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = thinkingQuery,
                            onValueChange = { thinkingQuery = it },
                            placeholder = { Text("Describe learning challenges or goals...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                isThinkingLoading = true
                                thinkingSteps.clear()
                                thinkingResponse = ""
                                val steps = listOf(
                                    "Retrieving student performance indicators for Archana S...",
                                    "Evaluating latest exam scores (Math 48/50, CS 47/50)...",
                                    "Determining learning curve parameters and potential gap areas...",
                                    "Applying advanced pedagogical tutoring recommendations..."
                                )
                                var currentStepIndex = 0
                                Timer().scheduleAtFixedRate(object : TimerTask() {
                                    override fun run() {
                                        if (currentStepIndex < steps.size) {
                                            thinkingSteps.add(steps[currentStepIndex])
                                            currentStepIndex++
                                        } else {
                                            thinkingResponse = "🧠 [gemini-3.1-pro-preview High-Level Plan]\n\nBased on Archana's high performance across subjects, we recommend advancing her to competitive Olympiad training in Mathematics (focusing on Combinatorics) and exploring full-stack Web Development in Computer Science."
                                            isThinkingLoading = false
                                            cancel()
                                        }
                                    }
                                }, 0, 800)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A6FDB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isThinkingLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Engage Deep AI Thinking", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        if (thinkingSteps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gemini Thinking Process Logs:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                            Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                thinkingSteps.forEach { step ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = "Step", tint = Color(0xFF2A6FDB), modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(step, fontSize = 9.sp, fontStyle = FontStyle.Italic)
                                    }
                                }
                            }
                        }

                        if (thinkingResponse.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE4EEFC)).padding(10.dp).clip(RoundedCornerShape(6.dp))) {
                                Text(thinkingResponse, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            "video" -> {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Veo Text-to-Video Laboratory", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                        Text("Model: veo-3.1-fast-generate-preview", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = videoPrompt,
                            onValueChange = { videoPrompt = it },
                            placeholder = { Text("Describe educational video...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("16:9", "9:16").forEach { ratio ->
                                FilterChip(
                                    selected = videoRatio == ratio,
                                    onClick = { videoRatio = ratio },
                                    label = { Text(ratio, fontSize = 11.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                isVideoLoading = true
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        isVideoLoading = false
                                        generatedVideoUrl = "veo_generated_success"
                                    }
                                }, 2000)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8A317)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isVideoLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Generate Veo Concept Video", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        if (generatedVideoUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Generated Educational Preview (${videoRatio}):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(if (videoRatio == "16:9") 1.77f else 0.56f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(36.dp))
                                    Text("Looped Concept Simulation", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(videoPrompt, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 12.dp))
                                }
                            }
                        }
                    }
                }
            }

            "voice" -> {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gemini Live Voice Portal", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                        Text("Model: gemini-3.1-flash-live-preview", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(if (isLiveVoiceCallActive) Color(0xFFFEE2E2) else Color(0xFFE2E8F0))
                                .clickable {
                                    isLiveVoiceCallActive = !isLiveVoiceCallActive
                                    liveVoiceText = if (isLiveVoiceCallActive) {
                                        "🟢 Live Connected!\nAI: 'Welcome to KVS EduShield Live Portal. How can I assist you today?'"
                                    } else {
                                        "Tap mic to start Live Voice Call..."
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Mic",
                                tint = if (isLiveVoiceCallActive) Color(0xFFEF4444) else Color(0xFF475569),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = liveVoiceText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isLiveVoiceCallActive) Color(0xFF1E293B) else Color.Gray,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }
            }

            "audio" -> {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Speech-to-Text Safety Logger", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                        Text("Model: gemini-3.5-flash", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                isRecording = true
                                isTranscribing = false
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        isRecording = false
                                        isTranscribing = true
                                        Timer().schedule(object : TimerTask() {
                                            override fun run() {
                                                isTranscribing = false
                                                transcriptionResult = "🎙️ [gemini-3.5-flash Audio Transcription]\n\n\"Archana entered class safely after arriving at 08:15 AM via bus 1.\""
                                            }
                                        }, 1000)
                                    }
                                }, 1500)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isRecording) "🔴 Recording..." else "Tap to Record safety notes", color = Color.White, fontSize = 12.sp)
                        }

                        if (isTranscribing) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Transcribing voice audio using Gemini...", fontSize = 10.sp, fontStyle = FontStyle.Italic)
                            }
                        }

                        if (transcriptionResult.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3E8FF)).padding(10.dp).clip(RoundedCornerShape(6.dp))) {
                                Text(transcriptionResult, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            "safety" -> {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Video Sentry Analyzer", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F2A5C))
                        Text("Model: gemini-3.1-pro-preview", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Gate Camera 1", "Gate Camera 2", "Bus 1 Feed").forEach { cam ->
                                FilterChip(
                                    selected = selectedCameraFeed == cam,
                                    onClick = { selectedCameraFeed = cam },
                                    label = { Text(cam, fontSize = 11.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                isVideoAnalyzing = true
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        isVideoAnalyzing = false
                                        videoAnalysisResult = "👁️ [gemini-3.1-pro-preview Analysis]\n\nTarget Feed: ${selectedCameraFeed}\n- Detected: 1 Visitor requesting early pickup of student ID s1 (Archana S).\n- Identity Match: Rajesh Nair (Uncle/Guardian) matched with high confidence."
                                    }
                                }, 1500)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isVideoAnalyzing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Analyze Live Feed Frame", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        if (videoAnalysisResult.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0F2FE)).padding(10.dp).clip(RoundedCornerShape(6.dp))) {
                                Text(videoAnalysisResult, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
