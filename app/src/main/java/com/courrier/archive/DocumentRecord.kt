package com.courrier.archive

import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

enum class DocumentType(val label: String) {
    INCOMING("Entrant"),
    OUTGOING("Sortant")
}

data class DocumentRecord(
    val id: String,
    val reference: String,
    val subject: String,
    val type: DocumentType,
    val counterpart: String,
    val date: Date,
    val notes: String,
    val filePath: String,
    val linkedReference: String = "",
    val folderNumber: String = "",
    val pagePaths: List<String> = listOf(filePath)
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("reference", reference)
        put("subject", subject)
        put("type", type.name)
        put("counterpart", counterpart)
        put("date", date.time)
        put("notes", notes)
        put("filePath", filePath)
        put("linkedReference", linkedReference)
        put("folderNumber", folderNumber)
        put("pagePaths", JSONArray(pagePaths))
    }

    companion object {
        fun fromJson(json: JSONObject): DocumentRecord {
            val pagesJson = json.optJSONArray("pagePaths")
            val pages = if (pagesJson != null) {
                (0 until pagesJson.length()).mapNotNull { index -> pagesJson.optString(index).takeIf { it.isNotBlank() } }
            } else {
                emptyList()
            }
            val legacyPath = json.optString("filePath")
            return DocumentRecord(
                id = json.optString("id"),
                reference = json.optString("reference"),
                subject = json.optString("subject"),
                type = runCatching { DocumentType.valueOf(json.optString("type")) }.getOrDefault(DocumentType.INCOMING),
                counterpart = json.optString("counterpart"),
                date = Date(json.optLong("date", 0L)),
                notes = json.optString("notes"),
                filePath = legacyPath,
                linkedReference = json.optString("linkedReference"),
                folderNumber = json.optString("folderNumber"),
                pagePaths = pages.ifEmpty { listOf(legacyPath).filter { it.isNotBlank() } }
            )
        }

        fun fromLegacyStorage(raw: String): DocumentRecord? {
            val parts = raw.split("|", limit = 9)
            if (parts.size < 8) return null
            return runCatching {
                val path = parts.getOrElse(7) { "" }
                DocumentRecord(
                    id = parts[0],
                    reference = parts[1],
                    subject = parts[2],
                    type = DocumentType.valueOf(parts[3]),
                    counterpart = parts[4],
                    date = Date(parts[5].toLong()),
                    notes = parts[6],
                    filePath = path,
                    linkedReference = parts.getOrElse(8) { "" },
                    pagePaths = listOf(path).filter { it.isNotBlank() }
                )
            }.getOrNull()
        }
    }
}
