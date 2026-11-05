package byow.Core;

import java.util.ArrayList;
import java.util.List;

/**
 * 轴对齐的矩形房间。
 *
 * <p>约定：x、y、width、height 描述的是房间的**地板内部区域**（闭区间），
 * 不包含外围墙壁。例如 x=5、y=3、width=4、height=2 表示地板占据
 * x ∈ [5, 8]、y ∈ [3, 4]。</p>
 *
 * <p>该类只负责几何计算，不直接修改 {@link World}。</p>
 */
public class Room {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /**
     * 创建一个房间。
     * @param x 最小 x 地板坐标
     * @param y 最小 y 地板坐标
     * @param width 地板宽度，必须 >= 1
     * @param height 地板高度，必须 >= 1
     */
    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** @return 最小 x 坐标 */
    public int left() {
        return x;
    }

    /** @return 最小 y 坐标 */
    public int bottom() {
        return y;
    }

    /** @return 最大 x 坐标（包含） */
    public int right() {
        return x + width - 1;
    }

    /** @return 最大 y 坐标（包含） */
    public int top() {
        return y + height - 1;
    }

    /** @return 地板宽度 */
    public int width() {
        return width;
    }

    /** @return 地板高度 */
    public int height() {
        return height;
    }

    /**
     * 返回房间中心坐标（整数除法，结果一定在地板范围内）。
     * @return 中心坐标
     */
    public Position center() {
        return new Position(x + width / 2, y + height / 2);
    }

    /**
     * 判断坐标是否位于房间地板上。
     * @param p 待判断坐标
     * @return 在地板范围内返回 true
     */
    public boolean contains(Position p) {
        return p.x() >= left() && p.x() <= right()
                && p.y() >= bottom() && p.y() <= top();
    }

    /**
     * 判断当前房间与另一个房间（连同指定的墙壁间距）是否重叠。
     *
     * <p>padding 用于保证两个房间之间至少留出若干格空隙，
     * 避免生成时房间贴在一起。</p>
     *
     * @param other 另一个房间
     * @param padding 额外要求的间隔格数（>= 0）
     * @return 若相交或间隔不足则返回 true
     */
    public boolean overlaps(Room other, int padding) {
        boolean disjoint = right() + padding < other.left()
                || other.right() + padding < left()
                || top() + padding < other.bottom()
                || other.top() + padding < bottom();
        return !disjoint;
    }

    /**
     * 返回房间内所有地板坐标，顺序固定（先按 x 再按 y）。
     * @return 地板坐标列表
     */
    public List<Position> floorTiles() {
        List<Position> result = new ArrayList<>();
        for (int i = left(); i <= right(); i += 1) {
            for (int j = bottom(); j <= top(); j += 1) {
                result.add(new Position(i, j));
            }
        }
        return result;
    }
}
