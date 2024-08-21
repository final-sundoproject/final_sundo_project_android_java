package com.example.sundo_project_app.evaluation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.example.sundo_project_app.R;

import java.util.List;

public class EvaluationDetailDialogFragment extends DialogFragment {

    private List<Evaluation> evaluationList;
    private Long evaluationId;

    public static EvaluationDetailDialogFragment newInstance(List<Evaluation> evaluationList, Long evaluationId, int noiseLevel, int scenery, int waterDepth, int windVolume) {
        EvaluationDetailDialogFragment fragment = new EvaluationDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("evaluation_list", (java.io.Serializable) evaluationList);
        args.putLong("evaluation_id", evaluationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.evaluation_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            evaluationList = (List<Evaluation>) getArguments().getSerializable("evaluation_list");
            evaluationId = getArguments().getLong("evaluation_id", -1);
        }

        Evaluation selectedEvaluation = findEvaluationById(evaluationId);

        if (selectedEvaluation != null) {
            TextView titleTextView = view.findViewById(R.id.textViewTitle);
            TextView registrantNameTextView = view.findViewById(R.id.textViewRegistrantName);
            TextView windVolume = view.findViewById(R.id.textViewWindVolume);
            TextView noiseLevel = view.findViewById(R.id.textviewNoiseLevel);
            TextView waterDepth = view.findViewById(R.id.textviewWaterDepth);
            TextView scenery = view.findViewById(R.id.textViewScenery);
            TextView averageRatingTextView = view.findViewById(R.id.textViewAverage);
            ImageView arImageView = view.findViewById(R.id.arImageView);

            titleTextView.setText("평가명: " + selectedEvaluation.getTitle());
            registrantNameTextView.setText("등록자명: " + selectedEvaluation.getRegistrantName());
            averageRatingTextView.setText("평점: " + selectedEvaluation.getAverageRating());
            windVolume.setText("풍속: " + selectedEvaluation.getWindVolume());
            noiseLevel.setText("소음: " + selectedEvaluation.getWindVolume());
            waterDepth.setText("수심: " + selectedEvaluation.getWaterDepth());
            scenery.setText("가시성: " + selectedEvaluation.getScenery());
            Glide.with(this).load(selectedEvaluation.getArImage()).into(arImageView);
        }
    }

    private Evaluation findEvaluationById(Long evaluationId) {
        for (Evaluation eval : evaluationList) {
            if (eval.getEvaluationId().equals(evaluationId)) {
                return eval;
            }
        }
        return null;
    }
}
