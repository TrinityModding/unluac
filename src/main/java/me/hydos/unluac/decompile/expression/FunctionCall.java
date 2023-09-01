package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class FunctionCall extends Expression {

    public Expression function;
    public Expression[] arguments;
    public boolean multiple;

    public FunctionCall(Expression function, Expression[] arguments, boolean multiple) {
        super(PRECEDENCE_ATOMIC);
        this.function = function;
        this.arguments = arguments;
        this.multiple = multiple;
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
        function.walk(w);
        for (var expression : arguments) {
            expression.walk(w);
        }
    }

    @Override
    public void print(Decompiler decompiler, Output out) {
        var args = new ArrayList<Expression>(arguments.length);
        if (isMethodCall()) {
            var obj = function.getTable();
            if (obj.isUngrouped()) {
                out.print("(");
                obj.print(decompiler, out);
                out.print(")");
            } else {
                obj.print(decompiler, out);
            }
            out.print(":");
            out.print(function.getField());
            args.addAll(Arrays.asList(arguments).subList(1, arguments.length));
        } else {
            if (function.isUngrouped()) {
                out.print("(");
                function.print(decompiler, out);
                out.print(")");
            } else {
                function.print(decompiler, out);
            }
            Collections.addAll(args, arguments);
        }
        out.print("(");
        printSequence(decompiler, out, args, false, true);
        out.print(")");
    }

    @Override
    public void printMultiple(Decompiler d, Output out) {
        if (!multiple) {
            out.print("(");
        }
        print(d, out);
        if (!multiple) {
            out.print(")");
        }
    }

    @Override
    public int getConstantIndex() {
        var index = function.getConstantIndex();
        for (var argument : arguments) {
            index = Math.max(argument.getConstantIndex(), index);
        }
        return index;
    }

    @Override
    public boolean beginsWithParen() {
        if (isMethodCall()) {
            var obj = function.getTable();
            return obj.isUngrouped() || obj.beginsWithParen();
        } else {
            return function.isUngrouped() || function.beginsWithParen();
        }
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    private boolean isMethodCall() {
        return function.isMemberAccess() && arguments.length > 0 && function.getTable() == arguments[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (FunctionCall) o;
        return multiple == that.multiple && Objects.equals(function, that.function) && Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(function, multiple);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }
}
