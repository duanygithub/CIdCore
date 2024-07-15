package net.duany.ciCore.variable;

import net.duany.ciCore.memory.MemOperator;
import net.duany.ciCore.symbols.Keywords;

public class CIdPOINTER implements Variable {
    int addr;
    int level = 0;
    Keywords targetType;

    protected CIdPOINTER(int address, int lvl, Keywords type) {
        addr = address;
        level = lvl;
    }

    public static CIdPOINTER createPOINTER(int lvl, int pAddress, Keywords type) {
        int address = MemOperator.allocateMemory(4);
        MemOperator.writeInt(address, pAddress);
        return new CIdPOINTER(address, lvl, type);
    }

    public int setValue(int address) {
        return MemOperator.writeInt(addr, address);
    }

    @Override
    public Integer getValue() {
        return MemOperator.readInt(addr);
    }

    @Override
    public Keywords getType() {
        return Keywords.Pointer;
    }

    @Override
    public int getAddress() {
        return addr;
    }

    @Override
    public Variable procOperation(Variable var, String op) {
        switch (op) {
            case "=" -> {
                int value = setValue(var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "+=" -> {
                int value = setValue(var.getValue().intValue() + getValue());
                return createPOINTER(value, level, targetType);
            }
            case "-=" -> {
                int value = setValue(var.getValue().intValue() - getValue());
                return createPOINTER(value, level, targetType);
            }
            case "*=" -> {
                int value = setValue(var.getValue().intValue() * getValue());
                return createPOINTER(value, level, targetType);
            }
            case "/=" -> {
                int value = setValue((int) (getValue() / var.getValue().floatValue()));
                return createPOINTER(value, level, targetType);
            }
            case "%=" -> {
                int value = setValue(getValue() % var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "&=" -> {
                if (var.getType() != Keywords.Int) return null;
                int value = setValue(getValue() & var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "|=" -> {
                if (var.getType() != Keywords.Int) return null;
                int value = setValue(getValue() | var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "^=" -> {
                if (var.getType() != Keywords.Int) return null;
                int value = setValue(getValue() ^ var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case ">>=" -> {
                if (var.getType() != Keywords.Int) return null;
                int value = setValue(getValue() >> var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "<<=" -> {
                if (var.getType() != Keywords.Int) return null;
                int value = setValue(getValue() << var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "++" -> {
                int value = setValue(getValue() + 1);
                return createPOINTER(value, level, targetType);
            }
            case "--" -> {
                int value = setValue(getValue() - 1);
                return createPOINTER(value, level, targetType);
            }
        }

        if (var.getType().equals(Keywords.Int)) {
            int value = getValue();
            return switch (op) {
                case "+" -> createPOINTER(value + var.getValue().intValue(), level, targetType);
                case "-" -> createPOINTER(value - var.getValue().intValue(), level, targetType);
                case "*" -> createPOINTER(value * var.getValue().intValue(), level, targetType);
                case "/" -> createPOINTER(value / var.getValue().intValue(), level, targetType);
                case "%" -> createPOINTER(value % var.getValue().intValue(), level, targetType);
                case ">>" -> createPOINTER(value >> var.getValue().intValue(), level, targetType);
                case "<<" -> createPOINTER(value << var.getValue().intValue(), level, targetType);
                case "&" -> createPOINTER(value & var.getValue().intValue(), level, targetType);
                case "|" -> createPOINTER(value | var.getValue().intValue(), level, targetType);
                case "~" -> createPOINTER(~value, level, targetType);
                case "!" -> createPOINTER(value == 0 ? 1 : 0, level, targetType);
                case "^" -> createPOINTER(value ^ var.getValue().intValue(), level, targetType);
                default -> null;
            };
        } else return null;
    }

    @Override
    public int cmp(Variable var) {
        int value = getValue();
        float val = var.getValue().floatValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }

    @Override
    public String toString() {
        return String.format("%x", MemOperator.readInt(addr));
    }
}
