package backend.instr;

import util.OpCode;

public class SyscallInstr extends Instr {
    public SyscallInstr() {
        super(OpCode.SYSCALL);
    }

    @Override
    public String toString() {
        return "syscall";
    }
}
