package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  A commit is an immutable snapshot of the tracked files at a point in time,
 *  together with metadata (message, timestamp) and a reference to its parent
 *  commit(s). Files are represented as a flat mapping from file name to the
 *  SHA-1 id of the corresponding blob.
 *
 *  表示一个 gitlet 提交对象。
 *  一个 commit 是某一时刻所有被跟踪文件的不可变快照，同时携带元数据
 *  （提交信息、时间戳）以及对父提交的引用。文件用「文件名 -> 对应 blob 的
 *  SHA-1」的扁平映射表示。
 *
 *  @author TODO
 */
public class Commit implements Serializable, Dumpable {

    /** The message of this Commit. / 该 commit 的提交信息。 */
    private String message;

    /** The timestamp of this Commit. The initial commit uses new Date(0).
     *  该 commit 的时间戳。初始 commit 使用 new Date(0)。 */
    private Date timestamp;

    /** The SHA-1 id of the (first) parent commit; null for the initial commit.
     *  第一个父提交的 SHA-1；初始 commit 为 null。 */
    private String parent;

    /** The SHA-1 id of the second parent; null unless this is a merge commit.
     *  第二个父提交的 SHA-1；非 merge commit 为 null。 */
    private String parent2;

    /** Mapping from tracked file name to the SHA-1 id of its blob in this commit.
     *  被跟踪文件名到其在本 commit 中 blob SHA-1 的映射。 */
    private TreeMap<String, String> blobs;

    /** Creates a Commit with the given metadata and file snapshot.
     *  用给定的元数据和文件快照创建一个 Commit。 */
    public Commit(String message, Date timestamp,
                  String parent, String parent2, TreeMap<String, String> blobs) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = parent;
        this.parent2 = parent2;
        this.blobs = blobs;
    }

    /** Returns the message of this commit. / 返回该 commit 的提交信息。 */
    public String getMessage() {
        return message;
    }

    /** Returns the timestamp of this commit. / 返回该 commit 的时间戳。 */
    public Date getTimestamp() {
        return timestamp;
    }

    /** Returns the first parent's SHA-1 id, or null for the initial commit.
     *  返回第一个父提交的 SHA-1；初始 commit 返回 null。 */
    public String getParent() {
        return parent;
    }

    /** Returns the second parent's SHA-1 id, or null if this is not a merge.
     *  返回第二个父提交的 SHA-1；非 merge commit 返回 null。 */
    public String getParent2() {
        return parent2;
    }

    /** Returns the (file name -> blob SHA-1) mapping of this commit.
     *  返回该 commit 的（文件名 -> blob SHA-1）映射。 */
    public TreeMap<String, String> getBlobs() {
        return blobs;
    }

    /** Returns true if this commit has two parents (i.e. is a merge commit).
     *  若该 commit 有两个父提交（即 merge commit）则返回 true。 */
    public boolean isMerge() {
        return parent2 != null;
    }

    @Override
    public void dump() {
        System.out.printf("message: %s%n", message);
        System.out.printf("timestamp: %s%n", timestamp);
        System.out.printf("parent: %s%n", parent);
        System.out.printf("parent2: %s%n", parent2);
        System.out.printf("blobs: %s%n", blobs);
    }
}
