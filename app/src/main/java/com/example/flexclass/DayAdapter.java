package com.example.flexclass;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
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

import java.util.ArrayList;
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
    static String week;
    String day;
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
        holder.selectedFormat = lesson.getFormat();
        holder.selectedTime = lesson.getTime();
        holder.etSubject.setText(lesson.getTitle());
        holder.etLinkOrAud.setText(lesson.getAudOrLink(lesson.getFormat()));
        holder.selectedDay = lesson.getDay();
        holder.selectedWeek = lesson.getWeek();
        holder.selectedType = lesson.getType();

        if (holder.selectedFormat.equals("Онлайн")) {
            holder.setSelectedButton(holder.bOnline, context);
            holder.resetButton(holder.bOffline, context);
            holder.etLinkOrAud.setHint("Ссылка");
            holder.btnPaste.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.etLinkOrAud.getLayoutParams();
            params.weight = 6;
            holder.etLinkOrAud.setLayoutParams(params);
        }
        else {
            holder.setSelectedButton(holder.bOffline, context);
            holder.resetButton(holder.bOnline, context);
            holder.etLinkOrAud.setHint("Аудитория");
            holder.btnPaste.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.etLinkOrAud.getLayoutParams();
            params.weight = 7;
            holder.etLinkOrAud.setLayoutParams(params);
        }

        switch (holder.selectedType) {
            case "лк":
                holder.setSelectedButton(holder.bLk, context);
                holder.resetButton(holder.bLb, context);
                holder.resetButton(holder.bPr, context);
                break;
            case "пр":
                holder.setSelectedButton(holder.bPr, context);
                holder.resetButton(holder.bLk, context);
                holder.resetButton(holder.bLb, context);
                break;
            default:
                holder.setSelectedButton(holder.bLb, context);
                holder.resetButton(holder.bLk, context);
                holder.resetButton(holder.bPr, context);
                break;
        }

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
        this.day = day;
    }

    // ViewHolder для хранения View
    public class MyViewHolder extends RecyclerView.ViewHolder {
        List<String> times = new ArrayList<>(Arrays.asList(
                "10:00-11:20",
                "11:30-12:50",
                "13:00-14:20",
                "15:00-16:20",
                "16:30-17:50",
                "18:00-19:20"
        ));
        TextView tvTime, tvSubject, tvType;
        View vFormat;
        LinearLayout defaultMod, editMod;
        EditText etLinkOrAud, etSubject;
        Spinner spDay, spWeek, spTime;
        String selectedType, selectedFormat, selectedTime, selectedDay, selectedWeek;
        Button ok, delete;
        ImageView exit;
        Button bOnline, bOffline, bLb, bLk, bPr;
        ImageButton btnPaste, btnClear;
        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Инициализация
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvType = itemView.findViewById(R.id.tvType);
            vFormat = itemView.findViewById(R.id.vFormat);

            spWeek = itemView.findViewById(R.id.spWeek_item);
            spDay = itemView.findViewById(R.id.spDay_item);
            spTime = itemView.findViewById(R.id.spTime_item);

            etLinkOrAud = itemView.findViewById(R.id.etLinkOrAud_item);
            btnPaste = itemView.findViewById(R.id.btnPaste_item);
            btnClear = itemView.findViewById(R.id.btnClear_item);
            etSubject = itemView.findViewById(R.id.etSubject_item);

            defaultMod = itemView.findViewById(R.id.defaultMode);
            editMod = itemView.findViewById(R.id.editMode_item);

            ok = itemView.findViewById(R.id.btnOk_item);
            delete = itemView.findViewById(R.id.btnDelete_item);
            exit = itemView.findViewById(R.id.btnExit_item);

            bOnline = itemView.findViewById(R.id.bOnline_item);
            bOffline = itemView.findViewById(R.id.bOffline_item);
            bLb = itemView.findViewById(R.id.bLb_item);
            bLk = itemView.findViewById(R.id.bLk_item);
            bPr = itemView.findViewById(R.id.bPr_item);

            bOnline.setOnClickListener(v -> {
                selectedFormat = "Онлайн";
                setSelectedButton(bOnline, context);
                resetButton(bOffline, context);
                etLinkOrAud.setHint("Ссылка");
                btnPaste.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) etLinkOrAud.getLayoutParams();
                params.weight = 6;
                etLinkOrAud.setLayoutParams(params);
            });

            bOffline.setOnClickListener(v -> {
                selectedFormat = "Оффлайн";
                setSelectedButton(bOffline, context);
                resetButton(bOnline, context);
                btnPaste.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) etLinkOrAud.getLayoutParams();
                params.weight = 7;
                etLinkOrAud.setLayoutParams(params);
            });

            bLb.setOnClickListener(v -> {
                selectedType = "лб";
                setSelectedButton(bLb, context);
                resetButton(bLk, context);
                resetButton(bPr, context);
            });

            bLk.setOnClickListener(v -> {
                selectedType = "лк";
                setSelectedButton(bLk, context);
                resetButton(bLb, context);
                resetButton(bPr, context);
            });

            bPr.setOnClickListener(v -> {
                selectedType = "пр";
                setSelectedButton(bPr, context);
                resetButton(bLk, context);
                resetButton(bLb, context);
            });

            btnClear.setOnClickListener(v -> {
                etLinkOrAud.setText("");
            });

            btnPaste.setOnClickListener(v -> {
                // Получаем буфер обмена
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    // Получаем текст из буфера обмена
                    CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
                    if (text != null) {
                        // Вставляем текст в EditText
                        etLinkOrAud.setText(text);
                    } else {
                        Toast.makeText(context, "Буфер обмена пуст", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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
                    String subject = etSubject.getText().toString();
                    String linkOrAud = etLinkOrAud.getText().toString();

                    // Проверяем, что все поля заполнены
                    if (subject.isEmpty() || linkOrAud.isEmpty()) {
                        Toast.makeText(context, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Создаем новый объект урока с обновленными данными
                    Lesson updatedLesson = new Lesson(
                            selectedTime,
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
                                if ((!selectedWeek.equals(week) && !selectedWeek.equals("Каждую неделю"))) {
                                    daySchedule.getLessons().remove(position);
                                    notifyItemRemoved(position);
                                }
                                if (!selectedDay.equals(day)) {
                                    Toast.makeText(context, "Обновите экран", Toast.LENGTH_SHORT).show();
                                }
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
        public void setSelectedButton(Button button, Context context) {
            button.setTextColor(ContextCompat.getColor(context, R.color.blue));
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }
        public void resetButton(Button button, Context context) {
            button.setTextColor(ContextCompat.getColor(context, R.color.white));
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
        }
        private void setSpinners() {
            setSpinner(spTime, times, selectedTime, selected -> selectedTime = selected);
            setSpinner(spDay, Arrays.asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"), selectedDay, selected -> selectedDay = selected);
            setSpinner(spWeek, Arrays.asList("Каждую неделю", "Числитель", "Знаменатель"), selectedWeek, selected -> selectedWeek = selected);
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
