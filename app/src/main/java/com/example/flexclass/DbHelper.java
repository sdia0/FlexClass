package com.example.flexclass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {


    public DbHelper(@Nullable Context context) {
        super(context, "schedule.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table lessons(" +
                "_id INTEGER primary key autoincrement," +
                "time TEXT not null," +
                "format TEXT not null," +
                "title TEXT not null," +
                "type TEXT," +
                "day TEXT not null," +
                "week TEXT not null," +
                "aud TEXT," +
                "link TEXT)");

        // Начало транзакции для вставки данных
        /*db.beginTransaction();
        try {
            for (Lesson lesson : getLessons()) {
                ContentValues values = new ContentValues();
                values.put("time", lesson.getTime());
                values.put("format", lesson.getFormat());
                values.put("title", lesson.getTitle());
                values.put("type", lesson.getType());
                values.put("day", lesson.getDay());
                values.put("week", lesson.getWeek());
                values.put("aud", lesson.getAud());
                values.put("link", lesson.getLink());
                db.insert("lessons", null, values);
            }
            db.setTransactionSuccessful(); // Подтвердить транзакцию
        }  catch (Exception e) {
            Log.e("DB_ERROR", "Error while inserting data: " + e.getMessage());
        } finally {
            db.endTransaction(); // Завершить транзакцию
        }*/
    }

    private List<Lesson> getLessons() {
        // Здесь вы можете получить данные для каждого дня недели
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

        return lessons;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists lessons");
        onCreate(db);
    }

    public List<Lesson> getData() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from lessons", null);
        List<Lesson> lessons = new ArrayList<>();

        while (cursor.moveToNext()) {
            Lesson lesson = new Lesson(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
            lesson.setId(cursor.getInt(0));
            lessons.add(lesson);
        }
        return lessons;
    }

    public Lesson getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from lessons where _id=?", new String[]{id + ""});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Lesson lesson = new Lesson(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8)
                );
                lesson.setId(cursor.getInt(0));
                return lesson;
            }
        }
        return null;
    }

    public List<Lesson> getScheduleForDay(String week, String day) {
        List<Lesson> lessons = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM lessons WHERE day = ? AND (week = ? OR week = 'Каждую неделю')", new String[]{day, week});

        if (cursor.moveToFirst()) {
            do {
                Lesson lesson = new Lesson(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8)
                );
                lesson.setId(cursor.getInt(0));
                lessons.add(lesson);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lessons;
    }

    public Boolean insertData(Lesson lesson) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("time", lesson.getTime());
        content.put("format", lesson.getFormat());
        content.put("title", lesson.getTitle());
        content.put("type", lesson.getType());
        content.put("day", lesson.getDay());
        content.put("week", lesson.getWeek());
        content.put("aud", lesson.getAud());
        content.put("link", lesson.getLink());

        long result = db.insert("lessons", null, content);
        Cursor cursor = db.rawQuery("SELECT _id, day, title FROM lessons", null);
        while (cursor.moveToNext()) {
            Log.d("Database", "_id: " + cursor.getInt(0) + ", day: " + cursor.getString(1) + ", title: " + cursor.getString(2));
        }
        cursor.close();
        return result == -1 ? false : true;
    }

    public Boolean updateData(Lesson lesson) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("time", lesson.getTime());
        content.put("format", lesson.getFormat());
        content.put("title", lesson.getTitle());
        content.put("type", lesson.getType());
        content.put("day", lesson.getDay());
        content.put("week", lesson.getWeek());
        content.put("aud", lesson.getAud());
        content.put("link", lesson.getLink());

        long result = db.update("lessons", content, "_id=?", new String[]{lesson.getId() + ""});
        return result == -1 ? false : true;
    }

    public Boolean deleteData(Lesson lesson) {
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete("lessons", "_id=?", new String[]{lesson.getId() + ""});
        return result == -1 ? false : true;
    }
}