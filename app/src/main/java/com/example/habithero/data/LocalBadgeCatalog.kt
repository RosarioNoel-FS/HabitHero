package com.example.habithero.data

import com.example.habithero.model.Badge

object LocalBadgeCatalog {

    private val allBadges = listOf(
        // Streaks
        Badge(
            id = "streak_1",
            name = "Streak 1 Day",
            description = "Achieve a 1-day streak.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fstreak1.png?alt=media&token=c8856b53-4bc9-40d0-ad27-b8bade482833",
            categoryKey = "Streaks",
            sortOrder = 1
        ),
        Badge(
            id = "streak_3",
            name = "Streak 3 Days",
            description = "Achieve a 3-day streak.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fstreak3.png?alt=media&token=fd21b0bc-678e-4e9d-ba22-8bd2f65b0e8d",
            categoryKey = "Streaks",
            sortOrder = 2
        ),
        Badge(
            id = "streak_7",
            name = "Streak 7 Days",
            description = "Achieve a 7-day streak.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fstreak7.png?alt=media&token=f22bd65c-3f3f-42dd-a65e-13343b5cb83f",
            categoryKey = "Streaks",
            sortOrder = 3
        ),
        Badge(
            id = "streak_14",
            name = "Streak 14 Days",
            description = "Achieve a 14-day streak.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fstreak14.png?alt=media&token=b4365478-c91e-44da-81f9-1aaa4ae40377",
            categoryKey = "Streaks",
            sortOrder = 4
        ),
        Badge(
            id = "streak_30",
            name = "Streak 30 Days",
            description = "Achieve a 30-day streak.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fstreak30.png?alt=media&token=d793e210-f819-4fb2-a174-a1274404f715",
            categoryKey = "Streaks",
            sortOrder = 5
        ),
        Badge(
            id = "streak_100",
            name = "Streak 100 Days",
            description = "Achieve a 100-day streak.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fstreak100.png?alt=media&token=e0d92902-ebd3-43bf-976d-ca0eae09ceda",
            categoryKey = "Streaks",
            sortOrder = 6
        ),
        Badge(
            id = "streak_365",
            name = "Streak 365 Days",
            description = "Achieve a 365-day streak.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fstreak365.png?alt=media&token=191e6cc6-5309-4a07-b8f9-fd8cca909eaa",
            categoryKey = "Streaks",
            sortOrder = 7
        ),

        // Active Habits
        Badge(
            id = "active_1",
            name = "1 Active Habit",
            description = "Have 1 active habit.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2F1_active_habit.png?alt=media&token=41980f3b-000c-475a-827d-52377de77cd8",
            categoryKey = "Active Habits",
            sortOrder = 1
        ),
        Badge(
            id = "active_3",
            name = "3 Active Habits",
            description = "Have 3 active habits.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2F3_active_habit.png?alt=media&token=3ef6326e-a877-4a35-bbad-c457ef5d277d",
            categoryKey = "Active Habits",
            sortOrder = 2
        ),
        Badge(
            id = "active_5",
            name = "5 Active Habits",
            description = "Have 5 active habits.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2F5_active_habit.png?alt=media&token=14aea63c-788f-48d2-bdb3-3e9a6ca460a8",
            categoryKey = "Active Habits",
            sortOrder = 3
        ),
        Badge(
            id = "active_7",
            name = "7 Active Habits",
            description = "Have 7 active habits.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2F7_active_habit.png?alt=media&token=dcb0dbf1-ed5e-4837-8e03-de72dfecfca0",
            categoryKey = "Active Habits",
            sortOrder = 4
        ),
        Badge(
            id = "active_10",
            name = "10 Active Habits",
            description = "Have 10 active habits.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2F10_active_habit.png?alt=media&token=94911942-9c1d-479f-ad93-abe2467a9a62",
            categoryKey = "Active Habits",
            sortOrder = 5
        ),

        // Completed Habits
        Badge(
            id = "complete_1",
            name = "Complete 1 Habit",
            description = "Complete a habit 1 time.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fcomplete1_habit.png?alt=media&token=1f91b14d-d21a-45bd-9a31-d25d88d7c706",
            categoryKey = "Completed Habits",
            sortOrder = 1
        ),
        Badge(
            id = "complete_10",
            name = "Complete 10 Habits",
            description = "Complete habits 10 times.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fcomplete10_habit.png?alt=media&token=3147689a-ee4d-4fd7-aceb-5831a75ea020",
            categoryKey = "Completed Habits",
            sortOrder = 2
        ),
        Badge(
            id = "complete_25",
            name = "Complete 25 Habits",
            description = "Complete habits 25 times.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fcomplete25_habit.png?alt=media&token=53b68ce1-4298-4de5-9b90-3408c520a588",
            categoryKey = "Completed Habits",
            sortOrder = 3
        ),
        Badge(
            id = "complete_50",
            name = "Complete 50 Habits",
            description = "Complete habits 50 times.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fcomplete50_habit.png?alt=media&token=e42ccfa7-e108-4091-92a3-78278399f6e3",
            categoryKey = "Completed Habits",
            sortOrder = 4
        ),
        Badge(
            id = "complete_100",
            name = "Complete 100 Habits",
            description = "Complete habits 100 times.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fcomplete100_habit.png?alt=media&token=95d5a43b-718e-444a-aa32-e2385e8e6da5",
            categoryKey = "Completed Habits",
            sortOrder = 5
        ),
        Badge(
            id = "complete_500",
            name = "Complete 500 Habits",
            description = "Complete habits 500 times.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fcomplete500_habit.png?alt=media&token=3694233e-9680-4bd3-809a-a54ebf0c4749",
            categoryKey = "Completed Habits",
            sortOrder = 6
        ),
        Badge(
            id = "complete_1000",
            name = "Complete 1000 Habits",
            description = "Complete habits 1000 times.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/badges%2Fcomplete1000_habit.png?alt=media&token=c7c15d8d-7c3e-4ad8-a05f-f6ea608d6dd0",
            categoryKey = "Completed Habits",
            sortOrder = 7
        )
    )

    fun getAllBadges(): List<Badge> = allBadges
}
