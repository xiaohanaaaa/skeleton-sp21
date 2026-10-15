package gitlet;

import static gitlet.Utils.message;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  Parses command-line arguments, validates operands, dispatches to
 *  {@link Repository}, and reports errors uniformly.
 *
 *  Gitlet 的驱动类，是 Git 版本控制系统的一个子集。
 *  负责解析命令行参数、校验操作数、分派到 {@link Repository}，并统一报告错误。
 *
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main <COMMAND> <OPERAND1> <OPERAND2> ...
     *  Dispatches to the corresponding Repository command.
     *
     *  用法：java gitlet.Main <命令> <操作数1> <操作数2> ...
     *  分派到对应的 Repository 命令。 */
    public static void main(String[] args) {
        // TODO
    }

    /** Throws an "Incorrect operands." error unless ARGS has exactly N entries.
     *  若 ARGS 的条目数不等于 N，则抛出 "Incorrect operands." 错误。 */
    private static void requireArgs(String[] args, int n) {
        // TODO
    }
}
