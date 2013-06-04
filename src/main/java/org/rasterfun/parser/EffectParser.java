package org.rasterfun.parser;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.MemoMismatches;
import org.parboiled.annotations.SuppressSubnodes;
import org.rasterfun.effect.Effect;
import org.rasterfun.parser.ast.Expr;
import org.rasterfun.parser.ast.Num;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Parser for raster generator files.
 */
public class EffectParser extends BaseParser<Expr> {

    protected static final String EFFECT = "effect";
    protected static final String VAR = "var";
    protected static final String VAL = "val";
    protected static final String INPUT = "input";
    protected static final String OUTPUT = "output";
    protected static final String CHANNEL= "channel";
    protected static final String DEFAULT = "default";

    protected static final String AND = "and";
    protected static final String OR = "or";
    protected static final String NOT = "not";

    protected static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(
            EFFECT, VAR, VAL, INPUT, OUTPUT, CHANNEL, DEFAULT, AND, OR, NOT));

    public EffectParser() {
    }

    protected Rule Annotations() {
        return null; // TODO parse annotations and store them as metadata.
    }

    protected Rule Effect() {
        // TODO: Parse to composite effect
        return Sequence(
                Annotations(),
                EFFECT, WhiteSpace(),
                Identifier(), WhiteSpace(), // TODO: Get identifier
                "{ ",
                ZeroOrMore(Statement()),
                "} "
        );
    }

    protected Rule Statement() {
        return null; // TODO: Assignment or effect invocation.
    }

    @SuppressSubnodes
    @MemoMismatches
    protected Rule Identifier() {
        return Sequence(
                Letter(),
                ZeroOrMore(LetterOrDigit())
        );
    }

    protected Rule Letter() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_');
    }

    @MemoMismatches
    protected Rule LetterOrDigit() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_');
    }

    protected Rule Number() {
        return Sequence(
                Sequence(
                        Optional(Ch('-')),
                        OneOrMore(Digit()),
                        Optional(
                                Ch('.'),
                                OneOrMore(Digit())
                        )
                ),
                push(new Num(Double.parseDouble(matchOrDefault("1")))),
                WhiteSpace()
        );
    }

    protected Rule Digit() {
        return CharRange('0', '9');
    }


    protected Rule WhiteSpace() {
        return ZeroOrMore(AnyOf(" \t\f\n\r"));
    }

    /** If a keyword or other string ends with a space, match any trailing whitespace.  */
    @Override
    protected Rule fromStringLiteral(String string) {
        return string.endsWith(" ") ?
               Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace()) :
               String(string);
    }

}
