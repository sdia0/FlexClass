package com.example.flexclass;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.MyViewHolder> {

    private Context context;
    private DaySchedule daySchedule;

    // Конструктор для передачи контекста и данных
    public DayAdapter(Context context, DaySchedule daySchedule) {
        this.context = context;
        this.daySchedule = daySchedule;
    }

    // Создание ViewHolder
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item, parent, false);
        return new MyViewHolder(view);
    }

    // Привязка данных к ViewHolder
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Lesson lesson = daySchedule.getLessons().get(position);

        holder.tvTime.setText(lesson.getTimeStart());
        holder.tvSubject.setText(lesson.getTitle());
        holder.tvType.setText(lesson.getType());

        // Настраиваем круг с цветом
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(getColor(lesson.getFormat()));
        holder.vFormat.setBackground(drawable);

        holder.tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToast("до " + lesson.getTimeEnd());
            }
        });

        holder.vFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToast(lesson.getFormat());
            }
        });

        holder.tvSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lesson.getFormat().equals("Онлайн")) makeToast("Долгое нажатие откроет ссылку");
            }
        });
        holder.tvType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lesson.getFormat().equals("Онлайн")) makeToast("Долгое нажатие откроет ссылку");
            }
        });
        holder.tvSubject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Ваш код для обработки долгого нажатия
                if (lesson.getFormat().equals("Онлайн")) Toast.makeText(v.getContext(), "Долгое нажатие!", Toast.LENGTH_SHORT).show();
                else makeToast(lesson.getAud());
                return true; // Возвращаем true, чтобы событие было обработано
            }
        });
        holder.tvType.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (lesson.getFormat().equals("Онлайн")) Toast.makeText(v.getContext(), "Долгое нажатие!", Toast.LENGTH_SHORT).show();
                else makeToast(lesson.getAud());
                return true; // Возвращаем true, чтобы событие было обработано
            }
        });
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
    public void makeToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // ViewHolder для хранения View
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvSubject, tvType;
        View vFormat;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvType = itemView.findViewById(R.id.tvType);
            vFormat = itemView.findViewById(R.id.vFormat);
        }
    }
}
