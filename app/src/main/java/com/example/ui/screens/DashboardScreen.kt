package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.MealPlan
import com.example.data.local.Recipe
import com.example.data.local.ShoppingItem
import com.example.data.local.UserSession
import com.example.ui.viewmodel.CookingViewModel
import com.example.ui.theme.*

enum class DashboardTab {
    DISCOVER, PLAY_RECIPE, MEAL_PLAN, INGREDIENTS, PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CookingViewModel,
    onLogout: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    var currentTab by remember { mutableStateOf(DashboardTab.DISCOVER) }

    // State for viewing detailed recipe
    var selectedDetailRecipe by remember { mutableStateOf<Recipe?>(null) }
    
    // Subscribe to detail recipe state flow (to keep stats, favorites dynamic)
    val recipeList by viewModel.recipes.collectAsState()
    val activeDetailRecipe = recipeList.find { it.id == selectedDetailRecipe?.id } ?: selectedDetailRecipe

    // Navigation trigger upon logging out
    LaunchedEffect(activeSession) {
        if (activeSession == null) {
            onLogout()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_cooking_logo_1779269686357),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = when (currentTab) {
                                DashboardTab.DISCOVER -> "Discover Recipes"
                                DashboardTab.PLAY_RECIPE -> "Play Kitchen Studio"
                                DashboardTab.MEAL_PLAN -> "Weekly Menu"
                                DashboardTab.INGREDIENTS -> "Ingredients Pantry"
                                DashboardTab.PROFILE -> "Chef Studio"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                },
                actions = {
                    if (currentTab == DashboardTab.PROFILE) {
                        IconButton(
                            onClick = { viewModel.handleLogout() },
                            modifier = Modifier.testTag("logout_btn")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Column {
                // Background Active Timer Overlay
                TimerWidgetOverlay(viewModel = viewModel, recipesList = recipeList) { activeId ->
                    val clickedRecipe = recipeList.find { it.id == activeId }
                    if (clickedRecipe != null) {
                        selectedDetailRecipe = clickedRecipe
                    }
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == DashboardTab.DISCOVER,
                        onClick = { currentTab = DashboardTab.DISCOVER },
                        icon = { Icon(if (currentTab == DashboardTab.DISCOVER) Icons.Filled.RestaurantMenu else Icons.Outlined.RestaurantMenu, null) },
                        label = { Text("Discover") },
                        modifier = Modifier.testTag("tab_discover")
                    )
                    NavigationBarItem(
                        selected = currentTab == DashboardTab.PLAY_RECIPE,
                        onClick = { currentTab = DashboardTab.PLAY_RECIPE },
                        icon = { Icon(if (currentTab == DashboardTab.PLAY_RECIPE) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircleOutline, null) },
                        label = { Text("Play Recipe") },
                        modifier = Modifier.testTag("tab_playrecipe")
                    )
                    NavigationBarItem(
                        selected = currentTab == DashboardTab.MEAL_PLAN,
                        onClick = { currentTab = DashboardTab.MEAL_PLAN },
                        icon = { Icon(if (currentTab == DashboardTab.MEAL_PLAN) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth, null) },
                        label = { Text("Planning") },
                        modifier = Modifier.testTag("tab_mealplan")
                    )
                    NavigationBarItem(
                        selected = currentTab == DashboardTab.INGREDIENTS,
                        onClick = { currentTab = DashboardTab.INGREDIENTS },
                        icon = { Icon(if (currentTab == DashboardTab.INGREDIENTS) Icons.Filled.Kitchen else Icons.Outlined.Kitchen, null) },
                        label = { Text("Ingredients") },
                        modifier = Modifier.testTag("tab_ingredients")
                    )
                    NavigationBarItem(
                        selected = currentTab == DashboardTab.PROFILE,
                        onClick = { currentTab = DashboardTab.PROFILE },
                        icon = { Icon(if (currentTab == DashboardTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, null) },
                        label = { Text("Chef Profile") },
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                DashboardTab.DISCOVER -> DiscoverPanel(viewModel = viewModel, onRecipeClick = { selectedDetailRecipe = it })
                DashboardTab.PLAY_RECIPE -> PlayRecipePanel(viewModel = viewModel, onSelectRecipeClick = { currentTab = DashboardTab.DISCOVER })
                DashboardTab.MEAL_PLAN -> MealPlanPanel(viewModel = viewModel, recipesList = recipeList)
                DashboardTab.INGREDIENTS -> IngredientsPanel(viewModel = viewModel, onRecipeClick = { selectedDetailRecipe = it })
                DashboardTab.PROFILE -> ProfilePanel(viewModel = viewModel, recipesList = recipeList)
            }
        }
    }

    // Recipe Detail Modal Full Screen Dialog
    activeDetailRecipe?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            viewModel = viewModel,
            onDismiss = { selectedDetailRecipe = null }
        )
    }
}

// ==========================================
// 1. DISCOVER TAB SCREEN PANEL
// ==========================================
@Composable
fun DiscoverPanel(
    viewModel: CookingViewModel,
    onRecipeClick: (Recipe) -> Unit
) {
    val recipesList by viewModel.recipes.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Breakfast", "Healthy", "Dinner", "Desserts")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search text field
        OutlinedTextField(
            value = searchKeyword,
            onValueChange = { searchKeyword = it },
            placeholder = { Text("Search delicious meals, chefs, or items...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchKeyword.isNotEmpty()) {
                {
                    IconButton(onClick = { searchKeyword = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            } else null,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("recipe_search_input"),
            singleLine = true
        )

        // Categories selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("chip_$category")
                )
            }
        }

        // Search and category filtered local recipes
        val filteredRecipes = recipesList.filter { recipe ->
            val matchesSearch = recipe.title.contains(searchKeyword, ignoreCase = true) ||
                    recipe.chefName.contains(searchKeyword, ignoreCase = true) ||
                    recipe.ingredientsString.contains(searchKeyword, ignoreCase = true)
            
            val matchesCategory = selectedCategory == "All" || recipe.category.equals(selectedCategory, ignoreCase = true)
            
            matchesSearch && matchesCategory
        }

        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentDissatisfied,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Recipes Found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try clearing search keywords or selecting a different cuisine filter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("recipes_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredRecipes) { recipe ->
                    RecipeCardItem(
                        recipe = recipe,
                        onCardClick = { onRecipeClick(recipe) },
                        onFavoriteToggle = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCardItem(
    recipe: Recipe,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp)) {
                // Remote Recipe Image
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Linear Dark Gradient overlay at bottom of image for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 180f
                            )
                        )
                )

                // Category Capsule
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = recipe.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Favorite Toggle Floating Heart Icon
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .testTag("recipe_fav_btn_${recipe.id}")
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (recipe.isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Overlaid prep time badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recipe.prepTime,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recipe.difficulty,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "by ${recipe.chefName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (recipe.totalCooked > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cooked ${recipe.totalCooked}x",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. MEAL PLANNER PANEL
// ==========================================
@Composable
fun MealPlanPanel(
    viewModel: CookingViewModel,
    recipesList: List<Recipe>
) {
    val mealPlans by viewModel.mealPlans.collectAsState()
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    var selectedDayToEdit by remember { mutableStateOf<String?>(null) }
    var selectedMealTypeToEdit by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Weekly Kitchen Planner",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Plan ahead, prep smart, and eat beautifully.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("meal_plan_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(days) { day ->
                val plan = mealPlans.find { it.dayOfWeek == day }
                DayPlanCard(
                    day = day,
                    plan = plan,
                    recipesList = recipesList,
                    onEditMeal = { mealType ->
                        selectedDayToEdit = day
                        selectedMealTypeToEdit = mealType
                    }
                )
            }
        }
    }

    // Modal Planner Entry Dialog
    if (selectedDayToEdit != null && selectedMealTypeToEdit != null) {
        MealSelectorDialog(
            day = selectedDayToEdit!!,
            mealType = selectedMealTypeToEdit!!,
            recipesList = recipesList,
            currentPlan = mealPlans.find { it.dayOfWeek == selectedDayToEdit!! },
            viewModel = viewModel,
            onDismiss = {
                selectedDayToEdit = null
                selectedMealTypeToEdit = null
            }
        )
    }
}

@Composable
fun DayPlanCard(
    day: String,
    plan: MealPlan?,
    recipesList: List<Recipe>,
    onEditMeal: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_card_$day"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (day) {
                        "Mon" -> "Monday"
                        "Tue" -> "Tuesday"
                        "Wed" -> "Wednesday"
                        "Thu" -> "Thursday"
                        "Fri" -> "Friday"
                        "Sat" -> "Saturday"
                        "Sun" -> "Sunday"
                        else -> day
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meals Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealSlotItem(
                    title = "Breakfast",
                    icon = Icons.Default.LightMode,
                    mealName = plan?.let {
                        if (it.breakfastRecipeId != null) {
                            recipesList.find { r -> r.id == it.breakfastRecipeId }?.title ?: "Buttermilk Pancakes"
                        } else it.breakfastCustomName
                    },
                    modifier = Modifier.weight(1f),
                    onEditClick = { onEditMeal("Breakfast") }
                )

                MealSlotItem(
                    title = "Lunch",
                    icon = Icons.Default.WbSunny,
                    mealName = plan?.let {
                        if (it.lunchRecipeId != null) {
                            recipesList.find { r -> r.id == it.lunchRecipeId }?.title ?: "Avocado Toast"
                        } else it.lunchCustomName
                    },
                    modifier = Modifier.weight(1f),
                    onEditClick = { onEditMeal("Lunch") }
                )

                MealSlotItem(
                     title = "Dinner",
                     icon = Icons.Default.DarkMode,
                     mealName = plan?.let {
                         if (it.dinnerRecipeId != null) {
                             recipesList.find { r -> r.id == it.dinnerRecipeId }?.title ?: "Tuscan Chicken"
                         } else it.dinnerCustomName
                     },
                     modifier = Modifier.weight(1f),
                     onEditClick = { onEditMeal("Dinner") }
                )
            }
        }
    }
}

@Composable
fun MealSlotItem(
    title: String,
    icon: ImageVector,
    mealName: String?,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onEditClick)
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = mealName ?: "Tap to Add",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (mealName != null) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (mealName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Dialog to select recipe or write custom meal description
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSelectorDialog(
    day: String,
    mealType: String,
    recipesList: List<Recipe>,
    currentPlan: MealPlan?,
    viewModel: CookingViewModel,
    onDismiss: () -> Unit
) {
    var isCustomEntry by remember { mutableStateOf(false) }
    var customMealName by remember { mutableStateOf("") }
    var selectedRecipeId by remember { mutableStateOf<Int?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                     text = "Schedule $mealType",
                     style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                     color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Plan for $day to organize ingredients and stay on track.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Selector Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isCustomEntry) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isCustomEntry = false }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Catalog",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (!isCustomEntry) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCustomEntry) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isCustomEntry = true }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Custom Description",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isCustomEntry) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isCustomEntry) {
                    OutlinedTextField(
                        value = customMealName,
                        onValueChange = { customMealName = it },
                        label = { Text("What are you preparing?") },
                        placeholder = { Text("e.g., Grilled salmon with rice") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .height(180.dp)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        items(recipesList) { recipe ->
                            val isSelected = selectedRecipeId == recipe.id
                            Text(
                                text = recipe.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { selectedRecipeId = recipe.id }
                                    .padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (isCustomEntry) {
                                viewModel.updateMealPlan(day, mealType, null, customMealName.ifBlank { null })
                            } else {
                                viewModel.updateMealPlan(day, mealType, selectedRecipeId, null)
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Plan")
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. INGREDIENTS PANTRY PANEL
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientsPanel(
    viewModel: CookingViewModel,
    onRecipeClick: (Recipe) -> Unit
) {
    val itemsList by viewModel.shoppingList.collectAsState()
    val recipesList by viewModel.recipes.collectAsState()
    val isGenerating by viewModel.isGeneratingRecipe.collectAsState()
    val generationError by viewModel.recipeGenerationError.collectAsState()

    var inputIngredient by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Quick toggles of popular ingredients
    val popularIngredients = listOf(
        "Chicken", "Tomato", "Pasta", "Rice", "Eggs", "Cheese", 
        "Spinach", "Garlic", "Onion", "Milk", "Flour", "Butter"
    )

    // Helper map of popular ingredients to emojis
    val ingredientEmojis = mapOf(
        "Chicken" to "🍗 Chicken",
        "Tomato" to "🍅 Tomato",
        "Pasta" to "🍝 Pasta",
        "Rice" to "🍚 Rice",
        "Eggs" to "🥚 Eggs",
        "Cheese" to "🧀 Cheese",
        "Spinach" to "🥬 Spinach",
        "Garlic" to "🧄 Garlic",
        "Onion" to "🧅 Onion",
        "Milk" to "🥛 Milk",
        "Flour" to "🌾 Flour",
        "Butter" to "🧈 Butter"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Dynamic AI Generation Loading State
        if (isGenerating) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chef AI is composing a masterpiece...",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choosing ratios, matching spices, and writing precise steps based on your pantry...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Section header
        Text(
            text = "My Ingredients Pantry",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Choose materials from standard chips or add custom ones to see matching culinary recipes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Selector Row / Grid (chips)
        Text(
            text = "Quick Pantry Add",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Render popular ingredients beautifully as selectable chips!
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            popularIngredients.forEach { ingredient ->
                val emojiText = ingredientEmojis[ingredient] ?: ingredient
                val isSelected = itemsList.any { it.name.equals(ingredient, ignoreCase = true) }

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val matchingItem = itemsList.find { it.name.equals(ingredient, ignoreCase = true) }
                        if (matchingItem != null) {
                            viewModel.deleteShoppingItem(matchingItem)
                        } else {
                            viewModel.addCustomShoppingItem(ingredient)
                        }
                    },
                    label = { Text(emojiText, style = MaterialTheme.typography.bodyMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text input to add custom ingredient
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputIngredient,
                    onValueChange = { inputIngredient = it },
                    placeholder = { Text("Add custom ingredient...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ingredient_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (inputIngredient.isNotBlank()) {
                            viewModel.addCustomShoppingItem(inputIngredient.trim())
                            inputIngredient = ""
                        }
                        focusManager.clearFocus()
                    })
                )

                IconButton(
                    onClick = {
                        if (inputIngredient.isNotBlank()) {
                            viewModel.addCustomShoppingItem(inputIngredient.trim())
                            inputIngredient = ""
                        }
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .shadow(2.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(44.dp)
                        .testTag("ingredient_add_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add ingredient",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display current selected pantry ingredients as a beautiful row of deletable chips
        if (itemsList.isNotEmpty()) {
            Text(
                text = "My Pantry (${itemsList.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsList.forEach { item ->
                    InputChip(
                        selected = true,
                        onClick = { viewModel.deleteShoppingItem(item) },
                        label = { Text(item.name) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Delete",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        } else {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Pantry is Empty",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Tap some quick ingredients above to formulate recipes!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- SECTION: AI GEN KEY & INTERACTION ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Michelin AI Kitchen Studio",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Generate a completely custom culinary guide using exactly the ingredients present in your pantry. Our chef AI writes difficulty, prep times, and step-by-step methods tailored to you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val pantryIngs = itemsList.map { it.name }
                        viewModel.generateRecipeFromPantry(pantryIngs) { recipe ->
                            onRecipeClick(recipe)
                        }
                    },
                    enabled = itemsList.isNotEmpty() && !isGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ai_generate_recipe_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Custom Recipe")
                }

                generationError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- SECTION: RECIPES MATCHES ---
        val pantryNames = itemsList.map { it.name.lowercase().trim() }
        val matchedRecipes = recipesList.map { recipe ->
            val recipeIngText = recipe.ingredientsString.lowercase()
            val matches = pantryNames.filter { ingredient -> recipeIngText.contains(ingredient) }
            recipe to matches
        }.filter { it.second.isNotEmpty() }
        .sortedByDescending { it.second.size }

        if (matchedRecipes.isNotEmpty()) {
            Text(
                text = "Matched Recipes (${matchedRecipes.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            matchedRecipes.forEach { (recipe, matches) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onRecipeClick(recipe) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = recipe.imageUrl,
                            contentDescription = recipe.title,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "By ${recipe.chefName} • ${recipe.prepTime} • ${recipe.difficulty}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Display the match badge list
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "Uses: ${matches.joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else if (itemsList.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SentimentDissatisfied, null, tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No direct database recipes match your selected items.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Use the 'Generate Custom Recipe' button above to let AI write a brand new recipe using these ingredients!",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. PROFILE TAB PANEL
// ==========================================
@Composable
fun ProfilePanel(
    viewModel: CookingViewModel,
    recipesList: List<Recipe>
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val shoppingList by viewModel.shoppingList.collectAsState()

    // Aggregate statistics
    val totalTimesCooked = recipesList.sumOf { it.totalCooked }
    val uniqueDishesCooked = recipesList.count { it.totalCooked > 0 }
    val favCount = recipesList.count { it.isFavorite }
    val activeListCount = shoppingList.count { !it.isBought }

    // Gamified chef rank calculation
    val chefRankName = when {
        totalTimesCooked >= 50 -> "Master Gastronomist 👑"
        totalTimesCooked >= 30 -> "Executive Chef 🍳"
        totalTimesCooked >= 15 -> "Sous Chef 🔪"
        totalTimesCooked >= 5 -> "Home Baker 🌾"
        else -> "Apprentice Gourmet 🌱"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("profile_user_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                 modifier = Modifier.padding(24.dp),
                 horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    if (activeSession?.photoUrl != null) {
                        AsyncImage(
                            model = activeSession?.photoUrl,
                            contentDescription = "Chef Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                     text = activeSession?.displayName ?: "AI Studio Chef",
                     style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                     color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                     text = activeSession?.email ?: "aitest9102025@gmail.com",
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Provider Capsule Capsule
                val provider = activeSession?.provider ?: "Google"
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            when (provider) {
                                "Google" -> Color(0xFFE2F0D9)
                                "Facebook" -> Color(0xFFE8F0FE)
                                "Apple" -> Color(0xFFECEFF1)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when (provider) {
                                "Google" -> Icons.Default.AccountCircle
                                "Facebook" -> Icons.Default.ThumbUp
                                "Apple" -> Icons.Default.Star
                                else -> Icons.Default.Email
                            },
                            contentDescription = null,
                            tint = when (provider) {
                                "Google" -> Color(0xFF388E3C)
                                "Facebook" -> Color(0xFF1565C0)
                                "Apple" -> Color.Black
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Connected via $provider",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (provider) {
                                "Google" -> Color(0xFF1B5E20)
                                "Facebook" -> Color(0xFF0D47A1)
                                "Apple" -> Color.Black
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
        }

        // Stats grid
        Text(
             text = "Culinary Metrics",
             style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
             color = MaterialTheme.colorScheme.onSurface,
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(vertical = 8.dp, horizontal = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCompactCard(
                label = "Total Cooks",
                stat = totalTimesCooked.toString(),
                icon = Icons.Default.SoupKitchen,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatsCompactCard(
                label = "Dishes Cooked",
                stat = uniqueDishesCooked.toString(),
                icon = Icons.Default.MenuBook,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCompactCard(
                label = "Favorites",
                stat = favCount.toString(),
                icon = Icons.Default.Favorite,
                color = CrimsonRose,
                modifier = Modifier.weight(1f)
            )
            StatsCompactCard(
                label = "Pantry Items",
                stat = activeListCount.toString(),
                icon = Icons.Default.LocalMall,
                color = SageGreenTertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chef badge trophy row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Culinary Ranking",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = chefRankName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatsCompactCard(
    label: String,
    stat: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stat,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ==========================================
// 5. RECIPE DETAIL FULL SHEET MODAL DIALOG
// ==========================================
@Composable
fun RecipeDetailDialog(
    recipe: Recipe,
    viewModel: CookingViewModel,
    onDismiss: () -> Unit
) {
    val activeTimerRecipeId by viewModel.timerRecipeId.collectAsState()
    val activeTimerRemaining by viewModel.timerRemaining.collectAsState()
    val isTimerRunning by viewModel.timerIsRunning.collectAsState()

    var activeCheckedIngredients by remember { mutableStateOf(setOf<String>()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
             shape = RoundedCornerShape(24.dp),
             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
             modifier = Modifier
                 .fillMaxWidth()
                 .fillMaxHeight(0.9f)
                 .testTag("recipe_detail_layout")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Image block with back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                    startY = 200f
                                )
                            )
                    )

                    // Close circular button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            .testTag("detail_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close details", tint = Color.Black)
                    }

                    // Floating details overview overlaid at bottom
                    Text(
                        text = recipe.category.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "by ${recipe.chefName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Cook it button!
                        Button(
                            onClick = { viewModel.completeCook(recipe.id, recipe.title) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            modifier = Modifier.testTag("recipe_cooked_btn_${recipe.id}")
                        ) {
                            Icon(Icons.Default.SoupKitchen, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cooked It", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RecipeParameterBadge(icon = Icons.Default.AccessTime, label = "Prep Time", value = recipe.prepTime)
                        RecipeParameterBadge(icon = Icons.Default.Restaurant, label = "Difficulty", value = recipe.difficulty)
                        RecipeParameterBadge(icon = Icons.Default.EmojiEvents, label = "Cooked", value = "${recipe.totalCooked} times")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Ingredients Sub-section
                    Text(
                        text = "Ingredients Needed",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Check items you already have, or tap shopping icon to compile shopping list.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    recipe.getIngredients().forEach { ingredient ->
                        val isChecked = ingredient in activeCheckedIngredients
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent)
                                .clickable {
                                    activeCheckedIngredients = if (isChecked) {
                                        activeCheckedIngredients - ingredient
                                    } else {
                                        activeCheckedIngredients + ingredient
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (isChecked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Single button to add this ingredient to our shopping list pantry!
                            IconButton(
                                onClick = { viewModel.addIngredientToShopping(ingredient) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddShoppingCart,
                                    contentDescription = "Add to shopping list",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Instructions Sub-section
                    Text(
                        text = "Step-By-Step Guide",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    recipe.getInstructions().forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Active step timer trigger
                    val estimatedPrepTimeMin = recipe.prepTime.replace(" mins", "").toIntOrNull() ?: 20
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Interactive Kitchen Timer",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Start a 1-minute test timer or coordinate with baking time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.startTimer(recipe.id, 60) }, // 1 min quick test
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Start 1 Min Test")
                                }

                                Button(
                                    onClick = { viewModel.startTimer(recipe.id, estimatedPrepTimeMin * 60) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Start ($estimatedPrepTimeMin Min)")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ==========================================
                    // COMMUNITY SOCIAL HUB (LIKES & COMMENTS)
                    // ==========================================
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Chef Community Buzz",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    val comments by viewModel.getCommentsForRecipe(recipe.id).collectAsState(initial = emptyList())
                    val likes by viewModel.getLikesForRecipe(recipe.id).collectAsState(initial = emptyList())
                    val activeSession by viewModel.activeSession.collectAsState()
                    val userEmail = activeSession?.email ?: ""
                    val hasLiked by viewModel.hasUserLikedRecipe(recipe.id, userEmail).collectAsState(initial = false)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "❤️ ${likes.size} Loves  •  💬 ${comments.size} Reviews",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        IconButton(
                            onClick = { viewModel.toggleLike(recipe.id) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .testTag("like_recipe_btn")
                        ) {
                            Icon(
                                imageVector = if (hasLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Toggle Like",
                                tint = if (hasLiked) CrimsonRose else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Comments display list
                    if (comments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SoupKitchen, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                                Text(
                                    text = "No reviews yet. Be the first to try!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            comments.take(15).forEach { comment ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (comment.authorName.isNotEmpty()) comment.authorName.take(1).uppercase() else "C",
                                                color = Color.White,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = comment.authorName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Chef",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = comment.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    var commentText by remember { mutableStateOf("") }
                    Row(
                         modifier = Modifier.fillMaxWidth(),
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                         OutlinedTextField(
                             value = commentText,
                             onValueChange = { commentText = it },
                             placeholder = { Text("Write a kitchen review...", style = MaterialTheme.typography.bodyMedium) },
                             singleLine = true,
                             modifier = Modifier
                                 .weight(1f)
                                 .testTag("comment_input"),
                             shape = RoundedCornerShape(12.dp),
                             textStyle = MaterialTheme.typography.bodyMedium
                         )

                         IconButton(
                             onClick = {
                                 if (commentText.isNotBlank()) {
                                     viewModel.addComment(recipe.id, commentText)
                                     commentText = ""
                                 }
                             },
                             modifier = Modifier
                                 .clip(CircleShape)
                                 .background(MaterialTheme.colorScheme.primary)
                                 .testTag("submit_comment_btn")
                         ) {
                             Icon(Icons.Default.Send, contentDescription = "Add review", tint = Color.White)
                         }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun RecipeParameterBadge(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
             text = value,
             style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
             color = MaterialTheme.colorScheme.onSurface
        )
        Text(
             text = label,
             style = MaterialTheme.typography.labelSmall,
             color = MaterialTheme.colorScheme.outline
        )
    }
}

// ==========================================
// 6. FLOATING ACTIVE KITCHEN TIMER WIDGET
// ==========================================
@Composable
fun TimerWidgetOverlay(
    viewModel: CookingViewModel,
    recipesList: List<Recipe>,
    onWidgetClick: (Int) -> Unit
) {
    val timerRecipeId by viewModel.timerRecipeId.collectAsState()
    val remaining by viewModel.timerRemaining.collectAsState()
    val total by viewModel.timerTotal.collectAsState()
    val isRunning by viewModel.timerIsRunning.collectAsState()

    AnimatedVisibility(
        visible = timerRecipeId != null && remaining != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val activeRecipe = recipesList.find { it.id == timerRecipeId } ?: return@AnimatedVisibility
        val progress = if (total > 0) (remaining!!.toFloat() / total.toFloat()) else 1.0f

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onWidgetClick(activeRecipe.id) }
                .testTag("floating_timer_bar"),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessAlarm,
                            contentDescription = "Active Timer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp)
                        )
                        Column {
                            Text(
                                text = "Timing: ${activeRecipe.title}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val sec = remaining!! % 60
                            val min = remaining!! / 60
                            Text(
                                text = String.format("%02d:%02d remaining", min, sec),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { viewModel.pauseResumeTimer() }) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause timer" else "Resume timer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { viewModel.stopTimer() }) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop timer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// GYM WORKOUT-STYLE TIMED STEPS "PLAY RECIPE" PANEL
// ==========================================================
@Composable
fun PlayRecipePanel(
    viewModel: CookingViewModel,
    onSelectRecipeClick: () -> Unit
) {
    val playingRecipe by viewModel.playingRecipe.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val stepTimeRemaining by viewModel.stepTimeRemaining.collectAsState()
    val stepTimeTotal by viewModel.stepTimeTotal.collectAsState()
    val isStepTimerRunning by viewModel.isStepTimerRunning.collectAsState()
    val bgSoundType by viewModel.currentBgSoundType.collectAsState()
    val bgSoundName by viewModel.currentBgSoundName.collectAsState()
    val recipesList by viewModel.recipes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (playingRecipe == null) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("workout_idle_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Play Recipe Kitchen Studio 🧑‍🍳",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Time every single cooking step like a pro-athlete's gym workout. Setup countdowns for chopping, simmering, or washing with soothing ambient sounds.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Launch Active Cooking Studio Runway",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (recipesList.isEmpty()) {
                Text("No recipes available.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recipesList.forEach { recipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectAndPlayRecipe(recipe) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = recipe.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = recipe.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "by ${recipe.chefName} • ${recipe.getInstructions().size} Timed Steps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.selectAndPlayRecipe(recipe) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp),
                                    modifier = Modifier.testTag("recipe_play_btn_${recipe.id}")
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PLAY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val recipe = playingRecipe!!
            val instructions = recipe.getInstructions()
            val totalSteps = instructions.size
            val currentInstruction = instructions.getOrNull(currentStepIndex) ?: "Ready to serve."
            val nextInstruction = instructions.getOrNull(currentStepIndex + 1)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STUDIO LIVE RUNWAY",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { viewModel.stopPlayingRecipe() },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .size(36.dp)
                        .testTag("stop_play_recipe_btn")
                ) {
                    Icon(Icons.Default.Close, "Quit Studio")
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("workout_active_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val stepFraction = if (totalSteps > 0) (currentStepIndex + 1).toFloat() / totalSteps.toFloat() else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STEP ${currentStepIndex + 1} OF $totalSteps",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${(stepFraction * 100).toInt()}% Complete",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { stepFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .testTag("step_animation_ring"),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "cook_animation")
                        val bounceOffset by infiniteTransition.animateFloat(
                            initialValue = -12f,
                            targetValue = 12f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1100, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "steam_rise"
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            drawCircle(
                                color = SageGreenTertiary.copy(alpha = 0.3f),
                                radius = 45f + bounceOffset,
                                center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                            )
                        }

                        Icon(
                            imageVector = when {
                                currentInstruction.contains("chop", ignoreCase = true) || currentInstruction.contains("cut", ignoreCase = true) || currentInstruction.contains("slice", ignoreCase = true) -> Icons.Default.Kitchen
                                currentInstruction.contains("heat", ignoreCase = true) || currentInstruction.contains("boil", ignoreCase = true) || currentInstruction.contains("simmer", ignoreCase = true) -> Icons.Default.HotTub
                                currentInstruction.contains("mix", ignoreCase = true) || currentInstruction.contains("stir", ignoreCase = true) || currentInstruction.contains("blend", ignoreCase = true) -> Icons.Default.SoupKitchen
                                currentInstruction.contains("wash", ignoreCase = true) || currentInstruction.contains("rinse", ignoreCase = true) -> Icons.Default.WaterDrop
                                else -> Icons.Default.SoupKitchen
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = currentInstruction,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    val progressFraction = if (stepTimeTotal > 0) stepTimeRemaining.toFloat() / stepTimeTotal.toFloat() else 0f
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(96.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 6.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stepTimeRemaining}s",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isStepTimerRunning) "RUNNING" else "PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.prevStep() },
                            enabled = currentStepIndex > 0,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("step_prev_btn")
                        ) {
                            Icon(Icons.Default.ArrowBack, "Prev Step")
                        }

                        Button(
                            onClick = { viewModel.toggleStepTimer() },
                            shape = CircleShape,
                            modifier = Modifier.size(68.dp).testTag("step_play_pause_btn"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isStepTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause Timer",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextStep() },
                            enabled = currentStepIndex < totalSteps - 1,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("step_next_btn")
                        ) {
                            Icon(Icons.Default.ArrowForward, "Next Step")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (nextInstruction != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "COMING UP NEXT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = nextInstruction,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth().testTag("soundtrack_box"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kitchen Ambient Audio Tracks 🎧",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (bgSoundType != "None") {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(12.dp)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }

                    if (bgSoundType != "None") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "STREAMING ACTIVE: $bgSoundName 📻",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "1. DEFAULT COOKING MELODIES",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Bossa Nova" to "Acoustic Bossa Nova Beats 🎸",
                            "Jazz Lounge" to "Cozy Jazz Sauté Room 🎹",
                            "Lofi Simmer" to "Acoustic Lofi Beats 🎧"
                        ).forEach { (short, label) ->
                            val isSel = bgSoundName == label
                            SuggestionChip(
                                onClick = { viewModel.setBackgroundSound("Music", label) },
                                label = { Text(short) },
                                border = if (isSel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.testTag("sound_${short.replace(" ", "_")}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "2. CHOOSE LOCAL SOUND FILE Presets",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Sizzle.wav" to "Pan Sizzles (Local Stream) 🍳",
                            "Timer.mp3" to "Retro Timer Buzzer (Local) 🔔",
                            "Boiling.wav" to "Stew Boils (Local Studio) 🍲"
                        ).forEach { (short, label) ->
                            val isSel = bgSoundName == label
                            SuggestionChip(
                                onClick = { viewModel.setBackgroundSound("Local", label) },
                                label = { Text(short) },
                                border = if (isSel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.testTag("sound_${short.replace(".", "_")}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "3. CONNECT TO RADIO STATIONS (Gourmet)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Chef FM" to "Tuned in to Chef FM (94.5 MHz) 📻",
                            "Baking FM" to "Baking Radio Lounge (101.3 MHz) 📻",
                            "Garden FM" to "Fresh Garden Hits (88.1 MHz) 📻"
                        ).forEach { (short, label) ->
                            val isSel = bgSoundName == label
                            SuggestionChip(
                                onClick = { viewModel.setBackgroundSound("Radio", label) },
                                label = { Text(short) },
                                border = if (isSel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.testTag("sound_${short.replace(" ", "_")}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { viewModel.setBackgroundSound("None", "") },
                        modifier = Modifier.align(Alignment.End).testTag("sound_mute_btn")
                    ) {
                        Icon(Icons.Default.VolumeOff, "Mute")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dampen Background Sound", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ==========================================
// RECIPE UPLOADER COMPOSABLE DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadRecipeDialog(
    viewModel: CookingViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("25 mins") }
    var difficulty by remember { mutableStateOf("Easy") }
    var category by remember { mutableStateOf("Lunch") }
    var imageUrl by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("upload_recipe_layout")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Share Your Recipe",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Publish your culinary masterpiece dynamically inside the Cooking Companion server database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recipe Title") },
                    placeholder = { Text("e.g. Grandma's Lemon Pie") },
                    modifier = Modifier.fillMaxWidth().testTag("add_title_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        label = { Text("Prep Time") },
                        modifier = Modifier.weight(1f).testTag("add_preptime_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        placeholder = { Text("Breakfast/Lunch/Dinner") },
                        modifier = Modifier.weight(1f).testTag("add_category_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    label = { Text("Difficulty (Easy/Medium/Hard)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_difficulty_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL (leave blank for preset)") },
                    placeholder = { Text("Unsplash image link") },
                    modifier = Modifier.fillMaxWidth().testTag("add_image_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Ingredients (Enter one per line)") },
                    placeholder = { Text("2 cups Rice\n1 tbsp Soy Sauce\n2 Eggs") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("add_ingredients_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions (Enter one per line)") },
                    placeholder = { Text("Wash vegetables.\nSimmer broth for 15 minutes.\nPlate and serve warm.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("add_instructions_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("add_cancel_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && ingredients.isNotBlank() && instructions.isNotBlank()) {
                                val ingString = ingredients.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("||")
                                val insString = instructions.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("||")
                                viewModel.uploadRecipe(
                                    title = title,
                                    prepTime = prepTime,
                                    difficulty = difficulty,
                                    category = category,
                                    imageUrl = imageUrl,
                                    ingredients = ingString,
                                    instructions = insString
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.5f).testTag("add_publish_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Publish Recipe")
                    }
                }
            }
        }
    }
}
