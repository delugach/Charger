package actorplugin;

import charger.Global;
import chargerlib.CDateTime;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.text.*;

/**
	Performs a basic clock "pulse" function for charger. Actor's name is "pulse".
 */
public class PulsePlugin implements charger.act.ActorPlugin
{

	/**
			@return Name by which the actor will be known throughout the system
	 */
	public String getPluginActorName()
	{
		return "pulse";
	}
	
	/**
		Assumption about input and output vectors is that inputs are numbered 1..n and outputs are numbered
			n+1 .. m.	
		@return List of input concepts (or graphs), each with a constraining type (or "T" )
	 */
	public ArrayList getPluginActorInputConceptList()
	{
		return charger.act.GraphUpdater.createConceptList( 2, "Number" );
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
	 */
	public ArrayList getPluginActorAttributes()
	{
		ArrayList v = new ArrayList();
		v.add( (Object)"executable" );
		v.add( (Object)"autonomous" );
		v.add( (Object)"trigger" );
		return v;
	}


	charger.obj.GNode gnodeToUpdate = null;
	charger.act.GraphUpdater myUpdater = null;
	Thread myUpdaterThread = null;

	private javax.swing.Timer timer = new javax.swing.Timer( 0, new ActionListener()
		{  public void actionPerformed( ActionEvent e )
			{
				performTimerAction();
			}
		});

    private void performTimerAction() {
        // simply a test referent so that I can see if propagation works.
        String now = new CDateTime().formatted( Global.ChargerDefaultDateTimeStyle );
//                    = DateFormat.getTimeInstance( DateFormat.MEDIUM ).format( Calendar.getInstance().getTime() );
        ( (charger.obj.Concept)gnodeToUpdate ).setReferent( ( (charger.obj.Concept)gnodeToUpdate ).getReferent(), true );
        //((charger.obj.Concept)gnodeToUpdate).setReferent( now, true );

        //((charger.obj.Concept)gnodeToUpdate).setReferent( "pulsed", true );
        // whatever we do to the output referent(s), mark them ready to activate
        gnodeToUpdate.setChanged( true );
        gnodeToUpdate.setActive( false );

        ThreadGroup tg = new ThreadGroup( "no frame" );
		if ( myUpdater.ownerFrame != null) tg = myUpdater.ownerFrame.threadgroup; 
		
				// point the new updater at the output node, assuming it's changed
		myUpdater = new charger.act.GraphUpdater( myUpdater.ownerFrame, gnodeToUpdate );
				// establish a new thread for the updater, and send it on its way
		myUpdaterThread = new Thread( tg, myUpdater, "pulse at " + now );
		myUpdaterThread.start();

		/*try {
			myUpdater.propagate( gnodeToUpdate );
		} catch ( charger.act.CGActorException cgae ) 
		{
			charger.Hub.error( "Actor exception during actor \"pulse\": " + cgae.getMessage() );
		}
		*/
	
		charger.Global.info( "end of performTimerAction for pulse at " + now );
	}


	/**
		Perform the actor's function. Called by GraphUpdater when input or output concepts change.
		Note: Does changing the interval stop the old timer and start a new one?
	 */
    public void performActorOperation( charger.act.GraphUpdater gu, ArrayList inputs, ArrayList outputs )
				throws charger.exception.CGActorException
	{
		myUpdater = gu;
		//String in1 = ((notio.NameDesignator)((charger.obj.Concept)(inputs.get( 0 ))).nxConcept.getReferent().getDesignator() ).getName();
		//charger.Global.info( "performing function for actor " + getPluginActorName() );
		//charger.Global.info( "  with " + inputs.size() + " inputs and " + outputs.size() + " outputs." );
		String intype1 = null;
		String inref1 = null;
		String intype2 = null;
		String inref2 = null;
				//charger.Global.info( "inputs " + inputs.toString() );
				//charger.Global.info( "outputs " + outputs.toString() );
		if ( inputs.size() == 2 )
		{
			intype1 = (String)((charger.obj.Concept)(inputs.get( 0 ))).getTypeLabel();
			inref1 = (String)((charger.obj.Concept)(inputs.get( 0 ))).getReferent();

			inref2 = (String)((charger.obj.Concept)(inputs.get( 1 ))).getReferent();
			intype2 = (String)((charger.obj.Concept)(inputs.get( 1 ))).getTypeLabel();
		}

		double interval;
		String intervalReferent = null;
		String enableType = null;
		if ( intype1.equalsIgnoreCase( "interval" ) ) 
		{
			intervalReferent = inref1;
			enableType = intype2;
		}
		else if ( intype2.equalsIgnoreCase( "interval" ))
		{
			intervalReferent = inref2;
			enableType = intype1;
		}
		else
		{
			// oops! neither referent was an interval
		}
		try
		{
			interval = Double.parseDouble( intervalReferent );
		} catch ( NumberFormatException nfe ) { interval = (double)0.0; }
			charger.Global.info( "Displaying the value for pulse plugin " + interval );
		if ( enableType.equalsIgnoreCase( "T") )
		{
					// set up the output node for updating
		//if ( outputs.size() > 0 )
			charger.EditFrame ownerFrame = 
				((charger.obj.Concept)(inputs.get( 0 ))).getOutermostGraph().getOwnerFrame();
			gnodeToUpdate = (charger.obj.GNode)(outputs.get( 0 ));
					// actual change to gnode is performed by timer's action listener

					// the initial (concept's) updater is already executing here
			//myUpdater.propagate( gnodeToUpdate );

					// set up the timer and start it		
			timer.setDelay( 1000 * (int)Math.round( interval ) );
			timer.setInitialDelay( 0 );
			//timer.setLogTimers( charger.Hub.infoOn );
			timer.start();
		}
		else
			timer.stop();
	}
	
	/**
		Perform any clean-up required by the actor when it is deleted or its graph is de-activated.
	 */
	public void stopActor()
	{
		timer.stop();
		charger.Global.info( getClass().getName() + " actor stopped." );
	}
	
	public String getSourceInfo()
	{
		return "Harry Delugach, 2003, delugach@uah.edu";
	}


}