package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserSession::class, 
        Recipe::class, 
        ShoppingItem::class, 
        MealPlan::class,
        RecipeComment::class,
        RecipeLike::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CookingDatabase : RoomDatabase() {

    abstract fun userSessionDao(): UserSessionDao
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun recipeCommentDao(): RecipeCommentDao
    abstract fun recipeLikeDao(): RecipeLikeDao

    companion object {
        @Volatile
        private var INSTANCE: CookingDatabase? = null

        fun getDatabase(context: Context): CookingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CookingDatabase::class.java,
                    "cooking_companion_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
