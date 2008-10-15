package org.hippoecm.frontend;

import java.util.ArrayList;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hippoecm.frontend.editor.ITemplateEngine;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.properties.JcrPropertyModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.IJcrService;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagSuggestPlugin extends RenderPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";
    
    static final Logger log = LoggerFactory.getLogger(TagSuggestPlugin.class);
    
    private static final long serialVersionUID = 1L;
    
    private JcrNodeModel nodeModel;
    private IJcrService jcrService;

    public TagSuggestPlugin(final IPluginContext context, IPluginConfig config) {
        super(context, config);
        
        nodeModel = (JcrNodeModel) getModel();
        jcrService = context.getService(IJcrService.class.getName(), IJcrService.class);
        
        String mode = config.getString("mode");
        if (ITemplateEngine.EDIT_MODE.equals(mode)) {
            Fragment fragment = new Fragment("tag-view", "suggestions", this);
            fragment.add(new TagSuggestView("view", nodeModel, jcrService));
            add(fragment);
        } else {
            add(new Fragment("tag-view", "empty", this));
        }
    }
    
    private class TagSuggestView extends RefreshingView {
        private static final long serialVersionUID = 1L;

        final JcrNodeModel nodeModel;
        final IJcrService jcrService;
        
        public TagSuggestView(String id, IModel model, final IJcrService jcrService) {
            super(id, model);
            
            nodeModel = (JcrNodeModel) model;
            this.jcrService = jcrService;
            
            /**
             * Construct the backend and pass it the node model
             */
            
        }
        
        @Override
        protected void populateItem(Item item){
            final String keyword = (String) item.getModelObject();
            Fragment fragment = new Fragment("keyword", "tag-fragment", this);
            AjaxFallbackLink link = new AjaxFallbackLink("link"){
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    try {
                        TagsModel tagsModel = new TagsModel(new JcrPropertyModel(nodeModel.getNode().getProperty(TagsPlugin.FIELD_NAME)));
                        tagsModel.addTag(keyword);
                        jcrService.flush(nodeModel);
                        log.debug("Send flush");
                    } catch (PathNotFoundException e) {
                        log.info(TagsPlugin.FIELD_NAME + " does not exist for this node. Attempting to create it.");
                        // the property hippostd:tags does not exist
                        String[] tags = {keyword};
                        try{
                            nodeModel.getNode().setProperty(TagsPlugin.FIELD_NAME, tags);
                        }catch (RepositoryException re){
                            log.error("Creation of " + TagsPlugin.FIELD_NAME + " failed.", e);
                        }
                    } catch (RepositoryException e) {
                        log.error("Repository error", e);
                    }
                }
                
            };
            link.add(new Label("link-text", keyword));
            fragment.add(link);
            item.add(fragment);
        }

        @Override
        protected Iterator<Model> getItemModels() {
            ArrayList<Model> items = new ArrayList<Model>();
            items.add(new Model("wine"));
            items.add(new Model("beer"));
            items.add(new Model("hippos"));
            return  items.iterator();
        }
        
    }

}
