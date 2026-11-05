package byow.Core;

import byow.TileEngine.Tileset;

import java.util.List;
import java.util.Random;

/**
 * 随机世界生成器。
 *
 * <p>输入固定种子后产生完全相同的世界（autograder 依赖这一点）。
 * 生成流程：</p>
 * <ol>
 *   <li>把所有格子填成 {@code Tileset.NOTHING}；</li>
 *   <li>随机尝试放置若干互不重叠的矩形 {@link Room}；</li>
 *   <li>把房间内部挖成 {@code Tileset.FLOOR}；</li>
 *   <li>按房间顺序用 L 形走廊连接相邻房间中心；</li>
 *   <li>给所有地板周围补上 {@code Tileset.WALL}，形成封闭世界；</li>
 *   <li>选择第一个房间的中心作为玩家出生点。</li>
 * </ol>
 *
 * <p>所有随机选择都来自构造时传入的同一个 {@link Random}，
 * 保证同种子可复现。</p>
 */
public class WorldGenerator {
    private static final int MIN_ROOM_WIDTH = 4;
    private static final int MAX_ROOM_WIDTH = 10;
    private static final int MIN_ROOM_HEIGHT = 4;
    private static final int MAX_ROOM_HEIGHT = 8;
    private static final int ROOM_ATTEMPTS = 60;
    private static final int ROOM_PADDING = 1;
    private static final int WORLD_MARGIN = 1;

    private final int width;
    private final int height;
    private final Random random;
    private final World world;
    private Position avatarStart;

    /**
     * 创建生成器。
     * @param width 世界宽度
     * @param height 世界高度
     * @param seed 随机种子
     */
    public WorldGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.random = new Random(seed);
        this.world = new World(width, height);
        this.avatarStart = new Position(width / 2, height / 2);
    }

    /**
     * 执行完整生成流程。
     * @return 生成好的世界
     */
    public World generate() {
        fillNothing();
        placeRooms();
        for (Room room : world.rooms()) {
            carveRoom(room);
        }
        connectRooms(world.rooms());
        addWalls();
        avatarStart = chooseAvatarStart();
        return world;
    }

    /** @return 生成过程选出的玩家出生点 */
    public Position startPosition() {
        return avatarStart;
    }

    /** 将所有格子初始化为 NOTHING。 */
    private void fillNothing() {
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world.setTile(x, y, Tileset.NOTHING);
            }
        }
    }

    /**
     * 随机尝试放置房间；重叠或越界的候选房间直接丢弃。
     * 如果尝试结束后房间数不足 2，则使用固定位置补足。
     */
    private void placeRooms() {
        for (int attempt = 0; attempt < ROOM_ATTEMPTS; attempt += 1) {
            int roomWidth = RandomUtils.uniform(random, MIN_ROOM_WIDTH, MAX_ROOM_WIDTH + 1);
            int roomHeight = RandomUtils.uniform(random, MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT + 1);
            int maxX = width - WORLD_MARGIN - roomWidth;
            int maxY = height - WORLD_MARGIN - roomHeight;
            if (maxX < WORLD_MARGIN || maxY < WORLD_MARGIN) {
                continue;
            }
            int roomX = RandomUtils.uniform(random, WORLD_MARGIN, maxX + 1);
            int roomY = RandomUtils.uniform(random, WORLD_MARGIN, maxY + 1);
            Room candidate = new Room(roomX, roomY, roomWidth, roomHeight);
            if (tryPlaceRoom(candidate)) {
                world.addRoom(candidate);
            }
        }
        ensureMinimumRooms();
    }

    /**
     * 尝试把一个候选房间加入世界。
     * @param candidate 候选房间
     * @return 成功放置返回 true
     */
    private boolean tryPlaceRoom(Room candidate) {
        if (candidate.left() < WORLD_MARGIN
                || candidate.right() > width - 1 - WORLD_MARGIN
                || candidate.bottom() < WORLD_MARGIN
                || candidate.top() > height - 1 - WORLD_MARGIN) {
            return false;
        }
        for (Room existing : world.rooms()) {
            if (candidate.overlaps(existing, ROOM_PADDING)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 如果随机放置的房间少于 2 个，就清空并用两个固定房间补足。
     * 这只在极端小世界或随机运气极差时才会触发。
     */
    private void ensureMinimumRooms() {
        if (world.rooms().size() >= 2) {
            return;
        }
        world.rooms().clear();
        int roomWidth = Math.min(MAX_ROOM_WIDTH, Math.max(2, width / 3));
        int roomHeight = Math.min(MAX_ROOM_HEIGHT, Math.max(2, height / 3));
        int leftX = WORLD_MARGIN;
        int leftY = WORLD_MARGIN;
        int rightX = width - WORLD_MARGIN - roomWidth;
        int rightY = height - WORLD_MARGIN - roomHeight;
        world.addRoom(new Room(leftX, leftY, roomWidth, roomHeight));
        if (rightX >= leftX + roomWidth + ROOM_PADDING) {
            world.addRoom(new Room(rightX, rightY, roomWidth, roomHeight));
        }
    }

    /**
     * 把房间内部全部设为 FLOOR。
     * @param room 房间
     */
    private void carveRoom(Room room) {
        for (Position p : room.floorTiles()) {
            world.setTile(p, Tileset.FLOOR);
        }
    }

    /**
     * 按列表顺序连接相邻房间：随机选择“先横后竖”或“先竖后横”，
     * 形成 L 形走廊。
     * @param roomList 已放置的房间
     */
    private void connectRooms(List<Room> roomList) {
        for (int i = 1; i < roomList.size(); i += 1) {
            Position from = roomList.get(i - 1).center();
            Position to = roomList.get(i).center();
            boolean horizontalFirst = RandomUtils.bernoulli(random);
            if (horizontalFirst) {
                carveHorizontalHallway(from.x(), to.x(), from.y());
                carveVerticalHallway(from.y(), to.y(), to.x());
            } else {
                carveVerticalHallway(from.y(), to.y(), from.x());
                carveHorizontalHallway(from.x(), to.x(), to.y());
            }
        }
    }

    /**
     * 在 y 行把 x1 到 x2 之间挖成地板。
     * @param x1 起点 x
     * @param x2 终点 x
     * @param y 行坐标
     */
    private void carveHorizontalHallway(int x1, int x2, int y) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);
        for (int x = start; x <= end; x += 1) {
            if (world.inBounds(x, y)) {
                world.setTile(x, y, Tileset.FLOOR);
            }
        }
    }

    /**
     * 在 x 列把 y1 到 y2 之间挖成地板。
     * @param y1 起点 y
     * @param y2 终点 y
     * @param x 列坐标
     */
    private void carveVerticalHallway(int y1, int y2, int x) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);
        for (int y = start; y <= end; y += 1) {
            if (world.inBounds(x, y)) {
                world.setTile(x, y, Tileset.FLOOR);
            }
        }
    }

    /**
     * 遍历所有地板，在其周围 8 个相邻的 NOTHING 格子上放 WALL。
     * 必须在挖完房间和走廊之后执行。
     */
    private void addWalls() {
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                if (world.tileAt(x, y) != Tileset.FLOOR) {
                    continue;
                }
                for (int dx = -1; dx <= 1; dx += 1) {
                    for (int dy = -1; dy <= 1; dy += 1) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (world.inBounds(nx, ny) && world.tileAt(nx, ny) == Tileset.NOTHING) {
                            world.setTile(nx, ny, Tileset.WALL);
                        }
                    }
                }
            }
        }
    }

    /**
     * 选择玩家出生点：优先取第一个房间中心；如果没有房间，
     * 则寻找任意地板；最后退化为世界中心。
     * @return 出生坐标
     */
    private Position chooseAvatarStart() {
        if (!world.rooms().isEmpty()) {
            return world.rooms().get(0).center();
        }
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                if (world.tileAt(x, y) == Tileset.FLOOR) {
                    return new Position(x, y);
                }
            }
        }
        return new Position(width / 2, height / 2);
    }
}
