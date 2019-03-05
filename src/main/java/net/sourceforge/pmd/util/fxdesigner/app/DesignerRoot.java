/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.app;

import org.reactfx.value.Val;

import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.fxdesigner.app.NodeSelectionSource.NodeSelectionEvent;

import javafx.stage.Stage;


/**
 * Provides access to the globals of the app.
 *
 * @author Clément Fournier
 */
public interface DesignerRoot {
    /**
     * Gets the logger of the application.
     *
     * @return The logger
     */
    EventLogger getLogger();


    /**
     * Gets the main stage of the application.
     *
     * @return The main stage
     */
    Stage getMainStage();


    /**
     * If true, some more events are pushed to the event log, and
     * console streams are open. This is enabled by the -v or --verbose
     * option on command line for now.
     */
    boolean isDeveloperMode();


    /**
     * Channel used to transmit node selection events to all interested components.
     */
    MessageChannel<NodeSelectionEvent> getNodeSelectionChannel();


    /**
     * Returns the compilation unit of the main editor. Empty if the source
     * is unparsable.
     */
    Val<Node> globalCompilationUnitProperty();


    /**
     * Returns the language version selected on the app. Never empty.
     */
    Val<LanguageVersion> globalLanguageVersionProperty();


    default LanguageVersion getGlobalLanguageVersion() {
        return globalLanguageVersionProperty().getValue();
    }

    Val<Boolean> isCtrlDownProperty();
}
