package cgif.generate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import charger.obj.Concept;
import charger.obj.DeepIterator;
import charger.obj.Graph;
import charger.obj.GraphObject;

/*
 CharGer - Conceptual Graph Editor
 Copyright 1998-2019 by Harry S. Delugach

 This package is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as
 published by the Free Software Foundation; either version 2.1 of the
 License, or (at your option) any later version. This package is 
 distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 details. You should have received a copy of the GNU Lesser General Public
 License along with this package; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/**
 * This class is used to generate unique label names for a conceptual graph.
 *   * @author Harry S. Delugach ( delugach@uah.edu ) Copyright 1998-2019 

 */
public class NameGenerator
{
    /**
     * Base name used for the label names
     */
    private static final String BASE_NAME = "x";
    
    /**
     * Next digit to concatenate with the base name to generate a new label
     * name.  This should always be positive.
     */
    private long nextDigit = 1;
    
    /**
     * Set to store all the label names that have been used
     */
    private Set<String> nameSet = new HashSet<String>();
    
    /**
     * Default constructor that creates a new name generator with an empty
     * set of used names.
     */
    public NameGenerator()
    {
    }
    
    /**
     * Creates a new name generator with the names from the specified graph
     * excluded as possible names to generate.
     * 
     * @param graph All the label names from the graph are added as to the
     *              set of names to exclude.
     */
    public NameGenerator(Graph graph)
    {
        // add the names from the specified graph
        addNames(graph);
    }
    
    /**
     * Adds the specified name to the set of names to excluded when
     * generating label names.
     * 
     * @param name Name to add to the exclusion set
     */
    public void addName(String name)
    {
        nameSet.add(name);
    }
    
    /**
     * Adds the defining and bound label names from the specified graph to
     * the list of names to exclude when generating new names.
     * 
     * @param graph Graph to traverse to look for label names
     */
    public void addNames(Graph graph)
    {
        // verify the input is valid
        if (graph == null)
            return;
        
        // traverse the graph looking for label names
        Iterator<GraphObject> itr = new DeepIterator(graph, GraphObject.Kind.GNODE);
        while (itr.hasNext())
        {
            Object obj = itr.next();
            if (obj instanceof Concept)
            {
                // referent names are of the form "[quantifier] [*|?|#]name"
                String referent = ((Concept)obj).getReferent();
                int index = referent.lastIndexOf("*");
                if (index < 0)
                {
                    index = referent.lastIndexOf("?");
                    if (index < 0)
                        index = referent.lastIndexOf("#");
                }

                // extract just the name of the defining or bound label if
                // there is one and add it to the exclusion set
                if (index >= 0 && index < referent.length()-1)
                {
                    String name = referent.substring(index+1).trim();
                    addName(name);
                }
                else
                {
                    // add explicit referent name
                    addName(referent.trim());
                }
            }
        }
    }
    
    /**
     * Returns true if the specified name is a label name that has not been
     * generated and hasn't been manually added to the set of names to
     * exclude.
     *  
     * @param name Name to test
     * @return Returns true if the name hasn't been used
     */
    public boolean isUseableName(String name)
    {
        return (name != null && !nameSet.contains(name));
    }
    
    /**
     * Looks for several cases: is the name null, the empty string, a single "*" or a set referent with *
     * @param name
     * @return 
     */
    public boolean isGenericName( String name ) {
        if ( name == null ) return true;
        if ( name.length() == 0 ) return true;
        if ( name.trim().equals("*")) return true;
        String trimmed = name.replaceAll(" ", "");
        if ( trimmed.trim().equals("{*}") ) return true;
        return false;
        
    }
    
    /**
     * Clears the set of names that have been used so far and resets the
     * name generation sequence.
     */
    public void clear()
    {
        nameSet.clear();
        nextDigit = 1;
    }
    
    /**
     * Returns a copy of the set of names that have been generated or
     * manually added to the exclusion set.
     * 
     * @return Returns the set of names that have been used
     */
    public Set<String> getNames()
    {
        return new HashSet<String>(nameSet);
    }
    
    /**
     * Generates and returns a label name that has not been used.  Only the
     * name is returned, not any symbols that need to precede it for CGIF.
     * The generated name is added to the internal set of names not to be used
     * again.
     * 
     * @return Returns a label name that has not been used
     */
    public String generateName()
    {
        String name;
        
        do {
            name = BASE_NAME + nextDigit++;
            
            // protect against wrap around to negative numbers
            if (nextDigit < 1)
                nextDigit = 1;
        } while (!isUseableName(name));
        
        return name;
    }
}
