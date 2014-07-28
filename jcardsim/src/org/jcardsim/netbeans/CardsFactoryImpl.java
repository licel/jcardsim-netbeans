package org.jcardsim.netbeans;

import org.netbeans.modules.javacard.ri.spi.CardsFactory;
import org.netbeans.modules.javacard.spi.Cards;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * jCardSim CardFactory
 * @author Vitaly Ligay email:vitaly.ligay@gmail.com
 */
public class CardsFactoryImpl extends CardsFactory {
    
    @Override
    protected Cards createCards(Lookup.Provider source) {
        DataObject platformDob = source.getLookup().lookup(DataObject.class);
        return new CardsImpl(platformDob);
    }
}
