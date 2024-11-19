package com.example.flexclass;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ContentActivity extends AppCompatActivity {
    EditText startTime, endTime, linkOrAud, subject;
    Spinner spFormat, spType, spDay, spWeek;
    DbHelper db;
    String selectedFormat, selectedType, selectedDay, selectedWeek;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_content);

        db = new DbHelper(this);

        spFormat = findViewById(R.id.spFormat);
        spType = findViewById(R.id.spType);
        spDay = findViewById(R.id.spDay);
        spWeek = findViewById(R.id.spWeek);

        setSpinner(spFormat, Arrays.asList("Онлайн", "Оффлайн"), selected -> selectedFormat = selected);
        setSpinner(spType, Arrays.asList("лб", "лк", "пр"), selected -> selectedType = selected);
        setSpinner(spDay, Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"), selected -> selectedDay = selected);
        setSpinner(spWeek, Arrays.asList("Четная неделя", "Нечетная неделя"), selected -> selectedWeek = selected);

    }
    public void setSpinner(Spinner spinner, List<String> spinnerItems, Consumer<String> onItemSelected) {
        // Создаем адаптер для Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

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
}