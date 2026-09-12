package com.david.collegeevents.presentation.createEvent

import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.david.collegeevents.data.remote.dto.DocumentDto
import com.david.collegeevents.data.remote.dto.JudgeDto
import com.david.collegeevents.data.remote.dto.SpeakerDto
import com.david.collegeevents.utils.DateTimeValue
import java.util.Calendar

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color.DarkGray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepBasicInfo(
    bannerLocalUri: Uri?, existingBannerUrl: String?, isUploading: Boolean, onPickBanner: () -> Unit,
    title: String, onTitleChange: (String) -> Unit,
    shortDescription: String, onShortDescChange: (String) -> Unit,
    detailedDescription: String, onDetailedDescChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    tags: List<String>, tagInput: String, onTagInputChange: (String) -> Unit,
    onAddTag: () -> Unit, onRemoveTag: (String) -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Technical", "Cultural", "Sports", "Workshop", "Seminar",
        "Competition", "Hackathon", "Fest", "Webinar", "Other"
    )

    Box(
        modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F5F9))
            .clickable { onPickBanner() },
        contentAlignment = Alignment.Center
    ) {
        val previewModel = bannerLocalUri ?: existingBannerUrl
        if (previewModel != null) {
            AsyncImage(model = previewModel, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Change Banner", color = Color.White, fontSize = 11.sp)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Upload, contentDescription = null, tint = Color(0xFF1A237E))
                Spacer(Modifier.height(8.dp))
                Text("Select Event Banner", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontSize = 14.sp)
                Text("Recommended size: 1200x630px", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }

    FormLabel("Event Title")
    OutlinedTextField(value = title, onValueChange = onTitleChange, placeholder = { Text("e.g., CodeSprint 2026") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

    FormLabel("Short Description")
    OutlinedTextField(value = shortDescription, onValueChange = onShortDescChange, placeholder = { Text("One-line summary") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

    FormLabel("Detailed Description")
    OutlinedTextField(
        value = detailedDescription, onValueChange = onDetailedDescChange,
        placeholder = { Text("Full details about the event...") },
        modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp), maxLines = 6
    )

    FormLabel("Category")
    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
        OutlinedTextField(
            value = category, onValueChange = {}, readOnly = true,
            placeholder = { Text("Select category") },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
        )
        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
            categories.forEach { cat ->
                DropdownMenuItem(text = { Text(cat) }, onClick = { onCategoryChange(cat); categoryExpanded = false })
            }
        }
    }

    FormLabel("Tags")
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = tagInput, onValueChange = onTagInputChange,
            placeholder = { Text("e.g., Kotlin, AI") },
            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onAddTag) { Icon(Icons.Default.Add, contentDescription = "Add tag") }
    }
    if (tags.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = { onRemoveTag(tag) },
                    label = { Text(tag) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepDateVenue(
    startDateTime: DateTimeValue, onStartChange: (DateTimeValue) -> Unit,
    endDateTime: DateTimeValue, onEndChange: (DateTimeValue) -> Unit,
    eventMode: String, onModeChange: (String) -> Unit,
    venue: String, onVenueChange: (String) -> Unit,
    onlineMeetingLink: String, onLinkChange: (String) -> Unit
) {
    FormLabel("Start Date & Time")
    DateTimeField(value = startDateTime, onChange = onStartChange)

    FormLabel("End Date & Time")
    DateTimeField(value = endDateTime, onChange = onEndChange)

    FormLabel("Event Mode")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        listOf("OFFLINE", "ONLINE", "HYBRID").forEachIndexed { index, mode ->
            SegmentedButton(
                selected = eventMode == mode,
                onClick = { onModeChange(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
            ) { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) }
        }
    }

    AnimatedVisibility(visible = eventMode != "ONLINE") {
        Column {
            FormLabel("Venue")
            OutlinedTextField(
                value = venue, onValueChange = onVenueChange,
                placeholder = { Text("e.g., Seminar Hall 2") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )
        }
    }

    AnimatedVisibility(visible = eventMode != "OFFLINE") {
        Column {
            FormLabel("Online Meeting Link")
            OutlinedTextField(
                value = onlineMeetingLink, onValueChange = onLinkChange,
                placeholder = { Text("https://meet.google.com/...") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun StepOrganizer(
    clubName: String, onClubChange: (String) -> Unit,
    coordinatorName: String, onCoordinatorNameChange: (String) -> Unit,
    coordinatorContact: String, onCoordinatorContactChange: (String) -> Unit
) {
    FormLabel("Organizing Club / Department")
    OutlinedTextField(value = clubName, onValueChange = onClubChange, placeholder = { Text("e.g., Coding Club") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

    FormLabel("Event Coordinator Name")
    OutlinedTextField(value = coordinatorName, onValueChange = onCoordinatorNameChange, placeholder = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

    FormLabel("Coordinator Contact (Email / Phone)")
    OutlinedTextField(value = coordinatorContact, onValueChange = onCoordinatorContactChange, placeholder = { Text("email@university.edu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepRegistration(
    registrationRequired: Boolean, onRequiredChange: (Boolean) -> Unit,
    registrationFee: String, onFeeChange: (String) -> Unit,
    registrationDeadline: DateTimeValue, onDeadlineChange: (DateTimeValue) -> Unit,
    totalSeats: String, onSeatsChange: (String) -> Unit,
    registrationType: String, onTypeChange: (String) -> Unit,
    minTeamSize: String, onMinTeamChange: (String) -> Unit,
    maxTeamSize: String, onMaxTeamChange: (String) -> Unit,
    registrationLink: String, onLinkChange: (String) -> Unit,
    eligibility: Set<String>, onEligibilityToggle: (String) -> Unit
) {
    val eligibilityOptions = listOf("ALL", "YEAR_1", "YEAR_2", "YEAR_3", "YEAR_4", "FACULTY", "EXTERNAL")

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Registration Required?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Switch(checked = registrationRequired, onCheckedChange = onRequiredChange)
    }

    AnimatedVisibility(visible = registrationRequired) {
        Column {
            FormLabel("Registration Fee")
            OutlinedTextField(value = registrationFee, onValueChange = onFeeChange, placeholder = { Text("Free or ₹100") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            FormLabel("Registration Deadline")
            DateTimeField(value = registrationDeadline, onChange = onDeadlineChange)

            FormLabel("Registration Link (agar external form)")
            OutlinedTextField(value = registrationLink, onValueChange = onLinkChange, placeholder = { Text("Optional") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            FormLabel("Registration Type")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("INDIVIDUAL", "TEAM").forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = registrationType == type,
                        onClick = { onTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                    ) { Text(type.lowercase().replaceFirstChar { it.uppercase() }) }
                }
            }

            AnimatedVisibility(visible = registrationType == "TEAM") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Min Team Size")
                        OutlinedTextField(value = minTeamSize, onValueChange = onMinTeamChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Max Team Size")
                        OutlinedTextField(value = maxTeamSize, onValueChange = onMaxTeamChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
            }
        }
    }

    FormLabel("Maximum Participants / Capacity")
    OutlinedTextField(value = totalSeats, onValueChange = onSeatsChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

    FormLabel("Eligibility")
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        eligibilityOptions.take(3).forEach { opt ->
            FilterChip(selected = opt in eligibility, onClick = { onEligibilityToggle(opt) }, label = { Text(opt) })
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        eligibilityOptions.drop(3).forEach { opt ->
            FilterChip(selected = opt in eligibility, onClick = { onEligibilityToggle(opt) }, label = { Text(opt) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepEventType(
    eventType: String, onEventTypeChange: (String) -> Unit,
    rulesText: String, onRulesChange: (String) -> Unit,
    judgingCriteria: String, onJudgingChange: (String) -> Unit,
    firstPrize: String, onFirstPrizeChange: (String) -> Unit,
    secondPrize: String, onSecondPrizeChange: (String) -> Unit,
    thirdPrize: String, onThirdPrizeChange: (String) -> Unit,
    hasParticipationCert: Boolean, onParticipationCertChange: (Boolean) -> Unit,
    hasWinnerCert: Boolean, onWinnerCertChange: (Boolean) -> Unit,
    problemStatement: String, onProblemChange: (String) -> Unit,
    submissionDeadline: DateTimeValue, onSubmissionDeadlineChange: (DateTimeValue) -> Unit
) {
    FormLabel("Event Type")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = eventType, onValueChange = {}, readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("GENERAL", "COMPETITION", "HACKATHON", "WORKSHOP").forEach { type ->
                DropdownMenuItem(text = { Text(type) }, onClick = { onEventTypeChange(type); expanded = false })
            }
        }
    }

    AnimatedVisibility(visible = eventType == "COMPETITION" || eventType == "HACKATHON") {
        Column {
            FormLabel("Rules & Guidelines")
            OutlinedTextField(value = rulesText, onValueChange = onRulesChange, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), maxLines = 5)

            FormLabel("Judging Criteria")
            OutlinedTextField(value = judgingCriteria, onValueChange = onJudgingChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("1st Prize")
                    OutlinedTextField(value = firstPrize, onValueChange = onFirstPrizeChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("2nd Prize")
                    OutlinedTextField(value = secondPrize, onValueChange = onSecondPrizeChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("3rd Prize")
                    OutlinedTextField(value = thirdPrize, onValueChange = onThirdPrizeChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Participation Certificate?", fontSize = 14.sp)
                Switch(checked = hasParticipationCert, onCheckedChange = onParticipationCertChange)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Winner Certificate?", fontSize = 14.sp)
                Switch(checked = hasWinnerCert, onCheckedChange = onWinnerCertChange)
            }

            AnimatedVisibility(visible = eventType == "HACKATHON") {
                Column {
                    FormLabel("Problem Statement")
                    OutlinedTextField(value = problemStatement, onValueChange = onProblemChange, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), maxLines = 5)

                    FormLabel("Submission Deadline")
                    DateTimeField(value = submissionDeadline, onChange = onSubmissionDeadlineChange)
                }
            }
        }
    }
}

@Composable
fun StepSpeakersJudges(
    speakers: List<SpeakerDto>, onAddSpeaker: (SpeakerDto) -> Unit, onRemoveSpeaker: (Int) -> Unit,
    judges: List<JudgeDto>, onAddJudge: (JudgeDto) -> Unit, onRemoveJudge: (Int) -> Unit
) {
    var showSpeakerDialog by remember { mutableStateOf(false) }
    var showJudgeDialog by remember { mutableStateOf(false) }

    Text("Speakers", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp))
    speakers.forEachIndexed { index, speaker ->
        EntityCard(title = speaker.name, subtitle = listOfNotNull(speaker.designation, speaker.organization).joinToString(" • "), onRemove = { onRemoveSpeaker(index) })
    }
    OutlinedButton(onClick = { showSpeakerDialog = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp)) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Add Speaker")
    }

    Spacer(Modifier.height(20.dp))

    Text("Judges / Mentors", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    judges.forEachIndexed { index, judge ->
        EntityCard(title = judge.name, subtitle = judge.designation.orEmpty(), onRemove = { onRemoveJudge(index) })
    }
    OutlinedButton(onClick = { showJudgeDialog = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp)) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Add Judge")
    }

    if (showSpeakerDialog) {
        SpeakerDialog(onConfirm = { onAddSpeaker(it); showSpeakerDialog = false }, onDismiss = { showSpeakerDialog = false })
    }
    if (showJudgeDialog) {
        JudgeDialog(onConfirm = { onAddJudge(it); showJudgeDialog = false }, onDismiss = { showJudgeDialog = false })
    }
}

@Composable
fun StepDocumentsSocials(
    pendingDocuments: List<PendingDocument>, existingDocuments: List<DocumentDto>,
    onPickDocument: () -> Unit, onRemovePending: (Int) -> Unit, onRemoveExisting: (Int) -> Unit,
    whatsappLink: String, onWhatsappChange: (String) -> Unit,
    telegramLink: String, onTelegramChange: (String) -> Unit,
    discordLink: String, onDiscordChange: (String) -> Unit
) {
    Text("Documents", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp))
    existingDocuments.forEachIndexed { index, doc ->
        EntityCard(title = doc.fileName ?: doc.docType, subtitle = doc.docType, onRemove = { onRemoveExisting(index) })
    }
    pendingDocuments.forEachIndexed { index, doc ->
        EntityCard(title = doc.fileName, subtitle = "${doc.docType} • Will upload on submit", onRemove = { onRemovePending(index) })
    }
    OutlinedButton(onClick = onPickDocument, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp)) {
        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Attach Document")
    }

    Spacer(Modifier.height(20.dp))

    Text("Community Links (Optional)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    FormLabel("WhatsApp Group Link")
    OutlinedTextField(value = whatsappLink, onValueChange = onWhatsappChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
    FormLabel("Telegram Link")
    OutlinedTextField(value = telegramLink, onValueChange = onTelegramChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
    FormLabel("Discord Link")
    OutlinedTextField(value = discordLink, onValueChange = onDiscordChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
}

@Composable
fun EntityCard(title: String, subtitle: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (subtitle.isNotBlank()) Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        IconButton(onClick = onRemove) { Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red) }
    }
}

@Composable
fun SpeakerDialog(onConfirm: (SpeakerDto) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Speaker") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = organization, onValueChange = { organization = it }, label = { Text("Organization") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = linkedin, onValueChange = { linkedin = it }, label = { Text("LinkedIn URL") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(SpeakerDto(name.trim(), null, designation.ifBlank { null }, organization.ifBlank { null }, bio.ifBlank { null }, linkedin.ifBlank { null }))
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun JudgeDialog(onConfirm: (JudgeDto) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Judge") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(JudgeDto(name.trim(), designation.ifBlank { null }, bio.ifBlank { null }))
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPickDialog(uri: Uri, onConfirm: (docType: String, fileName: String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var docType by remember { mutableStateOf("OTHER") }
    var fileName by remember { mutableStateOf(getFileName(context, uri)) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attach Document") },
        text = {
            Column {
                OutlinedTextField(value = fileName, onValueChange = { fileName = it }, label = { Text("File name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = docType, onValueChange = {}, readOnly = true, label = { Text("Type") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("RULES", "BROCHURE", "SCHEDULE", "PROBLEM_STATEMENT", "OTHER").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { docType = it; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(docType, fileName.ifBlank { "document" }) }) { Text("Attach") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun getFileName(context: android.content.Context, uri: Uri): String {
    var name = "document"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx != -1 && cursor.moveToFirst()) name = cursor.getString(idx)
    }
    return name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeField(value: DateTimeValue, onChange: (DateTimeValue) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = value.display(), onValueChange = {}, readOnly = true,
            placeholder = { Text("Select date & time") },
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
        )
        Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = value.dateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(value.copy(dateMillis = datePickerState.selectedDateMillis))
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = value.hour ?: cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = value.minute ?: cal.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(value.copy(hour = timePickerState.hour, minute = timePickerState.minute))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

fun validateStep(
    step: Int, title: String, shortDescription: String, detailedDescription: String, category: String,
    startDateTime: DateTimeValue, endDateTime: DateTimeValue, eventMode: String,
    venue: String, onlineMeetingLink: String,
    clubName: String, coordinatorName: String, coordinatorContact: String,
    registrationRequired: Boolean, registrationType: String, minTeamSize: String, maxTeamSize: String,
    eventType: String, problemStatement: String,
    bannerLocalUri: Uri?, existingBannerUrl: String?
): String? {
    return when (step) {
        0 -> when {
            bannerLocalUri == null && existingBannerUrl.isNullOrBlank() -> "Please select an event banner"
            title.isBlank() -> "Event title is required"
            shortDescription.isBlank() -> "Short description is required"
            detailedDescription.isBlank() -> "Detailed description is required"
            category.isBlank() -> "Please select a category"
            else -> null
        }
        1 -> when {
            !startDateTime.isComplete -> "Please select start date & time"
            !endDateTime.isComplete -> "Please select end date & time"
            eventMode != "ONLINE" && venue.isBlank() -> "Venue is required for offline/hybrid events"
            eventMode != "OFFLINE" && onlineMeetingLink.isBlank() -> "Online meeting link is required"
            else -> null
        }
        2 -> when {
            clubName.isBlank() -> "Club/Department name is required"
            coordinatorName.isBlank() -> "Coordinator name is required"
            coordinatorContact.isBlank() -> "Coordinator contact is required"
            else -> null
        }
        3 -> when {
            registrationRequired && registrationType == "TEAM" && (minTeamSize.toIntOrNull() == null || maxTeamSize.toIntOrNull() == null) ->
                "Please enter valid min/max team size"
            else -> null
        }
        4 -> when {
            eventType == "HACKATHON" && problemStatement.isBlank() -> "Problem statement is required for hackathons"
            else -> null
        }
        else -> null
    }
}