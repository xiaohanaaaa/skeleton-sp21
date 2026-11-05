package byow.Core;

import byow.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;

/**
 * 所有 StdDraw 界面绘制逻辑。
 *
 * <p>把菜单、种子输入提示、游戏 HUD、鼠标悬停 tile 描述集中在这里，
 * 避免 Engine 里混入大量绘图代码。HUD 不修改游戏状态，也不调用
 * {@code StdDraw.show()}；刷新由 Engine 统一负责。</p>
 */
public class HUD {
    private static final Color BACKGROUND = new Color(10, 10, 10);
    private static final Color TEXT = new Color(235, 235, 235);
    private static final Color ACCENT = new Color(128, 192, 128);
    private static final Font TITLE_FONT = new Font("Monaco", Font.BOLD, 42);
    private static final Font MENU_FONT = new Font("Monaco", Font.BOLD, 20);
    private static final Font HUD_FONT = new Font("Monaco", Font.PLAIN, 13);

    private static final String TITLE = "BYOW";
    private static final int HUD_HEIGHT = 2;

    /** 绘制主菜单：New Game / Load Game / Quit。 */
    public void showMainMenu() {
        StdDraw.clear(BACKGROUND);
        StdDraw.setPenColor(ACCENT);
        StdDraw.setFont(TITLE_FONT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.72, TITLE);

        StdDraw.setFont(MENU_FONT);
        StdDraw.setPenColor(TEXT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.48, "New Game (N)");
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.38, "Load Game (L)");
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.28, "Quit (Q)");
    }

    /**
     * 绘制种子输入界面。
     * @param seedSoFar 用户已经输入的种子数字
     */
    public void showSeedPrompt(String seedSoFar) {
        StdDraw.clear(BACKGROUND);
        StdDraw.setPenColor(ACCENT);
        StdDraw.setFont(TITLE_FONT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.72, "New Game");

        StdDraw.setFont(MENU_FONT);
        StdDraw.setPenColor(TEXT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.50, "Enter seed:");
        StdDraw.setPenColor(ACCENT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.40,
                seedSoFar.isEmpty() ? "_" : seedSoFar);
        StdDraw.setPenColor(TEXT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.28, "Press S to start");
    }

    /**
     * 绘制游戏 HUD（顶层状态栏）。
     * @param world 当前世界
     * @param avatar 当前玩家
     * @param seed 当前种子
     * @param steps 已走步数
     */
    public void showGameHUD(World world, Avatar avatar, long seed, int steps) {
        int width = world.width();
        int height = world.height();

        StdDraw.setPenColor(new Color(0, 0, 0));
        StdDraw.filledRectangle(width / 2.0, height - HUD_HEIGHT / 2.0,
                width / 2.0, HUD_HEIGHT / 2.0);

        StdDraw.setFont(HUD_FONT);
        StdDraw.setPenColor(TEXT);
        StdDraw.textLeft(1, height - 0.7, "Seed: " + seed);
        StdDraw.textLeft(1, height - 1.5, "Steps: " + steps);
        StdDraw.textRight(width - 1.0, height - 0.7, "WASD move   :q save & quit");

        if (avatar != null) {
            Position p = avatar.position();
            StdDraw.setPenColor(ACCENT);
            StdDraw.textRight(width - 1.0, height - 1.5,
                    "Avatar: (" + p.x() + ", " + p.y() + ")");
        }
    }

    /**
     * 显示鼠标悬停位置的 tile 描述。
     * @param world 当前世界
     * @param tile 悬停 tile；为 null 时不显示
     */
    public void showHoveredTile(World world, TETile tile) {
        if (tile == null) {
            return;
        }
        StdDraw.setFont(HUD_FONT);
        StdDraw.setPenColor(ACCENT);
        StdDraw.textLeft(1, 0.7, "Tile: " + tile.description());
    }

    /**
     * 显示一条提示信息（例如“没有找到存档”）。
     * @param message 提示文本
     */
    public void showMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        StdDraw.setFont(MENU_FONT);
        StdDraw.setPenColor(ACCENT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.16, message);
    }

    /** 显示游戏结束/存盘退出画面。 */
    public void showGameOver() {
        StdDraw.clear(BACKGROUND);
        StdDraw.setPenColor(TEXT);
        StdDraw.setFont(TITLE_FONT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.55, "Game Over");
        StdDraw.setFont(MENU_FONT);
        StdDraw.setPenColor(ACCENT);
        StdDraw.text(Engine.WIDTH / 2.0, Engine.HEIGHT * 0.40, "Press Q to quit");
    }
}
