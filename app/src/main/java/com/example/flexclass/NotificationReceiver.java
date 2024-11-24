package com.example.flexclass;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Получаем данные из Intent
        Log.d("NotificationReceiver", "onReceive triggered");
        Toast.makeText(context, "Абоба", Toast.LENGTH_SHORT).show();
        String lessonTitle = intent.getStringExtra("lesson_title");
        String lessonLink = intent.getStringExtra("lesson_link");

        // Создаем Intent для открытия ссылки, если это онлайн-формат
        Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(lessonLink));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openLinkIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Создаем уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "LESSON_CHANNEL")
                .setContentTitle("Скоро начнется " + lessonTitle)
                .setContentText(lessonLink)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Устанавливаем действие на нажатие
                .setAutoCancel(true);

        // Отправка уведомления
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }
}

