package org.hippocms.repository.webapp.menu.reset;

import javax.jcr.RepositoryException;

import org.apache.wicket.markup.html.basic.Label;
import org.hippocms.repository.webapp.Main;
import org.hippocms.repository.webapp.menu.AbstractDialogPage;

public class ResetDialogPage extends AbstractDialogPage {
    private static final long serialVersionUID = 1L;

    public ResetDialogPage(final ResetDialog dialog) {
        super(dialog);
        Label label;
        try {
            boolean changes = Main.getSession().hasPendingChanges();
            if (changes) {
                label = new Label("changes", "There are pending changes");
            } else {
                label = new Label("changes", "There are no pending changes");
            }
        } catch (RepositoryException e) {
            label = new Label("changes", "exception: " + e.getMessage());
        }
        add(label);
    }
    
    public void ok() {
        try {
            Main.getSession().refresh(false);
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void cancel() {
    }

}
