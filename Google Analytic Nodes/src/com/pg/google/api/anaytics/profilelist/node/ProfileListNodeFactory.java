package com.pg.google.api.anaytics.profilelist.node;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ProfileList" Node.
 * 
 *
 * @author Procter & Gamble, eBusiness
 */
public class ProfileListNodeFactory 
        extends NodeFactory<ProfileListNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileListNodeModel createNodeModel() {
        return new ProfileListNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<ProfileListNodeModel> createNodeView(final int viewIndex,
            final ProfileListNodeModel nodeModel) {
        return new ProfileListNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new ProfileListNodeDialog();
    }

}

