/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util.fxdesigner.app;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reactfx.value.Val;
import org.reactfx.value.Var;

import net.sourceforge.pmd.util.fxdesigner.app.services.AppServiceDescriptor;
import net.sourceforge.pmd.util.fxdesigner.app.services.CloseableService;
import net.sourceforge.pmd.util.fxdesigner.app.services.EventLoggerImpl;
import net.sourceforge.pmd.util.fxdesigner.app.services.GlobalDiskManagerImpl;
import net.sourceforge.pmd.util.fxdesigner.app.services.LogEntry;
import net.sourceforge.pmd.util.fxdesigner.app.services.LogEntry.Category;
import net.sourceforge.pmd.util.fxdesigner.app.services.OnDiskPersistenceManager;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


/**
 * Interface for the singleton of the app.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public final class DesignerRootImpl implements DesignerRoot {


    private final Stage mainStage;
    private final boolean developerMode;
    private final Var<Boolean> isCtrlDown = Var.newSimpleVar(false);


    private final Map<AppServiceDescriptor<?>, Object> services = new HashMap<>();
    private final Map<Set<AppServiceDescriptor<?>>, Runnable> hooks = new ConcurrentHashMap<>();


    public DesignerRootImpl(Stage mainStage, DesignerParams params) {
        this.mainStage = mainStage;
        this.developerMode = params.isDeveloperMode();

        registerService(LOGGER, new EventLoggerImpl(this));

        // vetoed by any other key press, so that eg CTRL+V repeatedly vetoes it
        mainStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> isCtrlDown.setValue(
            e.isControlDown() && e.getCode() == KeyCode.CONTROL));
        mainStage.addEventHandler(KeyEvent.KEY_RELEASED, e -> isCtrlDown.setValue(
            e.isControlDown() && e.getCode() == KeyCode.CONTROL));

        GlobalDiskManagerImpl diskManager = new GlobalDiskManagerImpl(this, params.getSettingsDirectory());
        registerService(DISK_MANAGER, diskManager);

        params.processDefaults(diskManager.defaultAppStateFile());

        registerService(PERSISTENCE_MANAGER, new OnDiskPersistenceManager(this, params.getPersistedInputFile(), params.getPersistedOutputFile()));
        registerService(NODE_SELECTION_CHANNEL, new MessageChannel<>(Category.SELECTION_EVENT_TRACING));
        registerService(LATEST_XPATH, new MessageChannel<>(Category.SELECTION_EVENT_TRACING));
    }


    @Override
    public Stage getMainStage() {
        return mainStage;
    }


    @Override
    public boolean isDeveloperMode() {
        return developerMode;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(AppServiceDescriptor<T> descriptor) {
        return (T) services.get(descriptor);
    }

    @Override
    public void afterServiceRegistered(Runnable run, AppServiceDescriptor<?>... descriptors) {
        if (services.keySet().containsAll(Arrays.asList(descriptors))) {
            run.run();
        } else {
            hooks.merge(
                new HashSet<>(Arrays.asList(descriptors)),
                run,
                (set, prev) -> () -> {
                    prev.run();
                    run.run();
                }
            );
        }
    }

    @Override
    public <T> void registerService(AppServiceDescriptor<T> descriptor, T component) {
        if (getService(LOGGER) != null) {
            // event the logger needs to be registered hehe
            getService(LOGGER).logEvent(LogEntry.serviceRegistered(descriptor, component));
        }
        services.put(descriptor, component);

        new HashMap<>(hooks).forEach((set, run) -> {
            if (services.keySet().containsAll(set)) {
                run.run();
                hooks.remove(set);
            }
        });
    }

    @Override
    public Val<Boolean> isCtrlDownProperty() {
        return isCtrlDown;
    }

    @Override
    public void shutdownServices() {
        services.forEach((descriptor, component) -> {
            if (component instanceof CloseableService) {
                try {
                    ((CloseableService) component).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
