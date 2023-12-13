package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.graphics.Color;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    static int blocks;
    static int flags;

    TextView minesTextView;
    ToggleButton toggleButton;

    // 상단 Mines 텍스트 업데이트해주는 메소드
    public void updateMinesText() {
        minesTextView.setText("Mines: " + flags);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TableLayout table;
        table = (TableLayout)findViewById(R.id.tableLayout);

        minesTextView = (TextView) findViewById(R.id.minesTextView);
        toggleButton = findViewById(R.id.toggleButton);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 깃발 모드
                    Toast.makeText(MainActivity.this, "깃발 모드", Toast.LENGTH_SHORT).show();
                } else {
                    // 블록 열기 모드
                    Toast.makeText(MainActivity.this, "블록 열기 모드", Toast.LENGTH_SHORT).show();
                }
            }
        });


        class BlockButton extends Button {
            private int x;
            private int y;
            private boolean mine;
            private boolean flag;
            private int neighborMines;
            private BlockButton[][] buttons;

            public BlockButton(Context context, int x, int y, BlockButton[][] buttons) {
                super(context);
                this.buttons = buttons;

                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                setLayoutParams(layoutParams);

                this.x = x;
                this.y = y;
                this.mine = false;
                this.flag = false;
                this.neighborMines = 0;


                // isChecked 값에 따라 블록 열기 메소드 동작할지 깃발 메소드 동작할지 결정
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (toggleButton.isChecked()) {
                            toggleFlag();
                        } else {
                            breakBlock(buttons);
                        }
                    }
                });
            }

            public void toggleFlag() {
                if (flag) {
                    setText("");
                    flag = false;
                    flags++;
                } else {
                    setText("🚩");
                    flag = true;
                    flags--;
                }
                updateMinesText();
            }

            public boolean breakBlock(BlockButton[][] buttons) {
                if (flag) {
                    return false;
                }
                setClickable(false);
                blocks--;

                // 승리
                if (blocks == 10) {
                    setText(String.valueOf(neighborMines));
                    setBackgroundColor(Color.rgb(255,255,255));
                    setAllBlocksNotClickable(buttons);
                    Toast.makeText(MainActivity.this, "You Win!", Toast.LENGTH_LONG).show();
                    return false;
                }

                // 패배
                if (mine == true) {
                    setAllBlocksNotClickable(buttons);
                    setText("💣");
                    Toast.makeText(MainActivity.this, "You Lose!", Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    setText(String.valueOf(neighborMines));
                    setBackgroundColor(Color.rgb(255,255,255));
                    // 주변에 지뢰가 없을 때 주변 블록도 열리도록 재귀호출하는 조건문 부분
                    if (neighborMines == 0) {
                        int x = this.x;
                        int y = this.y;
                        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
                        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
                        for (int i = 0; i < 8; i++) {
                            int nx = x + dx[i];
                            int ny = y + dy[i];
                            if (nx >= 0 && nx < 9 && ny >= 0 && ny < 9 && buttons[nx][ny].isClickable()) {
                                buttons[nx][ny].breakBlock(buttons);
                            }
                        }
                    }

                    return false;
                }
            }

            // 게임 종료 시 모든 버튼을 클릭 불가 상태로 만들기 위한 메소드
            public void setAllBlocksNotClickable(BlockButton[][] buttons) {
                for (int i = 0; i < buttons.length; i++) {
                    for (int j = 0; j < buttons[i].length; j++) {
                        buttons[i][j].setClickable(false);
                    }
                }
            }

            public void calculateNeighborMines(BlockButton[][] buttons) {
                if (!mine) {
                    int x = this.x;
                    int y = this.y;
                    int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
                    int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

                    for (int i = 0; i < 8; i++) {
                        int nx = x + dx[i];
                        int ny = y + dy[i];
                        if (nx >= 0 && nx < 9 && ny >= 0 && ny < 9 && buttons[nx][ny].mine) {
                            neighborMines++;
                        }
                    }
                }
            }
        }

        BlockButton[][] buttons = new BlockButton[9][9];

        TableRow[] tableRows = new TableRow[9];
        for (int i = 0; i < 9; i++) {
            tableRows[i] = new TableRow(this);
            table.addView(tableRows[i]);
        }

         for (int i = 0; i < 9; i++) {
             for (int j = 0; j < 9; j++) {
                 buttons[i][j] = new BlockButton(this, i, j, buttons);
                 tableRows[i].addView(buttons[i][j]);
             }
         }

        Random random = new Random();
        int minesToPlace = 10;

        while (minesToPlace > 0) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            if (!buttons[row][col].mine) {
                buttons[row][col].mine = true;
                minesToPlace--;
            }
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                buttons[i][j].calculateNeighborMines(buttons);
            }
        }

        blocks = 81;
        flags = 10;

        updateMinesText();
    }

}