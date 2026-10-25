package gitlet;

import java.util.Arrays;

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
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }
        try {
            switch (args[0]) {
                case "init":
                    requireArgs(args, 1);
                    Repository.init();
                    break;
                case "add":
                    requireArgs(args, 2);
                    Repository.add(args[1]);
                    break;
                case "commit":
                    requireArgs(args, 2);
                    Repository.commit(args[1]);
                    break;
                case "rm":
                    requireArgs(args, 2);
                    Repository.rm(args[1]);
                    break;
                case "log":
                    requireArgs(args, 1);
                    Repository.log();
                    break;
                case "global-log":
                    requireArgs(args, 1);
                    Repository.globalLog();
                    break;
                case "find":
                    requireArgs(args, 2);
                    Repository.find(args[1]);
                    break;
                case "status":
                    requireArgs(args, 1);
                    Repository.status();
                    break;
                case "checkout":
                    Repository.checkout(Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "branch":
                    requireArgs(args, 2);
                    Repository.branch(args[1]);
                    break;
                case "rm-branch":
                    requireArgs(args, 2);
                    Repository.rmBranch(args[1]);
                    break;
                case "reset":
                    requireArgs(args, 2);
                    Repository.reset(args[1]);
                    break;
                case "merge":
                    requireArgs(args, 2);
                    Repository.merge(args[1]);
                    break;
                default:
                    message("No command with that name exists.");
                    System.exit(0);
            }
        } catch (GitletException excp) {
            message(excp.getMessage());
            System.exit(0);
        }
    }

    /** Throws an "Incorrect operands." error unless ARGS has exactly N entries.
     *  若 ARGS 的条目数不等于 N，则抛出 "Incorrect operands." 错误。 */
    private static void requireArgs(String[] args, int n) {
        if (args.length != n) {
            throw Utils.error("Incorrect operands.");
        }
    }
}
