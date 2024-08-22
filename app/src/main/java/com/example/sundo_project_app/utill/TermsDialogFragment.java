package com.example.sundo_project_app.utill;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.sundo_project_app.R;

public class TermsDialogFragment extends DialogFragment {

    private OnTermsAcceptedListener listener;

    public interface OnTermsAcceptedListener {
        void onTermsAccepted();
    }

    public void setOnTermsAcceptedListener(OnTermsAcceptedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("이용 약관 및 개인정보 처리방침");

        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_terms_scroll, null);

        WebView webViewTerms = view.findViewById(R.id.webview_terms);
        WebView webViewPrivacyPolicy = view.findViewById(R.id.webview_privacy_policy);
        CheckBox checkBoxTerms = view.findViewById(R.id.checkBox_terms);
        CheckBox checkBoxPrivacyPolicy = view.findViewById(R.id.checkBox_privacy_policy);

        WebSettings webSettingsTerms = webViewTerms.getSettings();
        webSettingsTerms.setJavaScriptEnabled(true);

        WebSettings webSettingsPrivacy = webViewPrivacyPolicy.getSettings();
        webSettingsPrivacy.setJavaScriptEnabled(true);

        webViewTerms.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("TermsDialogFragment", "Terms page loaded: " + url);
            }
        });

        webViewPrivacyPolicy.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("TermsDialogFragment", "Privacy Policy page loaded: " + url);
            }
        });

        webViewTerms.loadUrl("file:///android_asset/terms.html");
        webViewPrivacyPolicy.loadUrl("file:///android_asset/privacy_policy.html");

        builder.setView(view)
                .setPositiveButton("확인", (dialog, which) -> {
                    if (listener != null && checkBoxTerms.isChecked() && checkBoxPrivacyPolicy.isChecked()) {
                        listener.onTermsAccepted();
                    } else {
                        Toast.makeText(requireActivity(), "모든 약관에 동의해야 합니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            AlertDialog alertDialog = (AlertDialog) dialog;
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            checkBoxTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updateButtonState(alertDialog, checkBoxTerms, checkBoxPrivacyPolicy));
            checkBoxPrivacyPolicy.setOnCheckedChangeListener((buttonView, isChecked) -> updateButtonState(alertDialog, checkBoxTerms, checkBoxPrivacyPolicy));
        });

        return dialog;
    }

    private void updateButtonState(AlertDialog dialog, CheckBox checkBoxTerms, CheckBox checkBoxPrivacyPolicy) {
        boolean isTermsChecked = checkBoxTerms.isChecked();
        boolean isPrivacyPolicyChecked = checkBoxPrivacyPolicy.isChecked();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isTermsChecked && isPrivacyPolicyChecked);
    }
}
