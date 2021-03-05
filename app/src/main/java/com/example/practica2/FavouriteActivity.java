package com.example.practica2;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica2.adapter.Adapter;
import com.example.practica2.databases.QuotationContract;
import com.example.practica2.databases.QuotationRoomDatabase;
import com.example.practica2.databases.QuotationSQLiteHelper;
import com.example.practica2.quotation.Quotation;
import com.example.practica2.threads.FavQuotationThread;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FavouriteActivity extends AppCompatActivity implements Adapter.OnItemLongClickListener, Adapter.OnItemClickListener {
    private static QuotationContract.Database prefDB;
    private Adapter adapter;
    private MenuItem menuRemoveAllQuotations;
    private List<Quotation> quotationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        prefDB = QuotationContract.getPreferenceDatabase(this);
        quotationList = new ArrayList<Quotation>();
        adapter = new Adapter(quotationList,this,this);
        FavQuotationThread favQuotationThread = new FavQuotationThread(this,prefDB);
        favQuotationThread.start();
/*
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(manager);
        List<Quotation> list = QuotationSQLiteHelper.getInstance(FavouriteActivity.this).getListAllQuotations();;
        */


            }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fav_menu,menu);
        menuRemoveAllQuotations = menu.findItem(R.id.removeAllQuotesMenu);
        menuRemoveAllQuotations.setVisible(adapter.getItemCount()>0);
        return super.onCreateOptionsMenu(menu);
    }

    public void onLoadQuotation (List<Quotation> quotationList){
        //visi
        this.quotationList.addAll(quotationList);
        menuRemoveAllQuotations.setVisible(this.quotationList != null && this.quotationList.size() > 0);
        //adapter = new Adapter(quotationList,this,this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(manager);

        recyclerView.setAdapter(adapter);


    }



    @Override
    public boolean onItemLongClickListener(Adapter adapter, int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FavouriteActivity.this);
        dialogBuilder.setMessage(R.string.confirm_clear_quotation);
        dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Quotation quotationToRemove = adapter.getPositionQuotation(position);
                removeQuotation(quotationToRemove);
                adapter.removeQuotationPosition(position);
            }


        });
        dialogBuilder.setNegativeButton(R.string.no, null);
        dialogBuilder.show();
        return true;
    }

    @Override
    public void onItemClickListener(Adapter adapter, int position) {
        try {
            getAuthorInfo(adapter.getPositionQuotation(position));
        } catch (IllegalArgumentException ex) {
            Toast.makeText(FavouriteActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException ex) {
            Toast.makeText(FavouriteActivity.this, "The information cannot be obtained",
                    Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.removeAllQuotesMenu:
             AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FavouriteActivity.this);
             dialogBuilder.setMessage(R.string.confirm_clear_all_fav);
             dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     removeAllQuotations();
                     adapter.removeAllQuotations();
                     item.setVisible(false);
                     }

             });
             dialogBuilder.setNegativeButton(R.string.no, null);
             dialogBuilder.show();
             return true;

             default:
                return super.onOptionsItemSelected(item);
            }

        }

    private void removeQuotation (final Quotation quotation){
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (prefDB){
                    case Room:
                        QuotationRoomDatabase.getInstance(FavouriteActivity.this).getQuotationDAO().removeQuotation(quotation);
                        break;
                    case SQLite:
                        QuotationSQLiteHelper.getInstance(FavouriteActivity.this).removeQuotationInDatabase(quotation);
                        break;
                }


            }
        }).start();

    }

    private void removeAllQuotations (){
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (prefDB){
                    case Room:
                        QuotationRoomDatabase.getInstance(FavouriteActivity.this).getQuotationDAO().deleteAllQuotes();

                        break;
                    case SQLite:
                        QuotationSQLiteHelper.getInstance(FavouriteActivity.this).removeAllQuotationsInDatabase();

                        break;
                }
            }
        }).start();


    }


    public void getAuthorInfo(Quotation q) throws UnsupportedEncodingException {
        if (q.getQuoteAuthor() == null || q.getQuoteAuthor().isEmpty()) {
            throw new IllegalArgumentException("The information cannot be obtained(author cannot be null or empty)");
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://en.wikipedia.org/wiki/Special:Search?search=" + URLEncoder.encode(q.getQuoteAuthor(), "UTF-8")));
        startActivity(intent);
    }

    /*
    public ArrayList <Quotation> getMockQuotations() {
        ArrayList<Quotation> list = new ArrayList<>();
        list.add(new Quotation(getString(R.string.quote1),getString(R.string.author1)));
        list.add(new Quotation(getString(R.string.quote2),getString(R.string.author2)));
        list.add(new Quotation(getString(R.string.quote3),getString(R.string.author3)));
        list.add(new Quotation(getString(R.string.quote4),getString(R.string.author4)));
        list.add(new Quotation(getString(R.string.quote5),getString(R.string.author5)));
        list.add(new Quotation(getString(R.string.quote6),getString(R.string.author6)));
        list.add(new Quotation(getString(R.string.quote7),getString(R.string.author7)));
        list.add(new Quotation(getString(R.string.quote8),getString(R.string.author8)));

        return list;
    }
     */
/*
            public List<Quotation> getDatabaseList(){
         new Thread(new Runnable() {
             @Override
             public void run() {
                 switch (prefDB) {
                     case Room:
                         return QuotationRoomDatabase.getInstance(FavouriteActivity.this).getQuotationDAO().getAllQuotationsFromDatabase();
                     case SQLite:
                         return QuotationSQLiteHelper.getInstance(FavouriteActivity.this).getListAllQuotations();
                     default: return QuotationSQLiteHelper.getInstance(this).getListAllQuotations();
                 }
             }
         }).start();
         */
    }








