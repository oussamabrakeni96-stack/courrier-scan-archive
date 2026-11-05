package com.courrier.archive

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
    val filePath: String
) {
    fun toStorageString(): String {
        return listOf(
            id,
            reference,
            subject,
            type.name,
            counterpart,
            date.time.toString(),
            notes,
            filePath
        ).joinToString("|")
    }

    companion object {
        fun fromStorageString(raw: String): DocumentRecord {
            val parts = raw.split("|", limit = 8)
            return DocumentRecord(
                id = parts.getOrElse(0) { "" },
                reference = parts.getOrElse(1) { "" },
                subject = parts.getOrElse(2) { "" },
                type = DocumentType.valueOf(parts.getOrElse(3) { DocumentType.INCOMING.name }),
                counterpart = parts.getOrElse(4) { "" },
                date = Date(parts.getOrElse(5) { "0" }.toLong()),
                notes = parts.getOrElse(6) { "" },
                filePath = parts.getOrElse(7) { "" }
            )
        }
    }
}
