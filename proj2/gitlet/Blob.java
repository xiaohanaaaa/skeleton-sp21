package gitlet;

/** Represents the saved contents of a single file.
 *  Blobs are content-addressable: a blob's id is the SHA-1 of its contents,
 *  so two files with identical contents share the same blob.
 *
 *  表示单个文件被保存的内容。
 *  blob 是内容寻址的：其 id 就是内容的 SHA-1，因此内容相同的两个文件共用
 *  同一个 blob。
 *
 *  @author TODO
 */
public class Blob implements Dumpable {

    /** The raw byte contents of the file. / 文件的原始字节内容。 */
    private byte[] contents;

    /** Creates a Blob holding CONTENTS. / 创建一个持有 CONTENTS 的 Blob。 */
    public Blob(byte[] contents) {
        this.contents = contents;
    }

    /** Returns the raw byte contents of this blob.
     *  返回该 blob 的原始字节内容。 */
    public byte[] getContents() {
        return contents;
    }

    @Override
    public void dump() {
        System.out.printf("blob length: %d bytes%n", contents.length);
    }
}
