package com.example.habithero

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.habithero.ui.settings.SettingsScreen
import com.example.habithero.ui.settings.SettingsViewModel
import com.example.habithero.ui.theme.HeroGold

sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Challenges : Screen("challenges", "Challenges", Icons.Default.EmojiEvents)
    object Rewards : Screen("rewards", "Rewards", Icons.Default.WorkspacePremium)
    object Settings : Screen("settings")
    object ChooseCategory : Screen("choose_category")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }
    object ChallengeDetail : Screen("challenge_detail/{challengeId}") {
        fun createRoute(challengeId: String) = "challenge_detail/$challengeId"
    }
    object CreateHabit : Screen("create_habit?habitId={habitId}") {
        fun createRoute(habitId: String? = null): String {
            return if (habitId != null) "create_habit?habitId=$habitId" else "create_habit"
        }
    }
    object HabitSelection : Screen("habit_selection/{categoryName}") {
        fun createRoute(categoryName: String) = "habit_selection/$categoryName"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Challenges,
    Screen.Rewards,
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = (context as? Activity)
    val haptics = LocalHapticFeedback.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Intercept back press on Home screen to minimize the app instead of closing it
    if (currentDestination?.route == Screen.Home.route) {
        BackHandler {
            activity?.moveTaskToBack(true)
        }
    }

    Scaffold(
        containerColor = Color.Transparent, // Let the theme gradient show through
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF010710), // Set a dark blue color
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(3.dp)
                                            .background(HeroGold, shape = RoundedCornerShape(2.dp))
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(3.dp)) // Maintain space
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(screen.icon!!, contentDescription = null)
                            }
                        },
                        label = { Text(screen.label!!) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HeroGold,
                            selectedTextColor = HeroGold,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent // Remove the default pill indicator
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel(),
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onFabClick = { navController.navigate(Screen.ChooseCategory.route) },
                    onHabitClick = { habit -> navController.navigate(Screen.HabitDetail.createRoute(habit.id)) }
                )
            }
            composable(Screen.Challenges.route) {
                val challengesViewModel: ChallengesViewModel = viewModel()
                ChallengesScreen(
                    viewModel = challengesViewModel,
                    onBackClick = { navController.popBackStack() },
                    onChallengeClick = { challengeId -> navController.navigate(Screen.ChallengeDetail.createRoute(challengeId)) }
                )
            }
            composable(
                route = Screen.ChallengeDetail.route,
                arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
            ) { 
                val viewModel: ChallengeDetailViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                LaunchedEffect(uiState.challengeAccepted) {
                    if (uiState.challengeAccepted) {
                         navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
                ChallengeDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Rewards.route) { RewardsScreen() }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel()
                val settingsState by settingsViewModel.state.collectAsState()
                SettingsScreen(
                    state = settingsState,
                    onBack = { navController.popBackStack() },
                    onChangeName = { settingsViewModel.onNameChange(it) },
                    onSaveName = { settingsViewModel.saveName() },
                    onPickPhoto = { settingsViewModel.uploadPhoto(it) },
                    onSignOut = {
                        settingsViewModel.signOut()
                        val intent = Intent(context, AuthenticationActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                )
            }
            composable(Screen.ChooseCategory.route) {
                val chooseCategoryViewModel: ChooseCategoryViewModel = viewModel()

                ChooseCategoryScreen(
                    viewModel = chooseCategoryViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCategoryClick = { categoryName ->
                        if (categoryName == "Create Your Own") {
                            navController.navigate(Screen.CreateHabit.createRoute())
                        } else {
                            navController.navigate(Screen.HabitSelection.createRoute(categoryName))
                        }
                    }
                )
            }
            composable(
                route = Screen.HabitDetail.route,
                arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink { uriPattern = "habithero://habit/{habitId}" })
            ) { backStackEntry ->
                val detailViewModel: DetailViewModel = viewModel()
                val habitUpdated by backStackEntry.savedStateHandle.getLiveData<Boolean>("habit_updated").observeAsState()
                LaunchedEffect(habitUpdated) {
                    if (habitUpdated == true) {
                        detailViewModel.refresh()
                        backStackEntry.savedStateHandle.remove<Boolean>("habit_updated")
                    }
                }
                HabitDetailScreen(
                    viewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { habitId -> navController.navigate(Screen.CreateHabit.createRoute(habitId)) }
                )
            }
            composable(
                route = Screen.CreateHabit.route,
                arguments = listOf(navArgument("habitId") { nullable = true; type = NavType.StringType })
            ) { backStackEntry ->
                val createHabitViewModel: CreateHabitViewModel = viewModel()
                val habitId = backStackEntry.arguments?.getString("habitId")

                CreateHabitScreen(
                    viewModel = createHabitViewModel,
                    onHabitCreatedOrUpdated = {
                        if (habitId != null) {
                            navController.previousBackStackEntry?.savedStateHandle?.set("habit_updated", true)
                            navController.popBackStack()
                        } else {
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.HabitSelection.route,
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) {
                HabitSelectionScreen(
                    onHabitAdded = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
