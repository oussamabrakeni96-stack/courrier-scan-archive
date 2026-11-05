package com.courrier.archive

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = DocumentRepository(applicationContext)
        setContent {
            CourierArchiveTheme {
                CourierArchiveApp(repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierArchiveApp(repository: DocumentRepository) {
    val context = LocalContext.current
    val activity = context as? Activity

    var type by remember { mutableStateOf(DocumentType.INCOMING) }
    var reference by remember { mutableStateOf("") }
    var folderNumber by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var counterpart by remember { mutableStateOf("") }
    var relatedReference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var documents by remember { mutableStateOf(repository.load()) }

    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(50)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                val pageUris = scanResult?.pages?.map { it.imageUri }.orEmpty()
                if (pageUris.isEmpty()) {
                    statusMessage = "Aucune page reçue du scanner."
                } else {
                    val savedPages = pageUris.mapIndexedNotNull { index, uri ->
                        copyScanPage(context, uri, index)
                    }
                    if (savedPages.isNotEmpty()) {
                        val timestamp = System.currentTimeMillis()
                        repository.add(
                            DocumentRecord(
                                id = timestamp.toString(),
                                reference = reference.trim().ifBlank { "REF-$timestamp" },
                                folderNumber = folderNumber.trim(),
                                subject = subject.trim().ifBlank { "Sans objet" },
                                type = type,
                                counterpart = counterpart.trim().ifBlank { "Non renseigné" },
                                date = Date(timestamp),
                                notes = notes.trim(),
                                filePath = savedPages.first(),
                                linkedReference = relatedReference.trim(),
                                pagePaths = savedPages
                            )
                        )
                        documents = repository.load()
                        statusMessage = "Courrier archivé : ${savedPages.size} page(s)."
                        reference = ""
                        folderNumber = ""
                        subject = ""
                        counterpart = ""
                        relatedReference = ""
                        notes = ""
                    } else {
                        statusMessage = "Impossible d'enregistrer les pages scannées."
                    }
                }
            } catch (e: Exception) {
                Log.e("CourierArchive", "Erreur lors de l'enregistrement du scan", e)
                statusMessage = "Erreur lors de l'enregistrement du scan."
            }
        } else {
            statusMessage = "Scan annulé."
        }
    }

    val visibleDocuments = documents.filter { document ->
        val query = searchQuery.trim()
        query.isEmpty() || document.reference.contains(query, ignoreCase = true) ||
            document.folderNumber.contains(query, ignoreCase = true) ||
            document.subject.contains(query, ignoreCase = true) ||
            document.counterpart.contains(query, ignoreCase = true) ||
            document.linkedReference.contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Courier Archive") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Nouveau courrier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(reference, { reference = it }, Modifier.fillMaxWidth(), label = { Text("Référence du courrier") }, singleLine = true)
                    OutlinedTextField(folderNumber, { folderNumber = it }, Modifier.fillMaxWidth(), label = { Text("Numéro de dossier") }, singleLine = true)
                    OutlinedTextField(subject, { subject = it }, Modifier.fillMaxWidth(), label = { Text("Objet") }, singleLine = true)
                    OutlinedTextField(counterpart, { counterpart = it }, Modifier.fillMaxWidth(), label = { Text("Expéditeur / destinataire") }, singleLine = true)
                    OutlinedTextField(relatedReference, { relatedReference = it }, Modifier.fillMaxWidth(), label = { Text("Référence liée entrant / sortant") }, singleLine = true)
                    OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Notes") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = type == DocumentType.INCOMING, onClick = { type = DocumentType.INCOMING }, label = { Text("Entrant") })
                        FilterChip(selected = type == DocumentType.OUTGOING, onClick = { type = DocumentType.OUTGOING }, label = { Text("Sortant") })
                    }
                    Button(
                        onClick = {
                            if (activity == null) {
                                statusMessage = "Scanner indisponible dans ce contexte."
                            } else {
                                scanner.getStartScanIntent(activity)
                                    .addOnSuccessListener { intentSender ->
                                        scanLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e("CourierArchive", "Impossible de démarrer le scanner", error)
                                        statusMessage = "Impossible de démarrer le scanner. Vérifiez Google Play services."
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Scanner plusieurs pages")
                    }
                    if (statusMessage.isNotBlank()) {
                        Text(statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Archive, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Archive locale", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher par dossier, numéro, référence ou objet") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (visibleDocuments.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFFF3F1F9)),
                        contentAlignment = Alignment.Center
                    ) { Text("Aucun document trouvé dans l'archive") }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleDocuments, key = { it.id }) { document ->
                        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(document.reference, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(document.type.label)
                                }
                                if (document.folderNumber.isNotBlank()) Text("Dossier : ${document.folderNumber}")
                                Text(document.subject)
                                Text("${document.counterpart} • ${document.date}")
                                Text("${document.pagePaths.size} page(s)", style = MaterialTheme.typography.bodySmall)
                                if (document.linkedReference.isNotBlank()) Text("Lié à : ${document.linkedReference}", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyScanPage(context: Context, uri: Uri, index: Int): String? {
    val directory = File(context.filesDir, "archives")
    if (!directory.exists() && !directory.mkdirs()) return null
    val target = File(directory, "scan_${System.currentTimeMillis()}_$index.jpg")
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.absolutePath
    } catch (e: Exception) {
        Log.e("CourierArchive", "Erreur copie page scannée", e)
        target.delete()
        null
    }
}
