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
        TextView tName, tBlank, tQuiz, tOpacity, tNbrPage;
        ImageView iMesh;
        SwitchCompat tAnswer;

        ViewHolder(final View itemView) {
            super(itemView);
            context = itemView.getContext();
            tLine = itemView.findViewById(R.id.one_layout);
            tName = itemView.findViewById(R.id.one_name);
            tBlank = itemView.findViewById(R.id.one_blank);
            tQuiz = itemView.findViewById(R.id.one_quiz);
            tOpacity = itemView.findViewById(R.id.one_opacity);
            tNbrPage = itemView.findViewById(R.id.one_nbr_page);
            iMesh = itemView.findViewById(R.id.one_mesh);
            tAnswer = itemView.findViewById(R.id.one_answer);
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

        onePos = holder.getAdapterPosition();
        Sudoku su = sudokus.get(onePos);
        holder.tName.setText(su.name);
        holder.tBlank.setText(String.valueOf(su.blank));
        holder.tQuiz.setText(String.valueOf(su.quiz));
        holder.tNbrPage.setText(String.valueOf(su.nbrPage));
        holder.tAnswer.setChecked(su.answer);

        holder.tLine.setOnClickListener(v -> {
            onePos = holder.getAdapterPosition();
            Intent intent = new Intent(holder.context, ActivityOneEdit.class);
            mActivity.startActivity(intent);
        });
    }
}