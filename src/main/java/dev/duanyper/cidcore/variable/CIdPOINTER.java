package dev.duanyper.cidcore.variable;

import dev.duanyper.cidcore.exception.CIdRuntimeException;
import dev.duanyper.cidcore.memory.MemOperator;
import dev.duanyper.cidcore.symbols.CIdPointerType;
import dev.duanyper.cidcore.symbols.CIdType;

import java.io.UnsupportedEncodingException;

public class CIdPOINTER implements Variable {
    final long addr;
    int level;
    final CIdType targetType;

    protected CIdPOINTER(long address, int lvl, CIdType type) {
        addr = address;
        level = lvl;
        targetType = type;
    }

    public static CIdPOINTER createPOINTER(int lvl, long pAddress, CIdType type) throws CIdRuntimeException {
        long address = MemOperator.getPool().allocateMemory(4);
        MemOperator.writeInt(address, (int) pAddress);
        return new CIdPOINTER(address, lvl, type);
    }

    public static CIdPOINTER createWithAllocatedAddress(long address, int lvl, CIdType type) {
        return new CIdPOINTER(address, lvl, type);
    }

    public long setValue(long address) throws CIdRuntimeException {
        return MemOperator.writeLong(addr, address);
    }

    @Override
    public Long getValue() throws CIdRuntimeException {
        return MemOperator.readLong(addr);
    }

    @Override
    public CIdPointerType getType() {
        return CIdType.createPointerType(level, targetType);
    }

    public CIdType getTargetType() {
        return targetType;
    }

    @Override
    public long getAddress() {
        return addr;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public Variable procOperation(Variable var, String op) throws CIdRuntimeException {
        switch (op) {
            case "=" -> {
                long value = setValue(var.getValue().longValue());
                return createPOINTER(level, value, targetType);
            }
            case "+=" -> {
                long value = setValue(var.getValue().longValue() + getValue());
                return createPOINTER(level, value, targetType);
            }
            case "-=" -> {
                long value = setValue(var.getValue().longValue() - getValue());
                return createPOINTER(level, value, targetType);
            }
            case "*=" -> {
                long value = setValue(var.getValue().longValue() * getValue());
                return createPOINTER(level, value, targetType);
            }
            case "/=" -> {
                long value = setValue((int) (getValue() / var.getValue().floatValue()));
                return createPOINTER(level, value, targetType);
            }
            case "%=" -> {
                long value = setValue(getValue() % var.getValue().longValue());
                return createPOINTER(level, value, targetType);
            }
            case "&=" -> {
                if (var.getType() != CIdType.Int) return null;
                long value = setValue(getValue() & var.getValue().longValue());
                return createPOINTER(level, value, targetType);
            }
            case "|=" -> {
                if (var.getType() != CIdType.Int) return null;
                long value = setValue(getValue() | var.getValue().longValue());
                return createPOINTER(level, value, targetType);
            }
            case "^=" -> {
                if (var.getType() != CIdType.Int) return null;
                long value = setValue(getValue() ^ var.getValue().longValue());
                return createPOINTER(level, value, targetType);
            }
            case ">>=" -> {
                if (var.getType() != CIdType.Int) return null;
                long value = setValue(getValue() >> var.getValue().longValue());
                return createPOINTER(level, value, targetType);
            }
            case "<<=" -> {
                if (var.getType() != CIdType.Int) return null;
                long value = setValue(getValue() << var.getValue().longValue());
                return createPOINTER(level, value, targetType);
            }
            case "++" -> {
                long value = 0;
                if (targetType instanceof CIdPointerType || targetType == CIdType.Int || targetType == CIdType.Void || targetType == CIdType.Float) {
                    value = setValue(getValue() + 4);
                } else if (targetType == CIdType.Char || targetType == CIdType.Boolean) {
                    value = setValue(getValue() + 1);
                }
                return createPOINTER(level, value, targetType);
            }
            case "--" -> {
                long value = setValue(getValue() - 1);
                return createPOINTER(level, value, targetType);
            }
        }

        if (var.getType().equals(CIdType.Int)) {
            long value = getValue();
            return switch (op) {
                case "+" -> createPOINTER(level, value + var.getValue().longValue(), targetType);
                case "-" -> createPOINTER(level, value - var.getValue().longValue(), targetType);
                case "*" -> createPOINTER(level, value * var.getValue().longValue(), targetType);
                case "/" -> createPOINTER(level, value / var.getValue().longValue(), targetType);
                case "%" -> createPOINTER(level, value % var.getValue().longValue(), targetType);
                case ">>" -> createPOINTER(level, value >> var.getValue().longValue(), targetType);
                case "<<" -> createPOINTER(level, value << var.getValue().longValue(), targetType);
                case "&" -> createPOINTER(level, value & var.getValue().longValue(), targetType);
                case "|" -> createPOINTER(level, value | var.getValue().longValue(), targetType);
                case "~" -> createPOINTER(level, ~value, targetType);
                case "!" -> createPOINTER(level, value == 0 ? 1 : 0, targetType);
                case "^" -> createPOINTER(level, value ^ var.getValue().longValue(), targetType);
                default -> null;
            };
        } else return null;
    }

    @Override
    public int cmp(Variable var) throws CIdRuntimeException {
        long value = getValue();
        float val = var.getValue().floatValue();
        if (val > value) return 1;
        else if (val < value) return -1;
        else if (val == value) return 0;
        throw new AssertionError();
    }

    public boolean isString() {
        return getLevel() == 1 && getTargetType() == CIdType.Char;
    }

    @Override
    public String toString() {
        try {
            long value = getValue();
            if (isString()) {
                long i = value;
                int strlen = 0;
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
                    return String.format("0x%x", MemOperator.readInt(addr));
                } catch (CIdRuntimeException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (CIdRuntimeException ignore) {
        }
        try {
            return String.format("0x%x", MemOperator.readInt(addr));
        } catch (CIdRuntimeException e1) {
            throw new RuntimeException(e1);
        }
    }

    @Override
    public int sizeOf() {
        return 4;
    }
}
