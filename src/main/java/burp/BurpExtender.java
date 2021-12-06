package burp;

import burp.MandrakeMessageTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
TODO:
    Class loading is done every time message tab is loaded and information about loaded JARs is in plugin stdout/err.
    A friendlier implementation would be have a UI element listing the loaded JARs with buttons to add/remove JARs
    similar to how BurpExtender plugins are loaded manually.
    Options:
        * Use ITab to implement a top level UI tab
        * Inside the Java Object window show the list so it's easier to manipulate the list as you're testing
*/

public class BurpExtender implements IBurpExtender, IMessageEditorTabFactory, IContextMenuFactory {

    public static IBurpExtenderCallbacks callbacks;
    public static IExtensionHelpers helpers;
    private PrintStream _stdout;
    private JDSer jdSer;

    //
    // implement IBurpExtender
    //
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();

        this.jdSer = new JDSer(this.helpers, callbacks);
        this._stdout = new PrintStream(callbacks.getStdout());

        // set our extension name
        callbacks.setExtensionName("Mandrake");

        // register ourselves as a message editor tab factory
        callbacks.registerContextMenuFactory(this);
        callbacks.registerMessageEditorTabFactory(this);

    }

    //
    // implement IMessageEditorTabFactory
    //
    @Override
    public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
        // create a new instance of our custom editor tab
        //return new SerializedJavaInputTab(controller, editable, helpers, utils, callbacks);
        //return new JDSerMessageTab(callbacks, helpers, utils, editable);
        return new MandrakeMessageTab(jdSer, editable);
    }

    //
    // implement IContextMenuFactory
    //
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        List<JMenuItem> menu = new ArrayList<>();
        Action reloadJarsAction = new ReloadJarsAction("Mandrake: Reload JARs", invocation);
        JMenuItem reloadJars = new JMenuItem(reloadJarsAction);

        menu.add(reloadJars);
        return menu;
    }

    class ReloadJarsAction extends AbstractAction {

        IContextMenuInvocation invocation;

        public ReloadJarsAction(String text, IContextMenuInvocation invocation) {
            super(text);
            this.invocation = invocation;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            _stdout.println("Reloading jars from " + jdSer.LIB_DIR);
            jdSer.refreshSharedClassLoader();
        }

    }

    //
    // class implementing IMessageEditorTab
    //
    
}