package com.urrecliner.sudoku2pdf;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Context mContext;
    int [][] answerTable;
    int [][] blankTable;
    int [][] solveTable;

    String [] blankTables;
    String [] answerTables;
    String suTableResult;
    int blanked = 0;    // blanked cell count
    int solved = 0;     // solved cell count (should be same with blanked
    int level = 5;
    int retrySum = 0;
    int looped = 0;
    int blankSum = 0;
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
        Long duration = System.currentTimeMillis();
        blankTables = new String[20];
        answerTables = new String[20];
        int sudokuCount = 0;
        while (sudokuCount < 20) {
            looped++;
            Log.w("process","Proccessing Loop:"+sudokuCount);
            make_answerTable(); // result in [] answerTable
//            dumpTable("GENERATED LOOP "+ sudokuCount, answerTable);
            int retryCount = 0;
            blanked = 1234; solved = 5678;
            while (blanked != solved){
//                Log.w("blanked","blanked:"+blanked+", solved="+solved+" so reBLANKING err:"+retryCount);
                blank_sudokuTable();
//                dumpTable("BLANKED LOOP "+ sudokuCount +" blank="+blanked, blankTable);
                solve_sudokuTable();
//                dumpTable("SOLVED "+ sudokuCount+" solved="+solved+" err="+retryCount, solveTable);
                retryCount++;
                if (retryCount > 10)
                    break;
            }
            retrySum += retryCount;
            if (blanked == solved) {
                blankSum += blanked;
//                int diffCount = 0;
//                for (int x = 0; x < 9; x++)
//                    for (int y = 0; y < 9; y++)
//                        if (answerTable[x][y] != solveTable[x][y]) diffCount++;
//
//                if (diffCount > 0) {
//                    dumpTable("DIFFERENT ....... ans "+diffCount, answerTable);
//                    dumpTable("DIFFERENT ....... sol "+diffCount, solveTable);
//                    dumpTable("DIFFERENT ....... blk "+diffCount, blankTable);
//                }
                Log.w("MAKE LOOP", "@@@@@ GOOD @@@@@@@@@@@  " + blanked + " vs " + solved + " sudokuCount " + sudokuCount+" retry:"+retryCount);
                answerTables[sudokuCount] = suArray2Str(answerTable);
                blankTables[sudokuCount] = suArray2Str(blankTable);
                sudokuCount++;
            }
        }
        duration = System.currentTimeMillis() - duration;

        String statistics = "\nGenerated Sudoku = "+sudokuCount+"\nGeneration loop ="+looped+"\nRetried ="+ retrySum +"\nGeneration time ="+(((float) duration)/1000f)+" secs."+"\nLEVEL = "+level+" avr blanked ="+(blankSum/sudokuCount)+"\n..";
        Toast.makeText(mContext,statistics, Toast.LENGTH_LONG).show();
        Log.w("DONE",statistics);

        MakePDF makePDF = new MakePDF();
        makePDF.createPDF(blankTables, answerTables);
    }

    int nextThree() { // 0 ~ 2
        return random.nextInt(3);
    }
    int nextRanged(int range) { // 0 ~ range
        return random.nextInt(range);
    }

//    void calculatePixels() {
//        DisplayMetrics dm = new DisplayMetrics();
//        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
//        try {
//            assert windowManager != null;
//            windowManager.getDefaultDisplay().getMetrics(dm);
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//        xPixels = dm.widthPixels;
//        yPixels = dm.heightPixels;
//    }

    void make_answerTable() {

        answerTable = new int[9][9];

        while (hasZeroBlock(answerTable)) {
            for (int blockX = 0; blockX < 3; blockX++) {
                for (int blockY = 0; blockY < 3; blockY++) {
                    if (answerTable[blockX*3][blockY*3] == 0) {
                        if (!isFillThisBlockOK(blockX, blockY)) {
//                            Log.w("zero","fillOneBlock failed "+blockX+", "+blockY);
                            clearBlock(nextThree(), nextThree());
                        }
                    }
                }
            }
        }
    }

    private boolean hasZeroBlock(int [][] tbl) {
        for (int x = 0;  x < 9; x +=3)
            for (int y = 0; y < 9; y +=3)
                if (tbl[x][y] == 0)
                    return true;
        return false;
    }

    private void clearBlock(int blockX, int blockY) {
//        Log.w("clearing", "Block "+blockX+":"+blockY);
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                answerTable[blockX*3+x][blockY*3+y] = 0;
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
            if (answerTable[x3+x][y3+y] == 0 && noDupVertical(x3+x, nbr) && noDupHorizontal(y3+y, nbr)) {
                answerTable[x3+x][y3+y] = nbr;
                oneLine.remove(pos);
                nbr++;
                loop = 81;
            }
            else
                loop--;
//            if (loop == 0)
//                dumpTable("x3 "+x3+" "+(x3+x)+", y3 "+y3+" "+(y3+y)+" nbr="+nbr);
        }
        return loop != 0;
//        dumpTable("xx:"+blockX+"  yy:"+blockY+" Created ..");
    }

    boolean noDupVertical(int x,int nbr) {
        for (int y = 0; y < 9; y++)
            if (answerTable[x][y] == nbr)
                return false;
        return true;
    }

    boolean noDupHorizontal(int y,int nbr) {
        for (int x = 0; x < 9; x++)
            if (answerTable[x][y] == nbr)
                return false;
        return true;
    }

    private void dumpTable(String s, int [][] tbl) {
        Log.w("r",s);
        String bar0  = "\n   0  1  2 | 3  4  5 | 6  7  8";
        String bar   = "\n   -  -  - | -  -  - | -  -  -";
        suTableResult = "  "+bar0+bar;
        for (int y = 0; y < 9; y++) {
            String str = "\n"+y+" ";
            for (int x = 0; x < 9; x++) {
                str += " "+tbl[x][y] + " ";
                if (x ==2 || x == 5)
                    str += "|";
            }
            suTableResult += str;
            if (y ==2 || y == 5)
                suTableResult += bar;
        }
        suTableResult += bar + bar0;
        Log.w("r",suTableResult);
    }

    String suArray2Str (int [][] tbl) {
        String result = "";
        for (int row = 0; row < 9; row++) {
            String s = "";
            for (int col = 0; col < 9; col++)
                s += tbl[row][col]+";";
            result += s +":";
        }
        return result;
    }

    final static int DIFFICULTY = 21;
    void blank_sudokuTable() {

        blankTable = new int[9][9];
        System.arraycopy(answerTable, 0, blankTable, 0, answerTable.length);
//        blankTable = copyTable(answerTable);
//        dumpTable("COPY answer to blank, here is initial blank table ", blankTable);
        blanked = 0;
        int target = level + level;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (nextRanged(DIFFICULTY) < target) {
                    blankTable[x][y] = 0;
                    blanked++;
                }
            }
        }
//        dumpTable("BLANK GENERATED +++++++++++ "+blanked, blankTable);
    }

    int [][] copyTable (int [][] srcTable) {
        int [][] tgtTable = new int [9][9];
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                tgtTable[x][y] = srcTable[x][y];
        return tgtTable;
    }

    void solve_sudokuTable() {
        solveTable = new int [9][9];
//        System.arraycopy( blankTable, 0, solveTable, 0, blankTable.length);
        solveTable = copyTable(blankTable);
//        dumpTable("BEGIN TO SOLVE, it should be same with blank ",solveTable);
        int [][][] workTable = new int [9][9][9];
        solved = 0;
        // fill workTable with impossible number verification code
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (solveTable[x][y] == 0) {
                    // fill x based
                    for (int xPos = 0; xPos < 9; xPos++) {
                        if (solveTable[xPos][y] != 0)
                            workTable[x][y][solveTable[xPos][y]-1] = 1;
                    }
                    for (int yPos = 0; yPos < 9; yPos++) {
                        if (solveTable[x][yPos] != 0)
                            workTable[x][y][solveTable[x][yPos]-1] = 1;
                    }
                    int xb = (x / 3) * 3;
                    int yb = (y / 3) * 3;
                    for (int yp = 0; yp < 3; yp++) {
                        for (int xp = 0; xp < 3; xp++) {
                            if (solveTable[xb+xp][yb+yp] != 0)
                                workTable[x][y][solveTable[xb+xp][yb+yp]-1] = 1;
                        }
                    }
                }
            }
        }
//        resulting(solTable, workTable);
        while (hasSolTableZero(solveTable)) {

            int pos = nextUniqueCell(solveTable, workTable);
            if (pos == 0)
                break;
            int x = pos % 9; int y = pos / 9;
            int nbr = 100;
            for (int z = 0; z < 9; z++) {
                if (workTable[x][y][z] == 0) {
                    nbr = z + 1;
                    break;
                }
            }
//            Log.w("found","x:"+x+" y:"+y+" from "+solTable[x][y]+ " > "+nbr+" ---");
            solveTable[x][y] = nbr;
            solved++;
            // notify other workTable cells to remove this nbr
            for (int yp = 0; yp < 9; yp++)
                workTable[x][yp][nbr-1] = 1;
            for (int xp = 0; xp < 9; xp++)
                workTable[xp][y][nbr-1] = 1;
            int xb = (x / 3) * 3;
            int yb = (y / 3) * 3;
            for (int yp = 0; yp < 3; yp++)
                for (int xp = 0; xp < 3; xp++)
                    workTable[xb+xp][yb+yp][nbr-1] = 1;
//            resulting(solTable, workTable);
        }
    }

    boolean hasSolTableZero(int [][] solTable) {
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                if (solTable[x][y] == 0)
                    return true;
        return false;
    }

    int nextUniqueCell(int [][] solTable, int [][][] workTable) {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (solTable[x][y] == 0) {
                    int cnt = 0;
                    for (int z = 0; z < 9; z++) {
                        cnt += workTable[x][y][z];
                    }
                    if (cnt == 8)
                        return y * 9 + x;
                }
            }
        }
        return 0;
    }

    void resulting(int [][] solTable, int [][][] workTable) {
        Log.w("A","");
        Log.w("A","");
        Log.w("A","");
        int count8 = 0;
//        dumpTable("SOLTABLE TABLE ",solTable);
        int [][] result = new int [9][9];
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (solTable[x][y] == 0) {
                    int cnt = 0;
                    for (int z = 0; z < 9; z++) {
                        cnt += workTable[x][y][z];
                    }
                    result [x][y] = cnt;
                    if (cnt == 8) {
                        count8++;
//                        Log.w("found  "+count8, "888 x:" + x + " y:" + y);
                    }
                }
                else
                    result[x][y] = 0;
            }
        }
//        dumpTable("RESULT TABLE ",result);
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
