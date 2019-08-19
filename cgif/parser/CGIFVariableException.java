/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cgif.parser;

/**
 * Used in cases where a CGIF variable is either malformed or 
 * an un-bound variable does not correspond to a bound one.
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class CGIFVariableException extends Exception {

    public CGIFVariableException( String msg ) {
        super( msg );
    }
}
