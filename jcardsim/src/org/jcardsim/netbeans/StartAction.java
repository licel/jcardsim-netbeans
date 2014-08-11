package org.jcardsim.netbeans;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Simple action 
 */
@ActionID(id = "org.jcardsim.netbeans.StartAction", category = "JavaCard3")
@ActionReference(path = "JavaCard3/kinds/jcardsim/Actions")
@ActionRegistration(displayName = "Start jCardSim")
public class StartAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent target) {
        Lookup selectedContext = Utilities.actionsGlobalContext();
        // I'm looking for current selected card
        JCardSimCard card = selectedContext.lookup(JCardSimCard.class);
        
        JOptionPane.showMessageDialog(null, "start action:" + card);
    }

}
