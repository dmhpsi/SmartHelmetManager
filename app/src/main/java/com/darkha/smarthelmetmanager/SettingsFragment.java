package com.darkha.smarthelmetmanager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private BluetoothHandler bluetooth;
    private TinyDB tinyDB;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        EditText editName = view.findViewById(R.id.edit_name);
        EditText editAddress = view.findViewById(R.id.edit_address);
        EditText editAllergy = view.findViewById(R.id.edit_allergy);
        EditText editBloodType = view.findViewById(R.id.edit_blood_type);

        String[] bloodTypes = new String[]{
                "Unknown",
                "A Rh+",
                "A Rh-",
                "B Rh+",
                "B Rh-",
                "O Rh+",
                "O Rh-",
                "AB Rh+",
                "AB Rh-"
        };
        List<String> bloodList = new ArrayList<>(Arrays.asList(bloodTypes));
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.layout_spinner_item,
                bloodList
        );
        Spinner spinnerBloodType = view.findViewById(R.id.spinner_blood_type);
        spinnerBloodType.setAdapter(bloodAdapter);
        spinnerBloodType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editBloodType.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        TextWatcher userInfoTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    JSONObject info = new JSONObject(tinyDB.getString(getContext().getString(R.string.key_user_info)));
                    if (info.getString("name").equals(editName.getText().toString())
                            && info.getString("address").equals(editAddress.getText().toString())
                            && info.getString("allergy").equals(editAllergy.getText().toString())
                            && info.getString("blood-type").equals(editBloodType.getText().toString())) {
                        view.findViewById(R.id.button_save_user_info).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.button_save_user_info).setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        editName.removeTextChangedListener(userInfoTextWatcher);
        editAllergy.removeTextChangedListener(userInfoTextWatcher);
        editAddress.removeTextChangedListener(userInfoTextWatcher);
        editBloodType.removeTextChangedListener(userInfoTextWatcher);

        try {
            JSONObject info = new JSONObject(tinyDB.getString(getContext().getString(R.string.key_user_info)));
            try {
                editName.setText(info.getString("name"));
                editAddress.setText(info.getString("address"));
                editAllergy.setText(info.getString("allergy"));
                editBloodType.setText(info.getString("blood-type"));
                int bloodIndex = bloodList.indexOf(info.getString("blood-type"));
                if (bloodIndex > -1) {
                    spinnerBloodType.setSelection(bloodIndex);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        editName.addTextChangedListener(userInfoTextWatcher);
        editAllergy.addTextChangedListener(userInfoTextWatcher);
        editAddress.addTextChangedListener(userInfoTextWatcher);
        editBloodType.addTextChangedListener(userInfoTextWatcher);

        view.findViewById(R.id.button_save_user_info).setOnClickListener(v -> {
            JSONObject info = new JSONObject();
            try {
                info.put("name", editName.getText());
                info.put("address", editAddress.getText());
                info.put("allergy", editAllergy.getText());
                info.put("blood-type", editBloodType.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            tinyDB.putString(getContext().getString(R.string.key_user_info), info.toString());
            v.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Saved", Toast.LENGTH_LONG).show();
            Functions.getInstance().hideKeyboard(getActivity());
        });

        EditText editNumber1 = view.findViewById(R.id.edit_number_1);
        EditText editNumber2 = view.findViewById(R.id.edit_number_2);
        EditText editNumber3 = view.findViewById(R.id.edit_number_3);

        TextWatcher phoneTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    JSONObject info = new JSONObject(tinyDB.getString(getContext().getString(R.string.key_phone_numbers)));
                    if (info.getString("number1").equals(editNumber1.getText().toString())
                            && info.getString("number2").equals(editNumber2.getText().toString())
                            && info.getString("number3").equals(editNumber3.getText().toString())) {
                        view.findViewById(R.id.button_save_phone).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.button_save_phone).setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        editNumber1.removeTextChangedListener(phoneTextWatcher);
        editNumber2.removeTextChangedListener(phoneTextWatcher);
        editNumber3.removeTextChangedListener(phoneTextWatcher);

        try {
            JSONObject info = new JSONObject(tinyDB.getString(getContext().getString(R.string.key_phone_numbers)));
            try {
                editNumber1.setText(info.getString("number1"));
                editNumber2.setText(info.getString("number2"));
                editNumber3.setText(info.getString("number3"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        editNumber1.addTextChangedListener(phoneTextWatcher);
        editNumber2.addTextChangedListener(phoneTextWatcher);
        editNumber3.addTextChangedListener(phoneTextWatcher);

        view.findViewById(R.id.button_save_phone).setOnClickListener(v -> {
            JSONObject info = new JSONObject();
            try {
                info.put("number1", editNumber1.getText());
                info.put("number2", editNumber2.getText());
                info.put("number3", editNumber3.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            tinyDB.putString(getContext().getString(R.string.key_phone_numbers), info.toString());
            v.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Saved", Toast.LENGTH_LONG).show();
            Functions.getInstance().hideKeyboard(getActivity());
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setServices(BluetoothHandler bluetooth, TinyDB tinyDB) {
        this.bluetooth = bluetooth;
        this.tinyDB = tinyDB;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
