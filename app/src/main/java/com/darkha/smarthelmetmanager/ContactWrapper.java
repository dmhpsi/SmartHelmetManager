package com.darkha.smarthelmetmanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactWrapper {
    private String name;
    private String number;

    public ContactWrapper(JSONObject jsonObject) {
        this.name = null;
        this.number = null;
        try {
            this.name = jsonObject.getString("name");
            this.number = jsonObject.getString("number");
        } catch (JSONException ignored) {
        }
    }

    public ContactWrapper(String name, String number) {
        this.number = number;
        this.name = name;
    }

    public boolean isValid() {
        return (name != null && number != null);
    }

    public boolean sameAs(@Nullable ContactWrapper another) {
        if (another != null) {
            return this.number.equals(another.number);
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return toJSON().toString();
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("number", number);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
