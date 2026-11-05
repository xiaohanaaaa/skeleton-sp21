package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

/**
 * 游戏总控制器：负责“输入 -> 状态机 -> 委托给 Game -> 渲染”。
 *
 * <p>职责边界：</p>
 * <ul>
 *   <li>{@link Game}：世界、玩家、移动规则、种子与移动历史；</li>
 *   <li>{@link WorldGenerator}：由种子确定性地生成世界；</li>
 *   <li>{@link SaveManager}：只保存“种子 + 移动历史”；</li>
 *   <li>{@link HUD}：所有 StdDraw 菜单/提示绘制；</li>
 *   <li>{@code Engine}：解析字符、维护菜单/种子/游戏中状态，并保证
 *       键盘模式与字符串模式走同一套 {@link #processChar(char)} 逻辑。</li>
 * </ul>
 *
 * <p>状态机：</p>
 * <pre>
 * MENU --N/n--> SEED --数字...S--> PLAYING --W/A/S/D--> PLAYING
 *   |                                  |
 *   +--L/l（有存档）--> PLAYING         +--':q'--> 保存并回到 MENU
 *   +--Q/q--> 退出
 * </pre>
 */
public class Engine {
    /* 世界尺寸，可按需要调整。 */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    /* 键盘模式每帧暂停毫秒数，避免空转占满 CPU。 */
    private static final int FRAME_PAUSE_MILLIS = 20;
    private static final String NO_SAVE_MESSAGE = "No saved game found.";

    private final TERenderer ter;
    private final HUD hud;
    private final SaveManager saveManager;
    private final StringBuilder seedBuffer;

    /* 当前游戏；处于 Menu/Seed 状态时可能为 null。 */
    private Game game;

    /* 当前输入状态。 */
    private Mode mode;

    /* ':q' 需要连续两个字符，用它记录是否刚输入过 ':'。 */
    private boolean colonPressed;

    /* 字符串模式遇到 Q/q 后提前结束解析。 */
    private boolean quitRequested;

    /* 主菜单上显示的一次性提示。 */
    private String message;

    private enum Mode {
        MENU, SEED, PLAYING
    }

    /** 创建引擎，初始化渲染器与辅助对象，但不启动游戏。 */
    public Engine() {
        this.ter = new TERenderer();
        this.hud = new HUD();
        this.saveManager = new SaveManager();
        this.seedBuffer = new StringBuilder();
        this.mode = Mode.MENU;
        this.message = "";
    }

    /**
     * 键盘交互入口：初始化窗口，进入循环，处理主菜单、种子输入、
     * 移动、鼠标悬停 HUD 与 :q 存档。
     */
    public void interactWithKeyboard() {
        ter.initialize(WIDTH, HEIGHT);
        game = null;
        quitRequested = false;
        message = "";
        returnToMenu();

        while (!quitRequested) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toLowerCase(StdDraw.nextKeyTyped());
                processChar(c);
            }
            renderWorld();
            StdDraw.pause(FRAME_PAUSE_MILLIS);
        }
        System.exit(0);
    }

    /**
     * 字符串交互入口（autograder 使用）。
     *
     * <p>与键盘模式逐字符等价：不弹窗、不绘制，只处理输入并返回最终世界。</p>
     *
     * @param input 输入字符串
     * @return 最终世界的 2D tile 数组；若整段输入没有成功创建/读取世界则返回 null
     */
    public TETile[][] interactWithInputString(String input) {
        game = null;
        quitRequested = false;
        message = "";
        returnToMenu();

        if (input != null) {
            for (int i = 0; i < input.length() && !quitRequested; i += 1) {
                processChar(Character.toLowerCase(input.charAt(i)));
            }
        }

        return currentFrame();
    }

    /**
     * 返回当前世界的文本表示，便于 Main 的 -s 模式打印与调试。
     * @return 世界字符串；没有世界时返回空字符串
     */
    @Override
    public String toString() {
        TETile[][] frame = currentFrame();
        return frame == null ? "" : TETile.toString(frame);
    }

    /**
     * 构造“当前画面”：复制世界并在地图上放上头像。
     * 不修改真实地图，避免头像格影响后续移动判断。
     * @return 带头像的 tile 数组；没有游戏时返回 null
     */
    private TETile[][] currentFrame() {
        if (game == null) {
            return null;
        }
        TETile[][] frame = TETile.copyOf(game.world().tiles());
        Position p = game.avatar().position();
        if (game.world().inBounds(p)) {
            frame[p.x()][p.y()] = Tileset.AVATAR;
        }
        return frame;
    }

    /**
     * 处理单个字符，是两种输入模式共享的唯一入口。
     * @param c 原始输入字符（调用方已转小写）
     */
    private void processChar(char c) {
        switch (mode) {
            case MENU:
                processMenuChar(c);
                break;
            case SEED:
                processSeedChar(c);
                break;
            case PLAYING:
                processPlayingChar(c);
                break;
            default:
                break;
        }
    }

    /**
     * MENU 状态处理：N 进入种子输入，L 读档，Q 退出。
     * @param c 输入字符
     */
    private void processMenuChar(char c) {
        if (c == 'n') {
            seedBuffer.setLength(0);
            colonPressed = false;
            message = "";
            mode = Mode.SEED;
        } else if (c == 'l') {
            loadSavedGame();
        } else if (c == 'q') {
            quitRequested = true;
        }
    }

    /**
     * SEED 状态处理：累积数字；遇到 S/s 解析种子并新开一局。
     * 非数字且非 S 的字符忽略。
     * @param c 输入字符
     */
    private void processSeedChar(char c) {
        if (c >= '0' && c <= '9') {
            seedBuffer.append(c);
        } else if (c == 's') {
            startNewGame(parseSeed());
        }
    }

    /**
     * PLAYING 状态处理：w/a/s/d 移动；':' + 'q' 保存并退出。
     * @param c 输入字符
     */
    private void processPlayingChar(char c) {
        if (colonPressed) {
            colonPressed = false;
            if (c == 'q') {
                saveAndQuit();
            }
            return;
        }
        if (c == ':') {
            colonPressed = true;
            return;
        }
        if (c == 'w' || c == 'a' || c == 's' || c == 'd') {
            game.move(c);
        }
    }

    /**
     * 使用给定种子新开一局。
     * @param seed 世界种子
     */
    private void startNewGame(long seed) {
        game = new Game(seed);
        colonPressed = false;
        message = "";
        mode = Mode.PLAYING;
    }

    /**
     * 读取存档并恢复游戏。
     * 若存档不存在，保持 MENU 状态并显示提示。
     */
    private void loadSavedGame() {
        SaveManager.SaveData data = saveManager.load();
        if (data == null) {
            message = NO_SAVE_MESSAGE;
            mode = Mode.MENU;
            return;
        }
        game = new Game(data.seed(), data.moves());
        colonPressed = false;
        message = "";
        mode = Mode.PLAYING;
    }

    /**
     * 保存当前游戏（种子 + 移动历史）并回到主菜单。
     */
    private void saveAndQuit() {
        if (game != null) {
            saveManager.save(game.seed(), game.moveHistory());
            game.end();
        }
        returnToMenu();
    }

    /**
     * 把 seedBuffer 解析为 long。
     * @return 解析出的种子；空字符串或非法输入返回 0
     */
    private long parseSeed() {
        if (seedBuffer.length() == 0) {
            return 0L;
        }
        try {
            return Long.parseLong(seedBuffer.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 根据当前 mode 绘制窗口：
     * MENU -> 主菜单；SEED -> 种子输入提示；PLAYING -> 世界 + HUD。
     */
    private void renderWorld() {
        if (mode == Mode.MENU) {
            hud.showMainMenu();
            hud.showMessage(message);
        } else if (mode == Mode.SEED) {
            hud.showSeedPrompt(seedBuffer.toString());
        } else if (mode == Mode.PLAYING && game != null) {
            ter.renderFrame(currentFrame());
            hud.showGameHUD(game.world(), game.avatar(), game.seed(),
                    game.moveHistory().length());
            updateHoveredTile();
        }
        StdDraw.show();
    }

    /**
     * 键盘模式下的鼠标悬停处理：把鼠标坐标转换为 tile 并交给 HUD 显示描述。
     */
    private void updateHoveredTile() {
        if (game == null) {
            return;
        }
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        if (!game.world().inBounds(mouseX, mouseY)) {
            return;
        }
        TETile tile = game.world().tileAt(mouseX, mouseY);
        if (game.avatar().position().equals(new Position(mouseX, mouseY))) {
            tile = Tileset.AVATAR;
        }
        hud.showHoveredTile(game.world(), tile);
    }

    /** 重置到主菜单状态（清空临时输入，不删除当前游戏对象）。 */
    private void returnToMenu() {
        mode = Mode.MENU;
        seedBuffer.setLength(0);
        colonPressed = false;
    }
}
