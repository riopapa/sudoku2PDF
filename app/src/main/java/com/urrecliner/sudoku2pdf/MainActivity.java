package com.urrecliner.sudoku2pdf;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Context mContext;
    int xPixels, yPixels;
    int [][] suTable;
    String [] suTables, ansTables;
    String suTableResult;
    int level = 5;
    Random random;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.w("start"," ------------------");

        mContext = getApplicationContext();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        random = new Random(System.currentTimeMillis());

        suTables = new String[20];
        ansTables = new String[20];
        for (int i = 0; i < 20; i++) {
            make_answerTable();
            ansTables[i] = suArray2Str();
            make_sudokoTable();
            suTables[i] = suArray2Str();
        }
        MakePDF makePDF = new MakePDF();
        makePDF.createPDF(suTables, ansTables);


//            int [][] check = str2suArray(suTables[i]);
//            for (int r = 0; r < 9; r++)
//                for (int c = 0; c < 9; c++)
//                    if (suTable[r][c] != check [r][c])
//                        Log.w("check", "R:"+r+" C"+c+" "+suTable[r][c]+" : "+check[r][c]);
//        calculatePixels();
//        show_suTable();
    }

    int nextThree() { // 0 ~ 2
        return random.nextInt(3);
    }
    int nextRanged(int range) { // 0 ~ range
        return random.nextInt(range);
    }

    void calculatePixels() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        try {
            assert windowManager != null;
            windowManager.getDefaultDisplay().getMetrics(dm);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        xPixels = dm.widthPixels;
        yPixels = dm.heightPixels;
    }

    void make_answerTable() {

        suTable = new int[9][9];

        long duration;
        duration = System.currentTimeMillis();
        while (hasZeroBlock()) {
            for (int blockX = 0; blockX < 3; blockX++) {
                for (int blockY = 0; blockY < 3; blockY++) {
                    if (suTable[blockX*3][blockY*3] == 0) {
                        if (!isFillThisBlockOK(blockX, blockY)) {
                            Log.w("zero","fillOneBlock failed "+blockX+", "+blockY);
                            clearBlock(nextThree(), nextThree());
                        }
                    }
                }
            }
        }
        duration = System.currentTimeMillis() - duration;
        dumpTable("all DONE "+ duration);
        Log.w("DONE", suTableResult);

    }

    private boolean hasZeroBlock() {
        for (int x = 0;  x < 9; x +=3)
            for (int y = 0; y < 9; y +=3)
                if (suTable[x][y] == 0)
                    return true;
        return false;
    }

    private void clearBlock(int blockX, int blockY) {
//        Log.w("clearing", "Block "+blockX+":"+blockY);
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                suTable[blockX*3+x][blockY*3+y] = 0;
    }

    private boolean isFillThisBlockOK(int blockX, int blockY) {
        int reRun = 3;
        do {
            if (buildOneBlock(blockX, blockY))
                return true;
            reRun--;
        } while (reRun > 0);
        clearBlock(blockX, blockY);
        return  false;
    }

    private boolean buildOneBlock(int blockX, int blockY) {
        int loop = 18;
        clearBlock(blockX, blockY);
//        Log.w("x1","Building x "+blockX+","+blockY+" Block");

        int x3 = blockX * 3, y3 = blockY * 3;
        ArrayList<Integer> oneLine = new ArrayList<>();
        for (int i = 0; i < 9; i++)
            oneLine.add(i+1);
        int nbr = 1;
        while (oneLine.size() > 0 && nbr < 10 && loop > 0) {
            int pos = nextRanged(oneLine.size());
            int val = oneLine.get(pos) - 1;
            int x = val / 3; int y = val % 3;
//            Log.w("buildOneBlock", x+" "+y+" pos "+pos+" size "+oneLine.size());
            if (suTable[x3+x][y3+y] == 0 && noDupVertical(x3+x, nbr) && noDupHorizontal(y3+y, nbr)) {
                suTable[x3+x][y3+y] = nbr;
                oneLine.remove(pos);
                nbr++;
                loop = 81;
            }
            else
                loop--;
//            if (loop == 0)
//                dumpTable("x3 "+x3+" "+(x3+x)+", y3 "+y3+" "+(y3+y)+" nbr="+nbr);
        }
        if (loop == 0)
            return false;
//        dumpTable("xx:"+blockX+"  yy:"+blockY+" Created ..");
        return true;
    }

    boolean noDupVertical(int x,int nbr) {
        for (int y = 0; y < 9; y++)
            if (suTable[x][y] == nbr)
                return false;
        return true;
    }

    boolean noDupHorizontal(int y,int nbr) {
        for (int x = 0; x < 9; x++)
            if (suTable[x][y] == nbr)
                return false;
        return true;
    }

    private void dumpTable(String s) {
        Log.w("r",s);
        Log.w("r"," ");
        String bar = "\n -  -  - | -  -  - | -  -  -";
        suTableResult = "  \n"+bar;
        for (int y = 0; y < 9; y++) {
            String str = "\n";
            for (int x = 0; x < 9; x++) {
                str += " "+suTable[x][y] + " ";
                if (x ==2 || x == 5)
                    str += "|";
            }
            suTableResult += str;
            if (y ==2 || y == 5)
                suTableResult += bar;
        }
        suTableResult += bar;
        Log.w("r",suTableResult);
    }

    String suArray2Str () {
        String result = "";
        for (int row = 0; row < 9; row++) {
            String s = "";
            for (int col = 0; col < 9; col++)
                s += suTable[row][col]+";";
            result += s +":";
        }
        return result;
    }

    void make_sudokoTable() {
        int count = level * 8 + nextRanged(9);
        while (count > 0) {
            int val = nextRanged(81);
            int x = val % 9; int y = val / 9;
            if (suTable[x][y] != 0) {
                suTable[x][y] = 0;
                count--;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
