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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    DbHelper db;
    TextView week;
    List<String> times = new ArrayList<>(Arrays.asList(
            "10:00-11:20",
            "11:30-12:50",
            "13:00-14:20",
            "15:00-16:20",
            "16:30-17:50",
            "18:00-19:20"
    ));
    List<String> ruDays = new ArrayList<>(Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"));
    List<String> engDays = new ArrayList<>(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
    List<DaySchedule> weekSchedule = new ArrayList<>();
    @SuppressLint("ResourceAsColor")
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

        db = new DbHelper(this);

        // Определение текущей недели
        Calendar currentCalendar = Calendar.getInstance();
        Calendar startOfYearCalendar = Calendar.getInstance();
        startOfYearCalendar.set(currentCalendar.get(Calendar.YEAR), Calendar.SEPTEMBER, 1);
        int weekOfYearStart = startOfYearCalendar.get(Calendar.WEEK_OF_YEAR);
        int weekOfYearCurrent = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int schoolWeek = weekOfYearCurrent - weekOfYearStart + 1;
        week = findViewById(R.id.tvWeek);
        if (schoolWeek % 2 != 0) week.setText("Знаменатель");
        else week.setText("Числитель");

        // Посмотреть расписание для другой недели
        week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (week.getText().toString().equals("Знаменатель")) week.setText("Числитель");
                else week.setText("Знаменатель");
            }
        });

        //Установка адаптеров

        weekSchedule.add(new DaySchedule("Monday", db.getScheduleForDay(week.getText().toString(), ruDays.get(0))));
        weekSchedule.add(new DaySchedule("Tuesday", db.getScheduleForDay(week.getText().toString(), ruDays.get(1))));
        weekSchedule.add(new DaySchedule("Wednesday", db.getScheduleForDay(week.getText().toString(), ruDays.get(2))));
        weekSchedule.add(new DaySchedule("Thursday", db.getScheduleForDay(week.getText().toString(), ruDays.get(3))));
        weekSchedule.add(new DaySchedule("Friday", db.getScheduleForDay(week.getText().toString(), ruDays.get(4))));
        weekSchedule.add(new DaySchedule("Saturday", db.getScheduleForDay(week.getText().toString(), ruDays.get(5))));

        // Привязка каждого адаптера к своим View
        for (int i = 0; i < weekSchedule.size(); i++) {
            // Нахождение RecyclerView по id
            int resId = getResources().getIdentifier(weekSchedule.get(i).getDayName().toLowerCase() + "Layout", "id", getPackageName());

            // Инициализация всех View
            View dayView = findViewById(resId);
            TextView tvDay = dayView.findViewById(R.id.tvDay);
            Button add = dayView.findViewById(R.id.addLessonByDay);

            Spinner spWeek = dayView.findViewById(R.id.spWeek_days);
            Spinner spTime = dayView.findViewById(R.id.spTime_days);

            EditText etLinkOrAud = dayView.findViewById(R.id.etLinkOrAud_days);
            EditText etSubject = dayView.findViewById(R.id.etSubject_days);

            LinearLayout editMod = dayView.findViewById(R.id.editMode_days);
            Button ok = dayView.findViewById(R.id.btnOk_days);
            ImageView exit = dayView.findViewById(R.id.btnExit_days);

            Button bOnline = dayView.findViewById(R.id.bOnline_days);
            Button bOffline = dayView.findViewById(R.id.bOffline_days);
            Button bLb = dayView.findViewById(R.id.bLb_days);
            Button bLk = dayView.findViewById(R.id.bLk_days);
            Button bPr = dayView.findViewById(R.id.bPr_days);

            // Установка значений по умолчанию
            String[] selectedFormat = {"Онлайн"};
            bOnline.setBackgroundColor(R.color.white);
            bOffline.setBackgroundColor(R.color.blue);
            String[] selectedType = {"лб"};
            bLb.setBackgroundColor(R.color.white);
            bLk.setBackgroundColor(R.color.blue);
            bPr.setBackgroundColor(R.color.blue);
            final String[] selectedTime = {times.get(0)};
            final String[] selectedWeek = {"Каждую неделю"};
            final String[] selectedDay = {"Понедельник"};

            // Установка спиннеров
            setSpinner(spTime, times, selectedTime[0], selected -> selectedTime[0] = selected);
            setSpinner(spWeek, Arrays.asList("Каждую неделю", "Числитель", "Знаменатель"), selectedWeek[0], selected -> selectedWeek[0] = selected);

            // Кнопка ДОБАВИТЬ, открывающая окно ввода данных
            int finalI = i;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editMod.setVisibility(View.VISIBLE);
                    selectedDay[0] = ruDays.get(finalI);
                }
            });

            bOnline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedFormat[0] = "Онлайн";
                    bOnline.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.white)); // Установить белый цвет
                    bOffline.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.blue)); // Установить синий цвет
                }
            });

            bOffline.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    selectedFormat[0] = "Оффлайн";
                    bOffline.setBackgroundColor(R.color.white);
                    bOnline.setBackgroundColor(R.color.blue);
                }
            });

            bLb.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    selectedType[0] = "лб";
                    bLb.setBackgroundColor(R.color.white);
                    bLk.setBackgroundColor(R.color.blue);
                    bPr.setBackgroundColor(R.color.blue);
                }
            });

            bLk.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    selectedType[0] = "лк";
                    bLb.setBackgroundColor(R.color.blue);
                    bLk.setBackgroundColor(R.color.white);
                    bPr.setBackgroundColor(R.color.blue);
                }
            });

            bPr.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    selectedType[0] = "пр";
                    bLb.setBackgroundColor(R.color.blue);
                    bLk.setBackgroundColor(R.color.blue);
                    bPr.setBackgroundColor(R.color.white);
                }
            });

            // Кнопка ok для ДОБАВЛЕНИЯ данных
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String subject = etSubject.getText().toString();
                    String linkOrAud = etLinkOrAud.getText().toString();

                    // Если хоть одно поле пустое


                    // Добавление новой записи
                    Lesson newLesson = new Lesson(
                            selectedTime[0],
                            selectedFormat[0],
                            subject,
                            selectedType[0],
                            selectedDay[0],
                            selectedWeek[0]
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

                    // Установка данных на адаптер
                    setDayAdapter(selectedDay[0]);
                    editMod.setVisibility(View.GONE);
                    etLinkOrAud.setText("");
                    etSubject.setText("");
                }
            });

            // Кнопка отмены добавления данных (скрывает окно добавления)
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editMod.setVisibility(View.GONE);
                    etLinkOrAud.setText("");
                    etSubject.setText("");
                }
            });
            RecyclerView recyclerView = dayView.findViewById(R.id.dayRecyclerView);
            weekSchedule.get(i).getLessons().sort(Comparator.comparing(Lesson::getTimeStart));
            DayAdapter adapter = new DayAdapter(this, weekSchedule.get(i), db, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            adapter.updateDayTitle(tvDay, ruDays.get(i));
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
    public void setDayAdapter(String day) {
        // Найти индекс дня в расписании
        int index = -1;
        for (int i = 0; i < weekSchedule.size(); i++) {
            if (weekSchedule.get(i).getDayName().equals(day)) {
                index = i;  // Сохраняем индекс найденного дня
                break;
            }
        }

        // Если день найден, заменяем объект
        if (index != -1) {
            // Получаем новый объект DaySchedule для данного дня
            DaySchedule newSchedule = new DaySchedule(engDays.get(index), db.getScheduleForDay(week.getText().toString(), ruDays.get(index)));
            // Заменяем старый объект новым в списке
            weekSchedule.set(index, newSchedule);
            // Установка адаптера
            int resId = getResources().getIdentifier(weekSchedule.get(index).getDayName().toLowerCase() + "Layout", "id", getPackageName());
            View dayView = findViewById(resId);
            TextView tvDay = dayView.findViewById(R.id.tvDay);
            RecyclerView recyclerView = dayView.findViewById(R.id.dayRecyclerView);
            weekSchedule.get(index).getLessons().sort(Comparator.comparing(Lesson::getTimeStart));
            DayAdapter adapter = new DayAdapter(this, weekSchedule.get(index), db, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            adapter.updateDayTitle(tvDay, ruDays.get(index));
        }
    }
    /*public void setDayAdapters() {
        //fillData();
        weekSchedule.add(new DaySchedule("Monday", db.getScheduleForDay(week.getText().toString(), ruDays.get(0))));
        weekSchedule.add(new DaySchedule("Tuesday", db.getScheduleForDay(week.getText().toString(), ruDays.get(1))));
        weekSchedule.add(new DaySchedule("Wednesday", db.getScheduleForDay(week.getText().toString(), ruDays.get(2))));
        weekSchedule.add(new DaySchedule("Thursday", db.getScheduleForDay(week.getText().toString(), ruDays.get(3))));
        weekSchedule.add(new DaySchedule("Friday", db.getScheduleForDay(week.getText().toString(), ruDays.get(4))));
        weekSchedule.add(new DaySchedule("Saturday", db.getScheduleForDay(week.getText().toString(), ruDays.get(5))));

        // Привязка каждого адаптера к своим View
        for (int i = 0; i < weekSchedule.size(); i++) {
            int resId = getResources().getIdentifier(weekSchedule.get(i).getDayName().toLowerCase() + "Layout", "id", getPackageName());
            View dayView = findViewById(resId);
            TextView tvDay = dayView.findViewById(R.id.tvDay);
            Button add = dayView.findViewById(R.id.addLessonByDay);
            int finalI = i;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editMod.setVisibility(View.VISIBLE);
                    selectedWeek = "Каждую неделю";
                    selectedDay = ruDays.get(finalI);
                    setSpinners();
                }
            });
            RecyclerView recyclerView = dayView.findViewById(R.id.dayRecyclerView);
            weekSchedule.get(i).getLessons().sort(Comparator.comparing(Lesson::getTimeStart));
            DayAdapter adapter = new DayAdapter(this, weekSchedule.get(i), db, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            adapter.updateDayTitle(tvDay, ruDays.get(i));
        }
    }*/
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
