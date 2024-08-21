package com.example.sundo_project_app.evaluation;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.sundo_project_app.R;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EvaluationDelete {

    private final Context context;
    private final long evaluationId;
    private final int position;
    private final EvaluationAdapter adapter;

    public EvaluationDelete(Context context, View itemView, long evaluationId, int position, EvaluationAdapter adapter) {
        this.context = context;
        this.evaluationId = evaluationId;
        this.position = position;
        this.adapter = adapter;

        ImageButton deleteButton = itemView.findViewById(R.id.deleteButton);
        if (deleteButton == null) {
            Log.e("EvaluationDelete", "deleteButton is null");
        } else {
            Log.d("EvaluationDelete", "deleteButton found");
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click event ", String.valueOf(evaluationId));
                deleteEvaluationFromServer();
            }
        });
    }

    private void deleteEvaluationFromServer() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseBody> call = apiService.deleteEvaluation(evaluationId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "평가가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    if (adapter != null) {
                        adapter.removeItem(position);
                    }
                } else {
                    int statusCode = response.code();
                    Toast.makeText(context, "삭제에 실패했습니다. 코드: " + statusCode, Toast.LENGTH_SHORT).show();
                    Log.e("DeleteError", "Response error code: " + statusCode);
                    Log.e("DeleteError", "Response error body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                Log.e("DeleteError", "Network request failed", t);
            }
        });
    }
}
