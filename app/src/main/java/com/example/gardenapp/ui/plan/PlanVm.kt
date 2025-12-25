package com.example.gardenapp.ui.plan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.*
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanVm @Inject constructor(
    private val repo: GardenRepository,
    private val referenceDao: ReferenceDao,
    private val settingsManager: SettingsManager // ADDED
) : ViewModel() {

    private val _currentGarden = mutableStateOf<GardenEntity?>(null)
    val currentGarden: State<GardenEntity?> = _currentGarden

    val lastUsedGroupId = settingsManager.lastUsedGroupId.stateIn(viewModelScope, SharingStarted.Lazily, null) // ADDED
    val lastUsedCultureId = settingsManager.lastUsedCultureId.stateIn(viewModelScope, SharingStarted.Lazily, null) // ADDED

    init {
        // тут пока ничего не нужно, но место под init-логику есть
    }

    fun loadGarden(gardenId: String) {
        viewModelScope.launch {
            // наблюдаем за садом и обновляем стейт при любых изменениях в БД
            repo.observeGardenById(gardenId).collect { garden ->
                _currentGarden.value = garden
            }
        }
    }

    // --- ПЛАН / ОБЪЕКТЫ ---

    fun plantsFlow(gardenId: String): Flow<List<PlantEntity>> =
        repo.plantsForGardens(gardenId)

    fun childGardensFlow(gardenId: String): Flow<List<GardenEntity>> =
        repo.getChildGardens(gardenId)

    fun getPendingTasksForGardens(gardenId: String): Flow<List<TaskInstanceEntity>> = // ADDED
        repo.getPendingTasksForGardens(gardenId)

    suspend fun upsertPlant(p: PlantEntity) =
        repo.upsertPlant(p)

    suspend fun deletePlant(p: PlantEntity) =
        repo.deletePlant(p)

    suspend fun upsertGarden(g: GardenEntity) =
        repo.upsertGarden(g)

    fun saveLastUsedIds(groupId: String, cultureId: String) { // ADDED
        viewModelScope.launch {
            settingsManager.setLastUsedIds(groupId, cultureId)
        }
    }

    suspend fun getGroupAndCultureIdsForVariety(varietyId: String): Pair<String, String>? {
        val variety = referenceDao.getVariety(varietyId).first()
        variety?.let {
            val culture = referenceDao.getCulture(it.cultureId).first()
            culture?.let { c ->
                return Pair(c.groupId, c.id)
            }
        }
        return null
    }

    // --- СПРАВОЧНИКИ (referenceDao) ---

    val referenceGroups: Flow<List<ReferenceGroupEntity>> =
        referenceDao.getGroups()

    fun getCulturesByGroup(groupId: String): Flow<List<ReferenceCultureEntity>> =
        referenceDao.getCulturesByGroup(groupId)

    fun getVarietiesByCulture(cultureId: String): Flow<List<ReferenceVarietyEntity>> =
        referenceDao.getVarietiesByCulture(cultureId)

    fun getTagsForVariety(varietyId: String): Flow<List<ReferenceTagEntity>> =
        referenceDao.getTagsForVariety(varietyId)

    fun getAllCultures(): Flow<List<ReferenceCultureEntity>> =
        referenceDao.getAllCultures()

    fun getAllVarieties(): Flow<List<ReferenceVarietyEntity>> =
        referenceDao.getAllVarieties()
}
