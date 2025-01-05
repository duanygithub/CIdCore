package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.CIdType;

public class CIdFLOAT implements Variable {
    final long addr;

    private CIdFLOAT(long address) {
        addr = address;
    }

    public static CIdFLOAT createFLOAT(String str) throws CIdRuntimeException {
        return createFLOAT(Float.parseFloat(str));
    }

    public static CIdFLOAT createFLOAT(float f) throws CIdRuntimeException {
        long address = MemOperator.allocateMemory(4);
        MemOperator.writeFloat(address, f);
        return new CIdFLOAT(address);
    }

    public static CIdFLOAT createFLOAT() {
        return new CIdFLOAT(MemOperator.allocateMemory(4));
    }

    public static CIdFLOAT createWithAllocatedAddress(int address) {
        return new CIdFLOAT(address);
    }

    public float setValue(float f) throws CIdRuntimeException {
        MemOperator.writeFloat(addr, f);
        return f;
    }

    @Override
    public Float getValue() throws CIdRuntimeException {
        return MemOperator.readFloat(addr);
    }

    @Override
    public CIdType getType() {
        return CIdType.Float;
    }

    @Override
    public long getAddress() {
        return addr;
    }

    @Override
    public Variable procOperation(Variable var, String op) throws CIdRuntimeException {
        switch (op) {
            case "=" -> {
                float value = setValue(var.getValue().floatValue());
                return createFLOAT(value);
            }
            case "+=" -> {
                float value = setValue(var.getValue().floatValue() + getValue());
                return createFLOAT(value);
            }
            case "-=" -> {
                float value = setValue(var.getValue().floatValue() - getValue());
                return createFLOAT(value);
            }
            case "*=" -> {
                float value = setValue(var.getValue().floatValue() * getValue());
                return createFLOAT(value);
            }
            case "/=" -> {
                float value = setValue((int) (getValue() / var.getValue().floatValue()));
                return createFLOAT(value);
            }
        }
        if (var.getType().equals(CIdType.Float)) {
            float value = getValue();
            return switch (op) {
                case "+" -> createFLOAT(value + (float) var.getValue());
                case "-" -> createFLOAT(value - (float) var.getValue());
                case "*" -> createFLOAT(value * (float) var.getValue());
                case "/" -> createFLOAT(value / (float) var.getValue());
                case "%" -> createFLOAT(value % (float) var.getValue());
                default -> null;
            };
        } else if (var.getType().equals(CIdType.Int)) {
            float value = getValue();
            return switch (op) {
                case "+" -> createFLOAT(value + (int) var.getValue());
                case "-" -> createFLOAT(value - (int) var.getValue());
                case "*" -> createFLOAT(value * (int) var.getValue());
                case "/" -> createFLOAT(value / (int) var.getValue());
                case "%" -> createFLOAT(value % (int) var.getValue());
                default -> null;
            };
        } else return null;
    }

    @Override
    public int cmp(Variable var) throws CIdRuntimeException {
        float value = getValue();
        float val = var.getValue().floatValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }

    @Override
    public String toString() {
        try {
            return ((Float) MemOperator.readFloat(addr)).toString();
        } catch (CIdRuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int sizeOf() {
        return 4;
    }
}
