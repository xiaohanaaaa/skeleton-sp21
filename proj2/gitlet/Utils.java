package gitlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;


/** Assorted utilities.
 *
 * Give this file a good read as it provides several useful utility functions
 * to save you some time.
 *
 * 杂项工具类。
 * 请仔细阅读本文件，它提供了若干实用的工具方法，可以帮你节省时间。
 *
 *  @author P. N. Hilfinger
 */
class Utils {

    /** The length of a complete SHA-1 UID as a hexadecimal numeral.
     *  完整 SHA-1 UID 的十六进制位数。 */
    static final int UID_LENGTH = 40;

    /* SHA-1 HASH VALUES. / SHA-1 哈希值。 */

    /** Returns the SHA-1 hash of the concatenation of VALS, which may
     *  be any mixture of byte arrays and Strings.
     *  返回 VALS 拼接后的 SHA-1 哈希值；VALS 可以是字节数组与字符串的任意混合。 */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /** Returns the SHA-1 hash of the concatenation of the strings in
     *  VALS.
     *  返回 VALS 中字符串拼接后的 SHA-1 哈希值。 */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* FILE DELETION / 文件删除 */

    /** Deletes FILE if it exists and is not a directory.  Returns true
     *  if FILE was deleted, and false otherwise.  Refuses to delete FILE
     *  and throws IllegalArgumentException unless the directory designated by
     *  FILE also contains a directory named .gitlet.
     *  若 FILE 存在且不是目录，则删除之。删除成功返回 true，否则返回 false。
     *  除非 FILE 所在目录下也包含名为 .gitlet 的目录，否则拒绝删除并抛出
     *  IllegalArgumentException。 */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /** Deletes the file named FILE if it exists and is not a directory.
     *  Returns true if FILE was deleted, and false otherwise.  Refuses
     *  to delete FILE and throws IllegalArgumentException unless the
     *  directory designated by FILE also contains a directory named .gitlet.
     *  若名为 FILE 的文件存在且不是目录，则删除之。删除成功返回 true，否则返回 false。
     *  除非 FILE 所在目录下也包含名为 .gitlet 的目录，否则拒绝删除并抛出
     *  IllegalArgumentException。 */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS / 读写文件内容 */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems.
     *  以字节数组返回 FILE 的全部内容。FILE 必须是普通文件。
     *  出错时抛出 IllegalArgumentException。 */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return the entire contents of FILE as a String.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems.
     *  以字符串返回 FILE 的全部内容。FILE 必须是普通文件。
     *  出错时抛出 IllegalArgumentException。 */
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /** Write the result of concatenating the bytes in CONTENTS to FILE,
     *  creating or overwriting it as needed.  Each object in CONTENTS may be
     *  either a String or a byte array.  Throws IllegalArgumentException
     *  in case of problems.
     *  将 CONTENTS 中各字节拼接后的结果写入 FILE，必要时创建或覆盖。
     *  CONTENTS 中每个对象可以是字符串或字节数组。
     *  出错时抛出 IllegalArgumentException。 */
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                    new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     *  Throws IllegalArgumentException in case of problems.
     *  从 FILE 读取一个 T 类型对象并转换为 EXPECTEDCLASS。出错时抛出
     *  IllegalArgumentException。 */
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Write OBJ to FILE. / 将 OBJ 写入 FILE。 */
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }

    /* DIRECTORIES / 目录 */

    /** Filter out all but plain files. / 只保留普通文件，过滤掉其余项。 */
    private static final FilenameFilter PLAIN_FILES =
        new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        };

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory.
     *  按 Java 字符串字典序返回目录 DIR 下所有普通文件的文件名列表。
     *  若 DIR 不是目录则返回 null。 */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /** Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory.
     *  按 Java 字符串字典序返回目录 DIR 下所有普通文件的文件名列表。
     *  若 DIR 不是目录则返回 null。 */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* OTHER FILE UTILITIES / 其它文件工具 */

    /** Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths.#get(String, String[])}
     *  method.
     *  将 FIRST 与 OTHERS 拼接为一个 File 路径，类似于
     *  {@link java.nio.file.Paths.#get(String, String[])} 方法。 */
    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /** Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths.#get(String, String[])}
     *  method.
     *  将 FIRST 与 OTHERS 拼接为一个 File 路径，类似于
     *  {@link java.nio.file.Paths.#get(String, String[])} 方法。 */
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }


    /* SERIALIZATION UTILITIES / 序列化工具 */

    /** Returns a byte array containing the serialized contents of OBJ.
     *  返回包含 OBJ 序列化内容的字节数组。 */
    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing commit.");
        }
    }



    /* MESSAGES AND ERROR REPORTING / 消息与错误报告 */

    /** Return a GitletException whose message is composed from MSG and ARGS as
     *  for the String.format method.
     *  返回一个 GitletException，其消息按 String.format 规则由 MSG 与 ARGS 组成。 */
    static GitletException error(String msg, Object... args) {
        return new GitletException(String.format(msg, args));
    }

    /** Print a message composed from MSG and ARGS as for the String.format
     *  method, followed by a newline.
     *  按 String.format 规则打印由 MSG 与 ARGS 组成的消息，并换行。 */
    static void message(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }
}
