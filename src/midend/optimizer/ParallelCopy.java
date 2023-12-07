package midend.optimizer;

import midend.ir.IRBuilder;
import midend.ir.value.Value;
import midend.ir.value.instruction.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ParallelCopy {
    // key is tar, val is src
    private final HashMap<Value, Value> copyMap;
    private final ArrayList<Move> moves;

    public ParallelCopy() {
        this.copyMap = new HashMap<>();
        this.moves = new ArrayList<>();
    }

    public void addCopy(Value tar, Value src) {
        copyMap.put(tar, src);
    }

    public void parallelToSeq() {
        while (!allIsUseless()) {
            Map.Entry<Value, Value> entry = getSimplePair();
            if (entry != null) {
                moves.add(new Move(entry.getKey(), entry.getValue()));
            } else {
                entry = getHardPair();
                if (entry != null) {
                    Value lv = IRBuilder.getInstance().buildLV(entry.getKey().getType());
                    moves.add(new Move(lv, entry.getValue()));
                    copyMap.put(entry.getKey(), lv);
                }
            }
        }
    }

    private boolean allIsUseless() {
        for (Map.Entry<Value, Value> entry : copyMap.entrySet()) {
            if (!entry.getKey().equals(entry.getValue()))
                return false;
        }
        return true;
    }

    private Map.Entry<Value, Value> getHardPair() {
        for (Map.Entry<Value, Value> entry : copyMap.entrySet()) {
            if (!entry.getKey().equals(entry.getValue()))
                return entry;
        }
        return null;
    }

    private Map.Entry<Value, Value> getSimplePair() {
        for (Iterator<Map.Entry<Value, Value>> it = copyMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Value, Value> entry = it.next();
            if (!copyMap.containsValue(entry.getKey())) {
                it.remove();
                return entry;
            }
        }
        return null;
    }

    public void buildMIPS() {
        parallelToSeq();
        moves.forEach(Move::buildMIPS);
    }

    public void buildFIFOMIPS() {
        parallelToSeq();
        moves.forEach(Move::buildFIFOMIPS);
    }

    @Override
    public String toString() {
        parallelToSeq();
        StringBuilder sb = new StringBuilder();
        for (Move move : moves) {
            sb.append("    ");
            sb.append(move);
            sb.append("\n");
        }
        return sb.toString();
    }
}
