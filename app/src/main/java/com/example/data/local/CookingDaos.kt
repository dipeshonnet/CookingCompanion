package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_sessions WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<UserSession?>

    @Query("SELECT * FROM user_sessions WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveSession(): UserSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSession)

    @Query("UPDATE user_sessions SET isLoggedIn = 0")
    suspend fun clearActiveSessions()
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY id ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1")
    fun getFavoriteRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeByIdFlow(id: Int): Flow<Recipe?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("UPDATE recipes SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Int, isFav: Boolean)

    @Query("UPDATE recipes SET totalCooked = totalCooked + 1 WHERE id = :id")
    suspend fun incrementCooked(id: Int)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY dateAdded DESC")
    fun getShoppingList(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE isBought = 1")
    suspend fun clearBoughtItems()
}

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans")
    fun getMealPlansFlow(): Flow<List<MealPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(plan: MealPlan)

    @Query("DELETE FROM meal_plans")
    suspend fun clearMealPlans()
}

@Dao
interface RecipeCommentDao {
    @Query("SELECT * FROM recipe_comments WHERE recipeId = :recipeId ORDER BY timestamp DESC")
    fun getCommentsForRecipeFlow(recipeId: Int): Flow<List<RecipeComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: RecipeComment)

    @Query("DELETE FROM recipe_comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Int)
}

@Dao
interface RecipeLikeDao {
    @Query("SELECT * FROM recipe_likes WHERE recipeId = :recipeId")
    fun getLikesForRecipeFlow(recipeId: Int): Flow<List<RecipeLike>>

    @Query("SELECT EXISTS(SELECT 1 FROM recipe_likes WHERE recipeId = :recipeId AND userEmail = :email)")
    fun hasUserLikedRecipeFlow(recipeId: Int, email: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: RecipeLike)

    @Query("DELETE FROM recipe_likes WHERE recipeId = :recipeId AND userEmail = :email")
    suspend fun removeLike(recipeId: Int, email: String)
}
