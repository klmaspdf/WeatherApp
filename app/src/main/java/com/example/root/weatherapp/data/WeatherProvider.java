package com.example.root.weatherapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.root.weatherapp.utilities.WeatherDateUtils;

public class WeatherProvider extends ContentProvider {

    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, CODE_WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());

        return true;
    }


    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_WEATHER:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long weatherDate =
                                value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if (!WeatherDateUtils.isDateNormalized(weatherDate)) {
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_WEATHER_WITH_DATE: {
                String normalizedUtcDateString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{normalizedUtcDateString};
                cursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_WEATHER: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override

    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsDeleted;
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case CODE_WEATHER:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
