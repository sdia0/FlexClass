package com.example.flexclass;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

public class DayAdapter extends BaseAdapter {
    private Context context;
    private DaySchedule daySchedule;
    View vFormat;
    public DayAdapter(Context context, DaySchedule daySchedule) {
        this.context = context;
        this.daySchedule = daySchedule;
    }

    @Override
    public int getCount() {
        return daySchedule.getLessons().size();
    }

    @Override
    public Object getItem(int position) {
        return daySchedule.getLessons().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_item, parent, false);
        }

        // Получаем текущий элемент из списка
        Lesson lesson = daySchedule.getLessons().get(position);

        // Устанавливаем значения для UI-элементов
        ((TextView) convertView.findViewById(R.id.tvTime)).setText(lesson.getTimeStart());
        ((TextView) convertView.findViewById(R.id.tvSubject)).setText(lesson.getTitle());
        ((TextView) convertView.findViewById(R.id.tvType)).setText(lesson.getType());

        // Настраиваем круг с цветом
        View vFormat = convertView.findViewById(R.id.vFormat);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(getColor(lesson.getFormat()));
        vFormat.setBackground(drawable);

        return convertView;
    }

    public int getColor(String format) {
        if (format.equals("Онлайн")) return ContextCompat.getColor(context, R.color.white);
        else return ContextCompat.getColor(context, R.color.yellow);
    }

    public void updateDayTitle(TextView tvDay, String day) {
        tvDay.setVisibility(View.VISIBLE);
        tvDay.setText(day);
    }
}
