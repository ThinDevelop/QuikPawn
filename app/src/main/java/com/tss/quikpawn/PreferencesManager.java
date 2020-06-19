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
    private static final String KEY_CONTACT = "contact";
    private static final String KEY_CONTACT_PHONE = "contact_phone";
    private static final String KEY_CONTACT_EMAIL = "contact_email";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_TAX_ID = "tax_id_number";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_BUILDING = "building";
    private static final String KEY_ALLEY = "alley";
    private static final String KEY_ROAD = "road";
    private static final String KEY_DISDRICT = "district";
    private static final String KEY_AMPHURE = "amphure";
    private static final String KEY_PROVINCE = "province";
    private static final String KEY_TYPE_CODE = "type_code";
    private static final String KEY_TYPE_NAME = "type_name";

    public void setTypeName(String value) {
        mPref.edit()
                .putString(KEY_TYPE_NAME, value)
                .commit();
    }

    public String getTypeName() {
        return mPref.getString(KEY_TYPE_NAME, "");
    }

    public void removeTypeName() {
        remove(KEY_TYPE_NAME);
    }



    public void setTypeCode(String value) {
        mPref.edit()
                .putString(KEY_TYPE_CODE, value)
                .commit();
    }

    public String getTypeCode() {
        return mPref.getString(KEY_TYPE_CODE, "");
    }

    public void removeTypeCode() {
        remove(KEY_TYPE_CODE);
    }




    public void setProvince(String value) {
        mPref.edit()
                .putString(KEY_PROVINCE, value)
                .commit();
    }

    public String getProvince() {
        return mPref.getString(KEY_PROVINCE, "");
    }

    public void removeProvince() {
        remove(KEY_PROVINCE);
    }




    public void setAmphure(String value) {
        mPref.edit()
                .putString(KEY_AMPHURE, value)
                .commit();
    }

    public String getAmphure() {
        return mPref.getString(KEY_AMPHURE, "");
    }

    public void removeAmphure() {
        remove(KEY_AMPHURE);
    }


    public void setDistrict(String value) {
        mPref.edit()
                .putString(KEY_DISDRICT, value)
                .commit();
    }

    public String getDistrict() {
        return mPref.getString(KEY_DISDRICT, "");
    }

    public void removeDistrict() {
        remove(KEY_DISDRICT);
    }


    public void setRoad(String value) {
        mPref.edit()
                .putString(KEY_ROAD, value)
                .commit();
    }

    public String getRoad() {
        return mPref.getString(KEY_ROAD, "");
    }

    public void removeRoad() {
        remove(KEY_ROAD);
    }




    public void setAlley(String value) {
        mPref.edit()
                .putString(KEY_ALLEY, value)
                .commit();
    }

    public String getAlley() {
        return mPref.getString(KEY_ALLEY, "");
    }

    public void removeAlley() {
        remove(KEY_ALLEY);
    }



    public void setBuilding(String value) {
        mPref.edit()
                .putString(KEY_BUILDING, value)
                .commit();
    }

    public String getBuilding() {
        return mPref.getString(KEY_BUILDING, "");
    }

    public void removeBuilding() {
        remove(KEY_BUILDING);
    }



    public void setNumber(String value) {
        mPref.edit()
                .putString(KEY_NUMBER, value)
                .commit();
    }

    public String getNumber() {
        return mPref.getString(KEY_NUMBER, "");
    }

    public void removeNumber() {
        remove(KEY_NUMBER);
    }



    public void setTaxId(String value) {
        mPref.edit()
                .putString(KEY_TAX_ID, value)
                .commit();
    }

    public String getTaxId() {
        return mPref.getString(KEY_TAX_ID, "");
    }

    public void removeTaxId() {
        remove(KEY_TAX_ID);
    }

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
        return mPref.getString(KEY_COMPANY_BRANCH_NAME, "").toUpperCase();
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
        return mPref.getString(KEY_COMPANY_NAME, "").toUpperCase();
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


    public void setContact(String value) {
        mPref.edit()
                .putString(KEY_CONTACT, value)
                .commit();
    }

    public String getContact() {
        return mPref.getString(KEY_CONTACT, "");
    }

    public void removeContact() {
        remove(KEY_CONTACT);
    }



    public void setContactPhone(String value) {
        mPref.edit()
                .putString(KEY_CONTACT_PHONE, value)
                .commit();
    }

    public String getContactPhone() {
        return mPref.getString(KEY_CONTACT_PHONE, "");
    }

    public void removeContactPhone() {
        remove(KEY_CONTACT_PHONE);
    }

    public void setContactEmail(String value) {
        mPref.edit()
                .putString(KEY_CONTACT_EMAIL, value)
                .commit();
    }

    public String getContactEmail() {
        return mPref.getString(KEY_CONTACT_EMAIL, "");
    }

    public void removeContactEmail() {
        remove(KEY_CONTACT_EMAIL);
    }

    public String getAddress() {
        StringBuilder address = new StringBuilder();
        String number = getNumber();
        String building = getBuilding();
        String alley = getAlley();
        String road = getRoad();
        String district = getDistrict();
        String amphure = getAmphure();
        String province = getProvince();

        if (!number.isEmpty()) {
            address.append(number + " ");
        }
        if (!building.isEmpty()) {
            address.append(building + " ");
        }
        if (!road.isEmpty()) {
            address.append(road + " ");
        }
        if (!alley.isEmpty()) {
            address.append(alley + " ");
        }
        if (!district.isEmpty()) {
            address.append(district + " ");
        }
        if (!amphure.isEmpty()) {
            address.append(amphure + " ");
        }
        if (!province.isEmpty()) {
            address.append(province + " ");
        }
        return address.toString();
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
