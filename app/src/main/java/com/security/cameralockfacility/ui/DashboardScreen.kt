package com.security.cameralockfacility.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.security.cameralockfacility.R
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color(0xFF0B101F),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ADMIN PORTAL",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0B101F) // Same as background for a seamless look
                ),
                // Add an optional action icon like a logout or profile button
                /*actions = {
                    IconButton(onClick = { *//* Handle Profile *//* }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                }*/
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        // The innerPadding now handles both the TopBar and BottomBar height
        Box(modifier = Modifier.padding(innerPadding)) {
            DashboardNavHost(navController)
        }
    }
}

@Composable
fun DashboardNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Facility.route
    ) {
        composable(NavigationItem.Facility.route) {
            FacilityContent()
        }
        composable(NavigationItem.ForceExit.route) {
            ForceExitContent()
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Facility,
        NavigationItem.ForceExit
    )

    NavigationBar(
        // Slightly lighter navy for the bar to create a subtle separation
        containerColor = Color(0xFF111727),
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        // Pass parameters directly to avoid TextStyle constructor conflicts
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2196F3), // Bright Blue from your logo
                    selectedTextColor = Color(0xFF2196F3),
                    unselectedIconColor = Color(0xFF8A92A6), // Muted Gray-Blue
                    unselectedTextColor = Color(0xFF8A92A6),
                    // This removes the large "pill" background for a cleaner look
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun DashboardPreview() {
    // You can see the dark theme and bottom navigation bar here
    DashboardScreen()
}

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Facility : NavigationItem("facility", R.drawable.icon_facility, "Facility")
    object ForceExit : NavigationItem("force_exit", R.drawable.icon_force_exit, "Force Exit")
}