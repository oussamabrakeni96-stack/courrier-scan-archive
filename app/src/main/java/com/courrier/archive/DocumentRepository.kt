package com.courrier.archive

import android.content.Context
import org.json.JSONArray
import org.json.JSONException

class DocumentRepository(private val context: Context) {
    private val key = "courier_documents"
    private val preferences by lazy {
        context.getSharedPreferences("archive_prefs", Context.MODE_PRIVATE)
    }

    fun load(): List<DocumentRecord> {
        val raw = preferences.getString(key, "").orEmpty()
        if (raw.isBlank()) return emptyList()

        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                runCatching { DocumentRecord.fromJson(array.getJSONObject(index)) }.getOrNull()
            }.sortedByDescending { it.date.time }
        } catch (_: JSONException) {
            // Migrate existing records stored as semicolon-separated pipe-delimited rows.
            raw.split(";")
                .filter { it.isNotBlank() }
                .mapNotNull { DocumentRecord.fromLegacyStorage(it) }
                .sortedByDescending { it.date.time }
                .also { migrated -> if (migrated.isNotEmpty()) save(migrated) }
        }
    }

    fun add(record: DocumentRecord) {
        save(listOf(record) + load())
    }

    fun save(records: List<DocumentRecord>) {
        val array = JSONArray()
        records.forEach { array.put(it.toJson()) }
        preferences.edit().putString(key, array.toString()).apply()
    }
}
