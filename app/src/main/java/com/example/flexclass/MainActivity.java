package com.example.flexclass;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView day;
    ImageButton btnAdd;
    DbHelper db;
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

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                startActivity(intent);
            }
        });

        day = findViewById(R.id.tvDay);

        List<String> days = new ArrayList<>(Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"));

        List<DaySchedule> weekSchedule = new ArrayList<>();
        db = new DbHelper(this);
        //fillData();
        weekSchedule.add(new DaySchedule("Monday", db.getScheduleForDay(days.get(0))));
        weekSchedule.add(new DaySchedule("Tuesday", db.getScheduleForDay(days.get(1))));
        weekSchedule.add(new DaySchedule("Wednesday", db.getScheduleForDay(days.get(2))));
        weekSchedule.add(new DaySchedule("Thursday", db.getScheduleForDay(days.get(3))));
        weekSchedule.add(new DaySchedule("Friday", db.getScheduleForDay(days.get(4))));
        weekSchedule.add(new DaySchedule("Saturday", db.getScheduleForDay(days.get(5))));

        // Привязка каждого адаптера к своим View
        for (int i = 0; i < weekSchedule.size(); i++) {
            int resId = getResources().getIdentifier(weekSchedule.get(i).getDayName().toLowerCase() + "Layout", "id", getPackageName());
            View dayView = findViewById(resId);

            TextView tvDay = dayView.findViewById(R.id.tvDay);
            RecyclerView recyclerView = dayView.findViewById(R.id.dayRecyclerView);

            DayAdapter adapter = new DayAdapter(this, weekSchedule.get(i));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            adapter.updateDayTitle(tvDay, days.get(i));
        }
    }

    // Пример метода для получения списка уроков для дня
    private List<Lesson> getLessonsForDay(String day) {
        // Здесь вы можете получить данные для каждого дня недели
        List<Lesson> lessons = new ArrayList<>();
        // Заполняем список уроков для дня
        // Например:
        switch(day) {
            case "Понедельник":
                lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", day, "Четная неделя", "2/628", ""));
                lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "пр", day, "Четная неделя", "", "ссылка"));
                break;
            case "Вторник":
                lessons.add(new Lesson("13:00-14:20", "Онлайн", "УП", "лб", day, "Четная неделя"));
                lessons.add(new Lesson("11:30-12:50", "Онлайн", "ТРПО", "пр", day, "Четная неделя"));
                break;
            case "Четверг":
                lessons.add(new Lesson("15:00-16:20", "Оффлайн", "КС", "пр", day, "Четная неделя"));
                lessons.add(new Lesson("16:30-17:50", "Онлайн", "Физика", "пр", day, "Четная неделя"));
                break;
            case "Пятница":
                lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", day, "Четная неделя"));
                lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "лк", day, "Четная неделя"));
                break;
            case "Суббота":
                lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", day, "Четная неделя"));
                lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "пр", day, "Четная неделя"));
                lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "лк", day, "Четная неделя"));
                break;
        }
        return lessons;
    }
    void fillData() {
        String packageName = getPackageName(); // Получаем имя пакета приложения

        // Данные для вставки
        List<Lesson> lessons = new ArrayList<>();

        lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", "Понедельник", "Четная неделя", "2/628", ""));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "пр", "Понедельник", "Четная неделя", "", "ссылка"));

        lessons.add(new Lesson("13:00-14:20", "Онлайн", "УП", "лб", "Вторник", "Четная неделя", "", "ссылка"));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "ТРПО", "пр", "Вторник", "Четная неделя", "", "ссылка"));

        lessons.add(new Lesson("15:00-16:20", "Оффлайн", "КС", "пр", "Четверг", "Четная неделя", "2/628", ""));
        lessons.add(new Lesson("16:30-17:50", "Онлайн", "Физика", "пр", "Четверг", "Четная неделя", "", "ссылка"));

        lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", "Пятница", "Четная неделя", "2/628", ""));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "лк", "Пятница", "Четная неделя", "", "ссылка"));

        lessons.add(new Lesson("10:00-11:20", "Оффлайн", "Системы ИИ", "лб", "Суббота", "Четная неделя", "2/628", ""));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "пр", "Суббота", "Четная неделя", "", "ссылка"));
        lessons.add(new Lesson("11:30-12:50", "Онлайн", "Физика", "лк", "Суббота", "Четная неделя", "", "ссылка"));

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
