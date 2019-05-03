//package com.cisco.usm.app.certificates.client.view;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.cisco.nm.vms.api.xsd.CertificateNodeEntry;
//import com.google.gwt.core.shared.GWT;
//import com.google.gwt.safehtml.shared.SafeHtml;
//import com.google.gwt.user.client.ui.HTML;
//import com.google.gwt.user.client.ui.IsWidget;
//import com.google.gwt.user.client.ui.VerticalPanel;
//import com.google.gwt.user.client.ui.Widget;
//import com.sencha.gxt.core.client.XTemplates;
//import com.sencha.gxt.widget.core.client.ContentPanel;
//import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;;
//
//public class CertificateTemplateWidget implements IsWidget {
//	
//	 List<CertificateNodeEntry> certificateNodeEntries = new ArrayList<CertificateNodeEntry>();
//
//	public interface DataRenderer extends XTemplates {
//		
//		@XTemplate(source = "certificateTpl.html")
//		public SafeHtml render(List<CertificateNodeEntry> list);
//
//		@XTemplate(source = "viewCertificate.html")
//		public SafeHtml renderTemplate(List<CertificateNode> list);
//
//	}
//
//	DataRenderer renderer = GWT.create(DataRenderer.class);
//
//	private VerticalPanel vp;
//
//	public CertificateTemplateWidget(List<CertificateNodeEntry> certificateNodeEntries) {
//		this.certificateNodeEntries = certificateNodeEntries;
//	}
//
//	public Widget asWidget() {
//		if (vp == null) {
//			vp = new VerticalPanel();
//			vp.setSpacing(10);
//
//			ContentPanel xpanel = new ContentPanel();
//			final VerticalLayoutContainer xRowLayoutContainer = new VerticalLayoutContainer();
//			xpanel.add(xRowLayoutContainer);
//
//			//final HTML text = new HTML(renderer.renderTemplate(root().getChildren()));
//			final HTML text = new HTML(renderer.render(certificateNodeEntries));
//			vp.add(text);
//
//			vp.setWidth("100%");
//			vp.add(xpanel);
//		}
//
//		return vp;
//	}
//
//	public CertificateNode root() {
//		
//		CertificateNode root = new CertificateNode();
//		root.setKey("root");
//		root.setValue("");
//
//		List<CertificateNode> children = new ArrayList<CertificateNode>();
//		root.setChildren(children);
//
//		CertificateNode child1 = new CertificateNode();
//		List<CertificateNode> children1 = new ArrayList<CertificateNode>();
//		child1.setChildren(children1);
//		child1.setKey("C1");
//		child1.setValue("");
//
//		CertificateNode child11 = new CertificateNode();
//		child11.setKey("C11");
//		child11.setValue("V11");
//		children1.add(child11);
//
//		CertificateNode child12 = new CertificateNode();
//		child12.setKey("C12");
//		child12.setValue("V12");
//		children1.add(child12);
//
//		CertificateNode child13 = new CertificateNode();
//		child13.setKey("C13");
//		child13.setValue("V13");
//		children1.add(child13);
//
//		children.add(child1);
//
//		CertificateNode child2 = new CertificateNode();
//		child2.setKey("C2");
//		child2.setValue("V2");
//		children.add(child2);
//
//		CertificateNode child3 = new CertificateNode();
//		child3.setKey("C3");
//		child3.setValue("V3");
//		children.add(child3);
//
//		CertificateNode child4 = new CertificateNode();
//		child4.setKey("C4");
//		child4.setValue("");
//		List<CertificateNode> children4 = new ArrayList<CertificateNode>();
//		child4.setChildren(children4);
//
//		CertificateNode child41 = new CertificateNode();
//		child41.setKey("C41");
//		child41.setValue("V41");
//		children4.add(child41);
//
//		CertificateNode child42 = new CertificateNode();
//		child42.setKey("C42");
//		child42.setValue("V42");
//		children4.add(child42);
//
//		CertificateNode child43 = new CertificateNode();
//		child43.setKey("C43");
//		child43.setValue("V43");
//		children4.add(child43);
//
//		children.add(child4);
//
//		return root;
//	}
//}
