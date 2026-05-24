package com.tracqi.fsensorapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.tracqi.fsensorapp.model.ChartData
import com.tracqi.fsensorapp.model.GpsUiState
import com.tracqi.fsensorapp.ui.theme.CyberSurface
import com.tracqi.fsensorapp.ui.theme.NeonCyan
import com.tracqi.fsensorapp.ui.theme.SecondaryText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    orientation: FloatArray,
    acceleration: FloatArray,
    orientationHistory: ChartData,
    accelerationHistory: ChartData,
    fusionName: String,
    gpsUiState: GpsUiState,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSettingsClick: () -> Unit,
    onReset: () -> Unit
) {
    val tabs = listOf("Orientation", "Acceleration", "GPS")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FSensor", color = NeonCyan) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface,
                    actionIconContentColor = NeonCyan
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = CyberSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = NeonCyan
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                title,
                                color = if (pagerState.currentPage == index) NeonCyan else SecondaryText
                            )
                        }
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> OrientationTab(orientation, orientationHistory, fusionName, onReset)
                    1 -> AccelerationTab(acceleration, accelerationHistory, fusionName, onReset)
                    2 -> GpsTab(gpsUiState, hasLocationPermission, onRequestPermission, onReset)
                }
            }
        }
    }
}
