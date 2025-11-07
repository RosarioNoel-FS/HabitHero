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
                    name = "10 Minutes Meditation",
                    category = "Mindfulness & Wellbeing",
                    emoji = "🧘",
                    completionHour = 6,
                    completionMinute = 30,
                    iconUrl = "https://example.com/meditation.png" // Placeholder
                ),
                HabitTemplate(
                    name = "Morning Stretching",
                    category = "Health & Fitness",
                    emoji = "🤸",
                    completionHour = 7,
                    completionMinute = 0,
                    iconUrl = "https://example.com/stretching.png" // Placeholder
                ),
                HabitTemplate(
                    name = "Drink 8 Glasses of Water",
                    category = "Health & Fitness",
                    emoji = "💧",
                    completionHour = 7,
                    completionMinute = 15,
                    iconUrl = "https://example.com/water.png" // Placeholder
                ),
                HabitTemplate(
                    name = "Make Your Bed",
                    category = "Daily Living & Organization",
                    emoji = "🛏️",
                    completionHour = 7,
                    completionMinute = 30,
                    iconUrl = "https://example.com/bed.png" // Placeholder
                ),
                HabitTemplate(
                    name = "Write 3 Gratitudes",
                    category = "Mindfulness & Wellbeing",
                    emoji = "✍️",
                    completionHour = 8,
                    completionMinute = 0,
                    iconUrl = "https://example.com/gratitude.png" // Placeholder
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
                HabitTemplate("Plan Your Day", "Productivity", "📅", 8, 0, ""),
                HabitTemplate("2 Hours Deep Work", "Productivity", "💻", 9, 30, ""),
                HabitTemplate("No Social Media for 90 Mins", "Digital Wellbeing", "📵", 10, 0, ""),
                HabitTemplate("Review Daily Progress", "Productivity", "📊", 17, 0, ""),
                HabitTemplate("Tidy Workspace for 5 Mins", "Organization", "🧹", 17, 30, "")
            )
        )
        // Add other challenges here...
    )
}
