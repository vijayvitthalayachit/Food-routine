package com.foodroutine.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foodroutine.app.ui.screens.GroceryScreen
import com.foodroutine.app.ui.screens.PlanScreen
import com.foodroutine.app.ui.screens.RecipeEditScreen
import com.foodroutine.app.ui.screens.RecipesScreen
import com.foodroutine.app.ui.screens.SettingsScreen
import com.foodroutine.app.ui.screens.TodayScreen
import com.foodroutine.app.ui.theme.FoodRoutineTheme
import com.foodroutine.app.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private var viewModelRef: AppViewModel? = null

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            FoodRoutineTheme {
                val vm: AppViewModel = viewModel()
                viewModelRef = vm
                handleShareIntent(intent, vm)
                FoodRoutineApp(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModelRef?.let { handleShareIntent(intent, it) }
    }

    private fun handleShareIntent(intent: Intent?, vm: AppViewModel) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { vm.onSharedText(it) }
            // Consume so recomposition doesn't re-trigger.
            intent.action = null
        }
    }
}

private data class Tab(val route: String, val labelRes: Int, val icon: ImageVector)

@Composable
fun FoodRoutineApp(vm: AppViewModel) {
    val navController: NavHostController = rememberNavController()
    val tabs = listOf(
        Tab("today", R.string.tab_today, Icons.Filled.Home),
        Tab("plan", R.string.tab_plan, Icons.Filled.CalendarMonth),
        Tab("recipes", R.string.tab_recipes, Icons.AutoMirrored.Filled.MenuBook),
        Tab("grocery", R.string.tab_grocery, Icons.Filled.ShoppingCart),
        Tab("settings", R.string.tab_settings, Icons.Filled.Settings)
    )

    // A share arrived: open the recipe editor prefilled with the link.
    val sharedDraft by vm.sharedDraft.collectAsState()
    androidx.compose.runtime.LaunchedEffect(sharedDraft) {
        if (sharedDraft != null) {
            val current = navController.currentBackStackEntry?.destination?.route
            if (current?.startsWith("recipe_edit") != true) {
                navController.navigate("recipe_edit")
            }
        }
    }

    Scaffold(
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo("today") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(padding)
        ) {
            composable("today") { TodayScreen(vm) }
            composable("plan") { PlanScreen(vm) }
            composable("recipes") {
                RecipesScreen(
                    vm = vm,
                    onAdd = { navController.navigate("recipe_edit") },
                    onEdit = { id -> navController.navigate("recipe_edit?recipeId=$id") }
                )
            }
            composable("grocery") { GroceryScreen(vm) }
            composable("settings") { SettingsScreen(vm) }
            composable("recipe_edit?recipeId={recipeId}") { entry ->
                val recipeId = entry.arguments?.getString("recipeId")?.toLongOrNull()
                RecipeEditScreen(
                    vm = vm,
                    recipeId = recipeId,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}
