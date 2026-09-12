package com.david.collegeevents.presentation.details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.david.collegeevents.data.remote.dto.DocumentDto
import com.david.collegeevents.data.remote.dto.JudgeDto
import com.david.collegeevents.data.remote.dto.SpeakerDto
import com.david.collegeevents.domain.model.EventDetail
import com.david.collegeevents.utils.DateTimeValue

@Composable
fun EventDetailScreen(
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current

    val bannerHeight = 280.dp
    val bannerHeightPx = 840f

    val dynamicAlpha by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 0f
            else {
                val offset = scrollState.firstVisibleItemScrollOffset.toFloat()
                (1f - (offset / bannerHeightPx)).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1A237E))
        }
    } else if (state.event != null) {
        val event = state.event

        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

            // Animated Header Banner Layer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight)
                    .graphicsLayer {
                        alpha = dynamicAlpha
                        translationY = -scrollState.firstVisibleItemScrollOffset.toFloat() * 0.4f
                    }
            ) {
                AsyncImage(
                    model = event.bannerUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        startY = 400f
                    )
                ))
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = bannerHeight - 24.dp, bottom = 120.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color.White)
                            .padding(24.dp)
                    ) {
                        // ── Club identity ──
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(Color(0xFF0F172A), RoundedCornerShape(8.dp))) {
                                Icon(Icons.Default.Adjust, contentDescription = null, tint = Color.Cyan, modifier = Modifier.align(Alignment.Center))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = event.clubName, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E), fontSize = 14.sp)
                                Text(text = "Organizing Committee", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = event.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), lineHeight = 32.sp)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = event.shortDescription, fontSize = 14.sp, color = Color(0xFF6B7280), lineHeight = 20.sp)

                        // ── Category + Tags ──
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text(event.category, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                            if (event.eventType != "GENERAL") {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(event.eventType, fontSize = 12.sp) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFFFF3E0))
                                )
                            }
                        }
                        if (event.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                event.tags.take(4).forEach { tag ->
                                    Box(
                                        modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) { Text(tag, fontSize = 11.sp, color = Color(0xFF475569)) }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Date & Venue ──
                        DetailMetaCard(
                            icon = Icons.Default.CalendarToday,
                            title = DateTimeValue.fromIso(event.startDateTime).display(),
                            sub = "Ends: ${DateTimeValue.fromIso(event.endDateTime).display()}",
                            badgeColor = Color(0xFFCCFBF1)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailMetaCard(
                            icon = if (event.eventMode == "ONLINE") Icons.Default.Videocam else Icons.Default.LocationOn,
                            title = when (event.eventMode) {
                                "ONLINE" -> "Online Event"
                                "HYBRID" -> event.venue ?: "Hybrid Event"
                                else -> event.venue ?: "Venue TBA"
                            },
                            sub = event.eventMode.lowercase().replaceFirstChar { it.uppercase() },
                            badgeColor = Color(0xFFE0E7FF)
                        )
                        if (!event.onlineMeetingLink.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { uriHandler.openUri(event.onlineMeetingLink) }) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Join Online Meeting", fontSize = 13.sp)
                            }
                        }

                        // ── Organizer ──
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("Organizer")
                        InfoRow(icon = Icons.Default.Person, label = "Coordinator", value = event.coordinatorName)
                        InfoRow(icon = Icons.Default.ContactMail, label = "Contact", value = event.coordinatorContact)

                        // ── About ──
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("About Event")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = event.detailedDescription, color = Color(0xFF4B5563), fontSize = 14.sp, lineHeight = 22.sp)

                        // ── Registration Details ──
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("Registration Details")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (event.registrationRequired) {
                            InfoRow(icon = Icons.Default.ConfirmationNumber, label = "Fee", value = event.registrationFee)
                            InfoRow(icon = Icons.Default.EventSeat, label = "Availability", value = event.seatAvailability)
                            InfoRow(icon = Icons.Default.Groups, label = "Registration Type", value = event.registrationType.lowercase().replaceFirstChar { it.uppercase() })
                            if (event.registrationType == "TEAM" && event.minTeamSize != null && event.maxTeamSize != null) {
                                InfoRow(icon = Icons.Default.Group, label = "Team Size", value = "${event.minTeamSize} – ${event.maxTeamSize} members")
                            }
                            if (!event.registrationDeadline.isNullOrBlank()) {
                                InfoRow(icon = Icons.Default.Timer, label = "Deadline", value = DateTimeValue.fromIso(event.registrationDeadline).display())
                            }
                            if (!event.registrationLink.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(onClick = { uriHandler.openUri(event.registrationLink) }) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("External Registration Form", fontSize = 13.sp)
                                }
                            }
                            if (event.eligibility.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Eligibility", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    event.eligibility.forEach { elig ->
                                        Box(modifier = Modifier.background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                            Text(elig, fontSize = 11.sp, color = Color(0xFF15803D))
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("No registration required for this event.", fontSize = 13.sp, color = Color.Gray)
                        }

                        // ── Competition / Hackathon details ──
                        if (event.eventType == "COMPETITION" || event.eventType == "HACKATHON") {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionTitle(if (event.eventType == "HACKATHON") "Hackathon Details" else "Competition Details")
                            Spacer(modifier = Modifier.height(8.dp))

                            if (event.eventType == "HACKATHON" && !event.problemStatement.isNullOrBlank()) {
                                Text("Problem Statement", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(event.problemStatement, fontSize = 13.sp, color = Color(0xFF4B5563), lineHeight = 20.sp)
                                if (!event.submissionDeadline.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    InfoRow(icon = Icons.Default.Upload, label = "Submission Deadline", value = DateTimeValue.fromIso(event.submissionDeadline).display())
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (!event.rulesText.isNullOrBlank()) {
                                Text("Rules & Guidelines", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(event.rulesText, fontSize = 13.sp, color = Color(0xFF4B5563), lineHeight = 20.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (!event.judgingCriteria.isNullOrBlank()) {
                                InfoRow(icon = Icons.Default.Rule, label = "Judging Criteria", value = event.judgingCriteria)
                            }

                            if (event.firstPrize != null || event.secondPrize != null || event.thirdPrize != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Prizes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    event.firstPrize?.let { PrizeBadge("🥇", it) }
                                    event.secondPrize?.let { PrizeBadge("🥈", it) }
                                    event.thirdPrize?.let { PrizeBadge("🥉", it) }
                                }
                            }

                            if (event.hasParticipationCert || event.hasWinnerCert) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (event.hasParticipationCert) CertBadge("Participation Certificate")
                                    if (event.hasWinnerCert) CertBadge("Winner Certificate")
                                }
                            }
                        }

                        // ── Speakers ──
                        if (event.speakers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionTitle("Speakers")
                            Spacer(modifier = Modifier.height(8.dp))
                            event.speakers.forEach { speaker ->
                                SpeakerRow(speaker, uriHandler)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        // ── Judges ──
                        if (event.judges.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionTitle("Judges / Mentors")
                            Spacer(modifier = Modifier.height(8.dp))
                            event.judges.forEach { judge ->
                                JudgeRow(judge)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // ── Documents ──
                        if (event.documents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionTitle("Documents & Resources")
                            Spacer(modifier = Modifier.height(8.dp))
                            event.documents.forEach { doc ->
                                DocumentRow(doc) { uriHandler.openUri(doc.fileUrl) }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // ── Community links ──
                        val hasSocials = !event.whatsappLink.isNullOrBlank() || !event.telegramLink.isNullOrBlank() || !event.discordLink.isNullOrBlank()
                        if (hasSocials) {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionTitle("Join the Community")
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                event.whatsappLink?.takeIf { it.isNotBlank() }?.let {
                                    SocialButton("WhatsApp", Color(0xFF25D366)) { uriHandler.openUri(it) }
                                }
                                event.telegramLink?.takeIf { it.isNotBlank() }?.let {
                                    SocialButton("Telegram", Color(0xFF229ED9)) { uriHandler.openUri(it) }
                                }
                                event.discordLink?.takeIf { it.isNotBlank() }?.let {
                                    SocialButton("Discord", Color(0xFF5865F2)) { uriHandler.openUri(it) }
                                }
                            }
                        }

                        // ── Registration status (if already registered) ──
                        if (event.isUserRegistered) {
                            Spacer(modifier = Modifier.height(24.dp))
                            RegistrationStatusCard(
                                seatNo = event.seatAvailability,
                                onDeregister = { viewModel.executeAction(register = false) },
                                isProcessing = state.actionLoading
                            )
                        }
                    }
                }
            }

            // Floating Nav Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingRoundNavButton(icon = Icons.Default.ArrowBack, onClick = onBack)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FloatingRoundNavButton(icon = Icons.Default.Share, onClick = {})
                    FloatingRoundNavButton(icon = Icons.Default.FavoriteBorder, onClick = {})
                }
            }

            // Bottom CTA — sirf tab dikhao jab registration required ho
            if (event.registrationRequired) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Registration Fee", color = Color.Gray, fontSize = 12.sp)
                            Text(text = event.registrationFee, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                        }

                        Button(
                            onClick = { viewModel.executeAction(register = true) },
                            modifier = Modifier.width(180.dp).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (event.isUserRegistered) Color.LightGray else Color(0xFF1A237E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !event.isUserRegistered && !state.actionLoading
                        ) {
                            if (state.actionLoading && !event.isUserRegistered) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    text = if (event.isUserRegistered) "Registered" else "Register Now",
                                    fontWeight = FontWeight.Bold,
                                    color = if (event.isUserRegistered) Color.DarkGray else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable small pieces ──────────────────────────────────────────────

@Composable
fun SectionTitle(text: String) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
        }
    }
}

@Composable
fun DetailMetaCard(icon: ImageVector, title: String, sub: String, badgeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).background(badgeColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(text = sub, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PrizeBadge(emoji: String, amount: String) {
    Column(
        modifier = Modifier.background(Color(0xFFFFFBEB), RoundedCornerShape(10.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
    }
}

@Composable
fun CertBadge(text: String) {
    Row(
        modifier = Modifier.background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, color = Color(0xFF1D4ED8))
    }
}

@Composable
fun SpeakerRow(speaker: SpeakerDto, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!speaker.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = speaker.photoUrl, contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.size(48.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(speaker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
            val subtitle = listOfNotNull(speaker.designation, speaker.organization).joinToString(" • ")
            if (subtitle.isNotBlank()) Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            if (!speaker.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(speaker.bio, fontSize = 11.sp, color = Color(0xFF6B7280), maxLines = 2)
            }
        }
        if (!speaker.linkedinUrl.isNullOrBlank()) {
            IconButton(onClick = { uriHandler.openUri(speaker.linkedinUrl) }) {
                Icon(Icons.Default.OpenInNew, contentDescription = "LinkedIn", tint = Color(0xFF1A237E), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun JudgeRow(judge: JudgeDto) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(judge.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF111827))
            if (!judge.designation.isNullOrBlank()) Text(judge.designation, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DocumentRow(doc: DocumentDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF1A237E), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(doc.fileName ?: doc.docType, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
            Text(doc.docType.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp, color = Color.Gray)
        }
        Icon(Icons.Default.Download, contentDescription = "Open", tint = Color.Gray, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SocialButton(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun RegistrationStatusCard(seatNo: String, onDeregister: () -> Unit, isProcessing: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "You're Registered!", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(modifier = Modifier.background(Color(0xFFA7F3D0), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(text = seatNo, color = Color(0xFF047857), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onDeregister,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE4E6)),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.Red, modifier = Modifier.size(16.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Deregister from Event", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Refunds available until 48h before start.", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun FloatingRoundNavButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}