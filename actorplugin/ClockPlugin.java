package actorplugin;

import charger.Global;
import charger.obj.Concept;
import chargerlib.CDateTime;
import javax.swing.*;
import java.util.*;
import java.text.*;

/**
 * Produces the current wall-clock time.
 */
public class ClockPlugin implements charger.act.ActorPlugin {

    /**
     * @return Name by which the actor will be known throughout the system
     */
    public String getPluginActorName() {
        return "time";
    }

    /**
     * Assumption about input and output vectors is that inputs are numbered
     * 1..n and outputs are numbered n+1 .. m.
     *
     * @return List of input concepts (or graphs), each with a constraining type
     * (or "T" )
     */
    public ArrayList<Concept> getPluginActorInputConceptList() {
        return new ArrayList<>();
        //return charger.act.GraphUpdater.createConceptVector( 1, "T" );
    }

    /**
     * Assumption about input and output vectors is that inputs are numbered
     * 1..n and outputs are numbered n+1 .. m.
     *
     * @return List of output concepts (or graphs), each with a constraining
     * type (or "T" )
     */
    public ArrayList<Concept> getPluginActorOutputConceptList() {
        return charger.act.GraphUpdater.createConceptList( 1, "T" );
    }

    /**
     * @return ArrayList of objects, usually in string form, indicating other
     * actor constraints. Currently "executable", "commutative"
     * "varyinginputcardinality" and "varyingoutputcardinality" are supported.
     */
    public ArrayList getPluginActorAttributes() {
        ArrayList v = new ArrayList<>();
        v.add( (Object)"executable" );
        v.add( (Object)"trigger" );
        v.add( (Object)"varyingInputCardinality" );

        return v;
    }

    /**
     * Perform the actor's function. Called by GraphUpdater when input or output
     * concepts change.
     */
    public void performActorOperation( charger.act.GraphUpdater gu, ArrayList inputs, ArrayList outputs ) throws charger.exception.CGActorException {
		//String in1 = ((notio.NameDesignator)((charger.obj.Concept)(inputs.get( 0 ))).nxConcept.getReferent().getDesignator() ).getName();
        //charger.Global.info( "performing function for actor " + getPluginActorName() );
        //charger.Global.info( "  with " + inputs.size() + " inputs and " + outputs.size() + " outputs." );
        String ins = null;
				//charger.Global.info( "inputs " + inputs.toString() );
        //charger.Global.info( "outputs " + outputs.toString() );
        if ( inputs.size() > 0 ) {
            ins = (String)( (charger.obj.Concept)( inputs.get( 0 ) ) ).getReferent();
        }
        charger.Global.info( "ins is " + ins );

        if ( outputs.size() > 0 ) {
            charger.obj.GNode gnodeToUpdate = ( (charger.obj.Concept)( outputs.get( 0 ) ) );
            String oldReferent = ( (charger.obj.Concept)gnodeToUpdate ).getReferent();
			//((charger.obj.Concept)gnodeToUpdate).setReferent( ins, true );
            // assume output referent has changed
            String now = new CDateTime().formatted( Global.ChargerDefaultDateTimeStyle);
//                    = DateFormat.getTimeInstance( DateFormat.MEDIUM ).format( Calendar.getInstance().getTime() );
            ( (charger.obj.Concept)gnodeToUpdate ).setReferent( now, true );
            gu.propagate( gnodeToUpdate );
        }
    }

    /**
     * Perform any clean-up required by the actor when it is deleted or its
     * graph is de-activated.
     */
    public void stopActor() {
        charger.Global.info( "Actor stopped." );
    }

    public String getSourceInfo() {
        return "Harry Delugach, 2003, delugach@uah.edu";
    }

}
