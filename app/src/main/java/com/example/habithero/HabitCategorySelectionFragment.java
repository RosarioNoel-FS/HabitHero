package com.example.habithero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayList;
import java.util.List;

public class HabitCategorySelectionFragment extends Fragment {

    private Button buttonHealthFitness;
    private Button buttonMindfulnessWellbeing;
    private Button buttonLearningGrowth;
    private Button buttonCreativityExpression;
    private Button buttonAdventureExploration;
    private Button buttonCreateYourOwn;

    public HabitCategorySelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.habit_category_selection_screen, container, false);

        buttonHealthFitness = view.findViewById(R.id.fitness_button);
        buttonMindfulnessWellbeing = view.findViewById(R.id.mindful_button);
        buttonLearningGrowth = view.findViewById(R.id.learning_button);
        buttonCreativityExpression = view.findViewById(R.id.creativity_button);
        buttonAdventureExploration = view.findViewById(R.id.adventure_button);
        buttonCreateYourOwn = view.findViewById(R.id.create_your_own_button);

        //when a button is pressed pass in the category and onCategorySelected will fetch the appropriate List
        buttonHealthFitness.setOnClickListener(v -> onCategorySelected("Health & Fitness"));
        buttonMindfulnessWellbeing.setOnClickListener(v -> onCategorySelected("Mindfulness & Well-being"));
        buttonLearningGrowth.setOnClickListener(v -> onCategorySelected("Learning & Growth"));
        buttonCreativityExpression.setOnClickListener(v -> onCategorySelected("Creativity & Expression"));
        buttonAdventureExploration.setOnClickListener(v -> onCategorySelected("Adventure & Exploration"));
        buttonCreateYourOwn.setOnClickListener(v -> onCategorySelected("Create Your Own"));

        return view;
    }
    //This method passes Category info (appropriate List, Category Name) to CategoryListFragment
    private void onCategorySelected(String category) {
        List<String> habitsList = DataHelper.getHabitsByCategory().get(category);

        CategoryListFragment categoryListFragment = new CategoryListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("habitsList", new ArrayList<>(habitsList));
        args.putString("category", category); // Pass the category name
        categoryListFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, categoryListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


}
