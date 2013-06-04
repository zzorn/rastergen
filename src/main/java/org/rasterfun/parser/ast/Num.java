package org.rasterfun.parser.ast;

/**
 *
 */
public class Num implements Expr {
    private final double value;

    public Num(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
