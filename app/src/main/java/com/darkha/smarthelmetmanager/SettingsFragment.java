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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

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

        return view;
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

                cursor.close();
            }
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
