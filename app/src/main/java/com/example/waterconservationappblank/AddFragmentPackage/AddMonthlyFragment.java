package com.example.waterconservationappblank.AddFragmentPackage;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waterconservationappblank.FirestoreRepo;
import com.example.waterconservationappblank.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddMonthlyFragment extends Fragment {

    private Button UploadFileButton;
    private Button MonthUploadbtn;
    private Button ViewUpload;
    private Button DeleteUpload;
    private EditText Billset;
    private EditText AdditionalInfoet;
    private EditText DateTimeet;
    private EditText DateView;
    private EditText DeleteDate;
    private TextView showDate;
    private TextView showPrice;
    private TextView showInfo;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private FirestoreRepo repo;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private androidx.cardview.widget.CardView cardview;
    public AddMonthlyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate layout
        View view = inflater.inflate(R.layout.fragment_add_monthly, container, false);
        repo = new FirestoreRepo(); // initialize firestore repository

        // initialize UI elements
        UploadFileButton = view.findViewById(R.id.btn_Upload_receipt);
        MonthUploadbtn = view.findViewById(R.id.btn_upload_monthly);
        Billset = view.findViewById(R.id.et_txt_montly_bills);
        AdditionalInfoet = view.findViewById(R.id.et_additional_info);
        DateTimeet = view.findViewById(R.id.et_DT_monthly);

        ViewUpload = view.findViewById(R.id.btn_view_monthly_upload);
        DateView = view.findViewById(R.id.et_DT_monthly_show);
        showDate = view.findViewById(R.id.tv_date_show);
        showPrice = view.findViewById(R.id.tv_water_bills_show);
        showInfo = view.findViewById(R.id.tv_add_info_show);
        cardview = view.findViewById(R.id.cardview_monthly); // card to show bill info

        DeleteUpload = view.findViewById(R.id.btn_delete_monthly);
        DeleteDate = view.findViewById(R.id.et_delete_monthy);

        // setup calendar and date formatter
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // file picker button
        UploadFileButton.setOnClickListener(v -> openFilePicker());
        initFilePickerLauncher(); // seyup result handler for file picker

        // date picker for input, view and delete
        DateTimeet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerMonthly(DateTimeet);
            }
        });

        DateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerMonthly(DateView);
            }
        });

        DeleteDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerMonthly(DeleteDate);
            }
        });

        // upload monthly bill button
        MonthUploadbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String BillInput = Billset.getText().toString().trim();
                String AddInfoInput = AdditionalInfoet.getText().toString().trim();
                String Date = DateTimeet.getText().toString().trim();

                // convert bill to number
                double BillAmount = Double.parseDouble(BillInput);

                if (BillInput.isEmpty()) {
                    Billset.setError("Please select a date");
                    return;
                }

                if (AddInfoInput.isEmpty()) {
                    AdditionalInfoet.setError("Enter water usage");
                    return;
                }

                if (Date.isEmpty()) {
                    DateTimeet.setError("Enter water usage");
                    return;
                }

                // confirm upload and upload to firestore
                Toast.makeText(getContext(), "Monthly bill uploaded successfully", Toast.LENGTH_SHORT).show();
                repo.uploadBills(Date,BillAmount,AddInfoInput);
            }
        });

        // view uploaded monthly bill info
        ViewUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cardview.getVisibility() == View.GONE)
                    cardview.setVisibility(View.VISIBLE); // show card with info
                String monthYear = DateView.getText().toString().trim(); // E.g., "May 2025"

                if (monthYear.isEmpty()) {
                    //OPTIONAL TOAST
                    DateView.setError("Enter Month-Year");
                    return;
                }

                // retrieve bill from firestore
                repo.readMonthlyBill(monthYear,
                        documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String month = documentSnapshot.getString("Month");
                                Double bills = documentSnapshot.getDouble("Monthly Bills");
                                String info = documentSnapshot.getString("Additional Info");


                                showDate.setText(month);
                                assert bills != null;
                                showPrice.setText(bills.toString());
                                showInfo.setText(info);
                            } else {

                                Toast.makeText(getContext(), "no data found", Toast.LENGTH_SHORT).show();
                                showDate.setText("No data found.");
                                showPrice.setText("");
                                showInfo.setText("");
                            }
                        },
                        e -> {

                            Toast.makeText(getContext(), "error reading data", Toast.LENGTH_SHORT).show();
                            showDate.setText("Error reading data.");
                            showPrice.setText("");
                            showInfo.setText("");
                            Log.e("Firestore", "Read failed", e);
                        }
                );
            }
        });

        // delete a monthly bill record
        DeleteUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deleteDate = DeleteDate.getText().toString().trim();

                if (deleteDate.isEmpty()) {
                    DeleteDate.setError("Please select a date");
                    return;
                }

                // toast deleted message and delete data from firestore
                Toast.makeText(getContext(), "Monthly bill deleted", Toast.LENGTH_SHORT).show();
                repo.deleteMonthly(deleteDate);
            }
        });

        return view;
    }

    // open file picker using intent
    private void openFilePicker(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent); // launch file picker
    }

    // setup file picker launcher and handle result
    private void initFilePickerLauncher(){
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), results -> {
                    if(results.getResultCode() == Activity.RESULT_OK)
                    {
                        Intent data = results.getData();
                        if(data != null && data.getData() != null) {
                            Uri uri = data.getData();
                            Toast.makeText(getContext(), "Selected: " + uri.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    // show date picker dialog and set selected date to EditText
    private void showDateTimePickerMonthly(EditText targetEditText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Format and set selected date
            String formattedDate = dateFormat.format(calendar.getTime());
            targetEditText.setText(formattedDate);

        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show(); //show date picker
    }
}


