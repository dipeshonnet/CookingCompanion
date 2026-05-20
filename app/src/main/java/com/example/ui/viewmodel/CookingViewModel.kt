package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.MealPlan
import com.example.data.local.Recipe
import com.example.data.local.ShoppingItem
import com.example.data.local.UserSession
import com.example.data.local.RecipeComment
import com.example.data.local.RecipeLike
import com.example.data.repository.CookingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CookingViewModel(private val repository: CookingRepository) : ViewModel() {

    // Auth State
    val activeSession: StateFlow<UserSession?> = repository.activeSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    // Database Flows
    val recipes: StateFlow<List<Recipe>> = repository.allRecipesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRecipes: StateFlow<List<Recipe>> = repository.favoriteRecipesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingList: StateFlow<List<ShoppingItem>> = repository.shoppingListFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mealPlans: StateFlow<List<MealPlan>> = repository.mealPlansFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Workout-Style Play Recipe Mode State Flow Definitions
    val playingRecipe = MutableStateFlow<Recipe?>(null)
    val currentStepIndex = MutableStateFlow(0)
    val stepTimeRemaining = MutableStateFlow(0)
    val stepTimeTotal = MutableStateFlow(1)
    val isStepTimerRunning = MutableStateFlow(false)

    // Culinary Ambient Audio System
    val currentBgSoundType = MutableStateFlow("None") // "None", "Music", "Local", "Radio"
    val currentBgSoundName = MutableStateFlow("")     // Custom descriptive subtitle

    private var playRecipeJob: Job? = null

    // Active Cooking Timer (Legacy / Standard)
    private val _timerRecipeId = MutableStateFlow<Int?>(null)
    val timerRecipeId: StateFlow<Int?> = _timerRecipeId.asStateFlow()

    private val _timerRemaining = MutableStateFlow<Int?>(null)
    val timerRemaining: StateFlow<Int?> = _timerRemaining.asStateFlow()

    private val _timerTotal = MutableStateFlow(0)
    val timerTotal: StateFlow<Int> = _timerTotal.asStateFlow()

    private val _timerIsRunning = MutableStateFlow(false)
    val timerIsRunning: StateFlow<Boolean> = _timerIsRunning.asStateFlow()

    private var timerJob: Job? = null

    // UI Feedback Message
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // Auth Actions
    fun handleSocialLogin(email: String, displayName: String, provider: String, photoUrl: String) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            delay(1500) // Realistic social login delay
            repository.login(email, displayName, provider, photoUrl)
            _isLoggingIn.value = false
            _uiMessage.emit("Successfully logged in via $provider")
        }
    }

    fun handleLogout() {
        viewModelScope.launch {
            stopTimer()
            repository.logout()
            _uiMessage.emit("Logged out safely")
        }
    }

    // Recipe Favorites
    fun toggleFavorite(recipeId: Int, isFav: Boolean) {
        viewModelScope.launch {
            repository.setRecipeFavorite(recipeId, isFav)
            _uiMessage.emit(if (isFav) "Added to Favorites!" else "Removed from Favorites")
        }
    }

    fun completeCook(recipeId: Int, recipeTitle: String) {
        viewModelScope.launch {
            repository.incrementCookCount(recipeId)
            _uiMessage.emit("Congrats! You cooked '$recipeTitle'!")
        }
    }

    // Shopping List Actions
    fun addIngredientToShopping(ingredientName: String) {
        viewModelScope.launch {
            repository.addShoppingItem(ingredientName, "Ingredients")
            _uiMessage.emit("Added '$ingredientName' to your Shopping list!")
        }
    }

    fun addCustomShoppingItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addShoppingItem(name, "Groceries")
            _uiMessage.emit("Added '$name'")
        }
    }

    fun toggleShoppingItemBought(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(isBought = !item.isBought))
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun clearCompletedShopping() {
        viewModelScope.launch {
            repository.clearCompletedShoppingItems()
            _uiMessage.emit("Cleared bought items")
        }
    }

    // Meal Plan Actions
    fun updateMealPlan(day: String, mealType: String, recipeId: Int?, customName: String?) {
        viewModelScope.launch {
            val plans = mealPlans.value
            val existing = plans.find { it.dayOfWeek == day } ?: MealPlan(day)
            
            val updated = when (mealType.lowercase()) {
                "breakfast" -> existing.copy(breakfastRecipeId = recipeId, breakfastCustomName = customName)
                "lunch" -> existing.copy(lunchRecipeId = recipeId, lunchCustomName = customName)
                "dinner" -> existing.copy(dinnerRecipeId = recipeId, dinnerCustomName = customName)
                else -> existing
            }
            repository.saveMealPlan(updated)
            _uiMessage.emit("Updated $mealType for $day!")
        }
    }

    // Comments, Likes, Sharing & Profile edits
    fun getCommentsForRecipe(recipeId: Int): Flow<List<RecipeComment>> = repository.getCommentsForRecipeFlow(recipeId)
    fun getLikesForRecipe(recipeId: Int): Flow<List<RecipeLike>> = repository.getLikesForRecipeFlow(recipeId)
    fun hasUserLikedRecipe(recipeId: Int, email: String): Flow<Boolean> = repository.hasUserLikedRecipeFlow(recipeId, email)

    fun addComment(recipeId: Int, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            repository.addCommentToRecipe(recipeId, session.displayName, session.photoUrl, text)
            _uiMessage.emit("Review added to recipe!")
        }
    }

    fun toggleLike(recipeId: Int) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            repository.toggleRecipeSocialLike(recipeId, session.email)
        }
    }

    fun uploadRecipe(
        title: String,
        prepTime: String,
        difficulty: String,
        category: String,
        imageUrl: String,
        ingredients: String,
        instructions: String
    ) {
        if (title.isBlank() || ingredients.isBlank() || instructions.isBlank()) return
        viewModelScope.launch {
            val session = activeSession.value
            val chefName = session?.displayName ?: "Community Chef"
            val newRecipe = Recipe(
                title = title,
                chefName = chefName,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=600&auto=format&fit=crop" },
                prepTime = prepTime,
                difficulty = difficulty,
                category = category,
                ingredientsString = ingredients,
                instructionsString = instructions,
                isFavorite = false,
                totalCooked = 0
            )
            repository.shareUserRecipe(newRecipe)
            _uiMessage.emit("Successfully shared '$title' with the community!")
        }
    }

    fun updateProfile(displayName: String, photoUrl: String, bio: String, dietaryPreference: String) {
        if (displayName.isBlank()) return
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            repository.updateUserProfile(session.email, displayName, photoUrl, bio, dietaryPreference)
            _uiMessage.emit("Profile updated successfully!")
        }
    }

    // Gym Workout-Style Active "Play Recipe" Sequence
    fun selectAndPlayRecipe(recipe: Recipe) {
        playRecipeJob?.cancel()
        playingRecipe.value = recipe
        currentStepIndex.value = 0
        val instructions = recipe.getInstructions()
        if (instructions.isNotEmpty()) {
            setupStep(0)
        }
    }

    fun setupStep(index: Int) {
        val recipe = playingRecipe.value ?: return
        val instructions = recipe.getInstructions()
        if (index >= 0 && index < instructions.size) {
            currentStepIndex.value = index
            val text = instructions[index].lowercase()
            val seconds = when {
                text.contains("wash") || text.contains("rinse") -> 15
                text.contains("chop") || text.contains("slice") || text.contains("shred") || text.contains("peel") -> 25
                text.contains("whisk") || text.contains("mix") || text.contains("stir") || text.contains("blend") -> 30
                text.contains("preheat") || text.contains("heat") -> 40
                text.contains("sear") || text.contains("fry") || text.contains("sauté") || text.contains("brown") || text.contains("grill") -> 45
                text.contains("boil") || text.contains("simmer") || text.contains("sauce") || text.contains("cook") -> 60
                text.contains("bake") || text.contains("oven") || text.contains("roast") || text.contains("slow-cook") -> 90
                else -> 20
            }
            stepTimeRemaining.value = seconds
            stepTimeTotal.value = seconds
            isStepTimerRunning.value = true
            startStepTimerLoop()
        }
    }

    private fun startStepTimerLoop() {
        playRecipeJob?.cancel()
        playRecipeJob = viewModelScope.launch {
            while (stepTimeRemaining.value > 0) {
                delay(1000)
                if (isStepTimerRunning.value) {
                    stepTimeRemaining.value -= 1
                }
            }
            val instructions = playingRecipe.value?.getInstructions() ?: emptyList()
            if (currentStepIndex.value < instructions.size - 1) {
                _uiMessage.emit("Step ${currentStepIndex.value + 1} finalized! Let's handle the next step.")
                setupStep(currentStepIndex.value + 1)
            } else {
                _uiMessage.emit("Recipe completed! Splendid performance Chef!")
                isStepTimerRunning.value = false
                val rId = playingRecipe.value?.id ?: 0
                val rTitle = playingRecipe.value?.title ?: ""
                completeCook(rId, rTitle)
            }
        }
    }

    fun toggleStepTimer() {
        isStepTimerRunning.value = !isStepTimerRunning.value
    }

    fun nextStep() {
        val instructions = playingRecipe.value?.getInstructions() ?: return
        if (currentStepIndex.value < instructions.size - 1) {
            setupStep(currentStepIndex.value + 1)
        }
    }

    fun prevStep() {
        if (currentStepIndex.value > 0) {
            setupStep(currentStepIndex.value - 1)
        }
    }

    fun stopPlayingRecipe() {
        playRecipeJob?.cancel()
        playingRecipe.value = null
        isStepTimerRunning.value = false
    }

    fun setBackgroundSound(type: String, name: String) {
        currentBgSoundType.value = type
        currentBgSoundName.value = name
        if (type == "None") {
            _bgSoundFeedback("Background sound muted")
        } else {
            _bgSoundFeedback("Connected to $name")
        }
    }

    private fun _bgSoundFeedback(msg: String) {
        viewModelScope.launch {
            _uiMessage.emit(msg)
        }
    }

    // Timer Logic
    fun startTimer(recipeId: Int, durationSeconds: Int) {
        timerJob?.cancel()
        _timerRecipeId.value = recipeId
        _timerRemaining.value = durationSeconds
        _timerTotal.value = durationSeconds
        _timerIsRunning.value = true

        timerJob = viewModelScope.launch {
            while (_timerRemaining.value != null && _timerRemaining.value!! > 0) {
                delay(1000)
                if (_timerIsRunning.value) {
                    _timerRemaining.value = _timerRemaining.value!! - 1
                }
            }
            if (_timerRemaining.value == 0) {
                _uiMessage.emit("Timer finished! Perfect cooking time achieved!")
                stopTimer()
            }
        }
    }

    fun pauseResumeTimer() {
        _timerIsRunning.value = !_timerIsRunning.value
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerRecipeId.value = null
        _timerRemaining.value = null
        _timerIsRunning.value = false
    }
}

class CookingViewModelFactory(private val repository: CookingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CookingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
