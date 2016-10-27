package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by heriyanto.liu on 10/27/2016.
 */

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDbHelper mDbHelper;

    private static final int PETS = 100;
    private static final int PET_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unkonwn URI " + uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetContract.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not support for " + uri);
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return deletePet(selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return deletePet(selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delete is not support for " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(values, selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not support for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values){

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null || name.isEmpty()){
            throw new IllegalArgumentException("Pet requires a name");
        }

        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender != null || !PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        if (values.size() == 0) {
            return null;
        }

        long Id = db.insert(PetEntry.TABLE_NAME, null, values);
        if (Id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri, Id);
    }

    private int updatePet(ContentValues values, String selection, String [] selectionArgs){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null || name.isEmpty()){
            throw new IllegalArgumentException("Pet requires a name");
        }

        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender != null || !PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        return db.update(PetEntry.TABLE_NAME, values,selection, selectionArgs);
    }

    private int deletePet(String selection, String[] selectionArgs){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
    }
}
