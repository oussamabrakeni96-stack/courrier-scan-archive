package com.courrier.archive

import android.content.Context

class DocumentRepository(private val context: Context) {
    private val key = "courier_documents"

    fun load(): List<DocumentRecord> {
        val raw = context.getSharedPreferences("archive_prefs", Context.MODE_PRIVATE)
            .getString(key, "")
            ?: ""

        if (raw.isBlank()) return emptyList()

        return raw.split(";")
            .filter { it.isNotBlank() }
            .map { DocumentRecord.fromStorageString(it) }
            .sortedByDescending { it.date.time }
    }

    fun add(record: DocumentRecord) {
        val existing = load().toMutableList()
        existing.add(0, record)
        save(existing)
    }

    fun save(records: List<DocumentRecord>) {
        val value = records.joinToString(";") { it.toStorageString() }
        context.getSharedPreferences("archive_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }
}
