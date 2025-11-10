package com.example.habithero

object ChallengeData {
    val challenges = listOf(
        Challenge(
            id = "morning_warrior",
            title = "Morning Warrior",
            description = "Transform your mornings and set the tone for incredible days. Start your journey as a morning person!",
            durationDays = 21,
            icon = "ic_morning_sun", // Placeholder icon name
            about = "Transform your mornings and set the tone for incredible days. Start your journey as a morning person!",
            whyItMatters = "Research shows that morning routines are linked to higher productivity, better mental health, and increased life satisfaction. By mastering your mornings, you master your day.",
            positiveEffects = listOf(
                "Increased energy and alertness throughout the day",
                "Better mental clarity and focus",
                "Improved mood and reduced stress"
            ),
            habits = listOf(
                HabitTemplate(
                    id = "meditate",
                    name = "10 Minutes Meditation",
                    category = "Mindfulness & Wellbeing",
                    emoji = "🧘",
                    completionHour = 6,
                    completionMinute = 30,
                    iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"
                ),
                HabitTemplate(
                    id = "stretch",
                    name = "Morning Stretching",
                    category = "Health & Fitness",
                    emoji = "🤸",
                    completionHour = 7,
                    completionMinute = 0,
                    iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"
                ),
                HabitTemplate(
                    id = "drink_water",
                    name = "Drink 8 Glasses of Water",
                    category = "Health & Fitness",
                    emoji = "💧",
                    completionHour = 7,
                    completionMinute = 15,
                    iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"
                ),
                HabitTemplate(
                    id = "make_bed",
                    name = "Make Your Bed",
                    category = "Daily Living & Organization",
                    emoji = "🛏️",
                    completionHour = 7,
                    completionMinute = 30,
                    iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"
                ),
                HabitTemplate(
                    id = "write_gratitude",
                    name = "Write 3 Gratitudes",
                    category = "Mindfulness & Wellbeing",
                    emoji = "✍️",
                    completionHour = 8,
                    completionMinute = 0,
                    iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"
                )
            )
        ),
        Challenge(
            id = "productivity_master",
            title = "Productivity Master",
            description = "Unlock your full potential and achieve more in less time. Become a productivity powerhouse!",
            durationDays = 30,
            icon = "ic_productivity_bolt", // Placeholder icon name
            about = "This challenge is designed to eliminate distractions, prioritize high-impact tasks, and create a focused work environment to help you achieve your professional and personal goals faster.",
            whyItMatters = "In a world full of distractions, the ability to focus is a superpower. This challenge builds the discipline needed to dedicate time to what truly matters, leading to higher quality work and more free time.",
            positiveEffects = listOf(
                "Significantly improved focus and concentration",
                "Ability to complete tasks more efficiently",
                "Reduced procrastination and decision fatigue",
                "Greater sense of accomplishment and control"
            ),
            habits = listOf(
                HabitTemplate(id = "plan_day", name = "Plan Your Day", category = "Productivity", emoji = "📅", completionHour = 8, completionMinute = 0, iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"),
                HabitTemplate(id = "deep_work", name = "2 Hours Deep Work", category = "Productivity", emoji = "💻", completionHour = 9, completionMinute = 30, iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"),
                HabitTemplate(id = "no_social", name = "No Social Media for 90 Mins", category = "Digital Wellbeing", emoji = "📵", completionHour = 10, completionMinute = 0, iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"),
                HabitTemplate(id = "review_progress", name = "Review Daily Progress", category = "Productivity", emoji = "📊", completionHour = 17, completionMinute = 0, iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0"),
                HabitTemplate(id = "tidy_workspace", name = "Tidy Workspace for 5 Mins", category = "Organization", emoji = "🧹", completionHour = 17, completionMinute = 30, iconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fchallenge.png?alt=media&token=062c1313-7ef7-4c85-bd53-3a9ab5ba8cb0")
            )
        )
        // Add other challenges here...
    )
}
