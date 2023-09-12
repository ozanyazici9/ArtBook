package com.ozanyazici.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ozanyazici.artbook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        artArrayList = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter = new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);

        //menunun gözükmesi için toolbar ekledik.
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        getData();

    }

    //Datbasedeki verileri alıp bir listeye attık çünkü recyclerviewde göstericez. İd sini aynı isimli kayıt olabilr diye çektik.
    //Aynı zamanda listede sanat eserine basıldığında tüm verileri getirileceği için id almak daha mantıklı.
    private void getData() {

        try {

            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts", null);
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                Art art = new Art(name,id);
                artArrayList.add(art);
            }
            //yeni veri geldiğinde recyclerview e eklenmesi için. Burada veri seti değişti haberin olsun diyoruz.
            artAdapter.notifyDataSetChanged();

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Menu layoutunu koda,Activitemize  bağlıyoruz.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Menu bağlamanın kendine özel ınflater' ı var onu kullanıyoruz.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.art_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Menudeki opsiyonlardan biri seçildiğinde ne olucak onu yazıyoruz.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Hangi opsiyon seçilmiş onu kontrol ediyoruz.
        if (item.getItemId() == R.id.add_art) {
            Intent intent = new Intent(this, ArtActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }
}