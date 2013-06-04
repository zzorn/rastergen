package org.rasterfun.effect;

import org.rasterfun.effect.container.EffectContainer;
import org.rasterfun.effect.variable.InputVariable;
import org.rasterfun.effect.variable.OutputVariable;
import org.rasterfun.effect.variable.VariableExpression;

/**
 * A simple effect that just scales the input and adds an offset.
 */
public class ScaleAddEffect extends EffectBase {

    public final InputVariable in     = addInput("in",     0f, Float.class, "Input value");
    public final InputVariable scale  = addInput("scale",  1f, Float.class, "Value to multiply the input value with");
    public final InputVariable offset = addInput("offset", 0f, Float.class, "Value to add to the result after multiplication.");
    public final OutputVariable out   = addOutput("out", "The result of in * scale + offset.", Float.class, new VariableExpression() {
        @Override
        public String getExpressionString(EffectContainer container, Effect effect, String internalVarPrefix) {
            return in.getExpr() + " * " + scale.getExpr() + " + " + offset.getExpr();
        }
    });

    public ScaleAddEffect() {
        this(1, 0);
    }

    public ScaleAddEffect(float scale) {
        this(scale, 0);
    }

    public ScaleAddEffect(float scale, float offset) {
        this.scale.setValue(scale);
        this.offset.setValue(offset);
    }

}
