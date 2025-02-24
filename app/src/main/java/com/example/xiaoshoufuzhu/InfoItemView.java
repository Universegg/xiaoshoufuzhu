package com.example.xiaoshoufuzhu;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class InfoItemView extends LinearLayout {
    private ImageView ivIcon;
    private TextView tvTitle;
    private TextView tvValue;
    private EditText etValue;
    private View divider;
    private boolean isEditable = false;

    public InfoItemView(Context context) {
        this(context, null);
    }

    public InfoItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_info_item, this, true);

        ivIcon = findViewById(R.id.iv_icon);
        tvTitle = findViewById(R.id.tv_title);
        tvValue = findViewById(R.id.tv_value);
        divider = findViewById(R.id.divider);

        // 动态添加EditText
        etValue = new EditText(context);
        etValue.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        etValue.setTextSize(16);
        etValue.setVisibility(GONE);

        // 添加到文本容器
        LinearLayout textContainer = (LinearLayout) tvValue.getParent();
        textContainer.addView(etValue, 1);

        // 处理自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InfoItemView);
        try {
            // 图标
            if (a.hasValue(R.styleable.InfoItemView_icon)) {
                ivIcon.setImageResource(a.getResourceId(R.styleable.InfoItemView_icon, 0));
            } else {
                ivIcon.setVisibility(GONE);
            }

            // 标题
            tvTitle.setText(a.getString(R.styleable.InfoItemView_title));

            // 分割线
            if (!a.getBoolean(R.styleable.InfoItemView_showDivider, true)) {
                divider.setVisibility(GONE);
            }
        } finally {
            a.recycle();
        }
    }

    // 设置可编辑模式
    public void setEditable(boolean editable) {
        isEditable = editable;
        tvValue.setVisibility(editable ? GONE : VISIBLE);
        etValue.setVisibility(editable ? VISIBLE : GONE);
        if (editable) {
            etValue.setText(tvValue.getText());
        } else {
            tvValue.setText(etValue.getText());
        }
    }

    // 设置显示值
    public void setValue(String text) {
        String displayText = !TextUtils.isEmpty(text) ? text : "未设置";
        if (isEditable) {
            etValue.setText(displayText);
        } else {
            tvValue.setText(displayText);
        }
    }

    // 获取当前值
    public String getValue() {
        return isEditable ? etValue.getText().toString() : tvValue.getText().toString();
    }

    // 其他辅助方法...
    public void setIcon(int resId) {
        ivIcon.setImageResource(resId);
        ivIcon.setVisibility(VISIBLE);
    }
}