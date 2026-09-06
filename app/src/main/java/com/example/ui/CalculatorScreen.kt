package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header & Navigation Tabs
            Column(modifier = Modifier.fillMaxWidth()) {
                HighDensityHeader(
                    themeMode = uiState.themeMode,
                    onToggleTheme = { viewModel.toggleTheme() },
                    historyCount = historyList.size,
                    onOpenHistory = { viewModel.setHistoryOpen(true) }
                )

                HighDensityNavTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }

            // Dynamic Calculator Screen Display
            CalculatorDisplay(
                expression = uiState.expression,
                previewResult = uiState.previewResult,
                finalResult = uiState.finalResult,
                error = uiState.error,
                selectedTab = uiState.selectedTab,
                isDegreeMode = uiState.isDegreeMode,
                modifier = Modifier.weight(1f)
            )

            // High Density Calculator Keypad (Basic or Scientific)
            CalculatorKeypad(
                selectedTab = uiState.selectedTab,
                isDegreeMode = uiState.isDegreeMode,
                isSecondFunction = uiState.isSecondFunction,
                onDigit = { viewModel.onDigit(it) },
                onDecimal = { viewModel.onDecimal() },
                onOperator = { viewModel.onOperator(it) },
                onFunction = { viewModel.onFunction(it) },
                onConstant = { viewModel.onConstant(it) },
                onParenthesis = { viewModel.onParenthesis() },
                onSpecial = { viewModel.onSpecial(it) },
                onBackspace = { viewModel.onBackspace() },
                onClear = { viewModel.onClear() },
                onEquals = { viewModel.onEquals() },
                onToggleDegree = { viewModel.toggleDegreeMode() },
                onToggleSecondFunction = { viewModel.toggleSecondFunction() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // History Bottom Sheet
    if (uiState.isHistoryOpen) {
        HistoryBottomSheet(
            historyList = historyList,
            sheetState = sheetState,
            onDismiss = { viewModel.setHistoryOpen(false) },
            onItemClick = { item, loadResult ->
                viewModel.onHistoryItemClick(item, loadResult)
            },
            onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
            onClearAll = { viewModel.clearAllHistory() }
        )
    }
}

@Composable
private fun HighDensityHeader(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    historyCount: Int,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity: Gradient Icon & CalcNeo Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f))
                    )
                }
            }

            Text(
                text = "CalcNeo",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Action Icons: Theme Mode Toggle & History
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme Mode switcher
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                    .size(38.dp)
                    .testTag("btn_theme_toggle")
            ) {
                val icon = when (themeMode) {
                    ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                    ThemeMode.DARK -> Icons.Default.DarkMode
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Calculation History button
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                    .size(38.dp)
                    .testTag("btn_history")
            ) {
                BadgedBox(
                    badge = {
                        if (historyCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(
                                    text = if (historyCount > 99) "99+" else historyCount.toString(),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Calculation History",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HighDensityNavTabs(
    selectedTab: CalculatorTab,
    onTabSelected: (CalculatorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            NavTabItem(
                title = "Basic",
                isSelected = selectedTab == CalculatorTab.BASIC,
                onClick = { onTabSelected(CalculatorTab.BASIC) },
                modifier = Modifier.weight(1f),
                testTag = "tab_basic"
            )

            NavTabItem(
                title = "Scientific",
                isSelected = selectedTab == CalculatorTab.SCIENTIFIC,
                onClick = { onTabSelected(CalculatorTab.SCIENTIFIC) },
                modifier = Modifier.weight(1f),
                testTag = "tab_scientific"
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun NavTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(),
        label = "tab_text_color"
    )

    val indicatorColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(),
        label = "tab_indicator_color"
    )

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
            .padding(bottom = 8.dp, top = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = textColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(indicatorColor)
            )
        }
    }
}
