package com.tss.quikpawn;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "QuickPawn";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_COMPANY_ID = "company_id";
    private static final String KEY_COMPANY_BRANCH_ID = "company_branch_id";
    private static final String KEY_COMPANY_NAME = "company_name";
    private static final String KEY_COMPANY_BRANCH_NAME = "company_branch_name";
    private static final String KEY_COMPANY_TID = "company_tid";
    private static final String KEY_COMPANY_SHOP = "company_shop";

    private static PreferencesManager sInstance;
    private final SharedPreferences mPref;

    private PreferencesManager(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(context);
        }
    }

    public static synchronized PreferencesManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }

    public void setToken(String value) {
        mPref.edit()
                .putString(KEY_TOKEN, value)
                .commit();
    }

    public String getToken() {
        return mPref.getString(KEY_TOKEN, "");
    }

    public void removeToken() {
        remove(KEY_TOKEN);
    }

    public void setUserId(String value) {
        mPref.edit()
                .putString(KEY_USER_ID, value)
                .commit();
    }

    public String getUserId() {
        return mPref.getString(KEY_USER_ID, "");
    }

    public void removeUserId() {
        remove(KEY_USER_ID);
    }

    public void setCompanyId(String value) {
        mPref.edit()
                .putString(KEY_COMPANY_ID, value)
                .commit();
    }

    public String getCompanyId() {
        return mPref.getString(KEY_COMPANY_ID, "");
    }

    public void removeCompanyId() {
        remove(KEY_COMPANY_ID);
    }

    public void setCompanyBranchId(String value) {
        mPref.edit()
                .putString(KEY_COMPANY_BRANCH_ID, value)
                .commit();
    }

    public String getCompanyBranchId() {
        return mPref.getString(KEY_COMPANY_BRANCH_ID, "");
    }

    public void removeCompanyBranchId() {
        remove(KEY_COMPANY_BRANCH_ID);
    }

    public void setCompanyBranchName(String value) {
        mPref.edit()
                .putString(KEY_COMPANY_BRANCH_NAME, value)
                .commit();
    }

    public String getCompanyBranchName() {
        return mPref.getString(KEY_COMPANY_BRANCH_NAME, "");
    }

    public void removeCompanyBranchName() {
        remove(KEY_COMPANY_BRANCH_NAME);
    }

    public void setCompanyName(String value) {
        mPref.edit()
                .putString(KEY_COMPANY_NAME, value)
                .commit();
    }

    public String getCompanyName() {
        return mPref.getString(KEY_COMPANY_NAME, "");
    }

    public void removeCompanyName() {
        remove(KEY_COMPANY_NAME);
    }


    public void setTID(String value) {
        mPref.edit()
                .putString(KEY_COMPANY_TID, value)
                .commit();
    }

    public String getTID() {
        return mPref.getString(KEY_COMPANY_TID, "");
    }

    public void removeTID() {
        remove(KEY_COMPANY_TID);
    }


    public void setShop(String value) {
        mPref.edit()
                .putString(KEY_COMPANY_SHOP, value)
                .commit();
    }

    public String getShop() {
        return mPref.getString(KEY_COMPANY_SHOP, "");
    }

    public void removeShop() {
        remove(KEY_COMPANY_SHOP);
    }

    public void remove(String key) {
        mPref.edit()
                .remove(key)
                .commit();
    }

    public boolean clear() {
        return mPref.edit()
                .clear()
                .commit();
    }
}
