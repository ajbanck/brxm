/*
 *  Copyright 2008 Hippo.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.hst.components.modules.paginatedview;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.hippoecm.hst.components.modules.paginatedview.bean.PaginatedListBean;
import org.hippoecm.hst.core.HSTHttpAttributes;
import org.hippoecm.hst.core.template.ContextBase;
import org.hippoecm.hst.core.template.ContextBaseFilter;
import org.hippoecm.hst.core.template.TemplateException;
import org.hippoecm.hst.core.template.module.ModuleBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic module for paginated browsing of a folder in the repository or a list of items
 * generated by an instance that implements the {@see PaginatedModuleElements} interface.
 *
 */
public class PaginateModule extends ModuleBase {
	private static final Logger log = LoggerFactory.getLogger(PaginateModule.class);
	
	//properties from the cms
	public static final String PAGESIZE_CMS_PROPERTY = "pagesize";
	public static final String PAGE_PARAMETER_CMS_PROPERTY = "pageParameter";
	public static final String ELEMENTS_CMS_PROPERTY = "elements";
	
	//elements types
	public static final String CLASS_ELEMENTS_TYPE = "class:";
	public static final String FOLDER_ELEMENTS_TYPE = "folder:";
	
	private int pageSize = 10; //default pagesize	
    private List items;
	
	public void setItems(List items) {
		this.items = items;
	}
	
	public void setPagesize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public String execute(HttpServletRequest request, HttpServletResponse response)
			throws TemplateException {	
		return null;
	}

	/**
	 * <p>
	 * The render method of the module. This module expects the following properties in the
	 * module definition in the repository:
	 * <ul>
	 * <li>pagesize - the number of items in a page</li>
	 * <li>pageParameter - the request parameter with the page number to display</li>
	 * <li>elements - defines where to get the page data (see below)</li>
	 * </ul>
	 * 
	 * <b>Elements</b> is a string property that has the syntax <b>folder:</b> followed by a
	 * repository path or is has the form <b>class:</b> followed by a class name. <br/>
	 * The first option generates a list containing the subnodes of the path node that are 
	 * displayed in the page<br/>
	 * The second option creates an instance of the given class name, that must implement the
	 * {@see PaginatedModuleElements} interface. 
	 * </p>
	 */
	public void render(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		List items = null;
		int pageId = 0;
		long total = 0;
		try {
			//init values from module & request
			String pageIdRequestParameter = getPropertyValueFromModuleNode(PAGE_PARAMETER_CMS_PROPERTY);
			pageId = getPageId(request, pageIdRequestParameter);			
			String elementsPropertyValue = getPropertyValueFromModuleNode(ELEMENTS_CMS_PROPERTY);
			
			PaginatedElements paginatedElements = null;
			if (elementsPropertyValue.startsWith(FOLDER_ELEMENTS_TYPE)) {
			   String folderMap = elementsPropertyValue.replaceFirst(FOLDER_ELEMENTS_TYPE, "");
			   
			   paginatedElements = new FolderPaginatedElements(request, folderMap);			   
			   
			} else if (elementsPropertyValue.startsWith(CLASS_ELEMENTS_TYPE)) {
			   String elementClassName = elementsPropertyValue.replaceFirst(CLASS_ELEMENTS_TYPE, "");
			   try {
				   Object o = Class.forName(elementClassName).newInstance();
				   if (PaginatedModuleElements.class.isAssignableFrom(o.getClass())) {
					   PaginatedModuleElements pme = (PaginatedModuleElements) o;
					   pme.setPageContext(pageContext);
					   pme.construct();
					   paginatedElements = pme;
				   }
				} catch (Exception e) {
					log.error("Cannot create PaginatedModuleElements instance from class " + elementClassName);
				} 
			}
			
			//get the elements of this page
			if (paginatedElements != null){
			   total = paginatedElements.totalItems();
			   int from = pageId * pageSize;
			   int to =  (pageId * pageSize >= total) ? (int) total : (pageId * pageSize) - 1; 
			   items = paginatedElements.getElements(from, to);
			}
			
		} catch (TemplateException e) {			
			log.error("Error getting items", e);
			items = new ArrayList();
		}
		
		PaginatedListBean paginatedData = new PaginatedListBean();
		paginatedData.setItems(items);
		paginatedData.setItemsInPage(items.size());
		paginatedData.setPageId(pageId);
		paginatedData.setTotal(total);
		pageContext.setAttribute("pageItems", paginatedData);
	}
	
	protected String getURL(HttpServletRequest request) throws TemplateException {
		String urlPrefix = (String) request.getAttribute(ContextBaseFilter.URLBASE_INIT_PARAMETER);
		String url = getPropertyValueFromModuleNode("url");
		return urlPrefix + url;
	}
	
	/**
	 * Returns the page number if it is passed through with a request parameter identified by the
	 * request parameter with name {@see pageRequestParameter}. If this parameter is not available on the
	 * request or if it cannot be parsed into a valid page number, then 0 is returned.
	 * @param request
	 * @param pageRequestParameter the request parameter with the page number as value
	 * @return
	 */
	protected int getPageId(HttpServletRequest request, String pageRequestParameter) {		
		int pageId = 0;
		try {
			pageId = Integer.parseInt(request.getParameter(pageRequestParameter));
		} catch (NumberFormatException e) {				
			log.error("Cannot parse requestParameter " + pageRequestParameter + " value=" + request.getParameter(pageRequestParameter));
			pageId = 0;
		}
		return pageId;
	}
	
	/**
	 * Returns a {@link NodeIterator} with the subnodes of the node located at the folderMap path.
	 * @param request
	 * @param folderMap the node path's subnodes returned as a NodeIterator
	 * @return
	 */
	protected NodeIterator getNodeIterator(HttpServletRequest request, String folderMap) {
		ContextBase contentContextBase = (ContextBase) request.getAttribute(HSTHttpAttributes.CURRENT_CONTENT_CONTEXTBASE_REQ_ATTRIBUTE);
		Node folderMapNode = contentContextBase.getRelativeNode(folderMap);
		
		NodeIterator nodeIterator = null;
		try {
			nodeIterator = folderMapNode.getNodes();
		} catch (RepositoryException e) {
			log.error("Cannot get nodeIterator for relative folderMap" + folderMap, e);
		}
		return nodeIterator;
	}

}

/**
 * {@see PaginatedElements} implementation that returns the content of a folder in the
 * repository as paginated chunks.
 *
 */
class FolderPaginatedElements implements PaginatedElements {
	private static final Logger log = LoggerFactory.getLogger(FolderPaginatedElements.class);
	
	private NodeIterator folderIterator;
	
	public FolderPaginatedElements(HttpServletRequest request, String folder) {
		
	}
	
	protected void initNodeIterator(HttpServletRequest request, String folderMap) {
		ContextBase contentContextBase = (ContextBase) request.getAttribute(HSTHttpAttributes.CURRENT_CONTENT_CONTEXTBASE_REQ_ATTRIBUTE);
		Node folderMapNode = contentContextBase.getRelativeNode(folderMap);
		
		folderIterator = null;
		try {
			folderIterator = folderMapNode.getNodes();
		} catch (RepositoryException e) {
			log.error("Cannot get nodeIterator for relative folderMap" + folderMap, e);
		}
	}
	
	public List<Node> getElements(int from, int to) {	
		List<Node> itemList = new ArrayList();		
		folderIterator.skip(from);
		int i = from;
		while (folderIterator.hasNext() && i++ < to) {
			itemList.add(folderIterator.nextNode());			
		}
		return itemList;
	}
	
	

	public int totalItems() {
		return (int) folderIterator.getSize();
	}
}
