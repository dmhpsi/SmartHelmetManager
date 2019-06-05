package com.darkha.smarthelmetmanager.ui.main;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.darkha.smarthelmetmanager.AppExecutors;
import com.darkha.smarthelmetmanager.AppLog;
import com.darkha.smarthelmetmanager.LogDatabase;
import com.darkha.smarthelmetmanager.R;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    String type;
    private PageViewModel pageViewModel;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_log_view, container, false);
        final TextView textView = root.findViewById(R.id.section_label);
        final Button button = root.findViewById(R.id.button_clear_log);
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
                AppExecutors.getInstance().diskIO().execute(() -> {
                    StringBuilder builder = new StringBuilder();
                    if (s != null && s.equals("safety")) {
                        type = "safety";
                    } else {
                        type = "device";
                    }
                    List<AppLog> appLogList = LogDatabase.getInstance(getContext()).logDao().getByType(type);

                    for (AppLog appLog : appLogList) {
                        builder.append("\n").append(appLog.toView());
                    }

                    AppExecutors.getInstance().mainThread().execute(() -> {
                        textView.setText(builder.toString());
                    });

                    button.setOnClickListener(v -> {
                        new AlertDialog.Builder(getContext())
                                .setMessage("Are you sure want to clear " + type + " log?")
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    AppExecutors.getInstance().diskIO().execute(() -> {
                                        LogDatabase.getInstance(getContext()).logDao().deleteByType(type);
                                    });
                                    textView.setText("");
                                    dialog.dismiss();
                                })
                                .setNegativeButton(R.string.no, (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .create()
                                .show();
                    });

                });
            }
        });


        return root;
    }
}