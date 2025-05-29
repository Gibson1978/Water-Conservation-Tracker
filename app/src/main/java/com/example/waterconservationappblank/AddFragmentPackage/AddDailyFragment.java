package com.example.waterconservationappblank.AddFragmentPackage;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;

import com.example.waterconservationappblank.FirestoreRepo;
import com.example.waterconservationappblank.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.widget.Toast;

public class AddDailyFragment extends Fragment {

    //required empty public constructor
    public AddDailyFragment() {}

    EditText setDateTimeet; //select date to upload usage
    EditText DeleteDailyet; //select date to delete usage
    EditText dailyUsageet; //enter daily water usage
    Calendar calendar; //calendar instance for date selection
    SimpleDateFormat dateFormat; //format for displaying the selected date
    Button dailyUploadbtn; //button to upload usage
    Button dailyDeletebtn; //button to delete usage
    FirestoreRepo repo; // firestore helper class to handle database actions

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate the layout for fragment_add_daily.xml
        View dailyView = inflater.inflate(R.layout.fragment_add_daily, container, false);

        // initialize firestone helper class
        repo = new FirestoreRepo();

        // link UI elements
        setDateTimeet = dailyView.findViewById(R.id.et_txt_add_daily_DT);
        dailyUsageet = dailyView.findViewById(R.id.et_txt_add_daily_usage);
        dailyUploadbtn = dailyView.findViewById(R.id.btn_upload_daily);
        DeleteDailyet = dailyView.findViewById(R.id.et_delete_daily);
        dailyDeletebtn = dailyView.findViewById(R.id.btn_delete_daily);

        // initialize calendar and set date format to (yyyy-mm-dd)
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // set click listener to show date picker when user taps on upload date field
        setDateTimeet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerDaily(setDateTimeet);
            }
        });

        // set click listener to show date picker when user taps on delete date field
        DeleteDailyet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerDaily(DeleteDailyet);
            }
        });

        // upload button logic
        dailyUploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get input value from EditText fields
                String dateInput = setDateTimeet.getText().toString().trim();
                String usageInput = dailyUsageet.getText().toString().trim();

                // convert usage input to double
                double usage = Double.parseDouble(usageInput);

                // validate date input
                if (dateInput.isEmpty()) {
                    setDateTimeet.setError("Please select a date");
                    return;
                }

                // validate usage input
                if (usageInput.isEmpty()) {
                    dailyUsageet.setError("Enter water usage");
                    return;
                }

                // toast of success upload and upload data to firestone
                Toast.makeText(getContext(), "Daily usage uploaded successfully", Toast.LENGTH_SHORT).show();
                repo.uploadUsage(dateInput,usage);
            }
        });

        // delete button logic
        dailyDeletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get selected date to delete
                String deleteDate = DeleteDailyet.getText().toString().trim();

                //validate delete date input
                if (deleteDate.isEmpty()) {
                    DeleteDailyet.setError("Please select a date");
                    return;
                }

                // toast success message and delete data from firestone
                Toast.makeText(getContext(), "Daily usage deleted successfully", Toast.LENGTH_SHORT).show();
                repo.deleteUsage(deleteDate);
            }
        });

        return dailyView;
    }

    // show a date picker dialog and set the selected date into the given EditText
    private void showDateTimePickerDaily(EditText targetEditText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {

            //set the selected date into the calendar object
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Update the EditText with the selected date here
            String formattedDate = dateFormat.format(calendar.getTime());
            targetEditText.setText(formattedDate);

        },
                // set initial date for the picker dialog
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // show the dialog
        datePickerDialog.show();
    }
}