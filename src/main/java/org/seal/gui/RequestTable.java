package org.seal.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.TableColumnModel;

import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.xacml3.Attributes;

public class RequestTable {

	public static GeneralTablePanel getRequestTable(String request,
			boolean xmlView) {
		Vector<Vector<Object>> data = convertRequest(request);
		String[] columnNames = { "No", "Category", "Type", "Name", "Value" };
		return new GeneralTablePanel(data, columnNames, 5);

		// this.xmlView
	}

	public static void setPreferredColumnWidths(GeneralTablePanel gtp,
			double totalWidth) {
		TableColumnModel columnModel = gtp.getTable().getColumnModel();
		if (totalWidth == 0) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			totalWidth = (int) (screenSize.getWidth() * 0.8);
		}

//		System.out.println(totalWidth + "totalWidth");
		if (totalWidth > 0) {
			columnModel.getColumn(0).setPreferredWidth(10);
			columnModel.getColumn(1).setPreferredWidth(
					(int) (totalWidth * 0.28));
			columnModel.getColumn(2).setPreferredWidth(
					(int) (totalWidth * 0.08));
			columnModel.getColumn(3).setPreferredWidth(
					(int) (totalWidth * 0.18));
			columnModel.getColumn(4).setPreferredWidth(
					(int) (totalWidth * 0.18));
			// table.getColumnModel().getColumn(5).setPreferredWidth((int)(totalWidth*0.10));
		}

	}

	private static Vector<Vector<Object>> convertRequest(String request) {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		AbstractRequestCtx requestCtx = null;
		int index = 1;
		if (request.equals("")) {
			Vector<Object> child = new Vector<Object>();
			child.add(index);
			child.add("null");
			child.add("null");
			child.add("null");
			child.add("null");
			data.add(child);
		} else {

			try {
				requestCtx = RequestCtxFactory.getFactory().getRequestCtx(
						request.replaceAll(">\\s+<", "><"));
			} catch (ParsingException e) {
				String error = "Invalid request  : " + e.getMessage();
				System.out.println(error);
			}
			Set<Attributes> attributesSet = requestCtx.getAttributesSet();
			// System.out.println(attributesSet.size());

			for (Attributes attrs : attributesSet) {

				// System.out.println(attrs.getCategory().toString());
				for (Attribute attr : attrs.getAttributes()) {
					Vector<Object> child = new Vector<Object>();
					child.add(index);
					child.add(attrs.getCategory().toString());
					child.add(attr.getType().toString());
					child.add(attr.getId().toString());
					child.add(attr.getValue().encode());
					// System.out.println(attr.getId().toString());
					// System.out.println(attr.getValue().encode());
					data.add(child);
					index++;
				}

			}
		}
		return data;
	}

//	public static void main(String args[]) {
//		String request = PolicyRunner
//				.readTextFile("/home/nshen/xpa/branch/XPA/GenTests/request4.txt");
//		convertRequest(request);
//	}

}
