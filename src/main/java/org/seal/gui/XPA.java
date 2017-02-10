package org.seal.gui;

import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.umu.editor.VentanaMensajes;
import org.umu.editorXacml3.PolicyEditorPanel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class XPA extends JFrame implements ItemListener, ActionListener {
	
	public int totalWidth;
	public int totalheight;

	protected Action newAction, openAction, saveAction, saveAsAction, checkSchemaAction;
	protected Action openTestsAction, generateCoverageTestsAction, generateMutationTestsAction, runTestsAction;
	protected Action openMutantsAction, generateMutantsAction, testMutantsAction;
	protected Action localizeFaultAction, fixFaultAction;
	protected JCheckBoxMenuItem[] items;
	protected Action saveOracleValuesAction;

	VentanaMensajes vm = new VentanaMensajes();
	boolean showVersionWarning = true;

	protected JTabbedPane mainTabbedPane;
	protected AbstractPolicyEditor editorPanel;

	protected TestPanel testPanel;
	protected MutationPanel mutationPanel;
	protected DebugPanel debugPanel;	
	
	public XPA() {
		try {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			init();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createNavigationIcon(String imageName) {
		String imgLocation = "org/umu/icons/" + imageName + ".gif";
		java.net.URL imageURL = this.getClass().getClassLoader()
				.getResource(imgLocation);
		if (imageURL == null) {
			System.err.println("Resource not found: " + imgLocation);
			return null;
		} else {
			return new ImageIcon(imageURL);
		}
	}

	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createPolicyMenu());
		menuBar.add(createTestMenu());
		menuBar.add(createMutationMenu());
		menuBar.add(createDebuggingMenu());
		menuBar.add(createHelpMenu());
		return menuBar;
	}

	private void createActions() {
		newAction = new NewAction("New", createNavigationIcon("new"), "New",
				new Integer(KeyEvent.VK_N));
		openAction = new OpenAction("Open...", createNavigationIcon("open"),
				"Open", new Integer(KeyEvent.VK_O));
		saveAction = new SaveAction("Save", createNavigationIcon("save"),
				"Save", new Integer(KeyEvent.VK_S));
		saveAsAction = new SaveAsAction("Save As...",
				createNavigationIcon("saveas"), "SaveAs", new Integer(
						KeyEvent.VK_A));
		checkSchemaAction = new CheckSchemaAction("Check Schema...",
				createNavigationIcon("CheckSchema"), "CheckSchema",
				new Integer(KeyEvent.VK_C));

		openTestsAction = new OpenTestsAction("Open Tests...",
				createNavigationIcon("opentests"), "OpenTests",
				new Integer(KeyEvent.VK_O));

		generateCoverageTestsAction = new GenerateCoverageBasedTestsAction("Generate Coverage-Based Tests...",
				createNavigationIcon("generatecoveragetests"), "GenerateCoverageBasedTests",
				new Integer(KeyEvent.VK_G));

		generateMutationTestsAction = new GenerateMutationBasedTestsAction("Generate Mutation-Based Tests...",
				createNavigationIcon("generatemutationtests"), "GenerateMutationBasedTests",
				new Integer(KeyEvent.VK_M));

		runTestsAction = new RunTestsAction("Run Tests",
				createNavigationIcon("runtests"), "RunTests", new Integer(
						KeyEvent.VK_R));

		openMutantsAction = new OpenMutantsAction(
				"Open Mutants...", createNavigationIcon("openmutants"),
				"OpenMutants", new Integer(KeyEvent.VK_P));

		generateMutantsAction = new GenerateMutantsAction(
				"Generate Mutants...", createNavigationIcon("generatemutants"),
				"GenerateMutants", new Integer(KeyEvent.VK_T));

		testMutantsAction = new RunMutantsAction("Test Mutants",
				createNavigationIcon("runmutants"), "TestMutants", new Integer(
						KeyEvent.VK_U));

		saveOracleValuesAction = new SaveOraclesAction("Save as Oracles",
				createNavigationIcon(""), "SaveResults", new Integer(
						KeyEvent.VK_A));

		localizeFaultAction = new LocalizeFaultAction("Localize Fault",
				createNavigationIcon(""), "LocalizeFault", new Integer(
						KeyEvent.VK_L));

		fixFaultAction = new FixFaultAction("Fix Fault",
				createNavigationIcon(""), "FixFault", new Integer(
						KeyEvent.VK_F));

	}

	public void createToolBar() {
		Insets margins = new Insets(1, 1, 1, 1);

		JButton button = null;
		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.PAGE_START);

		// new button
		button = new JButton(newAction);
		button.setMargin(margins);
		button.setBorderPainted(false);

		if (button.getIcon() != null) {
			button.setText(""); // an icon-only button
		}
		toolBar.add(button);

		// open button
		button = new JButton(openAction);
		button.setMargin(margins);
		button.setBorderPainted(false);
		if (button.getIcon() != null) {
			button.setText(""); // an icon-only button
		}
		toolBar.add(button);

		// save button
		button = new JButton(saveAction);
		button.setMargin(margins);
		button.setBorderPainted(false);
		if (button.getIcon() != null) {
			button.setText(""); // an icon-only button
		}
		toolBar.add(button);

	}

	protected JMenu createPolicyMenu() {
		JMenuItem menuItem = null;
		JMenu fileMenu = new JMenu("Policy");
		Action[] actions = { newAction, openAction, saveAction, saveAsAction,
				checkSchemaAction };
		for (int i = 0; i < actions.length; i++) {
			menuItem = new JMenuItem(actions[i]);
			menuItem.setIcon(null); // arbitrarily chose not to use icon
			fileMenu.add(menuItem);
		}
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("Exit"));
		return fileMenu;
	}

	protected JMenuItem createMenuItem(String menuName) {
		JMenuItem menuItem = new JMenuItem(menuName);
		menuItem.setActionCommand(menuName);
		menuItem.addActionListener(this);
		return menuItem;
	}

	protected JMenu createEditMenu() {
		JMenu editMenu = new JMenu("Edit");
		JMenuItem[] editItems = new JMenuItem[5];

		editItems[0] = new JMenuItem("Cut");
		editItems[1] = new JMenuItem("Copy");
		editItems[2] = new JMenuItem("Paste");
		editItems[3] = new JMenuItem("Find");
		editItems[4] = new JMenuItem("Replace");

		for (int i = 0; i < editItems.length; i++) {
			editItems[i].addItemListener(this);
			editMenu.add(editItems[i]);
		}

		return editMenu;
	}

	protected JMenu createTestMenu() {
		JMenu testMenu = new JMenu("Test");
		Action[] actions = { openTestsAction, generateCoverageTestsAction, generateMutationTestsAction, runTestsAction, saveOracleValuesAction };
		for (int i = 0; i < actions.length; i++) {
			JMenuItem menuItem = new JMenuItem(actions[i]);
			menuItem.setIcon(null); // arbitrarily chose not to use icon
			testMenu.add(menuItem);
		}

		JMenuItem coverageItem = new JMenuItem("Evaluate Coverage");
		testMenu.add(coverageItem);
		return testMenu;
	}

	protected JMenu createMutationMenu() {
		JMenu mutationMenu = new JMenu("Mutate");
		Action[] actions = { openMutantsAction, generateMutantsAction, testMutantsAction };
		for (int i = 0; i < actions.length; i++) {
			JMenuItem menuItem = new JMenuItem(actions[i]);
			menuItem.setIcon(null);
			mutationMenu.add(menuItem);
		}
		return mutationMenu;
	}

	protected JMenu createDebuggingMenu() {
		JMenu debuggingMenu = new JMenu("Debug");
		Action[] actions = {localizeFaultAction, fixFaultAction};
		for (int i = 0; i < actions.length; i++) {
			JMenuItem menuItem = new JMenuItem(actions[i]);
			menuItem.setIcon(null);
			debuggingMenu.add(menuItem);
		}
		return debuggingMenu;
	}


	protected JMenu createHelpMenu() {
		JMenu caMenu = new JMenu("Help");
		return caMenu;
	}

	public void itemStateChanged(ItemEvent e) {
		JCheckBoxMenuItem mi = (JCheckBoxMenuItem) (e.getSource());
		boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
		if (mi == items[0]) {
			openAction.setEnabled(selected);
		} else if (mi == items[1]) {
			saveAction.setEnabled(selected);
		}
	}

	public class NewAction extends AbstractAction {
		public NewAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			editorPanel.newFile();
		}
	}

	public class OpenAction extends AbstractAction {
		public OpenAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			editorPanel.openFile();
		}
	}

	public class SaveAction extends AbstractAction {
		public SaveAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
//			editorPanel.saveFile();
		}
	}

	public class SaveAsAction extends AbstractAction {
		public SaveAsAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
//			editorPanel.saveAsFile();
		}
	}

	public class CheckSchemaAction extends AbstractAction {
		public CheckSchemaAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
//			editorPanel.checkSchema();
		}
	}

	public class OpenTestsAction extends AbstractAction {
		public OpenTestsAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			testPanel.openTests();		
		}
	}

	public class GenerateCoverageBasedTestsAction extends AbstractAction {
		public GenerateCoverageBasedTestsAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			testPanel.generateCoverageBasedTests();		
		}
	}

	public class GenerateMutationBasedTestsAction extends AbstractAction {
		public GenerateMutationBasedTestsAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			testPanel.generateMutationBasedTests();		
		}
	}

	public class RunTestsAction extends AbstractAction {
		public RunTestsAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			testPanel.runTests();
		}
	}

	public class OpenMutantsAction extends AbstractAction {
		public OpenMutantsAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			mutationPanel.openMutants();
		}
	}

	public class GenerateMutantsAction extends AbstractAction {
		public GenerateMutantsAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			mutationPanel.generateMutants();
		}
	}

	public class RunMutantsAction extends AbstractAction {
		public RunMutantsAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			mutationPanel.testMutants();
		}
	}

	public class SaveOraclesAction extends AbstractAction {
		public SaveOraclesAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			testPanel.saveActualResponsesAsOracleValues();
		}
	}

	public class LocalizeFaultAction extends AbstractAction {
		public LocalizeFaultAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			debugPanel.localizeFault();
		}
	}

	public class FixFaultAction extends AbstractAction {
		public FixFaultAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			debugPanel.fixFault();
		}
	}

	public void updateMainTabbedPane(){
		mainTabbedPane.validate();
		mainTabbedPane.updateUI();
	}
	
	private void createMainTabbedPane() {
		editorPanel = new PolicyEditorPanel();
//		editorPanel = new EditorPanel(this);

		testPanel = new TestPanel(this);
		mutationPanel = new MutationPanel(this);
		debugPanel = new DebugPanel(this);
		
		mainTabbedPane = new JTabbedPane();
		mainTabbedPane.setBorder(BorderFactory.createEtchedBorder(0));
		mainTabbedPane.addTab("Policy",
				createNavigationIcon("images/policy.gif"), editorPanel);
		mainTabbedPane.addTab("Tests", createNavigationIcon("images/test.gif"),
				testPanel);
		mainTabbedPane.addTab("Mutants",
				createNavigationIcon("images/mutation.gif"), mutationPanel);
		mainTabbedPane.addTab("Debugging",
				createNavigationIcon("images/mutation.gif"), debugPanel);
		// mainTabbedPane.addChangeListener(this);
		mainTabbedPane.setSelectedComponent(editorPanel);
	}

	public void setToPolicyPane(){
		mainTabbedPane.setSelectedComponent(editorPanel);		
	}

	public void setToTestPane(){
		mainTabbedPane.setSelectedComponent(testPanel);		
	}

	public void setToMutantPane(){
		mainTabbedPane.setSelectedComponent(mutationPanel);		
	}

	private void init() throws Exception {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.totalWidth = (int) (screenSize.getWidth() * 0.8);
		this.totalheight = (int) (screenSize.getHeight() * 0.8);
		setPreferredSize(new Dimension(totalWidth, totalheight));
		createMainTabbedPane();
		createActions();

		JPanel contentPane = (JPanel) getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(mainTabbedPane, BorderLayout.CENTER);

		setJMenuBar(createMenuBar());

		createToolBar();
		if (getWorkingPolicyFile() != null) {
			setTitle("XACML Policy Analyzer - "
					+ getWorkingPolicyFile().getName());
		} else {
			setTitle("XACML Policy Analyzer - Unnamed");
		}
	}

	protected void exit() {
//		policyPanel.saveChanged();
		this.dispose();
	}

	public VentanaMensajes getVM() {
		return vm;
	}

	public void println(String string) {
		vm.getPrintStream().println(string);
	}

	public boolean hasWorkingPolicy(){
		return editorPanel.getWorkingPolicyFile()!=null;
	}
	
	public File getWorkingPolicyFile() {
		return editorPanel.getWorkingPolicyFile();
	}

	public String getWorkingPolicyFilePath() {
		return editorPanel.getWorkingPolicyFile().getAbsolutePath();
	}

	public PolicySpreadSheetTestSuite getWorkingTestSuite() {
		return testPanel.getPolicySpreadSheetTestSuite();
	}

	public String getWorkingTestSuiteFileName() {
		return testPanel.getWorkingTestSuiteFileName();
	}

	public boolean hasTests(){
		return testPanel.hasTests();
	}
	
	public boolean hasTestFailure(){
		return testPanel.hasTestFailure();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Exit")) {
			windowClosing();
		}
	}

	public void windowClosing() {
//		editorPanel.windowClosing();
		this.dispose();
	}
	
	
	public static void main(String[] args) {
		//
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// turn off bold fonts
					// UIManager.put("swing.boldMetal", Boolean.FALSE);

					// re-install the Metal Look and Feel
					// UIManager.setLookAndFeel(new
					// javax.swing.plaf.metal.MetalLookAndFeel());
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				XPA frame = new XPA();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
				
			}
		});
	}

}

class MiWindowAdapter extends WindowAdapter {
	private XPA adaptee;

	MiWindowAdapter(XPA adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.windowClosing();
	}
}
