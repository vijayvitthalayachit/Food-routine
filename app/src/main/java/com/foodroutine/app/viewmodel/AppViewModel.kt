package com.foodroutine.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.foodroutine.app.data.CustomFastEntity
import com.foodroutine.app.data.FoodRoutineRepository
import com.foodroutine.core.model.MealPlan
import com.foodroutine.core.model.Recipe
import com.foodroutine.core.model.RecipeSource
import com.foodroutine.core.panchang.FastingCalendar
import com.foodroutine.core.panchang.FastingReason
import com.foodroutine.core.panchang.Location
import com.foodroutine.core.panchang.PanchangCalculator
import com.foodroutine.core.panchang.PanchangDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Prefill passed from a share intent to the recipe editor. */
data class SharedRecipeDraft(val url: String, val source: RecipeSource)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FoodRoutineRepository(application)

    val recipes: StateFlow<List<Recipe>> = repository.recipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customFasts: StateFlow<List<CustomFastEntity>> = repository.customFasts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlan: StateFlow<MealPlan?> = repository.activePlan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Draft coming from an ACTION_SEND share (Instagram/Facebook/...). */
    val sharedDraft = MutableStateFlow<SharedRecipeDraft?>(null)

    val generating = MutableStateFlow(false)
    val planError = MutableStateFlow<String?>(null)

    private val location = Location.DEFAULT

    fun onSharedText(text: String) {
        val url = extractUrl(text) ?: text.trim()
        sharedDraft.value = SharedRecipeDraft(url, RecipeSource.fromUrl(url))
    }

    fun consumeSharedDraft() {
        sharedDraft.value = null
    }

    fun panchangToday(): PanchangDay = PanchangCalculator.panchangFor(LocalDate.now(), location)

    fun fastingReasonToday(): FastingReason? = fastingReasonFor(LocalDate.now())

    fun fastingReasonFor(date: LocalDate): FastingReason? {
        val customDates = customFasts.value.map { LocalDate.ofEpochDay(it.epochDay) }.toSet()
        return FastingCalendar(location, customDates).fastingReasonFor(date)
    }

    fun saveRecipe(recipe: Recipe, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { repository.saveRecipe(recipe) }
            onDone(id)
        }
    }

    fun deleteRecipe(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteRecipe(id) }
    }

    fun addCustomFast(date: LocalDate, label: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.addCustomFast(date, label) }
    }

    fun removeCustomFast(fast: CustomFastEntity) {
        viewModelScope.launch(Dispatchers.IO) { repository.removeCustomFast(fast) }
    }

    fun generatePlan(from: LocalDate = LocalDate.now()) {
        val pool = recipes.value
        if (pool.isEmpty()) {
            planError.value = "Add at least one recipe before generating a plan."
            return
        }
        generating.value = true
        planError.value = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val customDates =
                        customFasts.value.map { LocalDate.ofEpochDay(it.epochDay) }.toSet()
                    repository.generatePlan(from, location, customDates, pool)
                }
            } catch (e: Exception) {
                planError.value = e.message ?: "Failed to generate plan"
            } finally {
                generating.value = false
            }
        }
    }

    fun shoppingListByDate(plan: MealPlan) = repository.shoppingListByDate(plan)

    fun groceryTasks(plan: MealPlan) = repository.groceryTasks(plan)

    private fun extractUrl(text: String): String? =
        Regex("https?://\\S+").find(text)?.value
}
