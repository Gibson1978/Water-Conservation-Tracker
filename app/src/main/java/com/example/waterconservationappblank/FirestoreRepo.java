package com.example.waterconservationappblank;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreRepo {

    // interface for callbacl results from firestore queries
    public interface FirestoreCallback {
        void onUsageDataReceived(Map<String, Object> data); // optional: used for future expansion
        void onWeekDataReceived(List<DocumentSnapshot> weekDocs); // used fro weekly dashboard chart
        void onError(String errorMessage); // error handling
    }
    private final FirebaseFirestore db;

    // constructor initializes firestore instance
    public FirestoreRepo() {
        db = FirebaseFirestore.getInstance();
    }

    // Upload or update usage
    public void uploadUsage(String date, double waterUsed) {
        Map<String, Object> data = new HashMap<>();
        data.put("date", date);  // Make sure the field name is lowercase "date"
        data.put("WaterUsage", waterUsed);

        db.collection("DailyUpload")
                .document(date)
                .set(data)
                .addOnSuccessListener(v -> Log.d("Firestore", "Data Saved/Updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error Saving Data", e));
    }

    // Real-time listener for the latest week's data
    public void readWeekByDate(LocalDate anyDateInWeek, FirestoreCallback callback) {
        LocalDate monday = anyDateInWeek.with(DayOfWeek.MONDAY);
        LocalDate previousSunday = monday.minusDays(1);
        LocalDate sunday = monday.plusDays(6);

        Query weeklyQuery = db.collection("DailyUpload")
                .whereGreaterThanOrEqualTo("date", previousSunday.toString())
                .whereLessThanOrEqualTo("date", sunday.toString())
                .orderBy("date");

        weeklyQuery.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null) {
                        callback.onWeekDataReceived(snapshot.getDocuments());
                    } else {
                        callback.onError("No data for selected week");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // Delete record
    public void deleteUsage(String date) {
        db.collection("DailyUpload")
                .document(date)
                .delete()
                .addOnSuccessListener(v -> Log.d("FirestoreRepo", "Record Deleted"))
                .addOnFailureListener(e -> Log.e("FirestoreRepo", "Error deleting document", e));
    }

    //upload or updates monthly water bill data
    public void uploadBills(String monthYear, double price, String AddInfo) {
        Map<String, Object> data = new HashMap<>();
        data.put("Month", monthYear);
        data.put("Monthly Bills", price);  // Make sure the field name is lowercase "date"
        data.put("Additional Info", AddInfo );

        db.collection("MonthlyUpload")
                .document(monthYear)
                .set(data)
                .addOnSuccessListener(v -> Log.d("Firestore", "Data Saved/Updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error Saving Data", e));
    }

    // reads a specific monthly bill docs from firestore
    public void readMonthlyBill(String Date, OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection("MonthlyUpload")
                .document(Date)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // deletes a specific monthly bill document from firestore
    public void deleteMonthly(String date) {
        db.collection("MonthlyUpload")
                .document(date)
                .delete()
                .addOnSuccessListener(v -> Log.d("FirestoreRepo", "Record Deleted"))
                .addOnFailureListener(e -> Log.e("FirestoreRepo", "Error deleting document", e));
    }

}

