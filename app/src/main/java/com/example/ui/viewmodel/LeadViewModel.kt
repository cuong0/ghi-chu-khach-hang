package com.example.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Lead
import com.example.data.repository.LeadRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.Utility
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.TimeZone

class LeadViewModel(
    private val leadRepository: LeadRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    // Leads list
    val allLeads: StateFlow<List<Lead>> = leadRepository.allLeads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique areas combined from Saved Customers and User Custom Inputs
    val customAreas: StateFlow<List<String>> = settingsRepository.customAreas

    val distinctAreas: StateFlow<List<String>> = combine(
        leadRepository.distinctAreas,
        settingsRepository.customAreas
    ) { dbAreas, userCustomAreas ->
        // Return combined distinct list sorted alphabetically without hardcoded values
        (dbAreas + userCustomAreas).distinct().filter { it.isNotBlank() }.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomArea(area: String) {
        settingsRepository.addCustomArea(area)
    }

    fun removeCustomArea(area: String) {
        settingsRepository.removeCustomArea(area)
    }

    fun clearAllCustomAreas() {
        settingsRepository.clearAllCustomAreas()
    }

    // Selected area filter for Tab 2
    private val _selectedArea = MutableStateFlow<String?>(null)
    val selectedArea: StateFlow<String?> = _selectedArea.asStateFlow()

    // Leads filtered by area
    val filteredLeads: StateFlow<List<Lead>> = combine(allLeads, _selectedArea) { leads, area ->
        if (area.isNullOrEmpty()) {
            leads
        } else {
            leads.filter { it.area == area }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings & Profile from preferences
    val isDarkTheme: StateFlow<Boolean> = settingsRepository.isDarkTheme
    val isSyncEnabled: StateFlow<Boolean> = settingsRepository.isSyncEnabled
    val isLoggedIn: StateFlow<Boolean> = settingsRepository.isLoggedIn
    val userName: StateFlow<String> = settingsRepository.userName
    val userPhone: StateFlow<String> = settingsRepository.userPhone
    val userEmail: StateFlow<String> = settingsRepository.userEmail
    val userAvatarUri: StateFlow<String> = settingsRepository.userAvatarUri

    // Temporary values for "Add Customer" Screen form states
    val nameInput = MutableStateFlow("")
    val phoneInput = MutableStateFlow("")
    val addressInput = MutableStateFlow("")
    val pickedLatitude = MutableStateFlow<Double?>(null)
    val pickedLongitude = MutableStateFlow<Double?>(null)
    val areaInput = MutableStateFlow("")
    val notesInput = MutableStateFlow("")
    val recontactDateInput = MutableStateFlow(System.currentTimeMillis() + 24 * 60 * 60 * 1000) // Tomorrow default

    // Temporary values for "Auth/Signup" manual manual forms
    val authNameInput = MutableStateFlow("")
    val authPhoneInput = MutableStateFlow("")
    val authEmailInput = MutableStateFlow("")
    val authVerificationCode = MutableStateFlow("")
    val isCodeSent = MutableStateFlow(false)
    val sentCodeValue = MutableStateFlow("")

    fun selectArea(area: String?) {
        _selectedArea.value = area
    }

    fun setDarkTheme(enabled: Boolean) {
        settingsRepository.setDarkTheme(enabled)
    }

    fun setSyncEnabled(enabled: Boolean) {
        if (!isLoggedIn.value && enabled) {
            Toast.makeText(context, "Vui lòng đăng nhập từ mục Cá Nhân để bật đồng bộ hóa!", Toast.LENGTH_LONG).show()
            return
        }
        settingsRepository.setSyncEnabled(enabled)
        if (enabled) {
            Toast.makeText(context, "Đã bật tự động đồng bộ hóa đám mây!", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginWithGoogle(name: String, email: String, phone: String = "") {
        settingsRepository.login(name, phone.ifBlank { "Chưa cập nhật" }, email)
        Toast.makeText(context, "Đăng nhập Google thành công! Chào mừng $name.", Toast.LENGTH_SHORT).show()
    }

    fun loginWithFacebook(name: String, email: String, phone: String = "") {
        settingsRepository.login(name, phone.ifBlank { "Chưa cập nhật" }, email)
        Toast.makeText(context, "Đăng nhập Facebook thành công! Chào mừng $name.", Toast.LENGTH_SHORT).show()
    }

    fun logout() {
        settingsRepository.logout()
        Toast.makeText(context, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
    }

    fun updateAvatar(uri: String) {
        settingsRepository.updateAvatar(uri)
    }

    fun setManualSignUpInputs(name: String, phone: String, email: String) {
        authNameInput.value = name
        authPhoneInput.value = phone
        authEmailInput.value = email
    }

    fun sendVerificationCode() {
        val email = authEmailInput.value.trim()
        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(context, "Vui lòng nhập Email Gmail hợp lệ!", Toast.LENGTH_SHORT).show()
            return
        }
        val randomCode = (100000..999999).random().toString()
        sentCodeValue.value = randomCode
        isCodeSent.value = true
        // Simulate email sending success
        Toast.makeText(context, "Mã xác thực [ $randomCode ] đã được gửi tới $email!", Toast.LENGTH_LONG).show()
    }

    fun verifyAndSignUp(): Boolean {
        val enteredCode = authVerificationCode.value.trim()
        if (enteredCode == sentCodeValue.value) {
            settingsRepository.login(
                authNameInput.value.ifBlank { "Khách hàng Mới" },
                authPhoneInput.value.ifBlank { "090000000" },
                authEmailInput.value
            )
            Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
            // Reset fields
            authNameInput.value = ""
            authPhoneInput.value = ""
            authEmailInput.value = ""
            authVerificationCode.value = ""
            isCodeSent.value = false
            return true
        } else {
            Toast.makeText(context, "Mã xác minh không chính xác, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun sendResetPasswordCode() {
        val email = authEmailInput.value.trim()
        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(context, "Vui lòng nhập Email liên kết để khôi phục!", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, "Yêu cầu khôi phục mật khẩu đã được gửi tới $email!", Toast.LENGTH_LONG).show()
    }

    fun saveLead(onSuccess: () -> Unit) {
        val name = nameInput.value.trim()
        val phone = phoneInput.value.trim()
        val address = addressInput.value.trim()
        val area = areaInput.value.trim()
        val notes = notesInput.value.trim()
        val recontactDate = recontactDateInput.value

        if (name.isEmpty()) {
            Toast.makeText(context, "Tên khách hàng không được để trống!", Toast.LENGTH_SHORT).show()
            return
        }
        if (phone.isEmpty()) {
            Toast.makeText(context, "Số điện thoại không được để trống!", Toast.LENGTH_SHORT).show()
            return
        }
        if (area.isEmpty()) {
            Toast.makeText(context, "Khu vực không được để trống!", Toast.LENGTH_SHORT).show()
            return
        }

        // Auto-register newly entered custom area to persistent list
        addCustomArea(area)

        val lead = Lead(
            name = name,
            phone = phone,
            address = address,
            latitude = pickedLatitude.value,
            longitude = pickedLongitude.value,
            area = area,
            notes = notes,
            recontactDate = recontactDate
        )

        viewModelScope.launch {
            leadRepository.insert(lead)
            // Perform Android native Calendar Event insertion safely
            addCalendarEvent(context, name, recontactDate)
            
            // Clean up states
            nameInput.value = ""
            phoneInput.value = ""
            addressInput.value = ""
            pickedLatitude.value = null
            pickedLongitude.value = null
            areaInput.value = ""
            notesInput.value = ""
            recontactDateInput.value = System.currentTimeMillis() + 24 * 60 * 60 * 1000

            Toast.makeText(context, "Đã lưu khách hàng và tạo lịch hẹn hẹn gặp!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    fun deleteLead(lead: Lead) {
        viewModelScope.launch {
            leadRepository.delete(lead)
            Toast.makeText(context, "Đã xóa khách hàng ${lead.name}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCsvContentToExport(): String {
        return Utility.exportLeadsToCsv(allLeads.value)
    }

    fun importLeadsFromCsv(csvContent: String, onComplete: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val parsedLeads = Utility.parseLeadsFromCsv(csvContent)
                if (parsedLeads.isEmpty()) {
                    onError("Không tìm thấy dữ liệu hợp lệ hoặc tệp rỗng.")
                    return@launch
                }
                var importedCount = 0
                for (lead in parsedLeads) {
                    leadRepository.insert(lead)
                    if (lead.area.isNotEmpty()) {
                        addCustomArea(lead.area)
                    }
                    importedCount++
                }
                onComplete(importedCount)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Lỗi không xác định khi nhập dữ liệu CSV.")
            }
        }
    }

    private fun getWritableCalendarId(context: Context): Long {
        val projection = arrayOf(
            CalendarContract.Calendars._ID
        )
        try {
            val cursor = context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idCol = it.getColumnIndex(CalendarContract.Calendars._ID)
                    if (idCol != -1) {
                        return it.getLong(idCol)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 1L
    }

    private fun addCalendarEvent(context: Context, leadName: String, recontactTimeMillis: Long) {
        try {
            val cr = context.contentResolver
            val calId = getWritableCalendarId(context)
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, recontactTimeMillis)
                put(CalendarContract.Events.DTEND, recontactTimeMillis + 60 * 60 * 1000) // 1 Hour meeting duration
                put(CalendarContract.Events.TITLE, "Hẹn gặp $leadName (Lead Notes)")
                put(CalendarContract.Events.DESCRIPTION, "Gặp mặt hỗ trợ khách hàng tiềm năng: $leadName. Lịch được tạo tự động bởi Lead Notes.")
                put(CalendarContract.Events.CALENDAR_ID, calId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }
            val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                val eventId = uri.lastPathSegment?.toLongOrNull()
                if (eventId != null) {
                    val reminderValues = ContentValues().apply {
                        put(CalendarContract.Reminders.EVENT_ID, eventId)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                        put(CalendarContract.Reminders.MINUTES, 15) // Reminder trigger 15 minutes before re-contact
                    }
                    cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                }
            }
        } catch (e: SecurityException) {
            // Graceful fallback, will be pre-granted if the user permitted, elsewhere logs silently without crashing.
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class LeadViewModelFactory(
    private val leadRepository: LeadRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeadViewModel(leadRepository, settingsRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
