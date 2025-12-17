package com.example.gardenapp.ui.plan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.*
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlanVm @Inject constructor(
    private val repo: GardenRepository,
    private val referenceDao: ReferenceDao,
) : ViewModel() {

    private val _currentGarden = mutableStateOf<GardenEntity?>(null)
    val currentGarden: State<GardenEntity?> = _currentGarden

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
        repo.plants(gardenId)

    fun childGardensFlow(gardenId: String): Flow<List<GardenEntity>> =
        repo.getChildGardens(gardenId)

    suspend fun upsertPlant(p: PlantEntity) =
        repo.upsertPlant(p)

    suspend fun deletePlant(p: PlantEntity) =
        repo.deletePlant(p)

    suspend fun upsertGarden(g: GardenEntity) =
        repo.upsertGarden(g)

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
