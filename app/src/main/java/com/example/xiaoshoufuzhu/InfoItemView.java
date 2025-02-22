package com.example.xiaoshoufuzhu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;

public class InfoItemView extends LinearLayout {

    private ImageView ivIcon;
    private TextView tvTitle;
    private TextView tvValue;
    private View divider;

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
        setupViews();
        parseAttributes(context, attrs);
    }

    private void setupViews() {
        ivIcon = findViewById(R.id.iv_icon);
        tvTitle = findViewById(R.id.tv_title);
        tvValue = findViewById(R.id.tv_value);
        divider = findViewById(R.id.divider);

        // 设置默认值防止显示空内容
        tvValue.setText("未设置");
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InfoItemView);

        try {
            // 图标属性
            if (a.hasValue(R.styleable.InfoItemView_icon)) {
                Drawable icon = a.getDrawable(R.styleable.InfoItemView_icon);
                ivIcon.setImageDrawable(icon);
                ivIcon.setVisibility(VISIBLE);
            } else {
                ivIcon.setVisibility(GONE);
            }

            // 标题属性
            if (a.hasValue(R.styleable.InfoItemView_title)) {
                tvTitle.setText(a.getString(R.styleable.InfoItemView_title));
            }

            // 分割线可见性
            if (a.hasValue(R.styleable.InfoItemView_showDivider)) {
                boolean showDivider = a.getBoolean(R.styleable.InfoItemView_showDivider, true);
                divider.setVisibility(showDivider ? VISIBLE : GONE);
            }
        } finally {
            a.recycle();
        }
    }

    // region 公开方法

    /**
     * 设置显示值（自动处理空值）
     * @param text 要显示的文字内容
     */
    public void setInfoText(CharSequence text) {
        tvValue.setText(!TextUtils.isEmpty(text) ? text : "未设置");
    }

    /**
     * 设置图标资源
     * @param resId 图标资源ID（传0时隐藏图标）
     */
    public void setIcon(int resId) {
        if (resId != 0) {
            ivIcon.setImageResource(resId);
            ivIcon.setVisibility(VISIBLE);
        } else {
            ivIcon.setVisibility(GONE);
        }
    }

    /**
     * 设置标题文字
     * @param text 标题文字
     */
    public void setTitle(CharSequence text) {
        tvTitle.setText(text);
    }

    /**
     * 设置是否显示分割线
     * @param visible 是否显示分割线
     */
    public void setDividerVisible(boolean visible) {
        divider.setVisibility(visible ? VISIBLE : GONE);
    }
    // endregion

    // region 样式控制方法

    /**
     * 设置值文字颜色
     * @param color 颜色资源ID
     */
    public void setValueTextColor(int color) {
        tvValue.setTextColor(getResources().getColor(color));
    }

    /**
     * 设置标题文字颜色
     * @param color 颜色资源ID
     */
    public void setTitleTextColor(int color) {
        tvTitle.setTextColor(getResources().getColor(color));
    }

    /**
     * 设置值文字大小
     * @param unit 单位（TypedValue.COMPLEX_UNIT_*）
     * @param size 文字大小
     */
    public void setValueTextSize(int unit, float size) {
        tvValue.setTextSize(unit, size);
    }
    // endregion
}