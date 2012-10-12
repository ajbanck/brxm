/*
 *  Copyright 2012 Hippo.
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
package org.hippoecm.frontend.plugins.console.browser;

import java.util.Collection;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.apache.wicket.markup.html.tree.DefaultTreeState;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TreeNavigatorTest {

    private MutableTreeNode rootNode;

    @Before
    public void initRootNode() {
        rootNode = new DefaultMutableTreeNode();
    }

    @Test
    public void upSelectsParentIfFirstChildIsSelected() {
        final DefaultMutableTreeNode child = new DefaultMutableTreeNode();
        rootNode.insert(child, 0);

        ITreeState state = new DefaultTreeState();
        state.expandNode(rootNode);
        state.selectNode(child, true);

        TreeNavigator navigator = new TreeNavigator(state);
        navigator.up();

        final Collection<Object> selectedNodes = state.getSelectedNodes();
        assertEquals(1, selectedNodes.size());
        assertEquals(rootNode, selectedNodes.iterator().next());
    }

    @Test
    public void upSelectsLastChildOfExpandedPreviousSibling() {
        final DefaultMutableTreeNode sibling = new DefaultMutableTreeNode();
        rootNode.insert(sibling, 0);
        final DefaultMutableTreeNode siblingChild = new DefaultMutableTreeNode();
        sibling.insert(siblingChild, 0);

        final DefaultMutableTreeNode selected = new DefaultMutableTreeNode();
        rootNode.insert(selected, 1);

        ITreeState state = new DefaultTreeState();
        state.expandNode(rootNode);
        state.expandNode(sibling);
        state.selectNode(selected, true);

        TreeNavigator navigator = new TreeNavigator(state);
        navigator.up();

        final Collection<Object> selectedNodes = state.getSelectedNodes();
        assertEquals(1, selectedNodes.size());
        assertEquals(siblingChild, selectedNodes.iterator().next());
    }

    @Test
    public void downSelectsNextSiblingWhenNodeIsCollapsed() {
        final DefaultMutableTreeNode selected = new DefaultMutableTreeNode();
        rootNode.insert(selected, 0);

        final DefaultMutableTreeNode sibling = new DefaultMutableTreeNode();
        rootNode.insert(sibling, 1);

        ITreeState state = new DefaultTreeState();
        state.expandNode(rootNode);
        state.selectNode(selected, true);

        TreeNavigator navigator = new TreeNavigator(state);
        navigator.down();

        final Collection<Object> selectedNodes = state.getSelectedNodes();
        assertEquals(1, selectedNodes.size());
        assertEquals(sibling, selectedNodes.iterator().next());
    }

    @Test
    public void downSelectsNextChildWhenNodeIsExpanded() {
        final DefaultMutableTreeNode selected = new DefaultMutableTreeNode();
        rootNode.insert(selected, 0);
        final DefaultMutableTreeNode child = new DefaultMutableTreeNode();
        selected.insert(child, 0);

        final DefaultMutableTreeNode sibling = new DefaultMutableTreeNode();
        rootNode.insert(sibling, 1);

        ITreeState state = new DefaultTreeState();
        state.expandNode(rootNode);
        state.expandNode(selected);
        state.selectNode(selected, true);

        TreeNavigator navigator = new TreeNavigator(state);
        navigator.down();

        final Collection<Object> selectedNodes = state.getSelectedNodes();
        assertEquals(1, selectedNodes.size());
        assertEquals(child, selectedNodes.iterator().next());
    }

    @Test
    public void downSelectsSiblingWhenNodeIsExpandedButItHasNoChildren() {
        final DefaultMutableTreeNode selected = new DefaultMutableTreeNode();
        rootNode.insert(selected, 0);

        final DefaultMutableTreeNode sibling = new DefaultMutableTreeNode();
        rootNode.insert(sibling, 1);

        ITreeState state = new DefaultTreeState();
        state.expandNode(rootNode);
        state.expandNode(selected);
        state.selectNode(selected, true);

        TreeNavigator navigator = new TreeNavigator(state);
        navigator.down();

        final Collection<Object> selectedNodes = state.getSelectedNodes();
        assertEquals(1, selectedNodes.size());
        assertEquals(sibling, selectedNodes.iterator().next());
    }

    @Test
    public void downSelectsParentSiblingWhenSelectedNodeIsLast() {
        final DefaultMutableTreeNode selected = new DefaultMutableTreeNode();
        rootNode.insert(selected, 0);
        final DefaultMutableTreeNode child = new DefaultMutableTreeNode();
        selected.insert(child, 0);

        final DefaultMutableTreeNode sibling = new DefaultMutableTreeNode();
        rootNode.insert(sibling, 1);

        ITreeState state = new DefaultTreeState();
        state.expandNode(rootNode);
        state.expandNode(selected);
        state.selectNode(child, true);

        TreeNavigator navigator = new TreeNavigator(state);
        navigator.down();

        final Collection<Object> selectedNodes = state.getSelectedNodes();
        assertEquals(1, selectedNodes.size());
        assertEquals(sibling, selectedNodes.iterator().next());
    }

}
