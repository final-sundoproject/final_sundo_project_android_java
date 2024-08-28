package com.example.sundo_project_app.evaluation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sundo_project_app.R;
import com.example.sundo_project_app.utill.UrlManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDialogFragment extends DialogFragment {

    private List<Evaluation> evaluationList = new ArrayList<>();
    private EvaluationAdapter evaluationAdapter;
    private long companyCode;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.activity_evaluation_find_all, null);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        evaluationAdapter = new EvaluationAdapter(getContext(), evaluationList);
        recyclerView.setAdapter(evaluationAdapter);


        if (getArguments() != null) {
            companyCode = getArguments().getLong("companyCode", 0);
            Log.d("get companyCode fragment: {}", String.valueOf(companyCode));
            fetchDataAndUpdateRecyclerView();

        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("평가리스트")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }

    private void fetchDataAndUpdateRecyclerView() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UrlManager.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Log.d("dialogFragment companyCode: {}", String.valueOf(companyCode));
        Call<ResponseBody> call = apiService.getAllEvaluations(companyCode);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONArray jsonArray = new JSONArray(jsonResponse);
                        evaluationList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Evaluation evaluation = new Evaluation();
                            evaluation.setArImage(jsonObject.getString("arImage"));
                            evaluation.setTitle(jsonObject.getString("title"));
                            evaluation.setRegistrantName(jsonObject.getString("registrantName"));
                            evaluation.setScenery(jsonObject.getInt("scenery"));
                            evaluation.setWaterDepth(jsonObject.getInt("waterDepth"));
                            evaluation.setWindVolume(jsonObject.getInt("windVolume"));
                            evaluation.setNoiseLevel(jsonObject.getInt("noiseLevel"));
                            evaluation.setAverageRating(jsonObject.getInt("averageRating"));
                            evaluation.setEvaluationId((jsonObject.getLong("evaluationId")));

                            evaluation.setPriRegistrationDate((String) jsonObject.get("priRegistrationDate"));

                            evaluationList.add(evaluation);
                        }

                        evaluationAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("EvaluationFailure", "Network request failed", t);
            }
        });
    }
}
