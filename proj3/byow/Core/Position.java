package byow.Core;

import java.util.Objects;

/**
 * 不可变的二维整数坐标。
 *
 * <p>约定：x 是列（向右增大），y 是行（向上增大）。
 * 世界数组的访问方式为 {@code tiles[x][y]}，其中 y = 0 是屏幕最下面一行。</p>
 *
 * <p>该类不关心世界大小，越界判断由 {@link World} 负责。</p>
 */
public final class Position {
    private final int x;
    private final int y;

    /**
     * 创建一个坐标。
     * @param x 列坐标
     * @param y 行坐标
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** @return x 坐标 */
    public int x() {
        return x;
    }

    /** @return y 坐标 */
    public int y() {
        return y;
    }

    /**
     * 返回平移后的新坐标（不修改当前对象）。
     * @param dx x 方向偏移
     * @param dy y 方向偏移
     * @return 新坐标
     */
    public Position translate(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    /**
     * 返回上下左右四个相邻坐标，顺序固定：上、下、左、右。
     * @return 四个相邻坐标
     */
    public Position[] neighbors() {
        return new Position[] {
            new Position(x, y + 1),
            new Position(x, y - 1),
            new Position(x - 1, y),
            new Position(x + 1, y)
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position other = (Position) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
