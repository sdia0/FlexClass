package com.example.flexclass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeWithIconsCallback extends ItemTouchHelper.Callback {
    private final DayAdapter mAdapter;
    private final Drawable mDeleteIcon;
    private final Drawable mEditIcon;
    private final int mIconMargin;
    private float mDx;  // Сохраняем смахивание для анимации
    private boolean mIsSwiping = false; // Флаг смахивания
    Context context;
    public Rect mDeleteIconBounds;
    public Rect mEditIconBounds;
    private int mPosition = -1; // Позиция элемента для удаления
    public SwipeWithIconsCallback(DayAdapter adapter, Context context) {
        this.mAdapter = adapter;
        this.context = context;
        mDeleteIcon = ContextCompat.getDrawable(context, R.drawable.delete);
        mEditIcon = ContextCompat.getDrawable(context, R.drawable.edit);
        mDeleteIcon.setTint(Color.WHITE);
        mEditIcon.setTint(Color.WHITE);
        mIconMargin = (int) context.getResources().getDimension(R.dimen.icon_margin);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;  // Мы не реализуем перетаскивание
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mPosition = viewHolder.getAdapterPosition();
        mIsSwiping = true;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (mIsSwiping) {
            mDx = dX;  // Сохраняем значение смахивания
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        // Ограничиваем смахивание до определенной границы (например, 200 пикселей)
        if (dX < -290) {
            dX = -290;
        }

        // Рисуем иконки в зависимости от направления смахивания
        if (dX < 0) { // Смахивание влево (удаление)
            drawDeleteIcon(c, viewHolder, dX);
            drawEditIcon(c, viewHolder, dX);
        }

        // Вызываем метод для отрисовки сдвигаемого элемента
        viewHolder.itemView.setTranslationX(dX); // Сдвигаем элемент на dX
    }

    private void drawDeleteIcon(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        int itemHeight = viewHolder.itemView.getHeight();
        int iconWidth = 100;
        int iconHeight = 100;

        // Центрируем иконку по вертикали
        int iconTop = viewHolder.itemView.getTop() + (itemHeight - iconHeight) / 2;
        int iconBottom = iconTop + iconHeight;

        // Располагаем иконку для удаления справа
        int iconLeft = viewHolder.itemView.getRight() - mIconMargin - iconWidth;
        int iconRight = iconLeft + iconWidth;

        mDeleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        mDeleteIcon.draw(c);
        mDeleteIconBounds = new Rect(iconLeft, iconTop, iconRight, iconBottom);
    }

    private void drawEditIcon(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        int itemHeight = viewHolder.itemView.getHeight();
        int iconWidth = 100;
        int iconHeight = 100;

        // Центрируем иконку по вертикали
        int iconTop = viewHolder.itemView.getTop() + (itemHeight - iconHeight) / 2;
        int iconBottom = iconTop + iconHeight;

        // Располагаем иконку для редактирования справа
        int iconLeft = viewHolder.itemView.getRight() - mIconMargin - iconWidth - 116;
        int iconRight = iconLeft + iconWidth;

        mEditIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        mEditIcon.draw(c);
        mEditIconBounds = new Rect(iconLeft, iconTop, iconRight, iconBottom);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        mIsSwiping = false; // Завершаем смахивание
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Разрешаем смахивание влево
        int swipeFlags = ItemTouchHelper.LEFT;
        return makeMovementFlags(0, swipeFlags);  // 0 - перетаскивание запрещено
    }

    // Обработка нажатий на иконки (удаление и редактирование)
    public void onDeleteClicked(int position) {
        Toast.makeText(context, "Удаление", Toast.LENGTH_SHORT).show();
        mAdapter.removeItem(mPosition);
    }

    public void onEditClicked(int position) {
        Toast.makeText(context, "Редактирование", Toast.LENGTH_SHORT).show();
        mAdapter.editItem(mPosition);
    }

    // Метод для проверки нажатия на иконки
    public void checkForIconClick(float dX, int position) {
        // Если мы смахнули элемент, проверим, было ли нажатие на одну из иконок
        if (mDeleteIconBounds.contains((int) dX, (int) dX) && dX < -100) {
            onDeleteClicked(position);
        } else if (mEditIconBounds.contains((int) dX, (int) dX) && dX < -200) {
            onEditClicked(position);
        }
    }
}


