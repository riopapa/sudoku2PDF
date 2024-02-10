package com.riopapa.sudoku2pdf;

import static com.riopapa.sudoku2pdf.MainActivity.mActivity;
import static com.riopapa.sudoku2pdf.MainActivity.onePos;
import static com.riopapa.sudoku2pdf.MainActivity.sudokus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.riopapa.sudoku2pdf.Model.Sudoku;

public class OneAdapter extends RecyclerView.Adapter<OneAdapter.ViewHolder> {

    @Override
    public int getItemCount() {
        return sudokus.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        Context context;
        View tLine;
        TextView tName, tBlank, tQuiz, tOpacity, tAnswer,tNbrPage;
        ImageView iMesh;

        ViewHolder(final View itemView) {
            super(itemView);
            context = itemView.getContext();
            tLine = itemView.findViewById(R.id.one_layout);
            tName = itemView.findViewById(R.id.one_name);
            tBlank = itemView.findViewById(R.id.one_blank);
            tQuiz = itemView.findViewById(R.id.one_quiz);
            tOpacity = itemView.findViewById(R.id.one_opacity);
            iMesh = itemView.findViewById(R.id.one_mesh);
            tAnswer = itemView.findViewById(R.id.one_answer);
            tNbrPage = itemView.findViewById(R.id.one_nbr_page);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Sudoku su = sudokus.get(position);
        holder.tName.setText(su.name);
        holder.tBlank.setText(String.valueOf(su.blank));
        holder.tQuiz.setText(String.valueOf(su.quiz));
        holder.tAnswer.setText((su.answer) ? "Yes" : "No");
        holder.tOpacity.setText(String.valueOf(su.opacity));
        holder.iMesh.setImageResource((su.mesh == 0) ? R.drawable.mesh0_off :
                            (su.mesh == 1) ? R.drawable.mesh1_top : R.drawable.mesh2_on);
        holder.tNbrPage.setText(String.valueOf(su.nbrPage));
        holder.tLine.setOnClickListener(v -> {
            onePos = holder.getAdapterPosition();
            Intent intent = new Intent(holder.context, ActivityOneEdit.class);
            mActivity.startActivity(intent);
        });
    }
}