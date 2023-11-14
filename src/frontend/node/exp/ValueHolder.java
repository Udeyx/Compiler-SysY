package frontend.node.exp;

import midend.ir.Value.Value;

public interface ValueHolder {
    Value buildExpIR();
}
