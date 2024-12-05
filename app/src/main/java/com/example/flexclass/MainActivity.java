package com.example.flexclass;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private SwipeRefreshLayout swipeRefreshLayout;
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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        // fillData();

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
                if (week.getText().toString().equals("Знаменатель")) {
                    DayAdapter.week = "Числитель";
                    week.setText("Числитель");
                }
                else {
                    DayAdapter.week = "Знаменатель";
                    week.setText("Знаменатель");
                }
                // Подгрузить расписание для выбранной недели
                setDayAdapter("Понедельник");
                setDayAdapter("Вторник");
                setDayAdapter("Среда");
                setDayAdapter("Четверг");
                setDayAdapter("Пятница");
                setDayAdapter("Суббота");
            }
        });

        // Устанавливаем слушатель для действия обновления
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Подгрузить последние данные и отрисовать адаптеры заново
                setDayAdapter("Понедельник");
                setDayAdapter("Вторник");
                setDayAdapter("Среда");
                setDayAdapter("Четверг");
                setDayAdapter("Пятница");
                setDayAdapter("Суббота");
                swipeRefreshLayout.setRefreshing(false);
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
            ImageButton btnPaste = dayView.findViewById(R.id.btnPaste_days);
            ImageButton btnClear = dayView.findViewById(R.id.btnClear_days);
            EditText etSubject = dayView.findViewById(R.id.etSubject_days);

            LinearLayout editMod = dayView.findViewById(R.id.editMode_days);
            Button ok = dayView.findViewById(R.id.btnOk_days);
            ImageView exit = dayView.findViewById(R.id.btnExit_days);

            Button bOnline = dayView.findViewById(R.id.bOnline_days);
            Button bOffline = dayView.findViewById(R.id.bOffline_days);
            Button bLb = dayView.findViewById(R.id.bLb_days);
            Button bLk = dayView.findViewById(R.id.bLk_days);
            Button bPr = dayView.findViewById(R.id.bPr_days);

            Context context = dayView.getContext();
            // Установка значений по умолчанию
            String[] selectedFormat = {"Онлайн"};
            setSelectedButton(bOnline, context);
            resetButton(bOffline, context);
            etLinkOrAud.setHint("Ссылка");
            btnPaste.setVisibility(View.VISIBLE);
            btnClear.setVisibility(View.VISIBLE);
            String[] selectedType = {"лб"};
            setSelectedButton(bLb, context);
            resetButton(bLk, context);
            resetButton(bPr, context);
            final String[] selectedTime = {times.get(0)};
            final String[] selectedWeek = {"Каждую неделю"};
            final String[] selectedDay = {"Понедельник"};

            // Установка спиннеров
            setSpinner(spTime, times, selectedTime[0], selected -> selectedTime[0] = selected);
            setSpinner(spWeek, Arrays.asList("Каждую неделю", "Числитель", "Знаменатель"), selectedWeek[0], selected -> selectedWeek[0] = selected);

            // Кнопка ДОБАВИТЬ, открывающая окно ввода данных
            int finalI = i;
            add.setOnClickListener(v -> {
                editMod.setVisibility(View.VISIBLE);
                selectedDay[0] = ruDays.get(finalI);
            });

            bOnline.setOnClickListener(v -> {
                selectedFormat[0] = "Онлайн";
                setSelectedButton(bOnline, context);
                resetButton(bOffline, context);
                etLinkOrAud.setHint("Ссылка");
                btnPaste.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) etLinkOrAud.getLayoutParams();
                params.weight = 6;
                etLinkOrAud.setLayoutParams(params);
            });

            bOffline.setOnClickListener(v -> {
                selectedFormat[0] = "Оффлайн";
                setSelectedButton(bOffline, context);
                resetButton(bOnline, context);
                etLinkOrAud.setHint("Аудитория");
                btnPaste.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) etLinkOrAud.getLayoutParams();
                params.weight = 7;
                etLinkOrAud.setLayoutParams(params);
            });

            bLb.setOnClickListener(v -> {
                selectedType[0] = "лб";
                setSelectedButton(bLb, context);
                resetButton(bLk, context);
                resetButton(bPr, context);
            });

            bLk.setOnClickListener(v -> {
                selectedType[0] = "лк";
                setSelectedButton(bLk, context);
                resetButton(bLb, context);
                resetButton(bPr, context);
            });

            bPr.setOnClickListener(v -> {
                selectedType[0] = "пр";
                setSelectedButton(bPr, context);
                resetButton(bLk, context);
                resetButton(bLb, context);
            });

            btnClear.setOnClickListener(v -> {
                etLinkOrAud.setText("");
            });

            btnPaste.setOnClickListener(v -> {
                // Получаем буфер обмена
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    // Получаем текст из буфера обмена
                    CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
                    if (text != null) {
                        // Вставляем текст в EditText
                        etLinkOrAud.setText(text);
                    } else {
                        Toast.makeText(MainActivity.this, "Буфер обмена пуст", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Кнопка ok для ДОБАВЛЕНИЯ данных
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String subject = etSubject.getText().toString();
                    String linkOrAud = etLinkOrAud.getText().toString();

                    // Если хоть одно поле пустое
                    if (subject.isEmpty() || linkOrAud.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                        return;
                    }

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

                    // Проверка
                    if (result) {
                        // Закрыть окно добавления
                        editMod.setVisibility(View.GONE);

                        // Установка данных на адаптер
                        setDayAdapter(selectedDay[0]);

                        // Установка значений по умолчанию
                        etLinkOrAud.setText("");
                        etSubject.setText("");
                        selectedFormat[0] = "Онлайн";
                        setSelectedButton(bOnline, context);
                        resetButton(bOffline, context);
                        etLinkOrAud.setHint("Ссылка");
                        selectedType[0] = "лб";
                        setSelectedButton(bLb, context);
                        resetButton(bLk, context);
                        resetButton(bPr, context);
                        selectedTime[0] = times.get(0);
                        selectedWeek[0] = "Каждую неделю";
                        selectedDay[0] = "Понедельник";
                    }
                    else Toast.makeText(getApplicationContext(), "Возникла ошибка. Повторите попытку", Toast.LENGTH_SHORT).show();
                }
            });

            // Кнопка отмены добавления данных (скрывает окно добавления)
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editMod.setVisibility(View.GONE);

                    // Установка значений по умолчанию
                    etLinkOrAud.setText("");
                    etSubject.setText("");
                    selectedFormat[0] = "Онлайн";
                    setSelectedButton(bOnline, context);
                    resetButton(bOffline, context);
                    etLinkOrAud.setHint("Ссылка");
                    selectedType[0] = "лб";
                    setSelectedButton(bLb, context);
                    resetButton(bLk, context);
                    resetButton(bPr, context);
                    selectedTime[0] = times.get(0);
                    selectedWeek[0] = "Каждую неделю";
                    selectedDay[0] = "Понедельник";
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
    public void setSelectedButton(Button button, Context context) {
        button.setTextColor(ContextCompat.getColor(context, R.color.blue));
        button.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
    }
    public void resetButton(Button button, Context context) {
        button.setTextColor(ContextCompat.getColor(context, R.color.white));
        button.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
    }
    public void setDayAdapter(String day) {
        // Перевод названия дня на английский
        String engDay = engDays.get(ruDays.indexOf(day));

        // Найти индекс дня в расписании
        int index = -1;
        for (int i = 0; i < weekSchedule.size(); i++) {
            if (weekSchedule.get(i).getDayName().equals(engDay)) {
                index = i;  // Сохраняем индекс найденного дня
                break;
            }
        }

        // Если день найден, заменяем объект
        if (index != -1) {
            // Получаем новый объект DaySchedule для данного дня
            DaySchedule newSchedule = new DaySchedule(engDay, db.getScheduleForDay(week.getText().toString(), ruDays.get(index)));
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
