/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lunchtray.ui.OrderViewModel
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.model.LunchTrayAppScreenEnum
import com.example.lunchtray.ui.AccompanimentMenuPreview
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen
import com.example.lunchtray.ui.theme.LunchTrayTheme

// TODO: AppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayAppBar(
    currentScreen: LunchTrayAppScreenEnum,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp(

) {
    // Create ViewModel
    val viewModel: OrderViewModel = viewModel()
    //Create Controller and initialization
    val navController: NavHostController = rememberNavController()
    // initialize the backstack entry and name of the current screen
    val backStackEntry by navController.currentBackStackEntryAsState()
    // The name of the current screen should be either the name of the initial screen or the name of the destination screen if it currently exists.
    val currentScreen = LunchTrayAppScreenEnum.valueOf(
        backStackEntry?.destination?.route ?: LunchTrayAppScreenEnum.START.name
    )

    Scaffold(
        topBar = {

            LunchTrayAppBar(
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                currentScreen = currentScreen
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 12.dp),
            navController = navController,
            startDestination = LunchTrayAppScreenEnum.START.name
        ) {
            composable(route = LunchTrayAppScreenEnum.START.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = { navController.navigate(LunchTrayAppScreenEnum.ENTREE_MENU.name) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = LunchTrayAppScreenEnum.ENTREE_MENU.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController
                        )
                    },
                    onNextButtonClicked = { navController.navigate(LunchTrayAppScreenEnum.SIDE_DISH_MENU.name) },
                    onSelectionChanged = { viewModel.updateEntree(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = LunchTrayAppScreenEnum.SIDE_DISH_MENU.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onSelectionChanged = { viewModel.updateSideDish(it) },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController
                        )
                    },
                    onNextButtonClicked = { navController.navigate(LunchTrayAppScreenEnum.ACCOMPANIMENT_MENU.name) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = LunchTrayAppScreenEnum.ACCOMPANIMENT_MENU.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onSelectionChanged = { viewModel.updateAccompaniment(it) },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController
                        )
                    },
                    onNextButtonClicked = { navController.navigate(LunchTrayAppScreenEnum.CHECKOUT.name)},
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = LunchTrayAppScreenEnum.CHECKOUT.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(
                            viewModel,
                            navController
                    ) },
                    onNextButtonClicked = { navController.navigate(LunchTrayAppScreenEnum.START.name)},
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(LunchTrayAppScreenEnum.START.name, inclusive = false)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LunchTrayAppPreview() {
    Surface(
        modifier = Modifier.statusBarsPadding()
    ) {
        LunchTrayTheme {
            LunchTrayApp()
        }
    }
}
