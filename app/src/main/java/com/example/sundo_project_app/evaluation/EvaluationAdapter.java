package com.example.sundo_project_app.evaluation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.sundo_project_app.R;

import java.util.List;

public class EvaluationAdapter extends RecyclerView.Adapter<EvaluationAdapter.EvaluationViewHolder> {

    private final List<Evaluation> evaluationList;
    private final Context context;

    public EvaluationAdapter(Context context, List<Evaluation> evaluationList) {
        this.context = context;
        this.evaluationList = evaluationList;
    }

    @NonNull
    @Override
    public EvaluationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evaluation, parent, false);
        return new EvaluationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EvaluationViewHolder holder, int position) {
        Evaluation evaluation = evaluationList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(evaluation.getArImage())
                .into(holder.arImage);

        holder.titleTextView.setText("평가명: " + evaluation.getTitle());
        holder.registrantNameTextView.setText("등록자명: " + evaluation.getRegistrantName());
        holder.averageRatingTextView.setText("평점: " + evaluation.getAverageRating());


        holder.itemView.setOnClickListener(v -> {
            EvaluationDetailDialogFragment dialog = EvaluationDetailDialogFragment.newInstance(
                    evaluationList,
                    evaluation.getEvaluationId(),
                    evaluation.getNoiseLevel(),
                    evaluation.getScenery(),
                    evaluation.getWaterDepth(),
                    evaluation.getWindVolume()
            );
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "EvaluationDetailDialog");
        });


        new EvaluationDelete(context, holder.itemView, evaluation.getEvaluationId(), position, this);
    }

    @Override
    public int getItemCount() {
        return evaluationList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < evaluationList.size()) {
            evaluationList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class EvaluationViewHolder extends RecyclerView.ViewHolder {
        ImageView arImage;
        TextView titleTextView;
        TextView registrantNameTextView;
        TextView averageRatingTextView;

        public EvaluationViewHolder(@NonNull View itemView) {
            super(itemView);
            arImage = itemView.findViewById(R.id.arImage);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            registrantNameTextView = itemView.findViewById(R.id.textViewRegistrantName);
            averageRatingTextView = itemView.findViewById(R.id.textViewAverage);
        }
    }
}
