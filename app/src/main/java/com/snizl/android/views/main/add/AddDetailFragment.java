package com.snizl.android.views.main.add;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.views.base.BaseFragment;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddDetailFragment extends BaseFragment {
    @BindView(R.id.tv_page_title) TextView tvPageTitle;
    @BindView(R.id.et_title) EditText etTitle;
    @BindView(R.id.et_description) EditText etDescription;
    @BindView(R.id.tv_date) TextView tvDate;
    @BindView(R.id.tv_time) TextView tvTime;
    @BindView(R.id.duration_container) LinearLayout mDurationContainer;
    @BindView(R.id.sp_day) Spinner spDay;
    @BindView(R.id.sp_hour) Spinner spHour;
    @BindView(R.id.sp_minute) Spinner spMinute;

    @OnClick(R.id.tv_date)
    public void selectDate() {
        mDatePickerDialog.show();
    }

    @OnClick(R.id.tv_time)
    public void selectTime() {
        mTimePickerDialog.show();
    }

    private Context mContext;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_detail, container, false);
        ButterKnife.bind(this, view);

        initData();
        initUI();

        return view;
    }

    private void initData() {
        mContext = getContext();
        dateFormatter = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.US);
        timeFormatter = new SimpleDateFormat("h:mm a", Locale.US);
    }

    private void initUI() {
        tvPageTitle.setText(AppController.add_type == 0 ? "Details of your Deal" : "Details of your Promotion");

        etTitle.setText(AppController.title);
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AppController.title = s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        etDescription.setText(AppController.description);
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AppController.description = s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        setDateTimeField();

        mDurationContainer.setVisibility(AppController.add_type == 0 ? View.VISIBLE : View.INVISIBLE);

        if (AppController.add_type == 0) {
            setDurationField();
        }

    }

    private void setDateTimeField() {
        Calendar newCalendar = Calendar.getInstance();

        if (AppController.date != null) {
            tvDate.setText(dateFormatter.format(AppController.date.getTime()));
        }

        if (AppController.time != null) {
            tvTime.setText(timeFormatter.format(AppController.time));
        }

        mDatePickerDialog = new DatePickerDialog(mContext, R.style.AppTheme_Dialog, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tvDate.setText(dateFormatter.format(newDate.getTime()));
                AppController.date = newDate;
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        mTimePickerDialog = new TimePickerDialog(mContext, R.style.AppTheme_Dialog, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hr, int min) {
                Time time = new Time(hr, min, 0);
                tvTime.setText(timeFormatter.format(time));
                AppController.time = time;
            }
        }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);
    }

    private void setDurationField() {
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_duration_item);
        dayAdapter.setDropDownViewResource(R.layout.spinner_duration_item);
        for (int i = 0; i < 8; i++) {
            dayAdapter.add(i + " days");
        }
        spDay.setAdapter(dayAdapter);
        spDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                AppController.days = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ArrayAdapter < String > hourAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_duration_item);
        hourAdapter.setDropDownViewResource(R.layout.spinner_duration_item);
        for (int i = 0; i < 24; i++) {
            hourAdapter.add(i + " hrs");
        }
        spHour.setAdapter(hourAdapter);
        spHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                AppController.hours = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_duration_item);
        minuteAdapter.setDropDownViewResource(R.layout.spinner_duration_item);
        for (int i = 0; i < 60; i+=5) {
            minuteAdapter.add(i + " mins");
        }
        spMinute.setAdapter(minuteAdapter);
        spMinute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                AppController.minutes = position * 5;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spDay.setSelection(AppController.days);
        spHour.setSelection(AppController.hours);
        spMinute.setSelection(AppController.minutes / 5);
    }
}