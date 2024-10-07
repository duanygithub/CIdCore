package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.CIdPointerType;
import dev.duanyper.cidcore.symbols.CIdType;

import java.io.UnsupportedEncodingException;

public class CIdPOINTER implements Variable {
    int addr;
    int level = 0;
    CIdType targetType;

    protected CIdPOINTER(int address, int lvl, CIdType type) {
        addr = address;
        level = lvl;
        targetType = type;
    }

    public static CIdPOINTER createPOINTER(int lvl, int pAddress, CIdType type) throws CIdRuntimeException {
        int address = MemOperator.allocateMemory(4);
        MemOperator.writeInt(address, pAddress);
        return new CIdPOINTER(address, lvl, type);
    }

    public static CIdPOINTER createWithAllocatedAddress(int address, int lvl, CIdType type) {
        return new CIdPOINTER(address, lvl, type);
    }

    public int setValue(int address) throws CIdRuntimeException {
        return MemOperator.writeInt(addr, address);
    }

    @Override
    public Integer getValue() throws CIdRuntimeException {
        return MemOperator.readInt(addr);
    }

    @Override
    public CIdPointerType getType() {
        return CIdType.createPointerType(level, targetType);
    }

    public CIdType getTargetType() {
        return targetType;
    }

    @Override
    public int getAddress() {
        return addr;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public Variable procOperation(Variable var, String op) throws CIdRuntimeException {
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
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() & var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "|=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() | var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "^=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() ^ var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case ">>=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() >> var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "<<=" -> {
                if (var.getType() != CIdType.Int) return null;
                int value = setValue(getValue() << var.getValue().intValue());
                return createPOINTER(value, level, targetType);
            }
            case "++" -> {
                int value = 0;
                if (targetType instanceof CIdPointerType || targetType == CIdType.Int || targetType == CIdType.Void || targetType == CIdType.Float) {
                    value = setValue(getValue() + 4);
                } else if (targetType == CIdType.Char || targetType == CIdType.Boolean) {
                    value = setValue(getValue() + 1);
                }
                return createPOINTER(value, level, targetType);
            }
            case "--" -> {
                int value = setValue(getValue() - 1);
                return createPOINTER(value, level, targetType);
            }
        }

        if (var.getType().equals(CIdType.Int)) {
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
            int value = getValue();
            if (getTargetType() == CIdType.Char
                    && getLevel() == 1) {
                StringBuilder sb = new StringBuilder();
                int i = value, strlen = 0;
                try {
                    int b = MemOperator.readInt(i);
                    while (b != 0) {
                        strlen += 4;
                        b = MemOperator.readInt(i);
                        i += 4;
                    }
                } catch (CIdRuntimeException ignore) {
                }
                if (strlen == 0) {
                    return ("");
                } else {
                    strlen -= 4;
                    byte[] bytes = MemOperator.read(value, strlen);
                    try {
                        return (new String(bytes, "UTF-32"));
                    } catch (UnsupportedEncodingException ignore) {
                    }
                }
            } else {
                try {
                    return String.format("%x", MemOperator.readInt(addr));
                } catch (CIdRuntimeException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (CIdRuntimeException ignore) {
        }
        try {
            return String.format("%x", MemOperator.readInt(addr));
        } catch (CIdRuntimeException e1) {
            throw new RuntimeException(e1);
        }
    }

    @Override
    public int sizeOf() {
        return 4;
    }
}
