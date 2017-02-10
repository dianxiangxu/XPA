package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.umu.editor.AnalizadorSAX;
import org.umu.editor.CurrentCopia;
import org.umu.editor.CurrentPath;
import org.umu.editor.ElementPanel;
import org.umu.editor.ElementoXACML;
import org.umu.editor.MenuContextFactoryImpl;
import org.umu.editor.MiRenderer;
import org.umu.editor.PanelDocumento;
import org.umu.editor.ValidatorDialog;
import org.umu.editor.VentanaMensajes;
import org.umu.editor.XACMLPanelFactoryImpl;
import org.umu.editor.XMLFileFilter;
import org.xml.sax.SAXException;

public class PolicyPanel extends JPanel{
	
	private XPA xpa;
	
	JTree policyTree;
	File workingPolicyFile;
	
	DefaultTreeModel defaultTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
	JSplitPane jSplitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	JSplitPane jSplitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	VentanaMensajes vm = new VentanaMensajes();
	boolean showVersionWarning = true;
	
	public PolicyPanel(XPA xpa) {
		this.xpa = xpa;	
		setLayout(new BorderLayout());
		policyTree = new JTree(defaultTreeModel);
		policyTree.setCellRenderer(new MiRenderer());
		policyTree.setToolTipText("");
		policyTree.addMouseListener(new MiMouseAdapter(this));
		policyTree.addTreeSelectionListener(new MiTreeSelectionAdapter(this));
		JScrollPane jScrollPane1 = new JScrollPane(policyTree);
		jSplitPane1.setLeftComponent(jScrollPane1);
		jSplitPane1.setRightComponent(new JPanel());
		jSplitPane1.setResizeWeight(0.5);
		jSplitPane2.setTopComponent(jSplitPane1);
		jSplitPane2.setBottomComponent(new JScrollPane(vm));
		jSplitPane2.setResizeWeight(0.25);
		add(jSplitPane2, BorderLayout.CENTER);
	}
	
	public File getWorkingPolicyFile(){
		return workingPolicyFile;
	}
	
	public void setWorkingPolicyFile(File newPolicyFile){
		workingPolicyFile = newPolicyFile;
	}
	
	public void setPolicyTreeModel(String fileName) throws SAXException, IOException{
		AnalizadorSAX asax = new AnalizadorSAX();
		defaultTreeModel = (DefaultTreeModel) asax.analizar(fileName);
		policyTree.setModel(defaultTreeModel);
	}
	
	public void newFile(){
		saveChanged();
		DefaultMutableTreeNode raiz = new DefaultMutableTreeNode(new String("Policy Document"));
		DefaultTreeModel auxdtm = new DefaultTreeModel(raiz);
		defaultTreeModel = auxdtm;
		policyTree.setModel(defaultTreeModel);
		workingPolicyFile = null;
		xpa.setTitle("XACML Policy Analyzer - Unnamed");	
	}
	
	public void openFile(){
		saveChanged();
		JFileChooser cuadroAbrir = new JFileChooser();
		cuadroAbrir.setMultiSelectionEnabled(false);
		cuadroAbrir.setCurrentDirectory(CurrentPath.getInstancia()
				.getCurrdir());
		cuadroAbrir.setFileFilter(new XMLFileFilter("xml"));
		if (cuadroAbrir.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File temporal = cuadroAbrir.getSelectedFile();
			AnalizadorSAX asax = new AnalizadorSAX();
//			try {
				if (!temporal.toString().endsWith(".xml")) {
					JOptionPane.showMessageDialog(this,
							"The open File is not a Policy *.xml",
							"Error of Selection",
							JOptionPane.WARNING_MESSAGE);
				} else {

/*				
					CurrentPath.getInstancia().setCurrdir(
							temporal.getParentFile());
					xpa.println("Analyzing file:"+ temporal.getAbsolutePath());
					defaultTreeModel = (DefaultTreeModel) asax
							.analizar(temporal.getAbsolutePath());
					if (!asax.getErrorHandler().equalsIgnoreCase("")) {
						JOptionPane.showMessageDialog(this,
								asax.getErrorHandler(),
								"Errors produced in the parser",
								JOptionPane.WARNING_MESSAGE);
						xpa.println(asax.getErrorHandler());
					}
*/					
					workingPolicyFile = temporal;
//					xpa.println(readTextFile(temporal.getAbsolutePath()));

//					xpa.setTitle("UMU-XACML-Editor - " + workingPolicyFile.getName());
					xpa.setTitle("UMU-XACML-Editor - " + workingPolicyFile.getAbsolutePath());

//					policyTree.setModel(defaultTreeModel);
				}
/*			} catch (SAXException exc) {
				JOptionPane.showMessageDialog(this, exc.toString());
				xpa.println(exc.toString());
			} catch (IOException exc) {
				JOptionPane.showMessageDialog(this, exc.toString());
				xpa.println(exc.toString());
			}
*/		}
	}
	
	public void saveAsFile(){
		JFileChooser cuadroGuardar = new JFileChooser();
		cuadroGuardar.setCurrentDirectory(CurrentPath.getInstancia()
				.getCurrdir());
		cuadroGuardar.setFileFilter(new XMLFileFilter("xml"));
		cuadroGuardar.setMultiSelectionEnabled(false);

		if (cuadroGuardar.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File temporal = cuadroGuardar.getSelectedFile();
			if (temporal.exists()) {
				if ((new XMLFileFilter("xml")).accept(temporal)) {
					int resp = JOptionPane
							.showConfirmDialog(
									this,
									"The File already exists. Do you wish to change (overwrite) it?",
									"Saving...", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE);
					if (resp == JOptionPane.YES_OPTION) {
						AnalizadorSAX asax = new AnalizadorSAX();
						workingPolicyFile = temporal;
						xpa.println("Process file:"
										+ workingPolicyFile.getAbsolutePath());
						asax.procesaSalvar(
								(DefaultMutableTreeNode) defaultTreeModel
										.getRoot(), workingPolicyFile
										.getAbsolutePath());
						xpa.setTitle("UMU-XACML-Editor - "
								+ workingPolicyFile.getName());
					}
				}
			} else {
				if (temporal.getAbsolutePath().endsWith(".xml")) {
					workingPolicyFile = temporal;
				} else {
					workingPolicyFile = new File(temporal.getAbsolutePath()
							+ ".xml");
				}
				AnalizadorSAX asax = new AnalizadorSAX();
				xpa.println("Process file:" + workingPolicyFile.getAbsolutePath());
				asax.procesaSalvar(
						(DefaultMutableTreeNode) defaultTreeModel.getRoot(),
						workingPolicyFile.getAbsolutePath());
				xpa.setTitle("UMU-XACML-Editor - " + workingPolicyFile.getName());
			}

		}

	}
	
	public void saveFile(){
		AnalizadorSAX asax = new AnalizadorSAX();
		if (workingPolicyFile == null) {
			saveAsFile();
		} else {
			xpa.println("Process file:" + workingPolicyFile.getAbsolutePath());
			asax.procesaSalvar(
					(DefaultMutableTreeNode) defaultTreeModel.getRoot(),
					workingPolicyFile.getAbsolutePath());
		}

	}
	
	public void checkSchema(){
		ValidatorDialog validador = new ValidatorDialog(xpa.getVM().getPrintStream());
		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = validador.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		validador.setLocation((screenSize.width - frameSize.width) / 2,
				(screenSize.height - frameSize.height) / 2);

		validador.setModal(true);
		validador.setVisible(true);
	}
	
	protected void copy(){
		DefaultMutableTreeNode copia = (DefaultMutableTreeNode) policyTree
				.getLastSelectedPathComponent();
		if (copia != null && !copia.isRoot()) {
			CurrentCopia.getInstancia().setCurrNode(copiarNodos(copia));
		}
	}

	/*
	protected void paste(){
		DefaultMutableTreeNode selecto = (DefaultMutableTreeNode) policyTree
				.getLastSelectedPathComponent();
		if (selecto != null) {
			ElementoXACML eaux = (ElementoXACML) CurrentCopia
					.getInstancia().getCurrNode().getUserObject();
			DefaultMutableTreeNode clon = copiarNodos(CurrentCopia
					.getInstancia().getCurrNode());

			Object sUserObj = selecto.getUserObject();
			if (sUserObj instanceof ElementoXACML) {
				ElementoXACML sUO = (ElementoXACML) sUserObj;
				if (sUO.getMaxNumChild(eaux) == 1) {
					if (sobreescribirUnico(eaux, selecto)) {
						InsertarOrdenadoElemento ioe = new InsertarOrdenadoElemento(
								selecto, clon);
						ioe.ordenarInsercion();
						int pos = ioe.getPosicion();
						defaultTreeModel.insertNodeInto(clon, selecto, pos);

						policyTree.setModel(defaultTreeModel);
					}
				} else {
					InsertarOrdenadoElemento ioe = new InsertarOrdenadoElemento(
							selecto, clon);
					ioe.ordenarInsercion();
					int pos = ioe.getPosicion();
					defaultTreeModel.insertNodeInto(clon, selecto, pos);
					policyTree.setModel(defaultTreeModel);

				}
			} else {
				if (sobreescribirUnico(eaux, selecto)) {
					defaultTreeModel.insertNodeInto(clon, selecto,
							selecto.getChildCount());
					policyTree.setModel(defaultTreeModel);
					defaultTreeModel.reload(selecto);
				}
			}
		}		
	}
	*/
	
	/*
	protected void add(ActionEvent e){
		DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) policyTree
				.getLastSelectedPathComponent();
		if (nodo != null) {
			String nodoName = e.getActionCommand().replaceFirst("add ", "");
			if (nodoName.startsWith("Any") && showVersionWarning) {
				int resp = JOptionPane
						.showConfirmDialog(
								this,
								nodoName
										+ " is available in XACML 1.0 but not available in XACML 2.0, "
										+ "do you wish to add it?",
								"Warning", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (resp != JOptionPane.YES_OPTION) {
					return;
				}
				showVersionWarning = false;
			}
			crearNodos(nodoName, nodo);
			if (!nodo.isRoot()) {
				((ElementoXACML) nodo.getUserObject()).setVacio(false);
			}
		}
		// Para corregir una peque???a paradoja y evitar que salgan 2
		// Description
		this.valueChanged(new TreeSelectionEvent(this, null, null, null,
				null));
	}
	*/
	
	protected void remove(){
		DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) policyTree
				.getLastSelectedPathComponent();
		if (nodo != null) {
			eliminarNodos(nodo);
		}		
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Exit")) {
		}
		else if (e.getActionCommand().equalsIgnoreCase("copy")) {
			copy();
		} else if (e.getActionCommand().equalsIgnoreCase("paste")) {
//			paste();
		}
		else if (e.getActionCommand().startsWith("add ")) {
//			add(e);
		}
		else if (e.getActionCommand().startsWith("remove")) {
			remove();
		}
	}

	public void saveChanged() {
		if (defaultTreeModel.getChildCount(defaultTreeModel.getRoot()) > 0) {
			int resp = JOptionPane.showConfirmDialog(this,
					"Not Save. Do you wish to save the changes?", "Saving...",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (resp == JOptionPane.YES_OPTION) {
				saveFile();
			}
		}

	}

	private DefaultMutableTreeNode copiarNodos(DefaultMutableTreeNode node) {

		ElementoXACML aux = (ElementoXACML) ((ElementoXACML) node
				.getUserObject()).clone();
		DefaultMutableTreeNode nodoPadre = new DefaultMutableTreeNode(aux);

		Enumeration hijos = node.children();
		while (hijos.hasMoreElements()) {
			DefaultMutableTreeNode nodoHijo = copiarNodos((DefaultMutableTreeNode) hijos
					.nextElement());
			defaultTreeModel.insertNodeInto(nodoHijo, nodoPadre,
					nodoPadre.getChildCount());
		}
		return nodoPadre;
	}

	private void eliminarNodos(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode padre = (DefaultMutableTreeNode) node
				.getParent();
		defaultTreeModel.removeNodeFromParent(node);
		if (padre.getChildCount() == 0) {
			if (padre.getUserObject() instanceof ElementoXACML) {
				((ElementoXACML) padre.getUserObject()).setVacio(true);
			}
		}
		defaultTreeModel.reload(padre);
	}

	private boolean sobreescribirUnico(ElementoXACML elem,
			DefaultMutableTreeNode nodo) {
		Enumeration hijos = nodo.children();
		while (hijos.hasMoreElements()) {
			DefaultMutableTreeNode nodoHijo = (DefaultMutableTreeNode) hijos
					.nextElement();
			ElementoXACML elemHijo = (ElementoXACML) nodoHijo.getUserObject();
			if (elemHijo.getTipo() == elem.getTipo()) {
				int resp = JOptionPane
						.showConfirmDialog(
								this,
								elemHijo.getTipo()
										+ " is type unique. Do you wish to change (overwrite) it?",
								"overwriting...", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (resp == JOptionPane.YES_OPTION) {
					eliminarNodos(nodoHijo);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/*
	private void crearNodos(String s, DefaultMutableTreeNode nodo) {
		ElementoXACML aux = ElementoXACMLFactoryImpl.getInstance()
				.obtenerElementoXACML(s, new Hashtable());
		if (aux == null) {
			xpa.println("Element " + s + " not yet implement ");
			return;
		}
		aux.setVacio(true);
		DefaultMutableTreeNode naux = new DefaultMutableTreeNode(aux);
		if (nodo.getUserObject() instanceof ElementoXACML) {
			InsertarOrdenadoElemento ioe = new InsertarOrdenadoElemento(nodo,
					naux);
			ioe.ordenarInsercion();
			int pos = ioe.getPosicion();
			defaultTreeModel.insertNodeInto(naux, nodo, pos);
		} else {
			defaultTreeModel.insertNodeInto(naux, nodo, 0);
		}
		String hijosRequeridos[] = aux.getAllObligatory();
		if (hijosRequeridos != null) {
			aux.setVacio(false);
			for (int i = 0; i < hijosRequeridos.length; i++) {
				crearNodos(hijosRequeridos[i], naux);
			}
		}
		if (nodo.getUserObject() instanceof ElementoXACML) {
			defaultTreeModel.reload(nodo);
		} else {
			defaultTreeModel.reload();
		}

	}
*/
	
	public void valueChanged(TreeSelectionEvent e) {
		int original = jSplitPane1.getDividerLocation();
		DefaultMutableTreeNode selecto = (DefaultMutableTreeNode) policyTree
				.getLastSelectedPathComponent();
		if (selecto != null) {
			ElementPanel aux = XACMLPanelFactoryImpl.getInstance()
					.obtenerPanel(selecto);

			if (aux != null) {
				jSplitPane1.setRightComponent(new JScrollPane(aux));
			}
			// La raiz PolicyDocument
			else if (aux == null && selecto.isRoot()) {
				jSplitPane1.setRightComponent(new JScrollPane(
						new PanelDocumento(workingPolicyFile, defaultTreeModel, xpa.getVM()
								.getPrintStream())));
			} else {
				jSplitPane1.setRightComponent(new JPanel());
			}

			if (aux instanceof ElementPanel) {
				aux.setTreeModel(this.defaultTreeModel);
			}
		} else {
			jSplitPane1.setRightComponent(new JPanel());
		}
		jSplitPane1.setDividerLocation(original);
	}

	public void mouseReleased(MouseEvent e) {
		DefaultMutableTreeNode nodo;
		if (SwingUtilities.isRightMouseButton(e)) {
			int xCoord = e.getX();
			int yCoord = e.getY();

			TreePath path = policyTree.getPathForLocation(xCoord, yCoord);
			if (path != null) {
				MiActionAdapter listener = new MiActionAdapter(this);
				policyTree.setSelectionPath(path);
				nodo = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (nodo != null) {
					JPopupMenu jppmMenuContext = MenuContextFactoryImpl
							.getInstance().obtenerMenuContext(nodo);
					if (jppmMenuContext != null) {
						MenuElement[] aux = jppmMenuContext.getSubElements();
						int i = 0;
						while (i < aux.length) {
							((JMenuItem) aux[i]).addActionListener(listener);
							i++;
						}
						jppmMenuContext.show(policyTree, e.getX(), e.getY());
					}
				}
			}
		}
	}

	
	public void windowClosing() {
		if (defaultTreeModel.getChildCount(defaultTreeModel.getRoot()) > 0) {
			int resp = JOptionPane.showConfirmDialog(this,
					"Not Save. Do you wish to save the changes?", "Saving...",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (resp == JOptionPane.YES_OPTION) {
				saveFile();
			}
		}
	}

}

class MiActionAdapter implements ActionListener {
	private PolicyPanel adaptee;

	MiActionAdapter(PolicyPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.actionPerformed(e);
	}
}

class MiTreeSelectionAdapter implements TreeSelectionListener {
	private PolicyPanel adaptee;

	MiTreeSelectionAdapter(PolicyPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void valueChanged(TreeSelectionEvent e) {
		adaptee.valueChanged(e);
	}
}

class MiMouseAdapter extends MouseAdapter {
	private PolicyPanel adaptee;

	MiMouseAdapter(PolicyPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void mouseReleased(MouseEvent e) {
		adaptee.mouseReleased(e);
	}
}
