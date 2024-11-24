package com.example.flexclass;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    ImageButton btnAdd;
    DbHelper db;
    LinearLayout editMod;
    EditText startTime, endTime, etLinkOrAud, etSubject;
    Spinner spFormat, spType, spDay, spWeek;
    String selectedFormat, selectedType, selectedDay, selectedWeek;
    Button ok;
    ImageView exit;
    TextView week;
    public static String NOTIFICATION_CHANNEL_ID = "1001";
    public static String default_notification_id = "default";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ваш родительский макет с <include>

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spFormat = findViewById(R.id.spFormat);
        spType = findViewById(R.id.spType);
        spDay = findViewById(R.id.spDay);
        spWeek = findViewById(R.id.spWeek);

        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        etLinkOrAud = findViewById(R.id.etLinkOrAud);
        etSubject = findViewById(R.id.etSubject);

        editMod = findViewById(R.id.editMode);

        ok = findViewById(R.id.btnOk);
        exit = findViewById(R.id.btnExit);

        setSpinner(spFormat, Arrays.asList("Онлайн", "Оффлайн"), selectedFormat, selected -> selectedFormat = selected);
        setSpinner(spType, Arrays.asList("лб", "лк", "пр"), selectedType, selected -> selectedType = selected);
        setSpinner(spDay, Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"), selectedDay, selected -> selectedDay = selected);
        setSpinner(spWeek, Arrays.asList("Каждую неделю", "Числитель", "Знаменатель"), selectedWeek, selected -> selectedWeek = selected);

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMod.setVisibility(View.VISIBLE);
            }
        });

        db = new DbHelper(this);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = startTime.getText().toString() + "-" + endTime.getText().toString();
                String subject = etSubject.getText().toString();
                String linkOrAud = etLinkOrAud.getText().toString();
                if (startTime.getText().toString().isEmpty() || endTime.getText().toString().isEmpty() || subject.isEmpty() || linkOrAud.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Lesson newLesson = new Lesson(
                        time,
                        selectedFormat,
                        subject,
                        selectedType,
                        selectedDay,
                        selectedWeek
                );
                newLesson.setAudOrLink(linkOrAud);
                boolean result = db.insertData(newLesson);
                if (result) {
                    Toast.makeText(getApplicationContext(),
                            "Data inserted",
                            Toast.LENGTH_SHORT);
                } else
                    Toast.makeText(getApplicationContext(),
                            "Data not inserted",
                            Toast.LENGTH_SHORT);
                setDayAdapter();
                editMod.setVisibility(View.GONE);
                selectedFormat = "Онлайн";
                selectedType = "лб";
                selectedDay = "Понедельник";
                selectedWeek = "Каждую неделю";
                setSpinner(spFormat, Arrays.asList("Онлайн", "Оффлайн"), selectedFormat, selected -> selectedFormat = selected);
                setSpinner(spType, Arrays.asList("лб", "лк", "пр"), selectedType, selected -> selectedType = selected);
                setSpinner(spDay, Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"), selectedDay, selected -> selectedDay = selected);
                setSpinner(spWeek, Arrays.asList("Каждую неделю", "Числитель", "Знаменатель"), selectedWeek, selected -> selectedWeek = selected);
                startTime.setText("");
                endTime.setText("");
                etLinkOrAud.setText("");
                etSubject.setText("");
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMod.setVisibility(View.GONE);
            }
        });

        // Получаем текущую дату
        Calendar currentCalendar = Calendar.getInstance();
        Date currentDate = currentCalendar.getTime();

        // Определяем начало учебного года (1 сентября)
        Calendar startOfYearCalendar = Calendar.getInstance();
        startOfYearCalendar.set(currentCalendar.get(Calendar.YEAR), Calendar.SEPTEMBER, 1);

        // Получаем номер недели для обеих дат
        int weekOfYearStart = startOfYearCalendar.get(Calendar.WEEK_OF_YEAR);
        int weekOfYearCurrent = currentCalendar.get(Calendar.WEEK_OF_YEAR);

        // Рассчитываем текущую учебную неделю
        int schoolWeek = weekOfYearCurrent - weekOfYearStart + 1;

        // Устанавливаем номер учебной недели в TextView
        week = findViewById(R.id.tvWeek);
        if (schoolWeek % 2 != 0) week.setText("Знаменатель");
        else week.setText("Числитель");

        week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (week.getText().toString().equals("Знаменатель")) week.setText("Числитель");
                else week.setText("Знаменатель");
                setDayAdapter();
            }
        });
        // Запрос разрешения на отправку уведомлений для Android 13 (API 33) и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                // Запрашиваем разрешение
                ActivityCompat.requestPermissions(this,
                        new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
            }
        }
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "LESSON_CHANNEL",
                    "Уведомления об уроках",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Уведомления о начале уроков");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void setSpinner(Spinner spinner, List<String> spinnerItems, String selected, Consumer<String> onItemSelected) {
        // Создаем адаптер для Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int spinnerPosition = spinnerItems.indexOf(selected);
        if (spinnerPosition >= 0) spinner.setSelection(spinnerPosition);

        // Устанавливаем слушатель для Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                onItemSelected.accept(selected); // Передаем выбор через Consumer
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Действие, если ничего не выбрано
            }
        });
    }
    @SuppressLint("ScheduleExactAlarm")
    public void setNotificationAtTime(String time, Notification notification) {
        // Преобразуем строку времени "чч:мм" в миллисекунды
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date parsedTime = dateFormat.parse(time); // Преобразуем строку в Date
            calendar.setTime(parsedTime); // Устанавливаем время в календарь

            // Получаем текущее время и устанавливаем время для уведомления
            long targetTimeInMillis = calendar.getTimeInMillis();

            // Рассчитываем время для уведомления за 10 секунд до целевого времени
            long notificationTime = targetTimeInMillis; // 10 секунд до

            // Создаем PendingIntent для уведомления
            Intent notificationIntent = new Intent(this, MyNotificationPublisher.class);
            notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATIONID, 1);
            notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

            // Устанавливаем AlarmManager, чтобы уведомление появилось в нужное время
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            assert alarmManager != null;
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Notification getNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_id);
        builder.setContentTitle("Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        return builder.build();
    }
    public void setDayAdapter() {
        List<String> days = new ArrayList<>(Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"));
        List<DaySchedule> weekSchedule = new ArrayList<>();
        //fillData();
        weekSchedule.add(new DaySchedule("Monday", db.getScheduleForDay(week.getText().toString(), days.get(0))));
        weekSchedule.add(new DaySchedule("Tuesday", db.getScheduleForDay(week.getText().toString(), days.get(1))));
        weekSchedule.add(new DaySchedule("Wednesday", db.getScheduleForDay(week.getText().toString(), days.get(2))));
        weekSchedule.add(new DaySchedule("Thursday", db.getScheduleForDay(week.getText().toString(), days.get(3))));
        weekSchedule.add(new DaySchedule("Friday", db.getScheduleForDay(week.getText().toString(), days.get(4))));
        weekSchedule.add(new DaySchedule("Saturday", db.getScheduleForDay(week.getText().toString(), days.get(5))));

        // Привязка каждого адаптера к своим View
        for (int i = 0; i < weekSchedule.size(); i++) {
            int resId = getResources().getIdentifier(weekSchedule.get(i).getDayName().toLowerCase() + "Layout", "id", getPackageName());
            View dayView = findViewById(resId);
            TextView tvDay = dayView.findViewById(R.id.tvDay);
            RecyclerView recyclerView = dayView.findViewById(R.id.dayRecyclerView);
            weekSchedule.get(i).getLessons().sort(Comparator.comparing(Lesson::getTimeStart));
            DayAdapter adapter = new DayAdapter(this, weekSchedule.get(i), db, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            // Настройка ItemTouchHelper
            ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    // Определяем разрешенные направления смахивания
                    int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    return makeMovementFlags(0, swipeFlags);
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false; // Мы не обрабатываем перемещение элементов
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    // Удаляем элемент при смахивании
                    int position = viewHolder.getAdapterPosition();
                    adapter.removeItem(position); // Удаление элемента из адаптера
                }

                @Override
                public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                    super.onSelectedChanged(viewHolder, actionState);
                    // Можно добавить визуальные изменения для выделенного элемента, если нужно
                }

                @Override
                public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    // Можно добавить визуальные изменения для завершенного смахивания
                }
            };

            // Настройка ItemTouchHelper
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
            adapter.updateDayTitle(tvDay, days.get(i));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        setDayAdapter();
    }
    void fillData() {
        // Данные для вставки
        List<Lesson> lessons = new ArrayList<>();

        lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", "Понедельник", "Каждую неделю", "2/628", ""));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "пр", "Понедельник", "Каждую неделю", "",
                "https://us04web.zoom.us/j/78136553614?pwd=dU1jK2Z0NjFQZXBSZ1pBZ0pNQTRYZz09"));

        lessons.add(new Lesson("13:00-14:20", "Онлайн", "УП", "лб", "Вторник", "Каждую неделю", "", "ссылка"));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "ТРПО", "пр", "Вторник", "Каждую неделю", "", "ссылка"));

        lessons.add(new Lesson("16:30-17:50", "Оффлайн", "КС", "пр", "Четверг", "Числитель", "2/628", ""));
        lessons.add(new Lesson("15:00-16:20", "Онлайн", "Физика", "пр", "Четверг", "Каждую неделю", "", "ссылка"));

        lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", "Пятница", "Каждую неделю", "2/628", ""));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "лк", "Пятница", "Числитель", "", "ссылка"));

        lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", "Суббота", "Каждую неделю", "2/628", ""));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "пр", "Суббота", "Числитель", "", "ссылка"));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "лк", "Суббота", "Знаменатель", "", "ссылка"));

        // Получаем объект базы данных
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();

        // Очищаем таблицу
        sqLiteDatabase.execSQL("DELETE FROM lessons");

        // Вставляем новые данные
        for (Lesson lesson : lessons) {
            db.insertData(lesson);
        }
    }
}
