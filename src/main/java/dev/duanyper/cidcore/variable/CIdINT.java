package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.CIdType;

public class CIdINT implements Variable {
    final long addr;

    protected CIdINT(long address) {
        addr = address;
    }

    public static CIdINT createINT(String str) throws CIdRuntimeException {
        return createINT(Integer.parseInt(str));
    }

    public static CIdINT createINT(int n) throws CIdRuntimeException {
        long address = MemOperator.allocateMemory(4);
        MemOperator.writeInt(address, n);
        return new CIdINT(address);
    }

    public static CIdINT createINT() {
        return new CIdINT(MemOperator.allocateMemory(4));
    }

    public static CIdINT createWithAllocatedAddress(long address) {
        return new CIdINT(address);
    }

    public int setValue(int n) throws CIdRuntimeException {
        MemOperator.writeInt(addr, n);
        return n;
    }

    @Override
    public Integer getValue() throws CIdRuntimeException {
        return MemOperator.readInt(addr);
    }

    @Override
    public CIdType getType() {
        return CIdType.Int;
    }

    @Override
    public long getAddress() {
        return addr;
    }

    @Override
    public Variable procOperation(Variable var, String op) throws CIdRuntimeException {
        switch (op) {
            case "=" -> {
                int value = setValue(var.getValue().intValue());
                return createINT(value);
            }
            case "+=" -> {
                int value = setValue(var.getValue().intValue() + getValue());
                return createINT(value);
            }
            case "-=" -> {
                int value = setValue(var.getValue().intValue() - getValue());
                return createINT(value);
            }
            case "*=" -> {
                int value = setValue(var.getValue().intValue() * getValue());
                return createINT(value);
            }
            case "/=" -> {
                int value = setValue((int) (getValue() / var.getValue().floatValue()));
                return createINT(value);
            }
            case "%=" -> {
                int value = setValue(getValue() % var.getValue().intValue());
                return createINT(value);
            }
            case "&=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() & var.getValue().intValue());
                return createINT(value);
            }
            case "|=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() | var.getValue().intValue());
                return createINT(value);
            }
            case "^=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() ^ var.getValue().intValue());
                return createINT(value);
            }
            case ">>=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() >> var.getValue().intValue());
                return createINT(value);
            }
            case "<<=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() << var.getValue().intValue());
                return createINT(value);
            }
            case "++" -> {
                int value = setValue(getValue() + 1);
                return createINT(value);
            }
            case "--" -> {
                int value = setValue(getValue() - 1);
                return createINT(value);
            }
        }

        if (var.getType().equals(CIdType.Int)) {
            int value = getValue();
            return switch (op) {
                case "+" -> createINT(value + var.getValue().intValue());
                case "-" -> createINT(value - var.getValue().intValue());
                case "*" -> createINT(value * var.getValue().intValue());
                case "/" -> CIdFLOAT.createFLOAT((float) value / var.getValue().intValue());
                case "%" -> createINT(value % var.getValue().intValue());
                case ">>" -> createINT(value >> var.getValue().intValue());
                case "<<" -> createINT(value << var.getValue().intValue());
                case "&" -> createINT(value & var.getValue().intValue());
                case "|" -> createINT(value | var.getValue().intValue());
                case "~" -> createINT(~value);
                case "!" -> createINT(value == 0 ? 1 : 0);
                case "^" -> createINT(value ^ var.getValue().intValue());
                default -> null;
            };
        } else if (var.getType().equals(CIdType.Float)) {
            int value = getValue();
            return switch (op) {
                case "+" -> CIdFLOAT.createFLOAT(value + var.getValue().floatValue());
                case "-" -> CIdFLOAT.createFLOAT(value - var.getValue().floatValue());
                case "*" -> CIdFLOAT.createFLOAT(value * var.getValue().floatValue());
                case "/" -> CIdFLOAT.createFLOAT(value / var.getValue().floatValue());
                case "%" -> CIdFLOAT.createFLOAT(value % var.getValue().floatValue());
                default -> null;
            };
        } else return null;
    }

    @Override
    public int cmp(Variable var) throws CIdRuntimeException {
        int value = getValue();
        float val = var.getValue().floatValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }

    @Override
    public String toString() {
        try {
            return ((Integer) MemOperator.readInt(addr)).toString();
        } catch (CIdRuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int sizeOf() {
        return 4;
    }
}
