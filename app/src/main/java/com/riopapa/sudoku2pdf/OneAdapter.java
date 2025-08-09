package com.riopapa.sudoku2pdf;

import static com.riopapa.sudoku2pdf.ActivityMain.mActivity;
import static com.riopapa.sudoku2pdf.ActivityMain.onePos;
import static com.riopapa.sudoku2pdf.ActivityMain.sudokus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        String s;
        Sudoku su = sudokus.get(position);
        s =  "팀명 : " + su.name; holder.tName.setText(s);
        s = ((su.gridSize == 9) ? "9x9" : "6x6") + " 빈칸 수 : " + su.nbrOfBlank; holder.tBlank.setText(s);
        s = "전체 문제 수 : " + su.nbrOfQuiz; holder.tQuiz.setText(s);
        s = (su.answer) ? "답안지 만듬" : "답안지 안 만듬"; holder.tAnswer.setText(s);
        s = "인쇄 강도 : " + su.opacity; holder.tOpacity.setText(s);
        holder.iMesh.setImageResource((su.mesh == 0) ? R.drawable.mesh0_off :
                            (su.mesh == 1) ? R.drawable.mesh1_top :
                                    (su.mesh == 2) ? R.drawable.mesh2_on :R.drawable.mesh3_top);
        s = "페이지당 문제 수 : " + su.nbrPage; holder.tNbrPage.setText(s);
        holder.tLine.setOnClickListener(v -> {
            onePos = holder.getAdapterPosition();
            Intent intent = new Intent(holder.context, ActivityOneEdit.class);
            mActivity.startActivity(intent);
        });
    }
}