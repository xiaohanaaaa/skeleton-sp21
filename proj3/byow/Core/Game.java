package byow.Core;

/**
 * 一局游戏的运行时状态与规则。
 *
 * <p>该类不接触 StdDraw，也不解析菜单/种子输入，因此可以独立测试：
 * 给定种子和移动序列，结果必须完全确定。</p>
 *
 * <p>存档策略：只保存“种子 + 移动历史”，读档时重新生成世界并重放移动。
 * 这保证 {@code n<seed>s<movements>:q} 之后执行 {@code l<movements>}
 * 与一次性执行 {@code n<seed>s<movements>} 得到完全相同的世界。</p>
 */
public class Game {
    private World world;
    private Avatar avatar;
    private long seed;
    private StringBuilder moveHistory;
    private boolean gameOver;

    /**
     * 新开一局。
     * @param seed 世界生成种子
     */
    public Game(long seed) {
        this.seed = seed;
        WorldGenerator generator = new WorldGenerator(Engine.WIDTH, Engine.HEIGHT, seed);
        this.world = generator.generate();
        this.avatar = new Avatar(generator.startPosition());
        this.moveHistory = new StringBuilder();
        this.gameOver = false;
    }

    /**
     * 从存档恢复一局。
     * @param seed 存档中的种子
     * @param moves 存档中的移动历史
     */
    public Game(long seed, String moves) {
        this(seed);
        applyMoves(moves == null ? "" : moves);
    }

    /** @return 当前世界 */
    public World world() {
        return world;
    }

    /** @return 当前玩家 */
    public Avatar avatar() {
        return avatar;
    }

    /** @return 本局种子 */
    public long seed() {
        return seed;
    }

    /** @return 从开局到现在的移动历史（只含 w/a/s/d） */
    public String moveHistory() {
        return moveHistory.toString();
    }

    /** @return 游戏是否已结束 */
    public boolean isGameOver() {
        return gameOver;
    }

    /** 标记游戏结束（例如存盘退出或通关）。 */
    public void end() {
        this.gameOver = true;
    }

    /**
     * 执行一次移动命令。
     *
     * <p>只接受 {@code w/a/s/d}。无论移动是否被墙挡住，都会计入
     * moveHistory，保证历史可完整重放。</p>
     *
     * @param direction 方向字符
     * @return 实际位置发生变化返回 true
     */
    public boolean move(char direction) {
        if (gameOver) {
            return false;
        }
        char d = Character.toLowerCase(direction);
        int dx = 0;
        int dy = 0;
        switch (d) {
            case 'w':
                dy = 1;
                break;
            case 's':
                dy = -1;
                break;
            case 'a':
                dx = -1;
                break;
            case 'd':
                dx = 1;
                break;
            default:
                return false;
        }
        moveHistory.append(d);
        return avatar.move(dx, dy, world);
    }

    /**
     * 重放一串移动命令（读档时使用）。
     * @param moves 只包含 w/a/s/d 的字符串
     */
    public void applyMoves(String moves) {
        if (moves == null) {
            return;
        }
        for (int i = 0; i < moves.length(); i += 1) {
            move(moves.charAt(i));
        }
    }
}
