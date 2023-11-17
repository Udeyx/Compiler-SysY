package backend;

import backend.directive.Direct;
import backend.instr.Instr;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Target {
    private static final Target TARGET = new Target();
    private final ArrayList<Direct> directs;
    private final ArrayList<Instr> instrs;

    private Target() {
        this.instrs = new ArrayList<>();
        this.directs = new ArrayList<>();
    }

    public static Target getInstance() {
        return TARGET;
    }

    public void addDirect(Direct direct) {
        directs.add(direct);
    }

    public void addInstr(Instr instr) {
        instrs.add(instr);
    }

    @Override
    public String toString() {
        return directs.stream().map(Direct::toString)
                .collect(Collectors.joining("\n")) + "\n"
                + instrs.stream().map(Instr::toString)
                .collect(Collectors.joining("\n"));
    }
}
