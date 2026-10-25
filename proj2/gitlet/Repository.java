package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  Owns all command logic and persistence: commits and blobs are stored
 *  content-addressed under .gitlet/objects and .gitlet/commits, branches under
 *  .gitlet/refs/heads, and the current branch name in .gitlet/HEAD.
 *
 *  表示一个 gitlet 仓库。
 *  负责全部命令逻辑与持久化：commit 与 blob 按内容寻址存放在
 *  .gitlet/objects 和 .gitlet/commits 下，分支存放在 .gitlet/refs/heads 下，
 *  当前分支名存放在 .gitlet/HEAD。
 *
 *  @author TODO
 */
public class Repository {

    /** The current working directory. / 当前工作目录。 */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. / .gitlet 目录。 */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** Directory holding blobs, named by their contents' SHA-1.
     *  存放 blob 的目录，文件名即其内容的 SHA-1。 */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    /** Directory holding commits, named by SHA-1 of the serialized commit.
     *  存放 commit 的目录，文件名即序列化 commit 的 SHA-1。 */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /** Directory holding branch references. / 存放分支引用的目录。 */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    /** Directory holding branch heads (one file per branch).
     *  存放各分支 head 的目录（每个分支一个文件）。 */
    public static final File HEADS_DIR = join(REFS_DIR, "heads");

    /** File containing the name of the current branch.
     *  保存当前分支名的文件。 */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    /** File containing the serialized staging area.
     *  保存序列化暂存区的文件。 */
    public static final File STAGING_FILE = join(GITLET_DIR, "staging");

    /** Date format used when printing commits, e.g. Thu Nov 9 20:00:05 2017 -0800.
     *  打印 commit 时使用的时间格式。 */
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);

    /* ===================== Commands ===================== */

    /** Creates a new Gitlet repository in the current directory.
     *  在当前目录创建一个新的 Gitlet 仓库。 */
    public static void init() {
        if (GITLET_DIR.exists()) {
            throw error("A Gitlet version-control system already exists "
                    + "in the current directory.");
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        HEADS_DIR.mkdirs();

        Commit initial = new Commit("initial commit", new Date(0),
                null, null, new TreeMap<>());
        String sha = writeCommit(initial);
        setBranchSha("master", sha);
        setHeadBranch("master");
        writeStaging(new StagingArea());
    }

    /** Stages FILE_NAME for addition, or unstages it if identical to the
     *  current commit's version.
     *  将 FILE_NAME 标记为待添加；若与当前 commit 中的版本相同则取消暂存。 */
    public static void add(String fileName) {
        assertInitialized();
        File file = join(CWD, fileName);
        if (!file.isFile()) {
            throw error("File does not exist.");
        }
        String sha = writeBlob(readContents(file));
        StagingArea stage = readStaging();
        String current = headCommit().getBlobs().get(fileName);
        if (sha.equals(current)) {
            stage.unstageAddition(fileName);
        } else {
            stage.stage(fileName, sha);
        }
        stage.unstageRemoval(fileName);
        writeStaging(stage);
    }

    /** Saves a snapshot of tracked files and the staging area as a new commit.
     *  将跟踪文件与暂存区保存为一个新的 commit 快照。 */
    public static void commit(String message) {
        assertInitialized();
        StagingArea stage = readStaging();
        if (stage.isEmpty()) {
            throw error("No changes added to the commit.");
        }
        if (message.trim().isEmpty()) {
            throw error("Please enter a commit message.");
        }
        createCommit(message, null, stage);
    }

    /** Unstages FILE_NAME if staged for addition, otherwise stages it for
     *  removal if tracked.
     *  若 FILE_NAME 在待添加中则取消暂存；否则若被跟踪则标记为待删除。 */
    public static void rm(String fileName) {
        assertInitialized();
        Commit head = headCommit();
        StagingArea stage = readStaging();
        boolean stagedAddition = stage.hasAddition(fileName);
        boolean tracked = head.getBlobs().containsKey(fileName);
        if (!stagedAddition && !tracked) {
            throw error("No reason to remove the file.");
        }
        if (stagedAddition) {
            stage.unstageAddition(fileName);
        }
        if (tracked) {
            stage.stageRemoval(fileName);
            File file = join(CWD, fileName);
            if (file.exists() && !file.isDirectory()) {
                restrictedDelete(file);
            }
        }
        writeStaging(stage);
    }

    /** Prints the history from the current head to the initial commit.
     *  从当前 head 回溯到初始 commit 打印历史。 */
    public static void log() {
        assertInitialized();
        String sha = getHeadCommitSha();
        while (sha != null) {
            Commit commit = readCommit(sha);
            printCommitEntry(sha, commit);
            sha = commit.getParent();
        }
    }

    /** Prints information about every commit ever made.
     *  打印所有曾经创建过的 commit 的信息。 */
    public static void globalLog() {
        assertInitialized();
        for (String sha : allCommitShas()) {
            printCommitEntry(sha, readCommit(sha));
        }
    }

    /** Prints the ids of all commits with the given MESSAGE.
     *  打印所有具有给定 MESSAGE 的 commit 的 id。 */
    public static void find(String message) {
        assertInitialized();
        List<String> matches = new ArrayList<>();
        for (String sha : allCommitShas()) {
            if (readCommit(sha).getMessage().equals(message)) {
                matches.add(sha);
            }
        }
        if (matches.isEmpty()) {
            throw error("Found no commit with that message.");
        }
        for (String sha : matches) {
            message(sha);
        }
    }

    /** Prints the current status: branches, staged/removed/modified/untracked files.
     *  打印当前状态：分支、待添加/待删除/已修改/未跟踪的文件。 */
    public static void status() {
        assertInitialized();
        Commit head = headCommit();
        TreeMap<String, String> headBlobs = head.getBlobs();
        StagingArea stage = readStaging();
        String currentBranch = getHeadBranch();

        message("=== Branches ===");
        for (String branch : allBranches()) {
            if (branch.equals(currentBranch)) {
                message("*%s", branch);
            } else {
                message("%s", branch);
            }
        }
        message("");

        message("=== Staged Files ===");
        for (String name : stage.additionNames()) {
            message("%s", name);
        }
        message("");

        message("=== Removed Files ===");
        for (String name : stage.removalNames()) {
            message("%s", name);
        }
        message("");

        message("=== Modifications Not Staged For Commit ===");
        TreeMap<String, String> modified = new TreeMap<>();
        Set<String> candidates = new TreeSet<>();
        candidates.addAll(headBlobs.keySet());
        candidates.addAll(stage.additionNames());
        for (String name : candidates) {
            File file = join(CWD, name);
            boolean exists = file.isFile();
            if (stage.hasAddition(name)) {
                if (!exists) {
                    modified.put(name, "deleted");
                } else if (!sha1(readContents(file))
                        .equals(stage.getAdditionSha(name))) {
                    modified.put(name, "modified");
                }
            } else {
                if (stage.hasRemoval(name)) {
                    continue;
                }
                if (!exists) {
                    modified.put(name, "deleted");
                } else if (!sha1(readContents(file)).equals(headBlobs.get(name))) {
                    modified.put(name, "modified");
                }
            }
        }
        for (Map.Entry<String, String> entry : modified.entrySet()) {
            message("%s (%s)", entry.getKey(), entry.getValue());
        }
        message("");

        message("=== Untracked Files ===");
        List<String> working = plainFilenamesIn(CWD);
        if (working != null) {
            for (String name : working) {
                if (stage.hasAddition(name)) {
                    continue;
                }
                boolean tracked = headBlobs.containsKey(name)
                        && !stage.hasRemoval(name);
                if (!tracked) {
                    message("%s", name);
                }
            }
        }
        message("");
    }

    /** Dispatches the three forms of checkout based on OPERANDS.
     *  根据 OPERANDS 分派 checkout 的三种形式。 */
    public static void checkout(String[] operands) {
        assertInitialized();
        if (operands.length == 1) {
            checkoutBranch(operands[0]);
        } else if (operands.length == 2 && operands[0].equals("--")) {
            checkoutFile(null, operands[1]);
        } else if (operands.length == 3 && operands[1].equals("--")) {
            checkoutFile(operands[0], operands[2]);
        } else {
            throw error("Incorrect operands.");
        }
    }

    /** Creates a new branch BRANCH_NAME pointing at the current head.
     *  创建一个指向当前 head 的新分支 BRANCH_NAME。 */
    public static void branch(String branchName) {
        assertInitialized();
        if (branchExists(branchName)) {
            throw error("A branch with that name already exists.");
        }
        setBranchSha(branchName, getHeadCommitSha());
    }

    /** Deletes the branch pointer BRANCH_NAME.
     *  删除分支指针 BRANCH_NAME。 */
    public static void rmBranch(String branchName) {
        assertInitialized();
        if (!branchExists(branchName)) {
            throw error("A branch with that name does not exist.");
        }
        if (branchName.equals(getHeadBranch())) {
            throw error("Cannot remove the current branch.");
        }
        join(HEADS_DIR, branchName).delete();
    }

    /** Checks out all files of COMMIT_ID and moves the current branch head there.
     *  检出 COMMIT_ID 的全部文件，并将当前分支 head 移动过去。 */
    public static void reset(String commitId) {
        assertInitialized();
        String sha = findCommit(commitId);
        if (sha == null) {
            throw error("No commit with that id exists.");
        }
        Commit target = readCommit(sha);
        if (wouldBeOverwritten(target, getHeadCommitSha())) {
            throw error("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
        checkoutCommit(target);
        setBranchSha(getHeadBranch(), sha);
        writeStaging(new StagingArea());
    }

    /** Merges BRANCH_NAME into the current branch.
     *  将 BRANCH_NAME 合并进当前分支。 */
    public static void merge(String branchName) {
        assertInitialized();
        StagingArea stage = readStaging();
        if (!stage.isEmpty()) {
            throw error("You have uncommitted changes.");
        }
        if (!branchExists(branchName)) {
            throw error("A branch with that name does not exist.");
        }
        if (branchName.equals(getHeadBranch())) {
            throw error("Cannot merge a branch with itself.");
        }

        String headSha = getHeadCommitSha();
        String givenSha = getBranchSha(branchName);
        Commit head = readCommit(headSha);
        Commit given = readCommit(givenSha);
        String split = splitPoint(headSha, givenSha);

        if (split.equals(givenSha)) {
            message("Given branch is an ancestor of the current branch.");
            return;
        }
        if (split.equals(headSha)) {
            if (wouldBeOverwritten(given, headSha)) {
                throw error("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
            checkoutCommit(given);
            setBranchSha(getHeadBranch(), givenSha);
            message("Current branch fast-forwarded.");
            return;
        }

        Commit splitCommit = readCommit(split);
        if (mergeWouldOverwrite(head, given, splitCommit)) {
            throw error("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
        TreeMap<String, String> cur = head.getBlobs();
        TreeMap<String, String> giv = given.getBlobs();
        TreeMap<String, String> spl = splitCommit.getBlobs();
        TreeSet<String> names = new TreeSet<>();
        names.addAll(cur.keySet());
        names.addAll(giv.keySet());
        names.addAll(spl.keySet());

        boolean conflict = false;
        for (String name : names) {
            String c = cur.get(name);
            String g = giv.get(name);
            String s = spl.get(name);
            if (s != null) {
                boolean curChanged = !Objects.equals(c, s);
                boolean givChanged = !Objects.equals(g, s);
                if (!curChanged && givChanged) {
                    if (g == null) {
                        File file = join(CWD, name);
                        if (file.exists() && !file.isDirectory()) {
                            restrictedDelete(file);
                        }
                        stage.stageRemoval(name);
                    } else {
                        writeContents(join(CWD, name), readBlob(g));
                        stage.stage(name, g);
                    }
                } else if (curChanged && givChanged && !Objects.equals(c, g)) {
                    conflict = true;
                    stageConflict(stage, name, c, g);
                }
            } else {
                if (c == null && g != null) {
                    writeContents(join(CWD, name), readBlob(g));
                    stage.stage(name, g);
                } else if (c != null && g != null && !c.equals(g)) {
                    conflict = true;
                    stageConflict(stage, name, c, g);
                }
            }
        }
        writeStaging(stage);
        createCommit("Merged " + branchName + " into " + getHeadBranch() + ".",
                givenSha, stage);
        if (conflict) {
            message("Encountered a merge conflict.");
        }
    }

    /** Builds and stores a new commit from the current head plus STAGE, then
     *  moves the current branch to it.
     *  基于当前 head 与 STAGE 构造并存储一个新 commit，然后把当前分支移过去。 */
    private static void createCommit(String message, String parent2,
                                     StagingArea stage) {
        if (stage.isEmpty()) {
            throw error("No changes added to the commit.");
        }
        Commit head = headCommit();
        TreeMap<String, String> blobs = new TreeMap<>(head.getBlobs());
        for (String name : stage.additionNames()) {
            blobs.put(name, stage.getAdditionSha(name));
        }
        for (String name : stage.removalNames()) {
            blobs.remove(name);
        }
        Commit commit = new Commit(message, new Date(),
                getHeadCommitSha(), parent2, blobs);
        String sha = writeCommit(commit);
        setBranchSha(getHeadBranch(), sha);
        writeStaging(new StagingArea());
    }

    /* ===================== Persistence helpers ===================== */

    /** Aborts with an error if the current directory is not an initialized
     *  Gitlet repository.
     *  若当前目录不是已初始化的 Gitlet 仓库则报错退出。 */
    private static void assertInitialized() {
        if (!GITLET_DIR.isDirectory()) {
            throw error("Not in an initialized Gitlet directory.");
        }
    }

    /** Stores CONTENTS as a blob (deduplicated) and returns its SHA-1.
     *  将 CONTENTS 作为 blob 存储（去重）并返回其 SHA-1。 */
    private static String writeBlob(byte[] contents) {
        String sha = sha1(contents);
        File file = join(OBJECTS_DIR, sha);
        if (!file.isFile()) {
            writeObject(file, new Blob(contents));
        }
        return sha;
    }

    /** Reads and returns the raw bytes of the blob with id SHA.
     *  读取并返回 id 为 SHA 的 blob 的原始字节。 */
    private static byte[] readBlob(String sha) {
        Blob blob = readObject(join(OBJECTS_DIR, sha), Blob.class);
        return blob.getContents();
    }

    /** Returns true if a blob with id SHA exists.
     *  若 id 为 SHA 的 blob 存在则返回 true。 */
    private static boolean blobExists(String sha) {
        return join(OBJECTS_DIR, sha).isFile();
    }

    /** Serializes COMMIT and stores it under its content-addressed id.
     *  序列化 COMMIT 并以内容寻址的 id 存储。 */
    private static String writeCommit(Commit commit) {
        byte[] data = serialize(commit);
        String sha = sha1(data);
        File file = join(COMMITS_DIR, sha);
        if (!file.isFile()) {
            writeContents(file, data);
        }
        return sha;
    }

    /** Reads and deserializes the commit with id SHA.
     *  读取并反序列化 id 为 SHA 的 commit。 */
    private static Commit readCommit(String sha) {
        return readObject(join(COMMITS_DIR, sha), Commit.class);
    }

    /** Returns true if a commit with id SHA exists.
     *  若 id 为 SHA 的 commit 存在则返回 true。 */
    private static boolean commitExists(String sha) {
        return join(COMMITS_DIR, sha).isFile();
    }

    /** Reads the staging area, returning an empty one if none is stored.
     *  读取暂存区；若未存储则返回一个空暂存区。 */
    private static StagingArea readStaging() {
        if (!STAGING_FILE.isFile()) {
            return new StagingArea();
        }
        return readObject(STAGING_FILE, StagingArea.class);
    }

    /** Persists the staging area STAGING.
     *  持久化暂存区 STAGING。 */
    private static void writeStaging(StagingArea staging) {
        writeObject(STAGING_FILE, staging);
    }

    /** Returns the name of the current branch. / 返回当前分支名。 */
    private static String getHeadBranch() {
        return readContentsAsString(HEAD_FILE).trim();
    }

    /** Sets the current branch to NAME. / 将当前分支设为 NAME。 */
    private static void setHeadBranch(String name) {
        writeContents(HEAD_FILE, name);
    }

    /** Returns the commit id pointed to by branch NAME, or null.
     *  返回分支 NAME 指向的 commit id，没有则返回 null。 */
    private static String getBranchSha(String name) {
        File file = join(HEADS_DIR, name);
        if (!file.isFile()) {
            return null;
        }
        return readContentsAsString(file).trim();
    }

    /** Points branch NAME at commit id SHA.
     *  将分支 NAME 指向 commit id SHA。 */
    private static void setBranchSha(String name, String sha) {
        writeContents(join(HEADS_DIR, name), sha);
    }

    /** Returns true if a branch named NAME exists.
     *  若名为 NAME 的分支存在则返回 true。 */
    private static boolean branchExists(String name) {
        return join(HEADS_DIR, name).isFile();
    }

    /** Returns the names of all existing branches.
     *  返回所有已存在分支的名称。 */
    private static List<String> allBranches() {
        List<String> names = plainFilenamesIn(HEADS_DIR);
        return names == null ? new ArrayList<>() : names;
    }

    /** Returns the commit id of the current branch's head.
     *  返回当前分支 head 的 commit id。 */
    private static String getHeadCommitSha() {
        return getBranchSha(getHeadBranch());
    }

    /** Returns the Commit of the current branch's head.
     *  返回当前分支 head 对应的 Commit。 */
    private static Commit headCommit() {
        return readCommit(getHeadCommitSha());
    }

    /* ===================== Algorithm helpers ===================== */

    /** Resolves ABBREV (a full or unique-prefix commit id) to a full SHA-1,
     *  or null if no unique match exists.
     *  将 ABBREV（完整或唯一前缀的 commit id）解析为完整 SHA-1；
     *  若不存在唯一匹配则返回 null。 */
    private static String findCommit(String abbrev) {
        if (abbrev == null) {
            return null;
        }
        if (abbrev.length() == UID_LENGTH) {
            return commitExists(abbrev) ? abbrev : null;
        }
        String match = null;
        for (String sha : allCommitShas()) {
            if (sha.startsWith(abbrev)) {
                if (match != null) {
                    return null;
                }
                match = sha;
            }
        }
        return match;
    }

    /** Returns the SHA-1 ids of all commits ever made.
     *  返回所有曾经创建过的 commit 的 SHA-1。 */
    private static List<String> allCommitShas() {
        List<String> names = plainFilenamesIn(COMMITS_DIR);
        return names == null ? new ArrayList<>() : names;
    }

    /** Returns true if A is an ancestor of B (or equals B).
     *  若 A 是 B 的祖先（或相等）则返回 true。 */
    private static boolean isAncestor(String a, String b) {
        String sha = b;
        while (sha != null) {
            if (sha.equals(a)) {
                return true;
            }
            sha = readCommit(sha).getParent();
        }
        return false;
    }

    /** Returns the latest common ancestor of the commits SHA_A and SHA_B.
     *  返回 commit SHA_A 与 SHA_B 的最近公共祖先。 */
    private static String splitPoint(String shaA, String shaB) {
        Set<String> ancestorsA = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(shaA);
        while (!stack.isEmpty()) {
            String sha = stack.pop();
            if (sha == null || !ancestorsA.add(sha)) {
                continue;
            }
            Commit commit = readCommit(sha);
            if (commit.getParent() != null) {
                stack.push(commit.getParent());
            }
            if (commit.getParent2() != null) {
                stack.push(commit.getParent2());
            }
        }

        Set<String> seen = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(shaB);
        while (!queue.isEmpty()) {
            String sha = queue.poll();
            if (sha == null || !seen.add(sha)) {
                continue;
            }
            if (ancestorsA.contains(sha)) {
                return sha;
            }
            Commit commit = readCommit(sha);
            if (commit.getParent() != null) {
                queue.add(commit.getParent());
            }
            if (commit.getParent2() != null) {
                queue.add(commit.getParent2());
            }
        }
        return null;
    }

    /** Returns the blob SHA-1 of file NAME in COMMIT, or null if untracked.
     *  返回 COMMIT 中文件 NAME 的 blob SHA-1；未被跟踪则返回 null。 */
    private static String blobInCommit(Commit commit, String name) {
        return commit.getBlobs().get(name);
    }

    /** Overwrites the working directory with COMMIT's snapshot, deleting files
     *  tracked by the current head but absent from COMMIT. Does not clear staging.
     *  用 COMMIT 的快照覆盖工作目录，并删除「当前 head 跟踪但 COMMIT 中没有」的文件。
     *  不清空暂存区。 */
    private static void checkoutCommit(Commit target) {
        Commit head = headCommit();
        TreeMap<String, String> targetBlobs = target.getBlobs();
        for (Map.Entry<String, String> entry : targetBlobs.entrySet()) {
            writeContents(join(CWD, entry.getKey()), readBlob(entry.getValue()));
        }
        for (String name : head.getBlobs().keySet()) {
            if (!targetBlobs.containsKey(name)) {
                File file = join(CWD, name);
                if (file.exists() && !file.isDirectory()) {
                    restrictedDelete(file);
                }
            }
        }
    }

    /** Returns true if checking out TARGET would overwrite an untracked file.
     *  若检出 TARGET 会覆盖某个未跟踪文件则返回 true。 */
    private static boolean wouldBeOverwritten(Commit target, String curHeadSha) {
        Commit head = readCommit(curHeadSha);
        StagingArea stage = readStaging();
        for (String name : target.getBlobs().keySet()) {
            if (!head.getBlobs().containsKey(name) && !stage.hasAddition(name)) {
                if (join(CWD, name).exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns true if merging GIVEN into HEAD (relative to SPLIT) would
     *  overwrite an untracked working file.
     *  若以 SPLIT 为基准把 GIVEN 合并进 HEAD 时会覆盖某个未跟踪的工作区文件，
     *  则返回 true。 */
    private static boolean mergeWouldOverwrite(Commit head, Commit given,
                                               Commit split) {
        TreeMap<String, String> cur = head.getBlobs();
        TreeMap<String, String> giv = given.getBlobs();
        TreeMap<String, String> spl = split.getBlobs();
        Set<String> names = new TreeSet<>();
        names.addAll(cur.keySet());
        names.addAll(giv.keySet());
        names.addAll(spl.keySet());
        for (String name : names) {
            String c = cur.get(name);
            String g = giv.get(name);
            String s = spl.get(name);
            boolean willWrite;
            if (s != null) {
                boolean curChanged = !Objects.equals(c, s);
                boolean givChanged = !Objects.equals(g, s);
                willWrite = (!curChanged && givChanged && g != null)
                        || (curChanged && givChanged && !Objects.equals(c, g));
            } else {
                willWrite = (c == null && g != null)
                        || (c != null && g != null && !c.equals(g));
            }
            if (willWrite && !cur.containsKey(name)
                    && join(CWD, name).exists()) {
                return true;
            }
        }
        return false;
    }

    /** Writes a conflict-marked version of NAME and stages it.
     *  写入 NAME 的冲突标记版本并暂存。 */
    private static void stageConflict(StagingArea stage, String name,
                                      String curSha, String givenSha) {
        byte[] curBytes = (curSha == null) ? new byte[0] : readBlob(curSha);
        byte[] givenBytes = (givenSha == null) ? new byte[0] : readBlob(givenSha);
        File file = join(CWD, name);
        writeContents(file, "<<<<<<< HEAD\n", curBytes,
                "=======\n", givenBytes, ">>>>>>>\n");
        String sha = writeBlob(readContents(file));
        stage.stage(name, sha);
    }

    /** Checks out a single file NAME from the commit COMMIT_ID (or the current
     *  head if COMMIT_ID is null) into the working directory.
     *  从 commit COMMIT_ID（若为 null 则用当前 head）检出单个文件 NAME 到工作目录。 */
    private static void checkoutFile(String commitId, String fileName) {
        Commit commit;
        if (commitId == null) {
            commit = headCommit();
        } else {
            String sha = findCommit(commitId);
            if (sha == null) {
                throw error("No commit with that id exists.");
            }
            commit = readCommit(sha);
        }
        String blobSha = blobInCommit(commit, fileName);
        if (blobSha == null) {
            throw error("File does not exist in that commit.");
        }
        writeContents(join(CWD, fileName), readBlob(blobSha));
    }

    /** Checks out BRANCH_NAME, making it the current branch.
     *  检出 BRANCH_NAME 并使其成为当前分支。 */
    private static void checkoutBranch(String branchName) {
        if (!branchExists(branchName)) {
            throw error("No such branch exists.");
        }
        if (branchName.equals(getHeadBranch())) {
            throw error("No need to checkout the current branch.");
        }
        Commit target = readCommit(getBranchSha(branchName));
        if (wouldBeOverwritten(target, getHeadCommitSha())) {
            throw error("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
        checkoutCommit(target);
        setHeadBranch(branchName);
        writeStaging(new StagingArea());
    }

    /** Prints a single log entry for the commit identified by SHA.
     *  为 id 为 SHA 的 commit 打印一条 log 条目。 */
    private static void printCommitEntry(String sha, Commit commit) {
        message("===");
        message("commit %s", sha);
        if (commit.getParent2() != null) {
            message("Merge: %s %s", shortId(commit.getParent()),
                    shortId(commit.getParent2()));
        }
        message("Date: %s", DATE_FORMAT.format(commit.getTimestamp()));
        message("%s", commit.getMessage());
        message("");
    }

    /** Returns the first seven characters of SHA. / 返回 SHA 的前七个字符。 */
    private static String shortId(String sha) {
        if (sha == null) {
            return "null";
        }
        return sha.length() <= 7 ? sha : sha.substring(0, 7);
    }
}
