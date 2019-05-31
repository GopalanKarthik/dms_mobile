package com.dealermanagmentsystem.ui.enquiry.tasks;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dealermanagmentsystem.R;
import com.dealermanagmentsystem.adapter.SpinnerCustomAdapter;
import com.dealermanagmentsystem.data.model.common.CommonResponse;
import com.dealermanagmentsystem.data.model.common.Record;
import com.dealermanagmentsystem.data.model.tasks.ActivityTypeResponse;
import com.dealermanagmentsystem.ui.base.BaseActivity;
import com.dealermanagmentsystem.utils.ui.DMSToast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.dealermanagmentsystem.constants.Constants.EXTRA_LEAD_ID;

public class TaskCreateActivity extends BaseActivity implements ITasksCreateView {

    @BindView(R.id.sp_activity_type)
    Spinner spActivityType;
    @BindView(R.id.sp_assign)
    Spinner spUser;
    @BindView(R.id.et_summary)
    EditText etSummary;
    @BindView(R.id.et_note)
    EditText etNote;
    @BindView(R.id.et_follow_up_date)
    TextView txtFollowUpDate;
    Activity activity;
    TasksCreatePresenter presenter;
    int activityTypeId = -1;
    int userId = -1;
    private Calendar myCalendar;
    int leadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);

        activity = TaskCreateActivity.this;
        ButterKnife.bind(this);
        setStatusBarColor(getResources().getColor(R.color.bg));
        showTile("Activity Create");
        showBackButton();
        myCalendar = Calendar.getInstance();

        final Intent intent = getIntent();
        if (intent != null) {
            leadId = Integer.valueOf(intent.getStringExtra(EXTRA_LEAD_ID));
        }

        presenter = new TasksCreatePresenter(this);
        presenter.getActivityType(activity);
        presenter.getUsers(activity);

    }

    @OnClick(R.id.submit_task) //ButterKnife uses.
    public void createActivity() {
        final String strFollowUpDate = txtFollowUpDate.getText().toString();
        final String strSummary = etSummary.getText().toString();
        final String strNote = etNote.getText().toString();
        presenter.createTask(activity, strSummary, strNote, userId, activityTypeId, strFollowUpDate, leadId);

    }

    @OnClick(R.id.et_follow_up_date) //ButterKnife uses.
    public void selectFollowUpDate() {
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "yyyy-MM-dd";//In which you need put here "MM/dd/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                txtFollowUpDate.setText(sdf.format(myCalendar.getTime()));

            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(activity, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    public void onSuccessCreateTasks(CommonResponse response) {
        if (TextUtils.isEmpty(response.getError())) {
            DMSToast.showLong(activity, "Activity created successfully");
            finish();
        } else {
            DMSToast.showLong(activity, "Failed to create activity");
        }
    }

    @Override
    public void onSuccessActivityType(ActivityTypeResponse response) {
        final List<Record> activityRecords = response.getRecords();

        SpinnerCustomAdapter customAdapter = new SpinnerCustomAdapter(activity, activityRecords);
        spActivityType.setAdapter(customAdapter);

        int pos = 0;
        spActivityType.setSelection(pos);

        spActivityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                activityTypeId = activityRecords.get(i).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onSuccessUsers(ActivityTypeResponse response) {
        final List<Record> userRecords = response.getRecords();

        SpinnerCustomAdapter customAdapter = new SpinnerCustomAdapter(activity, userRecords);
        spUser.setAdapter(customAdapter);

        int pos = 0;
        spUser.setSelection(pos);

        spUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                userId = userRecords.get(i).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onError(String message) {
        DMSToast.showLong(activity, message);
    }
}