package com.example.habithero

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habithero.ui.settings.SettingsScreen
import com.example.habithero.ui.settings.SettingsViewModel
import com.example.habithero.ui.theme.HeroGold

sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Rewards : Screen("rewards", "Rewards", Icons.Default.EmojiEvents)
    object Settings : Screen("settings")
    object ChooseCategory : Screen("choose_category")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
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
    Screen.Rewards,
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = (context as? Activity)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Intercept back press on Home screen to minimize the app instead of closing it
    if (currentDestination?.route == Screen.Home.route) {
        BackHandler {
            activity?.moveTaskToBack(true)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = null) },
                        label = { Text(screen.label!!) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HeroGold,
                            selectedTextColor = HeroGold,
                            indicatorColor = MaterialTheme.colorScheme.surface
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
                val categoriesMap by chooseCategoryViewModel.categories.collectAsState()
                val cameFromCreateHabit = navController.previousBackStackEntry?.destination?.route?.startsWith("create_habit") == true

                ChooseCategoryScreen(
                    viewModel = chooseCategoryViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCategoryClick = { categoryName ->
                        if (cameFromCreateHabit) {
                            val iconUrl = categoriesMap[categoryName] ?: ""
                            navController.previousBackStackEntry?.savedStateHandle?.set("category_name", categoryName)
                            navController.previousBackStackEntry?.savedStateHandle?.set("category_icon_url", iconUrl)
                            navController.popBackStack()
                        } else {
                            if (categoryName == "Create Your Own") {
                                navController.navigate(Screen.CreateHabit.createRoute())
                            } else {
                                navController.navigate(Screen.HabitSelection.createRoute(categoryName))
                            }
                        }
                    }
                )
            }
            composable(
                route = Screen.HabitDetail.route,
                arguments = listOf(navArgument("habitId") { type = NavType.StringType })
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

                val categoryName by backStackEntry.savedStateHandle.getLiveData<String>("category_name").observeAsState()
                val categoryIconUrl by backStackEntry.savedStateHandle.getLiveData<String>("category_icon_url").observeAsState()

                LaunchedEffect(categoryName, categoryIconUrl) {
                    if (categoryName != null && categoryIconUrl != null) {
                        createHabitViewModel.onCategorySelected(categoryName!!, categoryIconUrl!!)
                        backStackEntry.savedStateHandle.remove<String>("category_name")
                        backStackEntry.savedStateHandle.remove<String>("category_icon_url")
                    }
                }

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
                    onBackClick = { navController.popBackStack() },
                    onCategoryClick = { navController.navigate(Screen.ChooseCategory.route) }
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

// --- Placeholder Screens --- //

@Composable
fun RewardsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Rewards Screen - Coming Soon!")
    }
}
