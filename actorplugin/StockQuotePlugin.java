//
//  StockQuotePlugin.java
//
//  Created by Harry Delugach on Fri Jul 18 2003.
//
package actorplugin;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;

/* 
	$Header$ 
*/
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
Interfaces with a separate quantity-displaying bar on the screen.
@see actorplugin.QuantityBarPlugin
 */
public class StockQuotePlugin implements charger.act.ActorPlugin
{
	private String urlString = "http://moneycentral.msn.com/scripts/webquote.dll?ipage=qd&Symbol=";
	private String pattern = ">Real-time quotes</a></td></tr><tr class=\"rs0\"><th colspan=\"4\"><span class=\"s1\">";
	private boolean alreadyWarned = false;
	
	private charger.EditFrame owner = null;
	/**
		Gives the value <code>"stockquote"</code> for this actor.
			@return Name by which the actor will be known throughout the system
	 */
	public String getPluginActorName()
	{
		return "stockquote";
	}
	
	/**
		Assumption about input and output vectors is that inputs are numbered 1..n and outputs are numbered
			n+1 .. m.	
		@return List of input concepts (or graphs), each with a constraining type (or "T" )
	 */
	public ArrayList getPluginActorInputConceptList()
	{
		return charger.act.GraphUpdater.createConceptList( 1, "T" );
	}

	/**
		Assumption about input and output vectors is that inputs are numbered 1..n and outputs are numbered
			n+1 .. m.	
		@return List of output concepts (or graphs), each with a constraining type (or "T" )
	 */	
	public ArrayList getPluginActorOutputConceptList()
	{
		return charger.act.GraphUpdater.createConceptList( 1, "T" );	
	}
	
	/**
				@return ArrayList of objects, usually in string form, indicating other actor constraints.
				Currently only "executable" and "commutative" are supported.
	 */
	public ArrayList getPluginActorAttributes()
	{
		ArrayList v = new ArrayList();
		v.add( (Object)"executable" );
		v.add( (Object)"trigger" );
		return v;
	}

	/**
		Perform the actor's function. Called by GraphUpdater when input or output concepts change.
	 */
    public void performActorOperation( charger.act.GraphUpdater dummy, ArrayList inputs, ArrayList outputs )
		throws charger.exception.CGActorException
	{
		//String in1 = ((notio.NameDesignator)((charger.obj.Concept)(inputs.get( 0 ))).nxConcept.getReferent().getDesignator() ).getName();
		//charger.Global.info( "performing function for actor " + getPluginActorName() );
		//charger.Global.info( "  with " + inputs.size() + " inputs and " + outputs.size() + " outputs." );
		String inref = null;
		String intype = null;
				//charger.Global.info( "inputs " + inputs.toString() );
				//charger.Global.info( "outputs " + outputs.toString() );
		if ( inputs.size() > 0 )
		{
			inref = (String)((charger.obj.Concept)(inputs.get( 0 ))).getReferent();
			intype = (String)((charger.obj.Concept)(inputs.get( 0 ))).getTypeLabel();
			try {
				owner = ((charger.obj.Concept)inputs.get( 0 )).getOutermostGraph().getOwnerFrame();
			} catch ( NullPointerException e ) { owner = null; }
		}
				if ( charger.Global.traceActors ) charger.Global.info( "inref is " + inref );
		charger.obj.Concept out = (charger.obj.Concept)outputs.get( 0 );
		out.setReferent( Float.toString( getQuote( inref ) ), true );
	}
	
	private float getQuote( String tickerSymbol )
	{
		try 
		{
			URL quoteserver = new URL( urlString + tickerSymbol );
			BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                quoteserver.openStream()));
	
			String inputLine;
			//boolean keepGoing = true;
	
			while ((inputLine = in.readLine()) != null ) // && keepGoing )
			{
						//charger.Global.info( "input line is " + inputLine );
				//System.out.println(inputLine);
				if ( inputLine.indexOf( pattern ) != -1 )
				{
					int start = inputLine.indexOf( pattern ) + pattern.length();
					int end = inputLine.indexOf( '<', start );
					float result =  Float.parseFloat( inputLine.substring( start, end ) );
							charger.Global.info( "I think that " + result + " is the result." );
					return result;
				}
			}
			in.close();
		} catch ( MalformedURLException me ) { charger.Global.warning( "Bad URL: " + me.getMessage() );
		} catch ( IOException ie ) 
		{ 
			if ( ! alreadyWarned )
			{
				alreadyWarned = true;
				charger.Global.error( "IO Exception " + ie.getMessage() );
				JOptionPane.showMessageDialog( owner, 
					getPluginActorName() + " error: Cannot access the Internet; " + ie.getMessage(),
						getPluginActorName(), JOptionPane.ERROR_MESSAGE );
			}
		}
		
		return 0f;
	}
	
	/**
		Perform any clean-up required by the actor when it is deleted or its graph is de-activated.
	 */
	public void stopActor()
	{
		charger.Global.info( getClass().getName() + " actor stopped." );
	}
	
	public String getSourceInfo()
	{
		return "Harry Delugach, 2003, delugach@uah.edu";
	}


}