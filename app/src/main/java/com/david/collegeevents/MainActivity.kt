package com.david.collegeevents

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.david.collegeevents.domain.repository.AdminEventRepository
import com.david.collegeevents.presentation.auth.LoginScreen
import com.david.collegeevents.presentation.auth.RegisterScreen
import com.david.collegeevents.presentation.createEvent.CreateEventScreen
import com.david.collegeevents.presentation.details.EventDetailScreen
import com.david.collegeevents.presentation.events.EventsScreen
import com.david.collegeevents.presentation.profile.ProfileScreen
import com.david.collegeevents.ui.theme.CollegeEventsTheme
import com.david.collegeevents.utils.Resource
import com.david.collegeevents.utils.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    // Direct injection for drop actions and background cloud clearings tracking workflows
    @Inject
    lateinit var adminEventRepository: AdminEventRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollegeEventsTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val startDestination = runBlocking {
                    val token = tokenManager.tokenFlow.first()
                    if (!token.isNullOrBlank()) "home" else "login"
                }

                val navController = rememberNavController()
                val currentRoute = navController
                    .currentBackStackEntryFlow
                    .collectAsState(initial = navController.currentBackStackEntry)
                    .value?.destination?.route

                // Contextual Action Bar (CAB) State trackers hooks
                var selectedEventId by remember { mutableStateOf<String?>(null) }
                var selectedEventBannerUrl by remember { mutableStateOf<String?>(null) }
                val isContextualMode = selectedEventId != null

                // Visibility configuration mapping matrices
                val showBottomBar = currentRoute in listOf("home", "profile")
                val showTopBar = currentRoute == "home"

                val userRole by tokenManager.userRoleFlow.collectAsState(initial = "STUDENT")
                val showFabButton = userRole == "ADMIN" || userRole == "TEACHER"

                Scaffold(
                    floatingActionButton = {
                        // FAB is only rendered when not in CAB selection mode to protect UX layouts consistency
                        if (showFabButton && showTopBar && !isContextualMode) {
                            FloatingActionButton(
                                onClick = { navController.navigate("create_event_route") },
                                containerColor = Color(0xFF1A237E),
                                contentColor = Color.White,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Publish Event")
                            }
                        }
                    },
                    topBar = {
                        if (showTopBar) {
                            TopAppBar(
                                title = {
                                    if (isContextualMode) {
                                        Text(
                                            "1 Event Selected",
                                            color = Color(0xFF1A237E),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.School,
                                                contentDescription = null,
                                                tint = Color(0xFF1A237E),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "College Event",
                                                color = Color(0xFF1A237E),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                },
                                navigationIcon = {
                                    if (isContextualMode) {
                                        IconButton(onClick = {
                                            selectedEventId = null
                                            selectedEventBannerUrl = null
                                        }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Cancel Selection",
                                                tint = Color.Black
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    if (isContextualMode) {
                                        // ── CAB ACTIVE ACTIONS: Visible only when an item is long-pressed ──
                                        IconButton(onClick = {
                                            selectedEventId?.let { id ->
                                                // Forward ID dynamically inside route paths arguments
                                                navController.navigate("create_event_route?editEventId=$id")
                                            }
                                            selectedEventId =
                                                null // Clear state selection context upon forward steering
                                            selectedEventBannerUrl = null
                                        }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Event",
                                                tint = Color(0xFF1A237E)
                                            )
                                        }

                                        IconButton(onClick = {
                                            val targetId = selectedEventId
                                            val targetBanner = selectedEventBannerUrl

                                            if (targetId != null) {
                                                scope.launch {
                                                    Toast.makeText(
                                                        context,
                                                        "Executing database deletion cascade...",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    //  DUAL PERSISTENCE DROP: Drops event database row + clears backend binary storage image files completely
                                                    adminEventRepository.dropEvent(targetId)
                                                        .collect { result ->
                                                            when (result) {
                                                                is Resource.Success -> {
                                                                    targetBanner?.let { bannerPath ->
                                                                        adminEventRepository.deleteImage(
                                                                            bannerPath
                                                                        ).launchIn(this)
                                                                    }
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Event wiped successfully.",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                    // Trigger dynamic pull updates state modifications or nav refresh
                                                                    navController.navigate("home") {
                                                                        popUpTo(
                                                                            "home"
                                                                        ) { inclusive = true }
                                                                    }
                                                                }

                                                                is Resource.Error -> {
                                                                    Toast.makeText(
                                                                        context,
                                                                        result.message
                                                                            ?: "Deletion failure operation transaction.",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }

                                                                is Resource.Loading -> { /* Loading state tracked */
                                                                }
                                                            }
                                                        }
                                                }
                                            }
                                            selectedEventId = null
                                            selectedEventBannerUrl = null
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Event",
                                                tint = Color.Red
                                            )
                                        }
                                    } else {
                                        IconButton(onClick = { }) {
                                            Icon(
                                                Icons.Default.NotificationsNone,
                                                contentDescription = null,
                                                tint = Color.Black
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                            )
                        }
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(containerColor = Color.White) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") { launchSingleTop = true }
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text("Events") },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(
                                            0xFFA7F3D0
                                        )
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        navController.navigate("profile") { launchSingleTop = true }
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.PersonOutline,
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text("Profile") },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(
                                            0xFFA7F3D0
                                        )
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(navController = navController) {
                                navController.navigate("home") {
                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                }
                            }
                        }

                        composable("register") {
                            RegisterScreen(navController = navController) {
                                navController.navigate("home") {
                                    popUpTo("register") {
                                        inclusive = true
                                    }
                                }
                            }
                        }

                        composable("home") {
                            //  Sync screen component tracking state variables references parameters
                            EventsScreen(
                                onEventClick = { eventId -> navController.navigate("event_details/$eventId") },
                                onNavigateToCreate = { navController.navigate("create_event_route") },
                                onNavigateToEdit = { id -> navController.navigate("create_event_route?editEventId=$id") },
                                selectedEventId = selectedEventId,
                                onSelectionChanged = { id, banner ->
                                    selectedEventId = id
                                    selectedEventBannerUrl = banner
                                }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                onEventClick = { eventId -> navController.navigate("event_details/$eventId") },
                                onLogoutDone = {
                                    navController.navigate("login") {
                                        popUpTo(0) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "event_details/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) {
                            EventDetailScreen(onBack = { navController.popBackStack() })
                        }

                        // HIGH LAYER PRODUCTION DEEP ROUTING SETUP FOR EDIT MODE INCOMPATIBILITIES
                        composable(
                            route = "create_event_route?editEventId={editEventId}",
                            arguments = listOf(
                                navArgument("editEventId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val editEventId = backStackEntry.arguments?.getString("editEventId")
                            CreateEventScreen(
                                editEventId = editEventId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}