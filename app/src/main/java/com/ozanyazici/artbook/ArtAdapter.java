package com.ozanyazici.artbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ozanyazici.artbook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    ArrayList<Art> artArrayList;

    public ArtAdapter(ArrayList<Art> artArrayList) {
        this.artArrayList = artArrayList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Bir Artholder oluşturmamız ve layout ile biribirine bağlamamız bekleniyor burada. Biz bindingi kullanarak bağlama yapıyoruz.

        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("artId",artArrayList.get(holder.getAdapterPosition()).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }

    //Recyclerview xml ini bağlayacağız.
    public class ArtHolder extends RecyclerView.ViewHolder {
        //Görünüm Tutucum
        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}// Ozetle onCreateViewHolder da xml' le /layout ile kodu bağlayıp ordaki görünümü almak için ArtHolder ın kurucusuna gönderiyorum.
//Görünümü binding de tutup holder.binding.recyclerViewTextView diyerek görünüme ulaşaıp textini istediğim gibi ayarlayabiliyorum.
