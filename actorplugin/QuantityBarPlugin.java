package actorplugin;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
Interfaces with a separate quantity-displaying bar on the screen.
@see chargerlib.QuantityBar
 */
public class QuantityBarPlugin implements charger.act.ActorPlugin
{
	private chargerlib.QuantityBar qbar = null;
	
	/**
			@return Name by which the actor will be known throughout the system
	 */
	public String getPluginActorName()
	{
		return "displaybar";
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
		return new ArrayList();		// empty vector; there are no outputs	
	}
	
	/**
				@return ArrayList of objects, usually in string form, indicating other actor constraints.
				Currently only "executable" and "commutative" are supported.
	 */
	public ArrayList getPluginActorAttributes()
	{
		ArrayList v = new ArrayList();
		v.add( (Object)"executable" );
		v.add( (Object)"displayable" );
		return v;
	}

	/**
		Perform the actor's function. Called by GraphUpdater when input or output concepts change.
	 */
    public void performActorOperation( charger.act.GraphUpdater dummy, ArrayList inputs, ArrayList outputs )
		throws charger.exception.CGActorException
	{
		//String in1 = ((notio.NameDesignator)((charger.obj.Concept)(inputs.get( 0 ))).nxConcept.getReferent().getDesignator() ).getName();
				charger.Global.info( "performing function for actor " + getPluginActorName() );
				charger.Global.info( "  with " + inputs.size() + " inputs and " + outputs.size() + " outputs." );
		String inref = null;
		String intype = null;
				//charger.Global.info( "inputs " + inputs.toString() );
				//charger.Global.info( "outputs " + outputs.toString() );
		if ( inputs.size() > 0 )
		{
			inref = (String)((charger.obj.Concept)(inputs.get( 0 ))).getReferent();
			intype = (String)((charger.obj.Concept)(inputs.get( 0 ))).getTypeLabel();
		}
				if ( charger.Global.traceActors ) charger.Global.info( "inref is " + inref );
		double value;
		try
		{
			value = Double.parseDouble( inref );
		} catch ( NumberFormatException nfe ) { value = (double)0.0; }
			//charger.Global.info( "Displaying the value " + value );
		// display the quantity bar here
		if ( qbar == null ) 
		{
                        double range = value * 2;
			qbar = new chargerlib.QuantityBar( value - range/2, value + range/2 );
			qbar.setDecimalPlaces( 2 );
			//qbar.setTitle( intype );
			//qbar.setMin( 0 );
			//qbar.setMax( value * 2 );
		}
                else if ( value < qbar.getMin() || value > qbar.getMax() )
                {
                    double range = value * 2;
                    qbar.setMin( value - range/2 );
                    qbar.setMax( value + range/2 );
                }
		qbar.setValue( value );
				charger.Global.info( "value is " + value + " in qbar " + this );
		qbar.setLegend( intype );
		qbar.setVisible( true );
		qbar.requestFocus();
		qbar.toFront();
	}
	
	/**
		Perform any clean-up required by the actor when it is deleted or its graph is de-activated.
	 */
	public void stopActor()
	{
		qbar.setVisible( false );
		qbar = null;
		charger.Global.info( getClass().getName() + " actor stopped." );
	}
	
	public String getSourceInfo()
	{
		return "Harry Delugach, 2003, delugach@uah.edu";
	}


}