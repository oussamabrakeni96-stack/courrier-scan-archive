package com.courrier.archive

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import java.io.File
import java.io.FileOutputStream
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

    var type by remember { mutableStateOf(DocumentType.INCOMING) }
    var reference by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var counterpart by remember { mutableStateOf("") }
    var relatedReference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var documents by remember { mutableStateOf(repository.load()) }

    val visibleDocuments = documents.filter { document ->
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            true
        } else {
            document.reference.contains(query, ignoreCase = true) ||
                document.subject.contains(query, ignoreCase = true) ||
                document.counterpart.contains(query, ignoreCase = true) ||
                document.linkedReference.contains(query, ignoreCase = true)
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                val fileName = "scan_${System.currentTimeMillis()}.jpg"
                val target = File(context.filesDir, "archives/$fileName")
                target.parentFile?.mkdirs()

                try {
                    FileOutputStream(target).use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    }

                    val record = DocumentRecord(
                        id = System.currentTimeMillis().toString(),
                        reference = reference.ifBlank { "REF-${System.currentTimeMillis()}" },
                        subject = subject.ifBlank { "Sans objet" },
                        type = type,
                        counterpart = counterpart.ifBlank { "Non renseigné" },
                        date = Date(),
                        notes = notes,
                        filePath = target.absolutePath,
                        linkedReference = relatedReference.trim()
                    )

                    repository.add(record)
                    documents = repository.load()

                    reference = ""
                    subject = ""
                    counterpart = ""
                    relatedReference = ""
                    notes = ""
                } catch (e: Exception) {
                    Log.e("CourierArchive", "Erreur lors du scan", e)
                }
            }
        }
    )

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nouveau courrier",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = reference,
                        onValueChange = { reference = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Référence") }
                    )

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Objet") }
                    )

                    OutlinedTextField(
                        value = counterpart,
                        onValueChange = { counterpart = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Expéditeur / destinataire") }
                    )

                    OutlinedTextField(
                        value = relatedReference,
                        onValueChange = { relatedReference = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Référence liée (entrant / sortant)") }
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = type == DocumentType.INCOMING,
                            onClick = { type = DocumentType.INCOMING },
                            label = { Text("Entrant") }
                        )
                        FilterChip(
                            selected = type == DocumentType.OUTGOING,
                            onClick = { type = DocumentType.OUTGOING },
                            label = { Text("Sortant") }
                        )
                    }

                    Button(
                        onClick = { takePicture.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Scanner le document")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Archive, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Archive locale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Recherche") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            if (visibleDocuments.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFFF3F1F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun document trouvé dans l'archive")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleDocuments) { document ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = document.reference,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(document.type.label)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(document.subject)
                                Text("${document.counterpart} • ${document.date.toLocaleString()}")

                                if (document.linkedReference.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Lié à: ${document.linkedReference}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (document.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(document.notes)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

