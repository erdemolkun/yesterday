package com.protel.yesterday.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.protel.yesterday.R;
import com.protel.yesterday.data.AppData;
import com.protel.yesterday.util.DegreeUtils;
import com.protel.yesterday.util.WundergroundUtils;

/**
 * Created by erdemmac on 06/11/15.
 */
public class WeatherView extends RelativeLayout {

    public WeatherView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    View mRoot;

    private void initialize(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflater.inflate(R.layout.layout_weather_row, this, true);
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeatherView, defStyle, 0);
//        a.recycle();
    }


    public void setInfo(double minVal, double maxVal, double dayValue, boolean isCelsius,
                        String icon, String weatherCond) {

        icon = WundergroundUtils.mapIconIfNeeded(icon);
        int resid = getResources().getIdentifier(icon, "drawable", getContext().getPackageName());

        TextView tvMax = (TextView) mRoot.findViewById(R.id.tv_row_degree_max);
        TextView tvMin = (TextView) mRoot.findViewById(R.id.tv_row_degree_min);
        TextView tvDay = (TextView) mRoot.findViewById(R.id.tv_row_degree);
        TextView tvInfo = (TextView) mRoot.findViewById(R.id.tv_row_info);
        ImageView ivInfo = (ImageView) mRoot.findViewById(R.id.iv_row_info);

        tvDay.setText(getWeatherText(isCelsius, dayValue));
        tvMax.setText(getWeatherText(isCelsius, maxVal));
        tvMin.setText(getWeatherText(isCelsius, minVal));

        tvInfo.setText(weatherCond);

        if (resid != 0) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), resid);
            Drawable wrapDrawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(getContext(), android.R.color.white));
            ivInfo.setImageDrawable(wrapDrawable);
        }
    }

    public void setInfo(String header) {
        TextView tvHeader = (TextView) mRoot.findViewById(R.id.tv_row_header);
        tvHeader.setText(header);
    }

    private SpannableString getWeatherText(boolean isCelcius, double tempval) {

        double realTempConverted = tempval;
        if (isCelcius && AppData.isFahrenheit()) {
            realTempConverted = DegreeUtils.celciusToFahrenheit(tempval);
        } else if (!isCelcius && !AppData.isFahrenheit()) {
            realTempConverted = DegreeUtils.fahrenheitToCelcius(tempval);
        }
        String degreeText = (int) realTempConverted + "";
        String allText = (int) realTempConverted + " " + getContext().getString(R.string.degree_sign) + "" + (AppData.isFahrenheit() ? "F" : "C");
        SpannableString spannableString = new SpannableString(allText);
        spannableString.setSpan(new RelativeSizeSpan(2f), 0, degreeText.length(), 0);
        return spannableString;

    }

    public void addFancyAnim() {

        BaseSpringSystem mSpringSystem = SpringSystem.create();
        // Create the animation spring.
//        final Spring mScaleSpring = mSpringSystem.createSpring().setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(20, 2));
        final Spring mScaleSpring = mSpringSystem.createSpring().setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(20, 2));

        // Add a listener to the spring when the Activity resumes.
        mScaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float mappedValueScale = transition((float) spring.getCurrentValue(), 1, 0.93f);
                WeatherView.this.setScaleX(mappedValueScale);
                WeatherView.this.setScaleY(mappedValueScale);
            }
        });

        // Add an OnTouchListener to the root view.
        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        // When pressed start solving the spring to 1.
                        mScaleSpring.setEndValue(1);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // When released start solving the spring to 0.
                        mScaleSpring.setEndValue(0);
                        break;
                }
                return true;
            }
        });
    }

    private float transition(float progress, float startValue, float endValue) {
        return (float) SpringUtil.mapValueFromRangeToRange(progress, 0, 1, startValue, endValue);
    }


    public void applyFancyAnim() {

        BaseSpringSystem mSpringSystem = SpringSystem.create();
        // Create the animation spring.

        final Spring mScaleSpring = mSpringSystem.createSpring().setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(20, 2));

        // Add a listener to the spring when the Activity resumes.
        mScaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringActivate(Spring spring) {
                super.onSpringActivate(spring);
                WeatherView.this.setEnabled(false);
            }

            @Override
            public void onSpringUpdate(Spring spring) {
                float mappedValueScale = transition((float) spring.getCurrentValue(), 1, 0.93f);
                WeatherView.this.setScaleX(mappedValueScale);
                WeatherView.this.setScaleY(mappedValueScale);
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
                if (spring.getCurrentValue() > 0.99f) {
                    mScaleSpring.setEndValue(0);
                } else {
                    WeatherView.this.setEnabled(true);
                }

            }
        });
        mScaleSpring.setEndValue(1);
    }

}
