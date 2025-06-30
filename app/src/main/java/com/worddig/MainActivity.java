package com.worddig;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity implements View.OnTouchListener {
    // ゲームデータ
    private final String[] animals = {"CAT", "DOG", "PIG", "BIRD", "FISH", "BEAR", "LION", "FROG", "DUCK", "GOAT", 
                                     "DEER", "WOLF", "FOX", "BAT", "OWL", "BEE", "ANT", "FLY", "RAT", "COW", 
                                     "SHEEP", "HORSE", "RABBIT", "TIGER", "GIRAFFE", "ELEPHANT", "MONKEY", "SNAKE", "TURTLE", "PEACOCK"};
    private final String[] fruits = {"APPLE", "GRAPE", "LEMON", "PEACH", "BERRY", "PLUM", "PEAR", "KIWI", "MANGO", 
                                    "MELON", "CHERRY", "ORANGE", "BANANA", "PAPAYA", "COCONUT", "AVOCADO"};
    
    // ゲーム状態
    private char[][] grid = new char[6][12];
    private List<String> targetWords = new ArrayList<>();
    private List<String> foundWords = new ArrayList<>();
    private int score = 0;
    private boolean isSelecting = false;
    private List<GameCell> selectedCells = new ArrayList<>();
    private GameCell startCell = null;
    
    // UI要素
    private GridLayout gameGrid;
    private TextView scoreLabel;
    private TextView phaseLabel;
    private LinearLayout targetWordsLayout;
    private LinearLayout foundWordsLayout;
    private TextView foundCountLabel;
    private TextView totalCountLabel;
    private GameCell[][] cells = new GameCell[6][12];
    
    // セルクラス
    private class GameCell extends Button {
        int row, col;
        
        GameCell(int row, int col) {
            super(MainActivity.this);
            this.row = row;
            this.col = col;
            
            // スマホ最適化：タッチフレンドリーなサイズ
            int size = getResources().getDisplayMetrics().widthPixels / 13; // 画面幅に合わせて調整
            setLayoutParams(new ViewGroup.LayoutParams(size, size));
            setBackgroundColor(Color.WHITE);
            setTextColor(Color.BLACK);
            setTextSize(12);
            setOnTouchListener(MainActivity.this);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GameCell cell = (GameCell) obj;
            return row == cell.row && col == cell.col;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupUI();
        generateGame();
    }
    
    private void initializeViews() {
        scoreLabel = findViewById(R.id.scoreLabel);
        phaseLabel = findViewById(R.id.phaseLabel);
        gameGrid = findViewById(R.id.gameGrid);
        targetWordsLayout = findViewById(R.id.targetWordsLayout);
        foundWordsLayout = findViewById(R.id.foundWordsLayout);
        foundCountLabel = findViewById(R.id.foundCountLabel);
        totalCountLabel = findViewById(R.id.totalCountLabel);
        
        Button newGameBtn = findViewById(R.id.newGameBtn);
        newGameBtn.setOnClickListener(v -> newGame());
    }
    
    private void setupUI() {
        // 背景グラデーション設定
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        GradientDrawable gradient = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.parseColor("#9333ea"), Color.parseColor("#2563eb")}
        );
        mainLayout.setBackground(gradient);
        
        // グリッド設定（スマホ最適化）
        gameGrid.setColumnCount(12);
        gameGrid.setRowCount(6);
        
        // セル作成
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 12; j++) {
                GameCell cell = new GameCell(i, j);
                cells[i][j] = cell;
                gameGrid.addView(cell);
            }
        }
    }
    
    private void generateGame() {
        // グリッドをクリア
        for (int i = 0; i < 6; i++) {
            Arrays.fill(grid[i], ' ');
        }
        
        // 単語を選択
        List<String> allWords = new ArrayList<>();
        allWords.addAll(Arrays.asList(animals));
        allWords.addAll(Arrays.asList(fruits));
        Collections.shuffle(allWords);
        
        targetWords.clear();
        List<String> shortWords = new ArrayList<>();
        List<String> longWords = new ArrayList<>();
        
        for (String word : allWords) {
            if (word.length() <= 5) {
                shortWords.add(word);
            } else {
                longWords.add(word);
            }
        }
        
        // 短い単語2-3個、長い単語3-4個を追加
        for (int i = 0; i < 3 && i < shortWords.size(); i++) {
            targetWords.add(shortWords.get(i));
        }
        for (int i = 0; i < 4 && i < longWords.size(); i++) {
            targetWords.add(longWords.get(i));
        }
        
        // 単語をグリッドに配置
        Random random = new Random();
        String[] directions = {"horizontal", "vertical", "diagonal"};
        
        for (String word : targetWords) {
            boolean placed = false;
            int attempts = 0;
            
            while (!placed && attempts < 50) {
                String direction = directions[random.nextInt(directions.length)];
                int startRow = random.nextInt(6);
                int startCol = random.nextInt(12);
                
                if (canPlaceWord(word, startRow, startCol, direction)) {
                    placeWordInGrid(word, startRow, startCol, direction);
                    placed = true;
                }
                attempts++;
            }
        }
        
        // 残りのセルをランダム文字で埋める
        String vowels = "AEIOU";
        String consonants = "BCDFGHJKLMNPQRSTVWXYZ";
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 12; j++) {
                if (grid[i][j] == ' ') {
                    String letters = random.nextDouble() < 0.3 ? vowels : consonants;
                    grid[i][j] = letters.charAt(random.nextInt(letters.length()));
                }
            }
        }
        
        foundWords.clear();
        score = 0;
        selectedCells.clear();
        
        updateDisplay();
    }
    
    private boolean canPlaceWord(String word, int startRow, int startCol, String direction) {
        int[] delta = getDirectionDelta(direction);
        int dRow = delta[0];
        int dCol = delta[1];
        
        for (int i = 0; i < word.length(); i++) {
            int row = startRow + i * dRow;
            int col = startCol + i * dCol;
            
            if (row >= 6 || col >= 12 || row < 0 || col < 0) return false;
            if (grid[row][col] != ' ' && grid[row][col] != word.charAt(i)) return false;
        }
        return true;
    }
    
    private void placeWordInGrid(String word, int startRow, int startCol, String direction) {
        int[] delta = getDirectionDelta(direction);
        int dRow = delta[0];
        int dCol = delta[1];
        
        for (int i = 0; i < word.length(); i++) {
            int row = startRow + i * dRow;
            int col = startCol + i * dCol;
            grid[row][col] = word.charAt(i);
        }
    }
    
    private int[] getDirectionDelta(String direction) {
        switch (direction) {
            case "horizontal": return new int[]{0, 1};
            case "vertical": return new int[]{1, 0};
            case "diagonal": return new int[]{1, 1};
            default: return new int[]{0, 1};
        }
    }
    
    private void updateDisplay() {
        // グリッド更新
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 12; j++) {
                cells[i][j].setText(String.valueOf(grid[i][j]));
            }
        }
        
        // スコア更新
        scoreLabel.setText("Score: " + score);
        
        // 目標単語更新
        targetWordsLayout.removeAllViews();
        for (String word : targetWords) {
            TextView wordView = new TextView(this);
            wordView.setText(word);
            wordView.setPadding(16, 8, 16, 8);
            wordView.setTextSize(14);
            
            if (foundWords.contains(word)) {
                wordView.setBackgroundColor(Color.parseColor("#bbf7d0"));
                wordView.setTextColor(Color.parseColor("#166534"));
            } else {
                wordView.setBackgroundColor(Color.parseColor("#f3f4f6"));
                wordView.setTextColor(Color.parseColor("#374151"));
            }
            
            targetWordsLayout.addView(wordView);
        }
        
        // 発見した単語更新
        foundWordsLayout.removeAllViews();
        for (String word : foundWords) {
            TextView wordView = new TextView(this);
            wordView.setText(word + " (+" + (word.length() * 10) + "pts)");
            wordView.setPadding(16, 8, 16, 8);
            wordView.setTextSize(14);
            wordView.setBackgroundColor(Color.parseColor("#dbeafe"));
            wordView.setTextColor(Color.parseColor("#1e40af"));
            foundWordsLayout.addView(wordView);
        }
        
        // カウント更新
        foundCountLabel.setText(String.valueOf(foundWords.size()));
        totalCountLabel.setText(String.valueOf(targetWords.size()));
    }
    
    // タッチイベント処理（スマホ最適化）
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!(v instanceof GameCell)) return false;
        GameCell cell = (GameCell) v;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchStart(cell);
                return true;
            case MotionEvent.ACTION_MOVE:
                // タッチ位置から該当セルを特定
                GameCell moveCell = getCellFromTouch(event.getRawX(), event.getRawY());
                if (moveCell != null) {
                    handleTouchMove(moveCell);
                }
                return true;
            case MotionEvent.ACTION_UP:
                handleTouchEnd();
                return true;
        }
        return false;
    }
    
    private GameCell getCellFromTouch(float x, float y) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 12; j++) {
                GameCell cell = cells[i][j];
                int[] location = new int[2];
                cell.getLocationOnScreen(location);
                
                if (x >= location[0] && x <= location[0] + cell.getWidth() &&
                    y >= location[1] && y <= location[1] + cell.getHeight()) {
                    return cell;
                }
            }
        }
        return null;
    }
    
    private void handleTouchStart(GameCell cell) {
        isSelecting = true;
        startCell = cell;
        selectedCells.clear();
        selectedCells.add(cell);
        updateSelection();
    }
    
    private void handleTouchMove(GameCell cell) {
        if (!isSelecting || startCell == null) return;
        
        List<GameCell> lineCells = getLineCells(startCell.row, startCell.col, cell.row, cell.col);
        selectedCells = lineCells;
        updateSelection();
    }
    
    private void handleTouchEnd() {
        if (selectedCells.size() > 0) {
            checkForWord();
        }
        isSelecting = false;
        startCell = null;
        selectedCells.clear();
        updateSelection();
    }
    
    private List<GameCell> getLineCells(int startRow, int startCol, int endRow, int endCol) {
        List<GameCell> cells = new ArrayList<>();
        int deltaRow = endRow - startRow;
        int deltaCol = endCol - startCol;
        
        if (deltaRow == 0) {
            // 水平
            int step = deltaCol > 0 ? 1 : -1;
            for (int col = startCol; col != endCol + step; col += step) {
                if (col >= 0 && col < 12) {
                    cells.add(this.cells[startRow][col]);
                }
            }
        } else if (deltaCol == 0) {
            // 垂直
            int step = deltaRow > 0 ? 1 : -1;
            for (int row = startRow; row != endRow + step; row += step) {
                if (row >= 0 && row < 6) {
                    cells.add(this.cells[row][startCol]);
                }
            }
        } else if (Math.abs(deltaRow) == Math.abs(deltaCol)) {
            // 対角線
            int rowStep = deltaRow > 0 ? 1 : -1;
            int colStep = deltaCol > 0 ? 1 : -1;
            int steps = Math.abs(deltaRow);
            
            for (int i = 0; i <= steps; i++) {
                int row = startRow + i * rowStep;
                int col = startCol + i * colStep;
                if (row >= 0 && row < 6 && col >= 0 && col < 12) {
                    cells.add(this.cells[row][col]);
                }
            }
        }
        
        return cells;
    }
    
    private void updateSelection() {
        // 全セルの選択状態をリセット
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 12; j++) {
                cells[i][j].setBackgroundColor(Color.WHITE);
            }
        }
        
        // 選択されたセルをハイライト
        for (GameCell cell : selectedCells) {
            cell.setBackgroundColor(Color.parseColor("#93c5fd"));
        }
    }
    
    private void checkForWord() {
        if (selectedCells.size() < 3) return;
        
        StringBuilder selectedLetters = new StringBuilder();
        for (GameCell cell : selectedCells) {
            selectedLetters.append(grid[cell.row][cell.col]);
        }
        
        String word = selectedLetters.toString();
        String reversedWord = new StringBuilder(word).reverse().toString();
        
        String foundWord = null;
        for (String targetWord : targetWords) {
            if (targetWord.equals(word) || targetWord.equals(reversedWord)) {
                foundWord = targetWord;
                break;
            }
        }
        
        if (foundWord != null && !foundWords.contains(foundWord)) {
            foundWords.add(foundWord);
            score += foundWord.length() * 10;
            
            updateDisplay();
            
            // 全単語発見チェック
            if (foundWords.size() == targetWords.size()) {
                phaseLabel.setText("Phase: All words found!");
                new Handler().postDelayed(this::newGame, 1000);
            }
        }
    }
    
    private void newGame() {
        generateGame();
        phaseLabel.setText("Phase: Find words!");
    }
}
