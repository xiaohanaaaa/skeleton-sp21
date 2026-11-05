package byow.Core;

/**
 * 玩家角色（头像）。
 *
 * <p>只保存玩家位置并提供移动规则；不负责读取键盘，也不负责绘制。
 * 输入映射（w/a/s/d -> 偏移）放在 {@link Game}，这样字符串模式和键盘模式
 * 可以共用完全相同的逻辑。</p>
 */
public class Avatar {
    private Position position;

    /**
     * 创建玩家。
     * @param start 初始位置，必须位于可通行 tile 上
     */
    public Avatar(Position start) {
        this.position = start;
    }

    /** @return 当前坐标 */
    public Position position() {
        return position;
    }

    /**
     * 尝试向指定方向移动一格。
     *
     * <p>移动合法当且仅当目标坐标 {@link World#isWalkable(Position)} 为 true。</p>
     *
     * @param dx x 方向偏移，取值 -1、0、1
     * @param dy y 方向偏移，取值 -1、0、1
     * @param world 当前世界
     * @return 成功移动返回 true，被墙挡住返回 false
     */
    public boolean move(int dx, int dy, World world) {
        Position target = position.translate(dx, dy);
        if (world.isWalkable(target)) {
            position = target;
            return true;
        }
        return false;
    }

    /**
     * 直接设置玩家位置（用于读档或调试）。
     * @param newPosition 新坐标
     */
    public void teleport(Position newPosition) {
        this.position = newPosition;
    }
}
