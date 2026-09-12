package com.david.collegeevents.presentation.createEvent

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.david.collegeevents.data.remote.dto.CreateEventRequest
import com.david.collegeevents.data.remote.dto.DocumentDto
import com.david.collegeevents.data.remote.dto.JudgeDto
import com.david.collegeevents.data.remote.dto.SpeakerDto
import com.david.collegeevents.utils.DateTimeValue
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

private val STEP_TITLES = listOf(
    "Basic Info", "Date & Venue", "Organizer", "Registration",
    "Event Type", "Speakers & Judges", "Documents & Links"
)

private val CATEGORIES = listOf(
    "Technical", "Cultural", "Sports", "Workshop", "Seminar",
    "Competition", "Hackathon", "Fest", "Webinar", "Other"
)

private val ELIGIBILITY_OPTIONS = listOf(
    "ALL", "YEAR_1", "YEAR_2", "YEAR_3", "YEAR_4", "FACULTY", "EXTERNAL"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    editEventId: String?,
    onBack: () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(0) }

    // ── Basic Info ──────────────────────────────────────────────────────
    var bannerLocalUri by remember { mutableStateOf<Uri?>(null) }
    var existingBannerUrl by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var shortDescription by remember { mutableStateOf("") }
    var detailedDescription by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var tagInput by remember { mutableStateOf("") }

    // ── Date & Venue ────────────────────────────────────────────────────
    var startDateTime by remember { mutableStateOf(DateTimeValue()) }
    var endDateTime by remember { mutableStateOf(DateTimeValue()) }
    var eventMode by remember { mutableStateOf("OFFLINE") }
    var venue by remember { mutableStateOf("") }
    var onlineMeetingLink by remember { mutableStateOf("") }

    // ── Organizer ───────────────────────────────────────────────────────
    var clubName by remember { mutableStateOf("") }
    var coordinatorName by remember { mutableStateOf("") }
    var coordinatorContact by remember { mutableStateOf("") }

    // ── Registration ────────────────────────────────────────────────────
    var registrationRequired by remember { mutableStateOf(true) }
    var registrationFee by remember { mutableStateOf("") }
    var registrationDeadline by remember { mutableStateOf(DateTimeValue()) }
    var totalSeats by remember { mutableStateOf("50") }
    var registrationType by remember { mutableStateOf("INDIVIDUAL") }
    var minTeamSize by remember { mutableStateOf("") }
    var maxTeamSize by remember { mutableStateOf("") }
    var registrationLink by remember { mutableStateOf("") }
    var eligibility by remember { mutableStateOf(setOf("ALL")) }

    // ── Event Type / Competition ────────────────────────────────────────
    var eventType by remember { mutableStateOf("GENERAL") }
    var rulesText by remember { mutableStateOf("") }
    var judgingCriteria by remember { mutableStateOf("") }
    var firstPrize by remember { mutableStateOf("") }
    var secondPrize by remember { mutableStateOf("") }
    var thirdPrize by remember { mutableStateOf("") }
    var hasParticipationCert by remember { mutableStateOf(false) }
    var hasWinnerCert by remember { mutableStateOf(false) }
    var problemStatement by remember { mutableStateOf("") }
    var submissionDeadline by remember { mutableStateOf(DateTimeValue()) }

    // ── Speakers / Judges ───────────────────────────────────────────────
    var speakers by remember { mutableStateOf(listOf<SpeakerDto>()) }
    var judges by remember { mutableStateOf(listOf<JudgeDto>()) }

    // ── Documents / Socials ─────────────────────────────────────────────
    var pendingDocuments by remember { mutableStateOf(listOf<PendingDocument>()) }
    var existingDocuments by remember { mutableStateOf(listOf<DocumentDto>()) }
    var whatsappLink by remember { mutableStateOf("") }
    var telegramLink by remember { mutableStateOf("") }
    var discordLink by remember { mutableStateOf("") }

    // ── Pickers ─────────────────────────────────────────────────────────
    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { bannerLocalUri = it } }   // 👈 sirf preview, upload nahi

    var showDocPicker by remember { mutableStateOf(false) }
    var pendingDocUri by remember { mutableStateOf<Uri?>(null) }
    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pendingDocUri = it; showDocPicker = true }
    }

    // ── Edit-mode prefill ───────────────────────────────────────────────
    LaunchedEffect(editEventId) {
        if (editEventId != null) viewModel.populateFieldsForEdit(editEventId)
    }

    LaunchedEffect(state.prefillEvent) {
        state.prefillEvent?.let { e ->
            existingBannerUrl = e.bannerUrl
            title = e.title
            shortDescription = e.shortDescription
            detailedDescription = e.detailedDescription
            category = e.category
            tags = e.tags

            startDateTime = DateTimeValue.fromIso(e.startDateTime)
            endDateTime = DateTimeValue.fromIso(e.endDateTime)
            eventMode = e.eventMode
            venue = e.venue.orEmpty()
            onlineMeetingLink = e.onlineMeetingLink.orEmpty()

            clubName = e.clubName
            coordinatorName = e.coordinatorName
            coordinatorContact = e.coordinatorContact

            registrationRequired = e.registrationRequired
            registrationFee = e.registrationFee
            registrationDeadline = DateTimeValue.fromIso(e.registrationDeadline)
            //totalSeats = e.registrationLink?.let { totalSeats } ?: totalSeats
            registrationType = e.registrationType
            minTeamSize = e.minTeamSize?.toString().orEmpty()
            maxTeamSize = e.maxTeamSize?.toString().orEmpty()
            registrationLink = e.registrationLink.orEmpty()
            eligibility = e.eligibility.toSet().ifEmpty { setOf("ALL") }

            eventType = e.eventType
            rulesText = e.rulesText.orEmpty()
            judgingCriteria = e.judgingCriteria.orEmpty()
            firstPrize = e.firstPrize.orEmpty()
            secondPrize = e.secondPrize.orEmpty()
            thirdPrize = e.thirdPrize.orEmpty()
            hasParticipationCert = e.hasParticipationCert
            hasWinnerCert = e.hasWinnerCert
            problemStatement = e.problemStatement.orEmpty()
            submissionDeadline = DateTimeValue.fromIso(e.submissionDeadline)

            speakers = e.speakers
            judges = e.judges
            existingDocuments = e.documents

            whatsappLink = e.whatsappLink.orEmpty()
            telegramLink = e.telegramLink.orEmpty()
            discordLink = e.discordLink.orEmpty()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is CreateEventViewModel.CreateEventUiEvent.ShowToast ->
                    Toast.makeText(context, event.msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(state.executionSuccess) {
        if (state.executionSuccess) {
            Toast.makeText(
                context,
                if (editEventId == null) "Event Published Successfully!" else "Event Saved!",
                Toast.LENGTH_LONG
            ).show()
            onBack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetErrors()
        }
    }

    // ── Doc-type confirm dialog (jab file pick ho jaye) ────────────────
    if (showDocPicker && pendingDocUri != null) {
        DocumentPickDialog(
            uri = pendingDocUri!!,
            onConfirm = { docType, fileName ->
                pendingDocuments = pendingDocuments + PendingDocument(pendingDocUri!!, docType, fileName)
                showDocPicker = false
                pendingDocUri = null
            },
            onDismiss = { showDocPicker = false; pendingDocUri = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // ── HEADER ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (currentStep == 0) onBack() else currentStep-- }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A237E))
            }
            Column {
                Text(
                    text = if (editEventId == null) "Create Event" else "Edit Event",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E)
                )
                Text(
                    text = "Step ${currentStep + 1} of ${STEP_TITLES.size} — ${STEP_TITLES[currentStep]}",
                    fontSize = 12.sp, color = Color.Gray
                )
            }
        }

        // ── Animated progress bar ──
        val progress by animateFloatAsState(
            targetValue = (currentStep + 1f) / STEP_TITLES.size,
            animationSpec = tween(350), label = "progress"
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = Color(0xFF1A237E),
            trackColor = Color(0xFFE5E7EB)
        )

        // ── STEP CONTENT (animated transitions) ──
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "step_transition"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    when (step) {
                        0 -> StepBasicInfo(
                            bannerLocalUri = bannerLocalUri, existingBannerUrl = existingBannerUrl,
                            isUploading = false,
                            onPickBanner = { bannerLauncher.launch("image/*") },
                            title = title, onTitleChange = { title = it },
                            shortDescription = shortDescription, onShortDescChange = { shortDescription = it },
                            detailedDescription = detailedDescription, onDetailedDescChange = { detailedDescription = it },
                            category = category, onCategoryChange = { category = it },
                            tags = tags, tagInput = tagInput, onTagInputChange = { tagInput = it },
                            onAddTag = {
                                if (tagInput.isNotBlank() && tagInput.trim() !in tags) {
                                    tags = tags + tagInput.trim(); tagInput = ""
                                }
                            },
                            onRemoveTag = { tags = tags - it }
                        )
                        1 -> StepDateVenue(
                            startDateTime = startDateTime, onStartChange = { startDateTime = it },
                            endDateTime = endDateTime, onEndChange = { endDateTime = it },
                            eventMode = eventMode, onModeChange = { eventMode = it },
                            venue = venue, onVenueChange = { venue = it },
                            onlineMeetingLink = onlineMeetingLink, onLinkChange = { onlineMeetingLink = it }
                        )
                        2 -> StepOrganizer(
                            clubName = clubName, onClubChange = { clubName = it },
                            coordinatorName = coordinatorName, onCoordinatorNameChange = { coordinatorName = it },
                            coordinatorContact = coordinatorContact, onCoordinatorContactChange = { coordinatorContact = it }
                        )
                        3 -> StepRegistration(
                            registrationRequired = registrationRequired, onRequiredChange = { registrationRequired = it },
                            registrationFee = registrationFee, onFeeChange = { registrationFee = it },
                            registrationDeadline = registrationDeadline, onDeadlineChange = { registrationDeadline = it },
                            totalSeats = totalSeats, onSeatsChange = { totalSeats = it },
                            registrationType = registrationType, onTypeChange = { registrationType = it },
                            minTeamSize = minTeamSize, onMinTeamChange = { minTeamSize = it },
                            maxTeamSize = maxTeamSize, onMaxTeamChange = { maxTeamSize = it },
                            registrationLink = registrationLink, onLinkChange = { registrationLink = it },
                            eligibility = eligibility, onEligibilityToggle = { opt ->
                                eligibility = if (opt in eligibility) eligibility - opt else eligibility + opt
                            }
                        )
                        4 -> StepEventType(
                            eventType = eventType, onEventTypeChange = { eventType = it },
                            rulesText = rulesText, onRulesChange = { rulesText = it },
                            judgingCriteria = judgingCriteria, onJudgingChange = { judgingCriteria = it },
                            firstPrize = firstPrize, onFirstPrizeChange = { firstPrize = it },
                            secondPrize = secondPrize, onSecondPrizeChange = { secondPrize = it },
                            thirdPrize = thirdPrize, onThirdPrizeChange = { thirdPrize = it },
                            hasParticipationCert = hasParticipationCert, onParticipationCertChange = { hasParticipationCert = it },
                            hasWinnerCert = hasWinnerCert, onWinnerCertChange = { hasWinnerCert = it },
                            problemStatement = problemStatement, onProblemChange = { problemStatement = it },
                            submissionDeadline = submissionDeadline, onSubmissionDeadlineChange = { submissionDeadline = it }
                        )
                        5 -> StepSpeakersJudges(
                            speakers = speakers, onAddSpeaker = { speakers = speakers + it },
                            onRemoveSpeaker = { idx -> speakers = speakers.toMutableList().apply { removeAt(idx) } },
                            judges = judges, onAddJudge = { judges = judges + it },
                            onRemoveJudge = { idx -> judges = judges.toMutableList().apply { removeAt(idx) } }
                        )
                        6 -> StepDocumentsSocials(
                            pendingDocuments = pendingDocuments,
                            existingDocuments = existingDocuments,
                            onPickDocument = { docLauncher.launch("*/*") },
                            onRemovePending = { idx -> pendingDocuments = pendingDocuments.toMutableList().apply { removeAt(idx) } },
                            onRemoveExisting = { idx -> existingDocuments = existingDocuments.toMutableList().apply { removeAt(idx) } },
                            whatsappLink = whatsappLink, onWhatsappChange = { whatsappLink = it },
                            telegramLink = telegramLink, onTelegramChange = { telegramLink = it },
                            discordLink = discordLink, onDiscordChange = { discordLink = it }
                        )
                    }
                    Spacer(Modifier.height(100.dp)) // bottom button ke liye space
                }
            }
        }

        // ── BOTTOM NAV ──
        HorizontalDivider(color = Color(0xFFE5E7EB))
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Back") }
            }

            Button(
                onClick = {
                    if (currentStep < STEP_TITLES.size - 1) {
                        val error = validateStep(
                            step = currentStep, title = title, shortDescription = shortDescription,
                            detailedDescription = detailedDescription, category = category,
                            startDateTime = startDateTime, endDateTime = endDateTime, eventMode = eventMode,
                            venue = venue, onlineMeetingLink = onlineMeetingLink,
                            clubName = clubName, coordinatorName = coordinatorName, coordinatorContact = coordinatorContact,
                            registrationRequired = registrationRequired, registrationType = registrationType,
                            minTeamSize = minTeamSize, maxTeamSize = maxTeamSize,
                            eventType = eventType, problemStatement = problemStatement,
                            bannerLocalUri = bannerLocalUri, existingBannerUrl = existingBannerUrl
                        )
                        if (error != null) {
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        } else {
                            currentStep++
                        }
                    } else {
                        // ── FINAL SUBMIT ──
                        val finalError = validateStep(
                            step = currentStep, title = title, shortDescription = shortDescription,
                            detailedDescription = detailedDescription, category = category,
                            startDateTime = startDateTime, endDateTime = endDateTime, eventMode = eventMode,
                            venue = venue, onlineMeetingLink = onlineMeetingLink,
                            clubName = clubName, coordinatorName = coordinatorName, coordinatorContact = coordinatorContact,
                            registrationRequired = registrationRequired, registrationType = registrationType,
                            minTeamSize = minTeamSize, maxTeamSize = maxTeamSize,
                            eventType = eventType, problemStatement = problemStatement,
                            bannerLocalUri = bannerLocalUri, existingBannerUrl = existingBannerUrl
                        )
                        if (finalError != null) {
                            Toast.makeText(context, finalError, Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.submitEvent(
                            eventId = editEventId,
                            bannerLocalUri = bannerLocalUri,
                            existingBannerUrl = existingBannerUrl,
                            pendingDocuments = pendingDocuments,
                            existingDocuments = existingDocuments
                        ) { finalBannerUrl, finalDocuments ->
                            CreateEventRequest(
                                title = title.trim(),
                                shortDescription = shortDescription.trim(),
                                detailedDescription = detailedDescription.trim(),
                                bannerUrl = finalBannerUrl,
                                category = category,
                                tags = tags,
                                startDateTime = startDateTime.toIso()!!,
                                endDateTime = endDateTime.toIso()!!,
                                eventMode = eventMode,
                                venue = venue.ifBlank { null },
                                onlineMeetingLink = onlineMeetingLink.ifBlank { null },
                                clubName = clubName.trim(),
                                coordinatorName = coordinatorName.trim(),
                                coordinatorContact = coordinatorContact.trim(),
                                registrationRequired = registrationRequired,
                                registrationFee = registrationFee.ifBlank { "Free" },
                                registrationDeadline = registrationDeadline.toIso(),
                                totalSeats = totalSeats.toIntOrNull() ?: 50,
                                registrationType = registrationType,
                                minTeamSize = minTeamSize.toIntOrNull(),
                                maxTeamSize = maxTeamSize.toIntOrNull(),
                                registrationLink = registrationLink.ifBlank { null },
                                eligibility = eligibility.toList(),
                                eventType = eventType,
                                rulesText = rulesText.ifBlank { null },
                                judgingCriteria = judgingCriteria.ifBlank { null },
                                firstPrize = firstPrize.ifBlank { null },
                                secondPrize = secondPrize.ifBlank { null },
                                thirdPrize = thirdPrize.ifBlank { null },
                                hasParticipationCert = hasParticipationCert,
                                hasWinnerCert = hasWinnerCert,
                                problemStatement = problemStatement.ifBlank { null },
                                submissionDeadline = submissionDeadline.toIso(),
                                speakers = speakers,
                                judges = judges,
                                documents = finalDocuments,
                                whatsappLink = whatsappLink.ifBlank { null },
                                telegramLink = telegramLink.ifBlank { null },
                                discordLink = discordLink.ifBlank { null }
                            )
                        }
                    }
                },
                modifier = Modifier.weight(if (currentStep > 0) 2f else 1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isPublishingEvent
            ) {
                AnimatedContent(targetState = state.isPublishingEvent, label = "btn_content") { publishing ->
                    if (publishing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(state.publishingStage ?: "Publishing...", fontSize = 13.sp, color = Color.White)
                        }
                    } else {
                        Text(
                            if (currentStep < STEP_TITLES.size - 1) "Next" else if (editEventId == null) "Publish Event" else "Save Changes",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}