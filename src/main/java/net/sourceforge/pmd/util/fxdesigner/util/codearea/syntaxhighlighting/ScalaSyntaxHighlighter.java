/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.util.codearea.syntaxhighlighting;

import java.io.StringReader;

import net.sourceforge.pmd.util.fxdesigner.util.codearea.LexerBasedHighlighter;

/**
 * Syntax highlighter for Java.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public final class ScalaSyntaxHighlighter extends LexerBasedHighlighter {


    public ScalaSyntaxHighlighter() {
        super("scala");
    }

    @Override
    protected JflexLexer newLexer(String text) {
        return new ScalaLexer(new StringReader(text));
    }
}
