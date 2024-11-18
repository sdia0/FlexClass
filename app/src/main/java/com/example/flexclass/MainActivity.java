package com.example.flexclass;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView day;
    Spinner spWeek;
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

        spWeek = findViewById(R.id.spWeek);

        // Создаем массив данных для Spinner
        List<String> spinnerItems = new ArrayList<>(Arrays.asList("Четная неделя", "Несчетная неделя"));

        // Создаем адаптер для Spinner
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerItems);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWeek.setAdapter(spAdapter);

        day = findViewById(R.id.tvDay);
        /*day.setText("Понедельник");
        mondayListView = findViewById(R.id.dayListView);
        LessonsAdapter adapter= new LessonsAdapter(this, getLessonsForDay("Понедельник"));
        mondayListView.setAdapter(adapter);*/

        List<String> days = new ArrayList<>(Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"));

        List<DaySchedule> weekSchedule = new ArrayList<>();
        weekSchedule.add(new DaySchedule("Monday", getLessonsForDay(days.get(0))));
        weekSchedule.add(new DaySchedule("Tuesday", getLessonsForDay(days.get(1))));
        weekSchedule.add(new DaySchedule("Wednesday", getLessonsForDay(days.get(2))));
        weekSchedule.add(new DaySchedule("Thursday", getLessonsForDay(days.get(3))));
        weekSchedule.add(new DaySchedule("Friday", getLessonsForDay(days.get(4))));
        weekSchedule.add(new DaySchedule("Saturday", getLessonsForDay(days.get(5))));

        // Привязка каждого адаптера к своим View
        for (int i = 0; i < weekSchedule.size(); i++) {
            int resId = getResources().getIdentifier(weekSchedule.get(i).getDayName().toLowerCase() + "Layout", "id", getPackageName());
            View dayView = findViewById(resId);

            TextView tvDay = dayView.findViewById(R.id.tvDay);
            ListView listView = dayView.findViewById(R.id.dayListView);

            DayAdapter adapter = new DayAdapter(this, weekSchedule.get(i));
            listView.setAdapter(adapter);
            if (weekSchedule.get(i).getLessons() != null) adapter.updateDayTitle(tvDay, days.get(i));
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
                lessons.add(new Lesson("10:00", "Оффлайн", "Системы ИИ", "лб", day, "Четная неделя"));
                lessons.add(new Lesson("11:30", "Онлайн", "Физика", "пр", day, "Четная неделя"));
                break;
            case "Вторник":
                lessons.add(new Lesson("13:00", "Онлайн", "УП", "лб", day, "Четная неделя"));
                lessons.add(new Lesson("11:30", "Онлайн", "ТРПО", "пр", day, "Четная неделя"));
                break;
            case "Четверг":
                lessons.add(new Lesson("15:00", "Оффлайн", "КС", "пр", day, "Четная неделя"));
                lessons.add(new Lesson("16:30", "Онлайн", "Физика", "пр", day, "Четная неделя"));
                break;
            case "Пятница":
                lessons.add(new Lesson("10:00", "Оффлайн", "Системы ИИ", "лб", day, "Четная неделя"));
                lessons.add(new Lesson("11:30", "Онлайн", "Физика", "лк", day, "Четная неделя"));
                break;
            case "Суббота":
                lessons.add(new Lesson("10:00", "Оффлайн", "Системы ИИ", "лб", day, "Четная неделя"));
                lessons.add(new Lesson("11:30", "Онлайн", "Физика", "пр", day, "Четная неделя"));
                lessons.add(new Lesson("13:00", "Онлайн", "Физика", "лк", day, "Четная неделя"));
                break;
        }
        return lessons;
    }
}
