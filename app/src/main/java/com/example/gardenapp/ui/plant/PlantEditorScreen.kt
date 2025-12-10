package com.example.gardenapp.ui.plant

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.ui.dashboard.UiEvent
import com.example.gardenapp.ui.plant.dialogs.AddCareRuleDialog
import com.example.gardenapp.ui.plant.dialogs.AddFertilizerLogDialog
import com.example.gardenapp.ui.plant.dialogs.AddHarvestLogDialog
import com.example.gardenapp.ui.plant.tabs.CareRulesTab
import com.example.gardenapp.ui.plant.tabs.FertilizerLogTab
import com.example.gardenapp.ui.plant.tabs.HarvestLogTab
import com.example.gardenapp.ui.plant.tabs.InfoTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlantEditorScreen(onBack: () -> Unit, vm: PlantEditorVm = hiltViewModel()) {
    val plant by vm.plant.collectAsState()
    val fertilizerLogs by vm.fertilizerLogs.collectAsState(initial = emptyList())
    val harvestLogs by vm.harvestLogs.collectAsState(initial = emptyList())
    val careRules by vm.careRules.collectAsState(initial = emptyList())
    val varietyDetails by vm.varietyDetails.collectAsState()
    val varietyTags by vm.varietyTags.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(message = event.message)
            }
        }
    }

    val tabTitles = listOf("Инфо", "Удобрения", "Урожай", "Уход")
    val pagerState = rememberPagerState { tabTitles.size }
    val scope = rememberCoroutineScope()

    var showAddFertilizerDialog by remember { mutableStateOf(false) }
    var showAddHarvestDialog by remember { mutableStateOf(false) }
    var showAddCareRuleDialog by remember { mutableStateOf(false) }

    if (showAddFertilizerDialog) {
        AddFertilizerLogDialog(
            onDismiss = { showAddFertilizerDialog = false },
            onAddLog = { grams, date, note ->
                vm.addFertilizerLog(grams, date, note)
                showAddFertilizerDialog = false
            }
        )
    }
    if (showAddHarvestDialog) {
        AddHarvestLogDialog(
            onDismiss = { showAddHarvestDialog = false },
            onAddLog = { weight, date, note ->
                vm.addHarvestLog(weight, date, note)
                showAddHarvestDialog = false
            }
        )
    }
    if (showAddCareRuleDialog) {
        AddCareRuleDialog(
            onDismiss = { showAddCareRuleDialog = false },
            onAddRule = { type, days ->
                vm.addCareRule(type, LocalDate.now(), days)
                showAddCareRuleDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(plant?.title ?: "Загрузка...") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }
            HorizontalPager(state = pagerState) {
                page ->
                Box(modifier = Modifier.fillMaxSize()) {
                     when (page) {
                        0 -> InfoTab(plant = plant, variety = varietyDetails, tags = varietyTags)
                        1 -> FertilizerLogTab(logs = fertilizerLogs, onAdd = { showAddFertilizerDialog = true }, onDelete = { vm.deleteFertilizerLog(it) })
                        2 -> HarvestLogTab(logs = harvestLogs, onAdd = { showAddHarvestDialog = true }, onDelete = { vm.deleteHarvestLog(it) })
                        3 -> CareRulesTab(rules = careRules, onAdd = { showAddCareRuleDialog = true }, onDelete = { vm.deleteCareRule(it) })
                    }
                }
            }
        }
    }
}
