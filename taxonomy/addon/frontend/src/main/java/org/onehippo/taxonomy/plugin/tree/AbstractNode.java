/*
 *  Copyright 2009-2014 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.taxonomy.plugin.tree;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.onehippo.taxonomy.api.Category;
import org.onehippo.taxonomy.api.Taxonomy;
import org.onehippo.taxonomy.plugin.model.CategoryModel;

public abstract class AbstractNode implements TreeNode, IDetachable {
    private static final long serialVersionUID = 1L;

    final String language;
    private IModel<Taxonomy> taxonomyModel;
    private List<CategoryNode> children = null;
    private Comparator<Category> categoryComparator;

    /**
     * @deprecated Use {@link #AbstractNode(IModel, String, Comparator)} instead.
     * @param taxonomyModel
     * @param language
     */
    public AbstractNode(IModel<Taxonomy> taxonomyModel, String language) {
        this(taxonomyModel, language, null);
    }

    public AbstractNode(IModel<Taxonomy> taxonomyModel, String language, Comparator<Category> categoryComparator) {
        this.taxonomyModel = taxonomyModel;
        this.language = language;
        this.categoryComparator = categoryComparator;
    }

    List<CategoryNode> getChildren() {
        if (children == null) {
            final List<? extends Category> categories = new LinkedList<Category>(getCategories());

            if (categoryComparator != null) {
                Collections.sort(categories, categoryComparator);
            }

            List<CategoryNode> tempChildren = new LinkedList<CategoryNode>();

            for (Category category : categories) {
                tempChildren.add(new CategoryNode(new CategoryModel(taxonomyModel, category.getKey()), language, categoryComparator));
            }

            children = tempChildren;
        }

        return children;
    }

    abstract List<? extends Category> getCategories();

    public Enumeration<CategoryNode> children() {
        return Collections.enumeration(getChildren());
    }

    public TreeNode getChildAt(int childIndex) {
        return getChildren().get(childIndex);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public int getChildCount() {
        return getChildren().size();
    }

    public int getIndex(TreeNode node) {
        CategoryNode childNode = (CategoryNode) node;
        int index = 0;
        for (CategoryNode item : getChildren()) {
            if (childNode.equals(item)) {
                break;
            }
            index++;
        }
        return index;
    }

    public void detach() {
        children = null;
        taxonomyModel.detach();
    }

    public Comparator<Category> getCategoryComparator() {
        return categoryComparator;
    }
}
