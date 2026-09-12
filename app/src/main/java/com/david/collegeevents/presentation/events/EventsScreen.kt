package com.david.collegeevents.presentation.events

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.david.collegeevents.domain.model.EventSummary
import com.david.collegeevents.utils.DateTimeValue
import com.david.collegeevents.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(
    onEventClick: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    selectedEventId: String?,                     // Received from MainActivity
    onSelectionChanged: (String?, String?) -> Unit, // Callback for MainActivity
    viewModel: EventsViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    /* club wise filter */
    //val categories = listOf("All", "Information Technology", "Cultural", "Sports", "Management", "Science", "Fine Arts", "Gender champions")

    /* category wise filter */
    val categories = listOf("All", "Technical", "Cultural", "Sports", "Workshop", "Seminar", "Competition", "Hackathon", "Fest", "Webinar", "Other")

    // Dynamic role check to verify long-press authorization locally
    val tokenManager = remember { TokenManager(context) }
    val userRole by tokenManager.userRoleFlow.collectAsState(initial = "STUDENT")

    val listState = rememberLazyListState()
    var isHeaderVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10) isHeaderVisible = false  // scroll down → hide
                if (available.y > 10) isHeaderVisible = true   // scroll up → show
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            //.background(Color(0xFFF8F9FA))
            .background(Color.White)
            .nestedScroll(nestedScrollConnection)
    ) {
        // Top Toolbar is fully managed in MainActivity now — No duplicate code here!
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFF1F1F5))
        // Greeting Header
        AnimatedVisibility(
            visible = isHeaderVisible,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Hey, ${state.currentUserName}!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Ready for some campus action today?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // Horizontal Category Filter Scrollbar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val isSelected = state.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.fetchEvents(category) },
                    label = { Text(category, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFA7F3D0),
                        selectedLabelColor = Color(0xFF047857),
                        containerColor = Color(0xFFE5E7EB),
                        labelColor = Color.DarkGray
                    ),
                    border = null
                )
            }
        }

        // Main List Rendering with Content Loading Guard
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF1A237E)
                )
            } else if (state.error != null) {
                Text(
                    state.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp),
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            } else if (state.eventsList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No events",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Text(
                        text = "There are no events registered in this block.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.eventsList) { event ->
                        val isSelected = selectedEventId == event.id

                        EventFeedItem(
                            event = event,
                            isSelected = isSelected,
                            onEventClick = { id ->
                                if (selectedEventId != null) {
                                    // If selection mode is active, clicking toggles selection
                                    onSelectionChanged(
                                        if (isSelected) null else id,
                                        if (isSelected) null else event.bannerUrl
                                    )
                                } else {
                                    // Normal click goes to detail screen
                                    onEventClick(id)
                                }
                            },
                            onEventLongClick = { targetEvent ->
                                // 🔴 SECURITY GUARD: Allow long press actions only for TEACHER or ADMIN
                                if (userRole == "TEACHER" || userRole == "ADMIN") {
                                    onSelectionChanged(targetEvent.id, targetEvent.bannerUrl)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Access Denied: Students cannot modify events.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventFeedItem(
    event: EventSummary,
    isSelected: Boolean,
    onEventClick: (String) -> Unit,
    onEventLongClick: (EventSummary) -> Unit
) {
    val isTrending = event.registrationBadge == "TRENDING"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            // 🔴 Handles normal single tap and contextual long clicks smoothly
            .combinedClickable(
                onClick = { onEventClick(event.id) },
                onLongClick = { onEventLongClick(event) }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE0E7FF) else Color.White // Highlight background if selected
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = event.bannerUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Custom Badges positioning mapping based on backend payload tags
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(
                            color = if (isTrending) Color(0xFF1A237E) else Color(0xFFA7F3D0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = event.registrationBadge,
                        color = if (isTrending) Color.White else Color(0xFF047857),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Check overlay badge displayed during active contextual selection states
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = Color(0xFF1A237E)
                    )
                }

                Text(
                    event.clubName,
                    color = Color(0xFF0D9488),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(DateTimeValue.fromIso(event.startDateTime).display(), color = Color.Gray, fontSize = 13.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        event.venue ?: if (event.eventMode == "ONLINE") "Online Event" else "Venue TBA",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}