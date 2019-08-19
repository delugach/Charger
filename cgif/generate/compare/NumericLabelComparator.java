package cgif.generate.compare;

import java.util.Comparator;

import charger.obj.GEdge;

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
 * This class attempts to compare the labels of two edges by assuming they
 * are both integers.
 * 
 */
public class NumericLabelComparator implements Comparator<GEdge>
{
    /**
     * Compares the labels of the two edges by assuming they are both
     * integers.  If either isn't an integer, a NumberFormatException will
     * be thrown. 
     * 
     * @param e1 First edge to compare
     * @param e2 Second edge to compare
     * @return Returns negative value if the label of e1 &lt; e2, returns 0 if they are
     *         equivalent, and returns [positive if the label of e1 &gt; e2.
     *      *  returns positive if the first is null, and negative if the 2nd is null.
     */
    public int compare(GEdge e1, GEdge e2) throws NumberFormatException
    {
        if (e1 == null)
            return 1;
        if (e2 == null)
            return -1;
        
        int label1 = Integer.parseInt(e1.getTextLabel());
        int label2 = Integer.parseInt(e2.getTextLabel());
        
        return label1 - label2;
    }
}
