package com.darkha.smarthelmetmanager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;


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
    private static final int SELECT_PHONE_NUMBER = 12;
    private static final int PERMISSIONS_READ_CONTACTS = 112;
    String[] READ_CONTACTS_PERMISSIONS = {
            Manifest.permission.READ_CONTACTS
    };
    LinearLayout contactContainer;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private BluetoothHandler bluetooth;
    private TinyDB tinyDB;
    private Button loginButton;
    private RelativeLayout accountContainer;
    EditText editName;
    EditText editAddress;
    EditText editAllergy;
    EditText editBloodType;
    List<String> bloodList;
    Spinner spinnerBloodType;

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

        editName = view.findViewById(R.id.edit_name);
        editAddress = view.findViewById(R.id.edit_address);
        editAllergy = view.findViewById(R.id.edit_allergy);
        editBloodType = view.findViewById(R.id.edit_blood_type);
        contactContainer = view.findViewById(R.id.layout_contacts_container);

        String[] bloodTypes = new String[]{
                "--",
                "A Rh+",
                "A Rh-",
                "B Rh+",
                "B Rh-",
                "O Rh+",
                "O Rh-",
                "AB Rh+",
                "AB Rh-"
        };
        bloodList = new ArrayList<>(Arrays.asList(bloodTypes));
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.layout_spinner_item,
                bloodList
        );
        spinnerBloodType = view.findViewById(R.id.spinner_blood_type);
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
                    JSONObject info = new JSONObject(tinyDB.getString(getContext().getString(R.string.KEY_USER_INFO)));
                    if (info.getString("name").equals(editName.getText().toString())
                            && info.getString("address").equals(editAddress.getText().toString())
                            && info.getString("allergy").equals(editAllergy.getText().toString())
                            && info.getString("blood-type").equals(editBloodType.getText().toString())) {
                        view.findViewById(R.id.button_save_user_info).setVisibility(View.GONE);
                    } else {
                        view.findViewById(R.id.button_save_user_info).setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    try {
                        JSONObject info;
                        info = new JSONObject("{\"name\":\"\",\"address\":\"\",\"allergy\":\"\",\"blood-type\":\"--\"}");
                        tinyDB.putString(getContext().getString(R.string.KEY_USER_INFO), info.toString());
                    } catch (JSONException ignored) {
                    }

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
            JSONObject info = new JSONObject(tinyDB.getString(getContext().getString(R.string.KEY_USER_INFO)));
            editName.setText(info.getString("name"));
            editAddress.setText(info.getString("address"));
            editAllergy.setText(info.getString("allergy"));
            editBloodType.setText(info.getString("blood-type"));
            int bloodIndex = bloodList.indexOf(info.getString("blood-type"));
            if (bloodIndex > -1) {
                spinnerBloodType.setSelection(bloodIndex);
            }
        } catch (Exception ignored) {
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

            tinyDB.putString(getContext().getString(R.string.KEY_USER_INFO), info.toString());
            tinyDB.putLong("info_time", System.currentTimeMillis() / 1000);
            syncData("info");
            v.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Saved", Toast.LENGTH_LONG).show();
            Functions.getInstance().hideKeyboard(getActivity());
        });

        view.findViewById(R.id.button_add_contacts).setOnClickListener(v -> {//Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(R.menu.add_options, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("From contacts")) {
                    if (hasPermissions()) {
                        Intent i = new Intent(Intent.ACTION_PICK);
                        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                        startActivityForResult(i, SELECT_PHONE_NUMBER);
                    } else {
                        permissionCheck();
                    }
                } else if (item.getTitle().equals("Manual")) {
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext())
                            .setView(R.layout.layout_edit_contact)
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .create();
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getActivity().getString(R.string.ok), (dialog, which) -> {
                        EditText editName_ = alertDialog.findViewById(R.id.edit_contact_name);
                        EditText editNumber_ = alertDialog.findViewById(R.id.edit_contact_number);

                        String newName = editName_.getText().toString();
                        String newNumber = editNumber_.getText().toString().replace(" ", "");
                        ContactWrapper newContact = new ContactWrapper(newName, newNumber);

                        ArrayList<String> savedContacts = tinyDB.getListString(getContext().getString(R.string.KEY_EMERGENCY_NUMBERS));
                        ArrayList<String> newContacts = new ArrayList<>();
                        for (String c : savedContacts) {
                            try {
                                JSONObject contactObject = new JSONObject(c);
                                if (!newContact.sameAs(new ContactWrapper(contactObject))) {
                                    newContacts.add(c);
                                }
                            } catch (Exception ignored) {

                            }
                        }
                        newContacts.add(newContact.toString());
                        tinyDB.putLong("contact_time", System.currentTimeMillis() / 1000);
                        tinyDB.putListString(getContext().getString(R.string.KEY_EMERGENCY_NUMBERS), newContacts);
                        refreshContacts();
                    });
                    alertDialog.show();
                }
                return true;
            });

            popup.show();//showing popup menu
        });

        refreshContacts();

        loginButton = view.findViewById(R.id.button_login);
        accountContainer = view.findViewById(R.id.layout_user_account);

        setLoginStatus(tinyDB.getBoolean("loggedin"));

        ImageButton logoutButton = view.findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setMessage("Are your sure want to log out? Synchronization will be disabled.")
                    .setPositiveButton("Log out", (dialog, which) -> {
                        tinyDB.putString("username", "");
                        tinyDB.putBoolean("loggedin", false);
                        tinyDB.putLong("info_time", 0);
                        tinyDB.putLong("contact_time", 0);
                        setLoginStatus(false);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });
        loginButton.setOnClickListener(v -> {
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext())
                    .setView(R.layout.layout_login)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .create();
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getActivity().getString(R.string.ok), (dialog, which) -> {
                EditText editUsername = alertDialog.findViewById(R.id.edit_username);
                EditText editPassword = alertDialog.findViewById(R.id.edit_password);
                String username_ = editUsername.getText().toString();
                String password_ = editPassword.getText().toString();
                JsonObject object = new JsonObject();
                object.addProperty("username", username_);
                object.addProperty("password", password_);
                AlertDialog loggingInDialog = new AlertDialog.Builder(getContext())
                        .setCancelable(false)
                        .setView(R.layout.progressbar_layout)
                        .setMessage("Logging in...")
                        .setPositiveButton(R.string.cancel, (dialog12, which12) -> {
                            AndroidNetworking.cancel("login");
                            dialog12.dismiss();
                        })
                        .create();
                loggingInDialog.show();
                AlertDialog failedDialog = new AlertDialog.Builder(getContext())
                        .setCancelable(false)
                        .setMessage("Login failed, please try again")
                        .setPositiveButton(R.string.ok, (dialog1, which1) -> {
                            dialog1.dismiss();
                        })
                        .create();
                AndroidNetworking.post("http://darkha.pythonanywhere.com/api_login")
                        .addStringBody(object.toString())
                        .setTag("login")
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                Log.e("WWWWWWWW", response.toString());
                                loggingInDialog.dismiss();
                                try {
                                    if (response.getString("result").equals("ok")) {
                                        tinyDB.putString("username", username_);
                                        tinyDB.putString("password", password_);
                                        Toast.makeText(getContext(), "Login successful", Toast.LENGTH_LONG).show();
                                        tinyDB.putBoolean("loggedin", true);
                                        setLoginStatus(true);
                                        tinyDB.putLong("info_time", 0);
                                        tinyDB.putLong("contact_time", 0);
                                        syncData("info");
                                        syncData("contacts");
                                    } else {
                                        failedDialog.show();
                                    }
                                } catch (JSONException ignored) {

                                }

                            }

                            @Override
                            public void onError(ANError error) {
                                // handle error
                                Log.e("WWWWWWWW", error.getErrorDetail());
                                loggingInDialog.dismiss();
                                failedDialog.show();
                            }
                        });
            });

            alertDialog.show();
        });

        return view;
    }

    void setLoginStatus(boolean isLoggedIn) {
        if (isLoggedIn) {
            loginButton.setVisibility(View.GONE);
            accountContainer.setVisibility(View.VISIBLE);
            TextView usernameView = accountContainer.findViewById(R.id.text_username);
            usernameView.setText(tinyDB.getString("username"));
        } else {
            loginButton.setVisibility(View.VISIBLE);
            accountContainer.setVisibility(View.GONE);
        }
    }

    void refreshContacts() {
        ArrayList<String> savedContacts = tinyDB.getListString(getString(R.string.KEY_EMERGENCY_NUMBERS));
        contactContainer.removeAllViews();
        for (String contact : savedContacts) {
            try {
                ContactView contactView = new ContactView(getContext(), new ContactWrapper(new JSONObject(contact)));
                contactView.setOnChange(this::refreshContacts);
                contactContainer.addView(contactView);
            } catch (JSONException ignored) {

            }
        }
        contactContainer.invalidate();
        syncData("contacts");
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

    public boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    void permissionCheck() {
        // Here, thisActivity is the current activity
        if (!hasPermissions(READ_CONTACTS_PERMISSIONS)) {
            new AlertDialog.Builder(getContext())
                    .setMessage("This app need contact reading permission in able to pick contact, please grant the permission")
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(getActivity(),
                                READ_CONTACTS_PERMISSIONS,
                                PERMISSIONS_READ_CONTACTS);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_READ_CONTACTS) {
            if ((grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(i, SELECT_PHONE_NUMBER);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = getContext().getContentResolver().query(contactUri, projection,
                    null, null, null);

            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(projection[0]);
                int nameIndex = cursor.getColumnIndex(projection[1]);
                String number = cursor.getString(numberIndex).replace(" ", "");
                String name = cursor.getString(nameIndex);

                ContactView contactView = new ContactView(getContext(), new ContactWrapper(name, number));
                contactView.setOnChange(this::refreshContacts);
                contactContainer.addView(contactView);
                syncData("contacts");

                cursor.close();
            }
        }
    }

    public void syncData(String type) {
        if (type.equals("info")) {
            String data = tinyDB.getString(getContext().getString(R.string.KEY_USER_INFO));
            String username = tinyDB.getString("username");
            String password = tinyDB.getString("password");
            long timestamp = tinyDB.getLong("info_time", 0);
            if (TextUtils.isEmpty(data)) {
                data = "";
            }
            if (TextUtils.isEmpty(username)) {
                username = "";
            }
            if (TextUtils.isEmpty(password)) {
                password = "";
            }
            JsonObject object = new JsonObject();
            object.addProperty("username", username);
            object.addProperty("password", password);
            object.addProperty("data", data);
            object.addProperty("timestamp", timestamp);

            AndroidNetworking.post("http://darkha.pythonanywhere.com/api_sync_info")
                    .addStringBody(object.toString())
                    .setTag("login")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
                            Log.e("WWWWWWWW", response.toString());
                            try {
                                if (response.getString("result").equals("ok")) {
                                    String resData = response.get("data").toString();
                                    long resTimestamp = response.getLong("timestamp");
                                    tinyDB.putString(getContext().getString(R.string.KEY_USER_INFO), resData);
                                    tinyDB.putLong("info_time", resTimestamp);

                                    try {
                                        JSONObject info = new JSONObject(tinyDB.getString(getContext().getString(R.string.KEY_USER_INFO)));
                                        editName.setText(info.getString("name"));
                                        editAddress.setText(info.getString("address"));
                                        editAllergy.setText(info.getString("allergy"));
                                        editBloodType.setText(info.getString("blood-type"));
                                        int bloodIndex = bloodList.indexOf(info.getString("blood-type"));
                                        if (bloodIndex > -1) {
                                            spinnerBloodType.setSelection(bloodIndex);
                                        }
                                    } catch (Exception ignored) {
                                    }

                                    Toast.makeText(getContext(), "Sync successful", Toast.LENGTH_LONG).show();
                                    setLoginStatus(true);
                                } else {
                                }
                            } catch (JSONException ignored) {

                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            Log.e("WWWWWWWW", error.getErrorDetail());
                        }
                    });

        }
        if (type.equals("contacts")) {
            String data = "";
            ArrayList<String> dataList = tinyDB.getListString(getContext().getString(R.string.KEY_EMERGENCY_NUMBERS));
            JSONArray dataArray = new JSONArray();
            for (String c : dataList) {
                try {
                    JSONObject x;
                    x = new JSONObject(c);
                    dataArray.put(x);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            data = dataArray.toString();
            Log.e("SYNC CONTACT", "data: " + data);

            String username = tinyDB.getString("username");
            String password = tinyDB.getString("password");
            long timestamp = tinyDB.getLong("contact_time", 0);
            if (TextUtils.isEmpty(data)) {
                data = "";
            }
            if (TextUtils.isEmpty(username)) {
                username = "";
            }
            if (TextUtils.isEmpty(password)) {
                password = "";
            }
            JsonObject object = new JsonObject();
            object.addProperty("username", username);
            object.addProperty("password", password);
            object.addProperty("data", data);
            object.addProperty("timestamp", timestamp);

            AndroidNetworking.post("http://darkha.pythonanywhere.com/api_sync_contacts")
                    .addStringBody(object.toString())
                    .setTag("login")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
                            Log.e("SYNC CONTACT", response.toString());
                            try {
                                if (response.getString("result").equals("ok")) {
                                    String resData = response.get("data").toString();
                                    long resTimestamp = response.getLong("timestamp");
                                    if (resTimestamp > timestamp) {
                                        try {
                                            JSONArray jsonArray = new JSONArray(resData);
                                            ArrayList<String> listData = new ArrayList<>();
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                listData.add(jsonArray.get(i).toString());
                                            }
                                            tinyDB.putLong("contact_time", resTimestamp);
                                            tinyDB.putListString(getContext().getString(R.string.KEY_EMERGENCY_NUMBERS), listData);
                                            refreshContacts();
                                        } catch (Exception ignored) {
                                            ignored.printStackTrace();
                                        }
                                    }

                                    Toast.makeText(getContext(), "Sync successful", Toast.LENGTH_LONG).show();
                                    setLoginStatus(true);
                                } else {
                                }
                            } catch (JSONException ignored) {

                                Log.e("SYNC CONTACT", "Exception");
                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            Log.e("SYNC CONTACT", error.getErrorDetail());
                        }
                    });

        }
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
