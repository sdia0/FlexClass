package com.example.flexclass;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.MyViewHolder> {

    private Context context;
    public DaySchedule daySchedule;
    private static DbHelper db;
    Activity activity;
    // Конструктор для передачи контекста и данных
    public DayAdapter(Context context, DaySchedule daySchedule, DbHelper db, Activity activity) {
        this.context = context;
        this.daySchedule = daySchedule;
        this.db = db;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Lesson lesson = daySchedule.getLessons().get(position);

        // Установка цвета в кружок
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(getColor(lesson.getFormat()));
        holder.vFormat.setBackground(drawable);

        // Подсказка для цвета
        holder.vFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, lesson.getFormat(), Toast.LENGTH_SHORT).show();
            }
        });

        // Установка данных в окне редактирования
        holder.tvTime.setText(lesson.getTimeStart());
        holder.tvSubject.setText(lesson.getTitle());
        holder.tvType.setText(lesson.getType());
        holder.startTime.setText(lesson.getTimeStart());
        holder.endTime.setText(lesson.getTimeEnd());
        holder.selectedFormat = lesson.getFormat();
        holder.etSubject.setText(lesson.getTitle());
        holder.etLinkOrAud.setText(lesson.getAudOrLink(lesson.getFormat()));
        holder.selectedDay = lesson.getDay();
        holder.selectedWeek = lesson.getWeek();
        holder.selectedType = lesson.getType();

        // Установка спиннеров
        holder.setSpinners();
    }

    // Удалить элемент
    public void removeItem(int position) {
        if (position >= 0 && position < getItemCount()) {
            Lesson lesson = daySchedule.getLessons().get(position);
            try {
                db.deleteData(lesson);
            } catch (Exception e) {
                Toast.makeText(context,
                        e.toString(),
                        Toast.LENGTH_SHORT).show();

                Log.i("keykey",lesson.getId()+"");
                Log.i("keykey",e.getMessage());
            }
            daySchedule.getLessons().remove(position);
            notifyItemRemoved(position);
        }
    }
    // Количество элементов
    @Override
    public int getItemCount() {
        return daySchedule.getLessons().size();
    }

    // Метод для получения цвета по типу
    private int getColor(String format) {
        if (format.equals("Онлайн"))
            return ContextCompat.getColor(context, R.color.white);
        else
            return ContextCompat.getColor(context, R.color.yellow);
    }

    public void updateDayTitle(TextView tvDay, String day) {
        tvDay.setVisibility(View.VISIBLE);
        tvDay.setText(day);
    }

    // ViewHolder для хранения View
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvSubject, tvType;
        View vFormat;
        LinearLayout defaultMod, editMod;
        EditText startTime, endTime, etLinkOrAud, etSubject;
        Spinner spFormat, spType, spDay, spWeek;
        String selectedFormat, selectedType, selectedDay, selectedWeek;
        Button ok, delete;
        ImageView exit;
        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Инициализация
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvType = itemView.findViewById(R.id.tvType);
            vFormat = itemView.findViewById(R.id.vFormat);

            spFormat = itemView.findViewById(R.id.spFormat);
            spType = itemView.findViewById(R.id.spType);
            spDay = itemView.findViewById(R.id.spDay);
            spWeek = itemView.findViewById(R.id.spWeek);

            startTime = itemView.findViewById(R.id.startTime);
            endTime = itemView.findViewById(R.id.endTime);
            etLinkOrAud = itemView.findViewById(R.id.etLinkOrAud);
            etSubject = itemView.findViewById(R.id.etSubject);

            defaultMod = itemView.findViewById(R.id.defaultMode);
            editMod = itemView.findViewById(R.id.editMode);

            ok = itemView.findViewById(R.id.btnOk);
            delete = itemView.findViewById(R.id.btnDelete);
            exit = itemView.findViewById(R.id.btnExit);

            // Переход по ссылке по долгому нажатию на элемент
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Lesson lesson = daySchedule.getLessons().get(getAdapterPosition());
                    if (lesson.getFormat().equals("Онлайн")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(lesson.getLink()));
                        activity.startActivity(browserIntent);
                    }
                    else {
                        Toast.makeText(context, lesson.getAud(), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            // Раскрыть окно редактирования по нажатию на элемент
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    defaultMod.setVisibility(View.GONE);
                    editMod.setVisibility(View.VISIBLE);
                }
            });

            // Кнопка ok для ОБНОВЛЕНИЯ данных
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Получаем введенные значения
                    String time = startTime.getText().toString() + "-" + endTime.getText().toString();
                    String subject = etSubject.getText().toString();
                    String linkOrAud = etLinkOrAud.getText().toString();

                    // Проверяем, что все поля заполнены
                    if (startTime.getText().toString().isEmpty() || endTime.getText().toString().isEmpty() || subject.isEmpty() || linkOrAud.isEmpty()) {
                        Toast.makeText(context, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Валидация данных
                    String sTime = startTime.getText().toString();
                    String eTime = endTime.getText().toString();
                    Pattern pattern = Pattern.compile("^(?:[01]\\d|2[0-3]):([0-5]\\d)$");
                    Matcher matcher1 = pattern.matcher(sTime);
                    Matcher matcher2 = pattern.matcher(eTime);
                    if (!matcher1.matches() || !matcher2.matches()) {
                        Toast.makeText(context, "Заполните время в формате чч:мм", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Создаем новый объект урока с обновленными данными
                    Lesson updatedLesson = new Lesson(
                            time,
                            selectedFormat,
                            subject,
                            selectedType,
                            selectedDay,
                            selectedWeek
                    );
                    updatedLesson.setAudOrLink(linkOrAud);
                    updatedLesson.setId(daySchedule.getLessons().get(getAdapterPosition()).getId());
                    try {
                        // Обновляем данные в базе
                        Boolean result = db.updateData(updatedLesson);

                        // Проверяем, удалось ли обновить данные
                        if (result) {
                            //Toast.makeText(context, "Data updated", Toast.LENGTH_SHORT).show();
                            // Обновляем данные в списке уроков
                            //Toast.makeText(context, newLesson.getId() + "", Toast.LENGTH_SHORT).show();
                            int position = getAdapterPosition(); // Получаем позицию текущего элемента в списке
                            if (position != RecyclerView.NO_POSITION) {
                                // Обновляем данные в адаптере
                                daySchedule.getLessons().set(position, updatedLesson);
                                daySchedule.getLessons().sort(Comparator.comparing(Lesson::getTimeStart));
                                notifyDataSetChanged(); // Уведомляем адаптер, что элемент обновлен
                            }
                        } else {
                            Toast.makeText(context, "Cannot update data", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // Обрабатываем исключения
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.i("keykey", e.getMessage());
                    }

                    // Возвращаемся в режим отображения
                    defaultMod.setVisibility(View.VISIBLE);
                    editMod.setVisibility(View.GONE);
                    setDefaultSpinners();
                    startTime.setText("");
                    endTime.setText("");
                    etLinkOrAud.setText("");
                    etSubject.setText("");
                }
            });

            // Кнопка отмены редактирования (скрывает окно редактирования)
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    defaultMod.setVisibility(View.VISIBLE);
                    editMod.setVisibility(View.GONE);
                }
            });

            // Кнопка удаления данных
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(getAdapterPosition());
                }
            });
        }
        private void setSpinners() {
            setSpinner(spFormat, Arrays.asList("Онлайн", "Оффлайн"), selectedFormat, selected -> {
                selectedFormat = selected;
                if (selected.equals("Онлайн")) etLinkOrAud.setHint("Ссылка");
                else etLinkOrAud.setHint("Аудитория");
            });
            setSpinner(spType, Arrays.asList("лб", "лк", "пр"), selectedType, selected -> selectedType = selected);
            setSpinner(spDay, Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"), selectedDay, selected -> selectedDay = selected);
            setSpinner(spWeek, Arrays.asList("Каждую неделю", "Числитель", "Знаменатель"), selectedWeek, selected -> selectedWeek = selected);
        }
        private void setDefaultSpinners() {
            selectedFormat = "Онлайн";
            selectedType = "лб";
            selectedDay = "Понедельник";
            selectedWeek = "Каждую неделю";
            setSpinners();
        }
        public void setSpinner(Spinner spinner, List<String> spinnerItems, String selected, Consumer<String> onItemSelected) {
            // Создаем адаптер для Spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(), R.layout.spinner_item, spinnerItems);
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
    }
}
