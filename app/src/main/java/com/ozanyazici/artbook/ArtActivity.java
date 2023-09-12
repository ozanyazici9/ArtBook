package com.ozanyazici.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.ozanyazici.artbook.databinding.ActivityArtBinding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;

    //Aktivite sonucu başlatıcı; görsel seçimi sonucu, izin verilmesi sonucu ne olucağını yazmak istiyorsak bunu kullanıyoruz.
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        //MainActivity' den gelen intentleri kontrol ediyorum.
        if(info.equals("new")) {
            //new art //Textleri ve imageView'ı boş olarak ayarlıyoruz.
            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.imageView.setImageResource(R.drawable.selectimagee);
            binding.button.setVisibility(View.VISIBLE);
        } else { //Art detay verilerini gösterme.
            int artId = intent.getIntExtra("artId",1); // eğer artId gelmezse null olmasın diye ne değer atyım diyor defaultValue o.
            binding.button.setVisibility(View.INVISIBLE);

            try {
                //RawQuery old. için sqlStatement yapamıyorum. String dizisi olarak verdiğim artId ? yerine geçicek.
                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()) {
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));
                    //Resim Db de byte dizisi olarak tutulduğu için byte dizisine atıp sonra bitmap e dönüştürüyoruz.
                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }

                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //Kayıt
    public void save(View view) {

        String name = binding.nameText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String year = binding.yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage, 200);
        //küçülttüğümüz resmi sıfır ve birlere çevirdik ve bir byte dizisine attık.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();


        //SQL
        try {

            //Veriler BLOB olarak kaydedilir.
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");
            //sonradan çalıştırılabilecek bir sqlite statement oluşturuyoruz.
            String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES(?, ?, ?, ?)";
            //Sonradan bağlama işlemi olan binding işlemlerini kolay yapmak için sqllitestatement yapıyoruz. Yani aslında sql işlemlerini dinamikleştirmeye yarıyor.
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString); //compile string i alıp database içinde çalıştıracak.
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //tüm aktiviteleri kapat ben dahil, sadece gideceğimi aç diyoruz.
        startActivity(intent);

    }


    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {
        //Resimleri sqlite' a kaydetmek için bitmap formatındaki resimleri küçültüyoruz.
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if(bitmapRatio > 1) {
            //yatay resim
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            //dikey resim
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return image.createScaledBitmap(image,100,100,true);
    }

    public void selectImage(View view) {
        //Android 33+ -> READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            //ContextCompat eğer kullanıcı iznin gerekmediği bir android sürümü kullanıyorsa iznin sorulmamasını sağlıyor. Yani her versiyona uymlu olması için
            //Read_media_ımages izni verilmiş mi cevabı packagemanager içinde veriliyor. Permission_granted izin verilmiş Permission_denied verilmemiş demek.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                //Burası izinin ne için olduğunu açıklamak için kullanılıyor. Ne zaman gösterileceğini android kendi karar veriyor.
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)) {

                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //izin isteme
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                        }
                    }).show();

                } else {
                    //eğer izin verilmemişse burada izin istiyoruz.
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }

            } else {
                //izin verilmişse galeriye gidiyoruz.
                //Intent action pick tutup almak gibi bişiy resim almak için kullanacağız. Daha sonra urı yı yani adresi veriyoruz.
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }

        } else {

            //ContextCompat eğer kullanıcı iznin gerekmediği bir android sürümü kullanıyorsa iznin sorulmamasını sağlıyor. Yani her versiyona uymlu olması için
            //Read_media_ımages izni verilmiş mi cevabı packagemanager içinde veriliyor. Permission_granted izin verilmiş Permission_denied verilmemiş demek.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //Burası izinin ne için olduğunu açıklamak için kullanılıyor. Ne zaman gösterileceğini android kendi karar veriyor.
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //izin isteme
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                        }
                    }).show();

                } else {
                    //eğer izin verilmemişse burada izin istiyoruz.
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

            } else {
                //izin verilmişse galeriye gidiyoruz.
                //Intent action pick tutup almak gibi bişiy resim almak için kullanacağız. Daha sonra urı yı yani adresi veriyoruz.
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }

        }


    }

    private void registerLauncher() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                //eğer kullanıcı bir resim seçtiyse
                if (result.getResultCode() == RESULT_OK){
                    //veriyi alıyoruz.
                    Intent intentFromResult = result.getData();
                    //veri varmı yokmu kontrol ediyoruz çünkü intentFromResult boşda olabilir.
                    if (intentFromResult != null) {
                        //getdata burda seçilen resmin adresini getiriyor.
                       Uri imageData = intentFromResult.getData();
                       //eğer sadece resmi imageview da göstericek olsaydım bunu kullanırdım ama aynı zamnda kaydedeceğim.
                       //binding.imageView.setImageURI(imageData);

                        try {
                            //Uri ı kullanarak resmi bitmap e dönüştüreceğiz.
                            if (Build.VERSION.SDK_INT >= 28) { //bu yöntem 28 ve üzeri api levele sahip olanlarda çalışıyor. o yüzden kontrol yapıyoruz.
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }

            }
            }
        });



        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result) {
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                } else {
                    //permission denied
                    Toast.makeText(ArtActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }


}