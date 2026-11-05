package byow.Core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * 极小化存档管理器。
 *
 * <p>保存内容只有两行：</p>
 * <pre>
 * seed
 * wasdwasd...
 * </pre>
 *
 * <p>世界由种子完全确定，移动序列可以重放；文件小且不依赖 TETile 的
 * 实现细节。</p>
 *
 * <p>存档文件位于程序运行目录（{@code System.getProperty("user.dir")}），
 * 因此 autograder 在同一目录连续调用时能读到上一次的存档。</p>
 */
public class SaveManager {
    private static final File CWD = new File(System.getProperty("user.dir"));
    private static final String SAVE_FILE_NAME = "byow_save.txt";

    /** @return 存档文件对象 */
    private File saveFile() {
        return new File(CWD, SAVE_FILE_NAME);
    }

    /** @return 存档是否存在 */
    public boolean exists() {
        return saveFile().isFile();
    }

    /**
     * 覆盖写入存档。
     * @param seed 世界种子
     * @param moves 移动历史
     */
    public void save(long seed, String moves) {
        String content = seed + System.lineSeparator()
                + (moves == null ? "" : moves) + System.lineSeparator();
        try {
            Files.write(saveFile().toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Failed to save game: " + e.getMessage());
        }
    }

    /**
     * 读取存档。
     * @return 存档数据；文件不存在或格式错误时返回 null
     */
    public SaveData load() {
        if (!exists()) {
            return null;
        }
        try {
            List<String> lines = Files.readAllLines(saveFile().toPath(), StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return null;
            }
            long seed = Long.parseLong(lines.get(0).trim());
            String moves = lines.size() > 1 ? lines.get(1).trim() : "";
            return new SaveData(seed, moves);
        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }

    /**
     * 存档内容的简单数据载体。
     */
    public static class SaveData {
        private final long seed;
        private final String moves;

        /**
         * @param seed 世界种子
         * @param moves 移动历史
         */
        public SaveData(long seed, String moves) {
            this.seed = seed;
            this.moves = moves;
        }

        /** @return 世界种子 */
        public long seed() {
            return seed;
        }

        /** @return 移动历史 */
        public String moves() {
            return moves;
        }
    }
}
