package org.jcardsim.netbeans;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.javacard.spi.Cards;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.Lookups;

/**
 * Not cards... There is only one card!
 */
public final class CardsImpl extends Cards implements Lookup.Provider {
    
    private final DataObject dob;
    
    private Lookup lkp;

    public CardsImpl(DataObject platformDob) {
        this.dob = platformDob;
    }

    @Override
    public List<? extends Provider> getCardSources() {
        return Collections.singletonList(this);
    }

    public synchronized Lookup getLookup() {
        if (lkp == null) {
            JavacardPlatform pform = dob.getLookup().lookup(JavacardPlatform.class);
            if (pform != null) { //file possibly deleted?
//                JCardSimCard theCard = new JCardSimCard(null, pform, null);
                JCardSimCard theCard = new JCardSimCard(pform);
                
                lkp = Lookups.fixed(theCard);
            } else {
                lkp = Lookup.EMPTY;
            }
        }
        return lkp;
    }
}
