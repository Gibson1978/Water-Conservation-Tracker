package com.example.waterconservationappblank;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InfoFragment extends Fragment {

    // constructor: binds the fragment to its layout file
    public InfoFragment() {
        super(R.layout.fragment_info); // Update with your layout name
    }

    // called when the view has been created, setup toggle behaviours
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup toggles for all FAQ questions
        setupToggle(view, R.id.question1, R.id.answer1);
        setupToggle(view, R.id.question2, R.id.answer2);
        setupToggle(view, R.id.question3, R.id.answer3);
        setupToggle(view, R.id.question4, R.id.answer4);
    }

    // configures toggle behaviours for a single question answer pair
    private void setupToggle(View rootView, int questionId, int answerId) {
        TextView question = rootView.findViewById(questionId);
        TextView answer = rootView.findViewById(answerId);

        question.setOnClickListener(v -> {
            if (answer.getVisibility() == View.GONE) {
                answer.setVisibility(View.VISIBLE); // show answer

                // Change arrow to UP
                question.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_arrow_drop_up_24, 0);
            } else {
                answer.setVisibility(View.GONE); // hide answer

                // Change arrow to DOWN
                question.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_arrow_drop_down_24, 0);
            }
        });
    }
}
