package cgif.generate.compare;

import java.util.Comparator;

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
 * This class compares the object ID's of two GraphObject's.
 * 
 */
public class ObjectIDComparator implements Comparator<GraphObject>
{
    /**
     * Compares the object ID's of the two GraphObect's.
     * 
     * @param o1 First object to compare
     * @param o2 Second object to compare
     * @return Returns -1 if the object ID of o1 &lt; o2, returns 0 if they are
     *         equivalent, and returns 1 if the object ID of o1 &gt; o2.
     *  returns 1 if the first is null, and -1 if the 2nd is null.
     */
    public int compare(GraphObject o1, GraphObject o2)
    {
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;
        
//        return (int)(o1.objectID - o2.objectID);
        if ( o2.objectID.equals( o2.objectID )) return 0;
        else return 1;
    }
}
