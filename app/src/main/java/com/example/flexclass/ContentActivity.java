package com.example.flexclass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

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
    EditText startTime, endTime, etLinkOrAud, etSubject;
    ImageButton btnInsert, btnDelete;
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

        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        etLinkOrAud = findViewById(R.id.etLinkOrAud);
        etSubject = findViewById(R.id.etSubject);

        btnInsert = findViewById(R.id.btnOk);
        btnDelete = findViewById(R.id.btnDelete);

        Intent intent = getIntent();
        Lesson lesson = (Lesson) intent.getSerializableExtra("lesson");

        if (lesson != null){
            startTime.setText(lesson.getTimeStart());
            endTime.setText(lesson.getTimeEnd());
            selectedFormat = lesson.getFormat();
            etLinkOrAud.setText(lesson.getAudOrLink(lesson.getFormat()));
            selectedDay = lesson.getDay();
            selectedWeek = lesson.getWeek();
            selectedType = lesson.getType();
        }

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = startTime.getText().toString() + "-" + endTime.getText().toString();
                String subject = etSubject.getText().toString();
                String linkOrAud = etLinkOrAud.getText().toString();
                if (time.isEmpty() || subject.isEmpty() || linkOrAud.isEmpty()) {
                    Toast.makeText(ContentActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Lesson lesson = new Lesson(
                    time,
                    selectedFormat,
                    subject,
                    selectedType,
                    selectedDay,
                    selectedWeek
                );
                lesson.setAudOrLink(linkOrAud);
                boolean result = db.insertData(lesson);
                if (result)
                {   Toast.makeText(getApplicationContext(),
                        "Data inserted",
                        Toast.LENGTH_SHORT);
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(),
                            "Data not inserted",
                            Toast.LENGTH_SHORT);
            }
        });
    }
    public void setSpinner(Spinner spinner, List<String> spinnerItems, Consumer<String> onItemSelected) {
        // Создаем адаптер для Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerItems);
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