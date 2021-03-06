package com.example.root.weatherapp.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import com.example.root.weatherapp.data.WeatherContract;
import com.example.root.weatherapp.data.WeatherPreferences;
import com.example.root.weatherapp.utilities.NetworkUtils;
import com.example.root.weatherapp.utilities.NotificationUtils;
import com.example.root.weatherapp.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class WeatherSyncTask {

    synchronized public static void syncWeather(Context context) {

        try {

            URL weatherRequestUrl = NetworkUtils.getUrl(context);

            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

            ContentValues[] weatherValues = OpenWeatherJsonUtils
                    .getWeatherContentValuesFromJson(context, jsonWeatherResponse);


            if (weatherValues != null && weatherValues.length != 0) {
                ContentResolver weatherContentResolver = context.getContentResolver();
                weatherContentResolver.delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);

                weatherContentResolver.bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        weatherValues);
                boolean notificationsEnabled = WeatherPreferences.areNotificationsEnabled(context);

                long timeSinceLastNotification = WeatherPreferences
                        .getEllapsedTimeSinceLastNotification(context);
                boolean oneDayPassedSinceLastNotification = false;
                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }

                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    NotificationUtils.notifyUserOfNewWeather(context);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
