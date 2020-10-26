package com.game.magictower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.game.magictower.Game.Status;
import com.game.magictower.res.Assets;
import com.game.magictower.res.GameGraphics;
import com.game.magictower.res.GlobalSoundPool;
import com.game.magictower.res.LiveBitmap;
import com.game.magictower.res.MonsterData;
import com.game.magictower.res.TowerDimen;
import com.game.magictower.res.TowerMap;
import com.game.magictower.util.FileUtil;
import com.game.magictower.widget.BitmapButton;
import com.game.magictower.widget.BitmapButton.onClickListener;
import com.game.magictower.widget.GameScreen;
import com.game.magictower.widget.GameView;

public class GameActivity extends Activity implements GameScreen {
    
    private static final String LOG_TAG = "GameActivity";
    
    private GameGraphics graphics;
    private GameView gameView;
    
    private GlobalSoundPool soundPool;
    private Assets assets;
    
    private Game currentGame;
    private Player player;
    private HashMap<Integer, LiveBitmap> imageMap;
    private Dialog dialog;
    private Forecast forecast;
    private Battle battle;
    private JumpFloor jumpFloor;
    private Shop shop;
    
    private boolean animFlag = true;
    private static boolean showMsgFlag = false;
    private static String msgInfo;
    
    protected static Intent getIntent(Context context, boolean load){
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra("load", load);
        return intent;
    }
    
    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (animFlag) {
                imageMap = assets.animMap0;
            } else {
                imageMap = assets.animMap1;
            }
            animFlag = !animFlag;
        }
    };
    
    private static final int MSG_ID_REMOVE_MSG = 1;
    
    private static final int MSG_DELAY_REMOVE_MSG = 1000;
    
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_REMOVE_MSG){
                showMsgFlag = false;
            }
            super.handleMessage(msg);
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        graphics = GameGraphics.getInstance();
        gameView = new GameView(this, graphics, this);
        setContentView(gameView);
        createButtons();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = GlobalSoundPool.getInstance(this);
        assets = Assets.getInstance();
        currentGame = Game.getInstance();
        player = currentGame.player;
        dialog = new Dialog(currentGame);
        forecast = new Forecast(currentGame);
        battle = new Battle(currentGame);
        jumpFloor = new JumpFloor(currentGame);
        resetGame();
        boolean load = getIntent().getBooleanExtra("load", false);
        if (load) {
            loadGame();
        }
        imageMap = assets.animMap0;
        timer.schedule(timerTask, 1000, 500);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (BitmapButton button: activeButtons){
            if (button.onTouch(ev)){
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }
    
    private void resetGame() {
        currentGame.newGame();
    }
    
    private void loadGame() {
        currentGame.fromString(FileUtil.read(this, "game"));
        currentGame.player.fromString(FileUtil.read(this, "player"));
        currentGame.npcInfo.fromString(FileUtil.read(this, "npc"));
        currentGame.mapFromString(FileUtil.read(this, "tower"));
    }
    
    private void saveGame() {
        FileUtil.write(this, "game", currentGame.toString());
        FileUtil.write(this, "player", currentGame.player.toString());
        FileUtil.write(this, "npc", currentGame.npcInfo.toString());
        FileUtil.write(this, "tower", currentGame.mapToString());
    }
    
    private ArrayList<BitmapButton> activeButtons = new ArrayList<BitmapButton>();
    private onClickListener btnClickListener = new onClickListener() {
        @Override
        public void onClicked(BitmapButton btn) {
            switch (currentGame.status) {
            case Playing:
                playBtnKey(btn.getId());
                break;
            case Fighting:
                break;
            case Shopping:
                shop.onBtnKey(btn.getId());
                break;
            case Dialoguing:
                dialog.onBtnKey(btn.getId());
                break;
            case Looking:
                forecast.onBtnKey(btn.getId());
                break;
            case Jumping:
                jumpFloor.onBtnKey(btn.getId());
                break;
            }
        }
        
        private void playBtnKey(int btnId){
            switch (btnId) {
            case BitmapButton.ID_UP:
                if (currentGame.status == Status.Playing) {
                    if (player.getPosY() - 1 < 11 && player.getPosY() - 1 >= 0) {
                        player.setToward(3);
                        interaction(player.getPosX(), player.getPosY() - 1);
                    }
                }
                break;
            case BitmapButton.ID_LEFT:
                if (currentGame.status == Status.Playing) {
                    if (player.getPosX() - 1 < 11 && player.getPosX() - 1 >= 0) {
                        player.setToward(0);
                        interaction(player.getPosX() - 1, player.getPosY());
                    }
                }
                break;
            case BitmapButton.ID_RIGHT:
                if (currentGame.status == Status.Playing) {
                    if (player.getPosX() + 1 < 11 && player.getPosX() + 1 >= 0) {
                        player.setToward(2);
                        interaction(player.getPosX() + 1, player.getPosY());
                    }
                }
                break;
            case BitmapButton.ID_DOWN:
                if (currentGame.status == Status.Playing) {
                    if (player.getPosY() + 1 < 11 && player.getPosY() + 1 >= 0) {
                        player.setToward(1);
                        interaction(player.getPosX(), player.getPosY() + 1);
                    }
                }
                break;
            case BitmapButton.ID_QUIT:
                finish();
                break;
            case BitmapButton.ID_NEW:
                resetGame();
                displayMessage("重新开始");
                break;
            case BitmapButton.ID_SAVE:
                saveGame();
                displayMessage("数据已保存！");
                break;
            case BitmapButton.ID_READ:
                loadGame();
                displayMessage("数据已读取！");
                break;
            case BitmapButton.ID_LOOK:
                if (currentGame.isHasForecast) {
                    forecast.show();
                }
                break;
            case BitmapButton.ID_JUMP:
                if (currentGame.isHasJump) {
                    jumpFloor.show();
                }
                break;
            case BitmapButton.ID_OK:
                //for test
                currentGame.currentFloor++;
                if (currentGame.currentFloor > 21) {
                    currentGame.currentFloor = 0;
                }
                currentGame.player.move(TowerMap.initPos[currentGame.currentFloor][0], TowerMap.initPos[currentGame.currentFloor][1]);
                break;
            }
        }
    };
    
    private void createButtons() {
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_UP, "↑"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_LEFT, "←"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_RIGHT, "→"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_DOWN, "↓"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_QUIT, "Q"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_NEW, "R"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_SAVE, "S"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_READ, "A"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_LOOK, "L"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_JUMP, "J"));
        activeButtons.add(BitmapButton.create(graphics, BitmapButton.ID_OK, "OK"));
        for (BitmapButton button: activeButtons){
            button.setOnClickListener(btnClickListener);
        }
    }
    
    private void drawActiveButtons(GameGraphics graphics, Canvas canvas){
        for (BitmapButton button: activeButtons){
            button.onPaint(canvas);
        }
    }
    
    @Override
    public void updateUI(GameGraphics graphics, Canvas canvas) {
        if (currentGame==null){
            Log.w(LOG_TAG, "updateUI called before game instance created.");
            return ;
        }
        drawActiveButtons(graphics, canvas);
        drawInfoPanel(graphics, canvas);
        drawTower(graphics, canvas);
        
        switch(currentGame.status) {
        case Playing:
            break;
        case Fighting:
            battle.draw(graphics, canvas);
            break;
        case Shopping:
            shop.draw(graphics, canvas);
            break;
        case Dialoguing:
            dialog.draw(graphics, canvas);
            break;
        case Looking:
            forecast.draw(graphics, canvas);
            break;
        case Jumping:
            jumpFloor.draw(graphics, canvas);
            break;
        }
        
        drawMsg(graphics, canvas);
     }
    
    private void drawInfoPanel(GameGraphics graphics, Canvas canvas) {
        graphics.drawText(canvas, player.getLevel() + "", TowerDimen.LEVEL_LEFT, TowerDimen.LEVEL_TOP);
        graphics.drawText(canvas, player.getHp() + "", TowerDimen.HP_LEFT, TowerDimen.HP_TOP);
        graphics.drawText(canvas, player.getAttack() + "", TowerDimen.ATTACK_LEFT, TowerDimen.ATTACK_TOP);
        graphics.drawText(canvas, player.getDefend() + "", TowerDimen.DEFEND_LEFT, TowerDimen.DEFEND_TOP);
        graphics.drawText(canvas, player.getMoney() + "", TowerDimen.MONEY_LEFT, TowerDimen.MONEY_TOP);
        graphics.drawText(canvas, player.getExp() + "", TowerDimen.EXP_LEFT, TowerDimen.EXP_TOP);

        graphics.drawText(canvas, player.getYkey() + "", TowerDimen.YKEY_LEFT, TowerDimen.YKEY_TOP);
        graphics.drawText(canvas, player.getBkey() + "", TowerDimen.BKEY_LEFT, TowerDimen.BKEY_TOP);
        graphics.drawText(canvas, player.getRkey() + "", TowerDimen.RKEY_LEFT, TowerDimen.RKEY_TOP);
        
        graphics.drawText(canvas, currentGame.currentFloor + "", TowerDimen.FLOOR_LEFT, TowerDimen.FLOOR_TOP);
    }
    
    private void drawTower(GameGraphics graphics, Canvas canvas) {
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                int id = currentGame.lvMap[currentGame.currentFloor][x][y];
                LiveBitmap bitmap = imageMap.get(id);
                graphics.drawBitmap(canvas, bitmap, TowerDimen.TOWER_LEFT + (y + 6) * TowerDimen.TOWER_GRID_SIZE, TowerDimen.TOWER_TOP + (x + 1) * TowerDimen.TOWER_GRID_SIZE);
            }
        }

        graphics.drawBitmap(canvas, player.getImage(), TowerDimen.TOWER_LEFT + (player.getPosX() + 6) * TowerDimen.TOWER_GRID_SIZE, TowerDimen.TOWER_TOP + (player.getPosY() + 1) * TowerDimen.TOWER_GRID_SIZE);
    }
    
    private void drawMsg(GameGraphics graphics, Canvas canvas) {
        if (showMsgFlag) {
            graphics.drawBitmap(canvas, assets.bkgBlank, TowerDimen.R_MSG.left, TowerDimen.R_MSG.top,
                        TowerDimen.R_MSG.width(), TowerDimen.R_MSG.height());
            graphics.drawText(canvas, msgInfo, TowerDimen.R_MSG.left, TowerDimen.R_MSG.top);
        }
    }
    
    public void interaction(int x, int y) {
        int id = currentGame.lvMap[currentGame.currentFloor][y][x];
        switch (id) {
            case 0:     // player move
                player.move(x, y);
                break;
            case 1:     // brick wall
                break;
            case 2:     // yellow door
                if (player.getYkey() > 0) {
                    currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                    player.setYkey(player.getYkey() - 1);
                    player.move(x, y);
                }
                break;
            case 3:     // blue door
                if (player.getBkey() > 0) {
                    currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                    player.setBkey(player.getBkey() - 1);
                    player.move(x, y);
                }
                break;
            case 4:     // red door
                if (player.getRkey() > 0) {
                    currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                    player.setRkey(player.getRkey() - 1);
                    player.move(x, y);
                }
                break;
            case 5:     // stone
                break;
            case 6:     // yellow hey
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setYkey(player.getYkey() + 1);
                displayMessage("得到一把 黄钥匙 ！");
                break;
            case 7:     // blue key
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setBkey(player.getBkey() + 1);
                displayMessage("得到一把 蓝钥匙 ！");
                break;
            case 8:     // red key
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setRkey(player.getRkey() + 1);
                displayMessage("得到一把 红钥匙 ！");
                break;
            case 9:     // sapphire
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setDefend(player.getDefend() + 3);
                displayMessage("得到一个蓝宝石 防御力加 3 ！");
                break;
            case 10:    // ruby
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setAttack(player.getAttack() + 3);
                displayMessage("得到一个红宝石 攻击力加 3 ！");
                break;
            case 11:    // red potion
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setHp(player.getHp() + 200);
                displayMessage("得到一个小血瓶 生命加 200 ！");
                break;
            case 12:    // blue potion
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setHp(player.getHp() + 500);
                displayMessage("得到一个大血瓶 生命加 500 ！");
                break;
            case 13:    // upstairs
                currentGame.currentFloor++;
                currentGame.maxFloor = Math.max(currentGame.maxFloor, currentGame.currentFloor);
                player.move(TowerMap.initPos[currentGame.currentFloor][0], TowerMap.initPos[currentGame.currentFloor][1]);
                break;
            case 14:    // downstairs
                currentGame.currentFloor--;
                player.move(TowerMap.finPos[currentGame.currentFloor][0], TowerMap.finPos[currentGame.currentFloor][1]);
                break;
            case 15:    // barrier not accessible
                break;
            case 19:    // sea of fire
                break;
            case 20:    // starry sky
                break;
            case 22:    // shop
                if (currentGame.currentFloor == 3) {
                    shop.show(0);
                } else if (currentGame.currentFloor == 11) {
                    shop.show(3);
                }
                break;
            case 24:    // fairy
                dialog.show(id);
                break;
            case 25:    // thief
            case 26:    // old man
            case 27:    // businessman
            case 28:    // princess
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                break;
            case 30:    // little flying feather
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setLevel(player.getLevel() + 1);
                player.setHp(player.getHp() + 1000);
                player.setAttack(player.getAttack() + 7);
                player.setDefend(player.getDefend() + 7);
                displayMessage("得到 小飞羽 等级提升一级 ！");
                break;
            case 31:    // big flying feather
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setLevel(player.getLevel() + 3);
                player.setHp(player.getHp() + 3000);
                player.setAttack(player.getAttack() + 21);
                player.setDefend(player.getDefend() + 21);
                displayMessage("得到 大飞羽 等级提升三级 ！");
                break;
            case 32:    // cross
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                currentGame.isHasCross = true;
                displayMessage("【幸运十字架】 把它交给序章中的仙子，可以将自身的所有能力提升一些（攻击、防御和生命值）。");
                break;
            case 33:    // holy water bottle
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setHp(player.getHp() * 2);
                displayMessage("【圣水瓶】 它可以将你的体质增加一倍（生命值加倍）。");
                break;
            case 34:    // emblem of light
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                currentGame.isHasForecast = true;
                displayMessage("【圣光徽】 按 L 键使用 查看怪物的基本情况。");
                break;
            case 35:    // compass of wind
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                currentGame.isHasJump = true;
                displayMessage("【风之罗盘】 按 J 键使用 在已经走过的楼层间进行跳跃。");
                break;
            case 36:    // key box
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setYkey(player.getYkey() + 1);
                player.setBkey(player.getBkey() + 1);
                player.setRkey(player.getRkey() + 1);
                displayMessage("得到 钥匙盒 各种钥匙数加 1 ！");
                break;
            case 38:    // hammer
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                currentGame.isHasHammer = true;
                displayMessage("【星光神榔】 把它交给第四层的小偷，小偷便会用它打开第十八层的隐藏地面（你就可以救出公主了）。");
                break;
            case 39:    // gold nugget
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setMoney(player.getMoney() + 300);
                displayMessage("得到 金块 金币数加 300 ！");
                break;
            case 40:    // monster
            case 41:    // monster
            case 42:    // monster
            case 43:    // monster
            case 44:    // monster
            case 45:    // monster
            case 46:    // monster
            case 47:    // monster
            case 48:    // monster
            case 49:    // monster
            case 50:    // monster
            case 51:    // monster
            case 52:    // monster
            case 53:    // monster
            case 54:    // monster
            case 55:    // monster
            case 56:    // monster
            case 57:    // monster
            case 58:    // monster
            case 59:    // monster
            case 60:    // monster
            case 61:    // monster
            case 62:    // monster
            case 63:    // monster
            case 64:    // monster
            case 65:    // monster
            case 66:    // monster
            case 67:    // monster
            case 68:    // monster
            case 69:    // monster
            case 70:    // monster
                if (Forecast.forecast(player, MonsterData.monsterMap.get(id)).equals("???")
                        || Integer.parseInt(Forecast.forecast(player, MonsterData.monsterMap.get(id))) >= player.getHp()) {
                    return;
                } else {
                    battle.show(id, x, y);
                }
                break;
            case 71:    // iron sword
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setAttack(player.getAttack() + 10);
                displayMessage("得到 铁剑 攻击加 10 ！");
                break;
            case 73:    // steel sword
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setAttack(player.getAttack() + 30);
                displayMessage("得到 钢剑 攻击加 30 ！");
                break;
            case 75:    // sword of light
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setAttack(player.getAttack() + 120);
                displayMessage("得到 圣光剑 攻击加 120 ！");
                break;
            case 76:    // iron shield
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setDefend(player.getDefend() + 10);
                displayMessage("得到 铁盾 防御加 10 ！");
                break;
            case 78:    // steel shield
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setDefend(player.getDefend() + 30);
                displayMessage("得到 钢盾 防御加 30 ！");
                break;
            case 80:    // light shield
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                player.setDefend(player.getDefend() + 120);
                displayMessage("得到 星光盾 防御加 120 ！");
                break;
            case 115:   // accessible guardrail
                currentGame.lvMap[currentGame.currentFloor][y][x] = 0;
                player.move(x, y);
                break;
        }
    }
    
    public static void displayMessage(String message) {
        showMsgFlag = true;
        msgInfo = message;
        handler.removeMessages(MSG_ID_REMOVE_MSG);
        handler.sendEmptyMessageDelayed(MSG_ID_REMOVE_MSG, MSG_DELAY_REMOVE_MSG);
    }
}
