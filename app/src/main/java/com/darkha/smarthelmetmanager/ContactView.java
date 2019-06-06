package com.darkha.smarthelmetmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.PhoneNumberUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class ContactView extends LinearLayout {
    private TinyDB tinyDB;
    private ContactWrapper contact;
    private Runnable onChange;

    public ContactView(Context context) {
        super(context);
    }

    public ContactView(Context context, ContactWrapper contact) {
        super(context);

        LinearLayout.inflate(context, R.layout.layout_contact_view, this);
        TextView contactNameView = this.findViewById(R.id.text_contact_name);
        TextView contactNumberView = this.findViewById(R.id.text_contact_number);
        tinyDB = new TinyDB(context);
        this.contact = contact;

        if (contact.isValid()) {
            contactNameView.setText(contact.getName());
            contactNumberView.setText(PhoneNumberUtils.formatNumber(contact.getNumber(), "VN"));
            ArrayList<String> savedContacts = tinyDB.getListString(context.getString(R.string.KEY_EMERGENCY_NUMBERS));
            ArrayList<String> newContacts = new ArrayList<>();
            for (String c : savedContacts) {
                try {
                    JSONObject contactObject = new JSONObject(c);
                    if (!contact.sameAs(new ContactWrapper(contactObject))) {
                        newContacts.add(c);
                    }
                } catch (Exception ignored) {

                }
            }
            newContacts.add(contact.toString());
            tinyDB.putLong("contact_time", System.currentTimeMillis() / 1000);
            tinyDB.putListString(context.getString(R.string.KEY_EMERGENCY_NUMBERS), newContacts);
        } else {
            this.setVisibility(GONE);
        }
        this.findViewById(R.id.button_delete).setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setMessage("Are you sure want to delete this emergency number?")
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        ArrayList<String> savedContacts = tinyDB.getListString(context.getString(R.string.KEY_EMERGENCY_NUMBERS));
                        ArrayList<String> newContacts = new ArrayList<>();
                        for (String c : savedContacts) {
                            try {
                                JSONObject contactObject = new JSONObject(c);
                                if (!contact.sameAs(new ContactWrapper(contactObject))) {
                                    newContacts.add(c);
                                }
                            } catch (Exception ignored) {

                            }
                        }
                        tinyDB.putLong("contact_time", System.currentTimeMillis() / 1000);
                        tinyDB.putListString(context.getString(R.string.KEY_EMERGENCY_NUMBERS), newContacts);
                        if (onChange != null) {
                            onChange.run();
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });
        this.findViewById(R.id.button_edit).setOnClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setView(R.layout.layout_edit_contact)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create();
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), (dialog, which) -> {
                EditText editName = alertDialog.findViewById(R.id.edit_contact_name);
                EditText editNumber = alertDialog.findViewById(R.id.edit_contact_number);

                String newName = editName.getText().toString();
                String newNumber = editNumber.getText().toString().replace(" ", "");
                ContactWrapper newContact = new ContactWrapper(newName, newNumber);

                ArrayList<String> savedContacts = tinyDB.getListString(context.getString(R.string.KEY_EMERGENCY_NUMBERS));
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
                tinyDB.putListString(context.getString(R.string.KEY_EMERGENCY_NUMBERS), newContacts);
                if (onChange != null) {
                    onChange.run();
                }
            });
            alertDialog.show();
            EditText editName = alertDialog.findViewById(R.id.edit_contact_name);
            EditText editNumber = alertDialog.findViewById(R.id.edit_contact_number);

            editName.setText(contact.getName());
            editNumber.setText(contact.getNumber());

        });
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }
}
