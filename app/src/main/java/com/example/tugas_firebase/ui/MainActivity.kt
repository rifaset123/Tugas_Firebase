package com.example.tugas_firebase.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tugas_firebase.database.Report
import com.example.tugas_firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FieldValue.delete
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val reportCollectionRef = firestore.collection("report")
    private lateinit var executorService: ExecutorService
    private lateinit var binding: ActivityMainBinding
    private var updateId = ""
    private val reportListLiveData: MutableLiveData<List<Report>> by lazy {
        MutableLiveData<List<Report>>()
    }
    private val listReports = mutableListOf<Report>() // Tambahkan list untuk menyimpan data yang akan ditampilkan
    private lateinit var rvAdapter: RvAdapter // Deklarasikan adapter di sini

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        executorService = Executors.newSingleThreadExecutor()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvAdapter = RvAdapter(listReports,
            onItemClick = { report ->
                executorService.execute {
                    // Inside onItemClick in your notesAdapter
                    val position = listReports.indexOf(report)
                    val selectedNote = listReports[position] // Assuming position is the clicked item position
                    val intent = Intent(this@MainActivity, SecondActivity::class.java)
                    intent.putExtra("SELECTED_NOTE", selectedNote)
                    startActivityForResult(intent, 2)
                }
            },
            onItemLongClick = { report ->
                deleteReport(report = report) })

        with(binding){
            recyclerView.apply {
                adapter = rvAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }

        binding.btnNew.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivityForResult(intent, 1)
        }
        observeBudgets()
        getAllBudgets()
    }


    private fun getAllBudgets() {
        observeReportChanges()
    }
    private fun observeBudgets() {
        reportListLiveData.removeObservers(this) // Hapus observer sebelum menambahkan yang baru
        reportListLiveData.observe(this) { report->
            listReports.clear()
            listReports.addAll(report)
            runOnUiThread {
                rvAdapter.notifyDataSetChanged()
            }
        }
    }
    private fun observeReportChanges() {
        reportCollectionRef.addSnapshotListener { snapshots, error->
            if (error != null) {
                Log.d("MainActivity", "Error listening for budget changes: ", error)
                return@addSnapshotListener
            }
            val reports = snapshots?.toObjects(Report::class.java)
            if (reports != null) {
                reportListLiveData.postValue(reports)
            }
        }
    }
    private fun updateReport(report: Report) {
        report.id = updateId
        reportCollectionRef.document(updateId).set(report)
            .addOnFailureListener {
                Log.d("MainActivity", "Error updating budget: ", it)
            }
    }
    private fun deleteReport(report: Report) {
        if (report.id.isEmpty()) {
            Log.d("MainActivity", "Error deleting: budget ID is empty!")
            return
        }
        reportCollectionRef.document(report.id).delete()
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting budget: ", it)
            }
    }
}