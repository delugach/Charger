/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cgif.parser;

import charger.obj.GraphObject;
import java.util.HashMap;

/**
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class ReferentMap {
        /** Key is a variable name (e.g., x1) or string referent */
    private HashMap<String, GraphObject> objectmap = new HashMap<String, GraphObject>();
    
    public ReferentMap() {
        
    }
    
    public GraphObject getObjectByReferent( String ref ) {
        if ( ref == null ) return null;
        if ( ref.startsWith( "?"))
            ref = "*" + ref.substring( 1);
        return objectmap.get( ref );
    }
    
    public void putObjectByReferent( String ref, GraphObject go ) throws CGIFVariableException {
        if ( getObjectByReferent( ref ) != null ) {
            throw new CGIFVariableException( "Variable " + ref + " already exists.");
        } else {
            objectmap.put(  ref, go);
        }
    }
    
    public void clear() {
        objectmap.clear();
    }
}
