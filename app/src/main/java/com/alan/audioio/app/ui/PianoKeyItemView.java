/*
 * Copyright (c) 2019-present AlanWang4523 <alanwang4523@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alan.audioio.app.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alan.audioio.R;

/**
 * Author: AlanWang4523.
 * Date: 2020/10/17 18:34.
 * Mail: alanwang4523@gmail.com
 */
public class PianoKeyItemView extends RelativeLayout {

    private OnClickListener mOnClickListener;
    private ObjectAnimator mSmallAnimator;
    private ObjectAnimator mBigAnimator;
    private TextView textView;
    public PianoKeyItemView(Context context) {
        super(context);
        initView(null, 0);
    }

    public PianoKeyItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs, 0);
    }

    public PianoKeyItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, defStyleAttr);
    }

    private void initView(AttributeSet attrs, int defStyle) {
        LayoutInflater.from(getContext()).inflate(R.layout.piano_key_item_layout, this);

        float mRoundRadius = 20.0f;
        int fillColor = this.getResources().getColor(R.color.key_A);
        String text = "";
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PianoKeyItemView, defStyle, 0);
            mRoundRadius = a.getDimension(R.styleable.PianoKeyItemView_civ_corner_radius, mRoundRadius);
            fillColor = a.getColor(R.styleable.PianoKeyItemView_civ_fill_color, fillColor);
            text = a.getString(R.styleable.PianoKeyItemView_civ_text);
            a.recycle();
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(mRoundRadius);
        drawable.setColor(fillColor);
        this.setBackground(drawable);

        textView = this.findViewById(R.id.tv_key_name);
        textView.setText(text);
    }

    public String getKeyName() {
        return textView.getText().toString();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSmallAnimation();
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(v);
                }
            }
        });
    }

    private void showSmallAnimation() {
        if (mBigAnimator != null) {
            mBigAnimator.cancel();
        }
        if (mSmallAnimator == null) {
            createSmallAnimator();
        } else {
            mSmallAnimator.cancel();
        }
        mSmallAnimator.start();
    }

    private void showBigAnimation() {
        if (mSmallAnimator != null) {
            mSmallAnimator.cancel();
        }
        if (mBigAnimator == null) {
            createBigAnimator();
        } else {
            mBigAnimator.cancel();
        }
        mBigAnimator.start();
    }

    private void createSmallAnimator() {
        mSmallAnimator = ObjectAnimator.ofPropertyValuesHolder(this,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.95f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.95f),
                PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.5f));
        mSmallAnimator.setDuration(50);
        mSmallAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showBigAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void createBigAnimator() {
        mBigAnimator = ObjectAnimator.ofPropertyValuesHolder(this,
                PropertyValuesHolder.ofFloat("scaleX", 0.95f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 0.95f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0.5f, 1f));
        mSmallAnimator.setDuration(100);
    }
}
