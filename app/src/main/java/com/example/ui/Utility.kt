package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Lead
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utility {
    fun formatDateTime(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Chưa xác định"
        }
    }

    fun formatDateOnly(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Chưa xác định"
        }
    }

    fun openGoogleMaps(context: Context, address: String, latitude: Double?, longitude: Double?) {
        try {
            val intentUri = if (latitude != null && longitude != null) {
                Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(address.ifEmpty { "Khách hàng" })})")
            } else if (address.isNotEmpty()) {
                Uri.parse("geo:0,0?q=${Uri.encode(address)}")
            } else {
                Uri.parse("geo:14.0583,108.2772?q=Vietnam")
            }
            val intent = Intent(Intent.ACTION_VIEW, intentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackUri = if (latitude != null && longitude != null) {
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                } else if (address.isNotEmpty()) {
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
                } else {
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=Vietnam")
                }
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                // Gracefully catch any unhandled activity launch errors
            }
        }
    }

    // High fidelity simulated locations in Vietnam for the Map Picker click interactions
    data class SimulatedLocation(
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double
    )

    val SIMULATED_VIETNAM_LOCATIONS = listOf(
        SimulatedLocation("Hồ Chí Minh", "120 Lê Lợi, Phường Bến Thành, Quận 1, TP. HCM", 10.7769, 106.6953),
        SimulatedLocation("Hà Nội", "36 Cát Linh, Quận Đống Đa, Hà Nội", 21.0285, 105.8542),
        SimulatedLocation("Đà Nẵng", "48 Bạch Đằng, Hải Châu, Đà Nẵng", 16.0544, 108.2022),
        SimulatedLocation("Cần Thơ", "15 Hòa Bình, Ninh Kiều, Cần Thơ", 10.0371, 105.7882),
        SimulatedLocation("Hải Phòng", "52 Lạch Tray, Ngô Quyền, Hải Phòng", 20.8449, 106.6881),
        SimulatedLocation("Nha Trang", "78 Trần Phú, Nha Trang, Khánh Hòa", 12.2388, 109.1967),
        SimulatedLocation("Đà Lạt", "01 Trần Quốc Toản, Đà Lạt, Lâm Đồng", 11.9404, 108.4402)
    )

    // Export a list of Leads to standard UTF-8 BOM CSV that is opened perfectly by Microsoft Excel
    fun exportLeadsToCsv(leads: List<Lead>): String {
        val sb = StringBuilder()
        // Unicode UTF-8 BOM character to let Excel know this is encoded in UTF-8
        sb.append("\uFEFF")
        // CSV CSV Header
        sb.append("Mã khách hàng,Họ và tên,Số điện thoại,Địa chỉ,Vĩ độ,Kinh độ,Khu vực,Ghi chú,Ngày liên hệ lại,Ngày tạo\n")
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi"))
        for (lead in leads) {
            val rowData = listOf(
                lead.id.toString(),
                lead.name,
                lead.phone,
                lead.address,
                lead.latitude?.toString() ?: "",
                lead.longitude?.toString() ?: "",
                lead.area,
                lead.notes,
                sdf.format(Date(lead.recontactDate)),
                sdf.format(Date(lead.createdAt))
            )
            val escapedLine = rowData.joinToString(",") { escapeCsvField(it) }
            sb.append(escapedLine).append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsvField(field: String): String {
        // Enclose in quotes if it contains separator, quote, or new lines
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            val escaped = field.replace("\"", "\"\"")
            return "\"$escaped\""
        }
        return field
    }

    // Parse an exported CSV back to Lead instances matching the original fields
    fun parseLeadsFromCsv(csvContent: String): List<Lead> {
        val leads = mutableListOf<Lead>()
        val rows = splitCsvRows(csvContent)
        if (rows.isEmpty()) return emptyList()

        val headerRow = rows[0]
        val hasHeaders = headerRow.any { 
            it.contains("Mã khách hàng") || it.contains("Họ và tên") || it.contains("Số điện thoại") || it.contains("khách hàng") 
        }
        val dataStartIndex = if (hasHeaders) 1 else 0

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi"))

        for (i in dataStartIndex until rows.size) {
            val cols = rows[i]
            if (cols.size < 3) continue // Needs at least ID, Name, Phone

            try {
                var rawId = cols[0].trim()
                // Clean up any BOM character if reading directly on start
                if (rawId.startsWith("\uFEFF")) {
                    rawId = rawId.substring(1)
                }

                val idValue = rawId.toIntOrNull() ?: 0
                val nameValue = cols.getOrNull(1)?.trim() ?: ""
                val phoneValue = cols.getOrNull(2)?.trim() ?: ""

                // Ignore incomplete entries
                if (nameValue.isEmpty()) continue

                val addressValue = cols.getOrNull(3)?.trim() ?: ""
                val latValue = cols.getOrNull(4)?.trim()?.toDoubleOrNull()
                val lngValue = cols.getOrNull(5)?.trim()?.toDoubleOrNull()
                val areaValue = cols.getOrNull(6)?.trim() ?: ""
                val notesValue = cols.getOrNull(7)?.trim() ?: ""

                val recontactStr = cols.getOrNull(8)?.trim() ?: ""
                val recontactMillis = try {
                    sdf.parse(recontactStr)?.time ?: (System.currentTimeMillis() + 86400000)
                } catch (e: Exception) {
                    recontactStr.toLongOrNull() ?: (System.currentTimeMillis() + 86400000)
                }

                val createdStr = cols.getOrNull(9)?.trim() ?: ""
                val createdMillis = try {
                    sdf.parse(createdStr)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    createdStr.toLongOrNull() ?: System.currentTimeMillis()
                }

                leads.add(
                    Lead(
                        id = idValue,
                        name = nameValue,
                        phone = phoneValue,
                        address = addressValue,
                        latitude = latValue,
                        longitude = lngValue,
                        area = areaValue,
                        notes = notesValue,
                        recontactDate = recontactMillis,
                        createdAt = createdMillis
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return leads
    }

    private fun splitCsvRows(csv: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        var currentField = StringBuilder()
        var inQuotes = false
        var colIndex = 0
        val length = csv.length

        while (colIndex < length) {
            val c = csv[colIndex]
            if (inQuotes) {
                if (c == '"') {
                    if (colIndex + 1 < length && csv[colIndex + 1] == '"') {
                        currentField.append('"')
                        colIndex++ // skip escaped quote
                    } else {
                        inQuotes = false
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> {
                        inQuotes = true
                    }
                    ',' -> {
                        currentRow.add(currentField.toString())
                        currentField = StringBuilder()
                    }
                    '\n', '\r' -> {
                        currentRow.add(currentField.toString())
                        currentField = StringBuilder()
                        if (currentRow.isNotEmpty() && currentRow.any { it.isNotEmpty() }) {
                            result.add(currentRow)
                        }
                        currentRow = mutableListOf()
                        if (c == '\r' && colIndex + 1 < length && csv[colIndex + 1] == '\n') {
                            colIndex++ // skip CRLF secondary piece
                        }
                    }
                    else -> {
                        currentField.append(c)
                    }
                }
            }
            colIndex++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            if (currentRow.any { it.isNotEmpty() }) {
                result.add(currentRow)
            }
        }
        return result
    }
}
