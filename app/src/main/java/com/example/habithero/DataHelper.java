package com.example.habithero;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class DataHelper {

        public static Map<String, List<String>> getHabitsByCategory() {
            Map<String, List<String>> habitsByCategory = new HashMap<>();

            habitsByCategory.put("Health & Fitness", Arrays.asList(
                    "Go for a 30-minute walk", "Drink 8 glasses of water daily", "Follow a morning workout routine", "Try a new healthy recipe",
                    "Practice mindfulness meditation", "Do 10 minutes of stretching", "Get at least 7 hours of sleep", "Eat a balanced breakfast",
                    "Take the stairs instead of the elevator", "Track your meals in a food journal", "Cut down on processed foods",
                    "Incorporate a new type of exercise", "Take a yoga class", "Set a step goal for the day", "Try a new form of cardio",
                    "Practice portion control during meals"
            ));

            habitsByCategory.put("Mindfulness & Well-being", Arrays.asList(
                    "Practice gratitude journaling", "Meditate for 15 minutes", "Spend time in nature", "Perform a random act of kindness",
                    "Listen to calming music or sounds", "Disconnect from screens for an hour", "Practice progressive muscle relaxation",
                    "Write down three things you love about yourself", "Explore a new hobby or creative activity", "Take a technology-free day",
                    "Practice positive affirmations", "Try a new relaxation technique", "Attend a mindfulness workshop or class",
                    "Spend quality time with loved ones", "Engage in a self-care activity", "Try a guided visualization exercise"
            ));

            habitsByCategory.put("Learning & Growth", Arrays.asList(
                    "Read a chapter from a book", "Listen to a short educational podcast", "Watch a TED Talk on a new topic",
                    "Learn a new word or phrase in a foreign language", "Read an insightful article", "Solve a puzzle or brain teaser",
                    "Watch a documentary on a subject of interest", "Reflect on something new you've learned",
                    "Explore a new website or online resource", "Follow an educational YouTube channel", "Read a thought-provoking quote",
                    "Discuss a current event with someone", "Take a mini-lesson on a skill you want to improve",
                    "Summarize a video or article you've consumed", "Share something interesting you've learned with a friend",
                    "Set a learning goal for the day and pursue it"
            ));

            habitsByCategory.put("Creativity & Expression", Arrays.asList(
                    "Write in a journal for creative ideas", "Sketch or doodle for 15 minutes", "Work on a creative writing project",
                    "Try your hand at photography", "Explore a new art medium or craft", "Create a vision board for your goals",
                    "Experiment with a new recipe or cooking technique", "Write a short poem or haiku", "Design your own digital artwork",
                    "Practice a musical instrument for 30 minutes", "Attend a local art exhibit or gallery", "Take a dance or movement class",
                    "Try a DIY home decor project", "Capture the beauty of your surroundings in a photo",
                    "Write a letter to your future self", "Share your creative work on social media"
            ));

            habitsByCategory.put("Adventure & Exploration", Arrays.asList(
                    "Go for a nature walk", "Try a new sport", "Explore your city", "Learn a dance move", "Hike a new trail", "Engage in geocaching",
                    "Attend a fitness class", "Ride a bike", "Try a new water sport", "Visit a cultural event", "Engage in artistic activities",
                    "Climb a hill or stairs", "Play a new game", "Photograph your adventures", "Visit a nearby attraction", "Practice yoga outdoors"
            ));

            return habitsByCategory;
        }


//        public static int getCategoryIcon(String category) {
//            Map<String, Integer> iconMap = new HashMap<>();
//
//            iconMap.put("Health & Fitness", R.drawable.health);
//            iconMap.put("Mindfulness & Well-being", R.drawable.mindfulness);
//            iconMap.put("Learning & Growth", R.drawable.learning);
//            iconMap.put("Creativity & Expression", R.drawable.creativity);
//            iconMap.put("Adventure & Exploration", R.drawable.adventure);
//
//            // Return the icon based on the category or a default icon if the category is not in the map
//            int icon = iconMap.getOrDefault(category, R.drawable.default_icon);
//            Log.d("DataHelper", "Category: " + category + ", Icon Resource: " + icon);
//            return icon;    }


    }

