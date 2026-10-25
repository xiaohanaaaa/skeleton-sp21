package gitlet;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Represents the staging area of a Gitlet repository.
 *  Tracks files staged for addition (mapped to the blob SHA-1 captured at the
 *  time of `add`) and file names staged for removal. This class owns the
 *  staging semantics and does not expose its mutable collections directly.
 *
 *  表示 Gitlet 仓库的暂存区。
 *  记录「待添加」的文件（映射到 add 时捕获的 blob SHA-1）以及「待删除」的文件名。
 *  本类负责暂存区的语义，不直接对外暴露其可变集合。
 *
 *  @author TODO
 */
public class StagingArea implements Serializable {

    /** Files staged for addition: file name -> blob SHA-1 captured at add time.
     *  待添加的文件：文件名 -> add 时捕获的 blob SHA-1。 */
    private TreeMap<String, String> additions;

    /** File names staged for removal. / 待删除的文件名。 */
    private TreeSet<String> removals;

    /** Creates an empty staging area. / 创建一个空暂存区。 */
    public StagingArea() {
        additions = new TreeMap<>();
        removals = new TreeSet<>();
    }

    /** Returns true if nothing is staged for addition or removal.
     *  若待添加与待删除都为空则返回 true。 */
    public boolean isEmpty() {
        return additions.isEmpty() && removals.isEmpty();
    }

    /** Returns true if NAME is staged for addition.
     *  若 NAME 在待添加中则返回 true。 */
    public boolean hasAddition(String name) {
        return additions.containsKey(name);
    }

    /** Returns the blob SHA-1 staged for addition under NAME, or null.
     *  返回 NAME 在待添加中对应的 blob SHA-1，没有则返回 null。 */
    public String getAdditionSha(String name) {
        return additions.get(name);
    }

    /** Returns a read-only view of the names staged for addition.
     *  返回待添加文件名的只读视图。 */
    public Set<String> additionNames() {
        return Collections.unmodifiableSet(additions.keySet());
    }

    /** Returns true if NAME is staged for removal.
     *  若 NAME 在待删除中则返回 true。 */
    public boolean hasRemoval(String name) {
        return removals.contains(name);
    }

    /** Returns a read-only view of the names staged for removal.
     *  返回待删除文件名的只读视图。 */
    public Set<String> removalNames() {
        return Collections.unmodifiableSet(removals);
    }

    /** Stages NAME for addition with the given blob SHA-1, cancelling any
     *  pending removal of the same name.
     *  以给定的 blob SHA-1 将 NAME 标记为待添加，并取消同名文件的待删除标记。 */
    public void stage(String name, String sha) {
        additions.put(name, sha);
        removals.remove(name);
    }

    /** Removes NAME from the addition stage, if present.
     *  若存在，则将 NAME 从待添加中移除。 */
    public void unstageAddition(String name) {
        additions.remove(name);
    }

    /** Stages NAME for removal, cancelling any pending addition of the same name.
     *  将 NAME 标记为待删除，并取消同名文件的待添加标记。 */
    public void stageRemoval(String name) {
        removals.add(name);
        additions.remove(name);
    }

    /** Removes NAME from the removal stage, if present.
     *  若存在，则将 NAME 从待删除中移除。 */
    public void unstageRemoval(String name) {
        removals.remove(name);
    }

    /** Clears the entire staging area. / 清空整个暂存区。 */
    public void clear() {
        additions.clear();
        removals.clear();
    }
}
