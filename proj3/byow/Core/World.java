package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏世界的核心数据容器。
 *
 * <p>只负责保存地图（tile 网格）和房间列表，并提供越界/可通行判断。
 * 世界的**生成**在 {@link WorldGenerator}，玩家状态在 {@link Avatar}，
 * 渲染在 {@link HUD}。</p>
 *
 * <p>坐标约定：{@code tiles[x][y]}，y = 0 为最下面一行。</p>
 */
public class World {
    private final TETile[][] tiles;
    private final List<Room> rooms;

    /**
     * 创建指定尺寸的空世界，所有格子初始化为 NOTHING。
     * @param width 宽度（列数）
     * @param height 高度（行数）
     */
    public World(int width, int height) {
        this.tiles = new TETile[width][height];
        this.rooms = new ArrayList<>();
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** @return 世界宽度 */
    public int width() {
        return tiles.length;
    }

    /** @return 世界高度 */
    public int height() {
        return tiles[0].length;
    }

    /**
     * 返回底层 2D tile 数组，供渲染器和 Engine 的返回值使用。
     * @return tile 数组
     */
    public TETile[][] tiles() {
        return tiles;
    }

    /**
     * 判断坐标是否在世界范围内。
     * @param x 列坐标
     * @param y 行坐标
     * @return 在范围内返回 true
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
    }

    /**
     * 判断坐标是否在世界范围内。
     * @param p 坐标
     * @return 在范围内返回 true
     */
    public boolean inBounds(Position p) {
        return p != null && inBounds(p.x(), p.y());
    }

    /**
     * 读取坐标处的 tile；越界返回 null。
     * @param x 列坐标
     * @param y 行坐标
     * @return 对应 tile
     */
    public TETile tileAt(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }
        return tiles[x][y];
    }

    /**
     * 读取坐标处的 tile；越界返回 null。
     * @param p 坐标
     * @return 对应 tile
     */
    public TETile tileAt(Position p) {
        if (p == null) {
            return null;
        }
        return tileAt(p.x(), p.y());
    }

    /**
     * 设置坐标处的 tile；越界时忽略。
     * @param x 列坐标
     * @param y 行坐标
     * @param tile 新 tile
     */
    public void setTile(int x, int y, TETile tile) {
        if (inBounds(x, y)) {
            tiles[x][y] = tile;
        }
    }

    /**
     * 设置坐标处的 tile；越界时忽略。
     * @param p 坐标
     * @param tile 新 tile
     */
    public void setTile(Position p, TETile tile) {
        if (p != null) {
            setTile(p.x(), p.y(), tile);
        }
    }

    /**
     * 判断玩家能否移动到该坐标。
     * 当前约定地板和已解锁的门可以通行，墙壁/空地/越界都不可通行。
     * @param p 目标坐标
     * @return 可通行返回 true
     */
    public boolean isWalkable(Position p) {
        TETile tile = tileAt(p);
        return tile == Tileset.FLOOR || tile == Tileset.UNLOCKED_DOOR;
    }

    /**
     * 记录一个房间。
     * @param room 房间
     */
    public void addRoom(Room room) {
        rooms.add(room);
    }

    /** @return 世界中的所有房间（只读语义） */
    public List<Room> rooms() {
        return rooms;
    }
}
