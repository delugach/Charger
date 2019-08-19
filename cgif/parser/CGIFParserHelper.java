/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cgif.parser;

import cgif.parser.javacc.Token;
import charger.Global;
import charger.obj.Actor;
import charger.obj.Arrow;
import charger.obj.Concept;
import charger.obj.DeepIterator;
import charger.obj.GenSpecLink;
import charger.obj.Graph;
import charger.obj.GraphObject;
import charger.obj.Referent;
import charger.obj.Relation;
import charger.obj.TypeLabel;
import charger.cgx.CGXParser;
import charger.exception.CGEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Harry S. Delugach (delugach@uah.edu)
 */
public class CGIFParserHelper {

    public ReferentMap referents = new ReferentMap();

    /**
     * Looks for a charger comment in either the first or second token.
     * Returns first one found.
     * @param token1
     * @param token2
     * @return 
     */
    public static String extractChargerComment( Token token ) {
        if ( token == null || token.specialToken == null ) {
            return null;
        }
        String comment = token.specialToken.toString();
        String chargerComment = null;
        System.out.println( "Comment is " + comment );
        if ( comment.contains( Global.CharGerCGIFCommentStart ) ) {
            int start = comment.indexOf( Global.CharGerCGIFCommentStart ) + Global.CharGerCGIFCommentStart.length();
            chargerComment = comment.substring( start, comment.length() - 2 );
            System.out.println( "Charger comment is \"" + chargerComment + "\"" );
        }
        return chargerComment;
    }
    
    public static String extractChargerComment( Token t1, Token t2 ) {
        String chargerComment = extractChargerComment( t1 );
        if ( chargerComment  != null )
            return chargerComment;
        else {
            chargerComment = extractChargerComment( t2 );
            if ( chargerComment == null )
                return null;
            else
                return chargerComment;
        }
    }
    
    public void parseChargerLayout( String chargerComment, GraphObject go ) {
        CGXParser parser = null;
        try {
            parser = new CGXParser( chargerComment );
        } catch ( CGEncodingException ex ) {    // CR-1005 won't ever happen internally
            Logger.getLogger( CGIFParserHelper.class.getName() ).log( Level.SEVERE, null, ex );
        }
        parser.parseLayoutOnly( go );
        go.resizeIfNecessary();
    }

    /**
     * Processes the concept from cgif07.jj (as converted into CGIFParser.java)
     * @param graph The graph into which the concept will be inserted
     * @param type The type name to be made into a type label
     * @param referent the referent as established by the parser
     * @param layout CGX layout string, if present in a CGIF comment
     */
    public Concept makeConcept( Graph g, String type, Referent referent, String layout ) {
        System.out.println( "Create concept - type: " + type + " - referent: " + referent
            + " - layout = " + layout);
        Concept concept = new Concept();
        concept.setReferent( unquotify( referent.getReferentString() ), false );
        concept.setTypeLabel( unquotify( type ) );
        if ( referents.getObjectByReferent( referent.getCgifVariableReference()) == null ) {
            try {
                referents.putObjectByReferent( referent.getCgifVariableReference(), concept);
            } catch ( CGIFVariableException ex ) {
                Logger.getLogger(CGIFParserHelper.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }
        g.insertObject( concept );
        if ( layout != null ) parseChargerLayout( layout, concept );
        return concept;
    }

    /**
     * Use the CGIF information to instantiate a new type label.
     * @param graph The graph into which the concept will be inserted
     * @param type The type name to be made into a type label
     * @param layout CGX layout string, if present in a CGIF comment
     */
    public void makeTypeLabel( Graph g,  String type, String layout )  throws CGIFSubtypeException {
        System.out.println( "Create typelabel - type: " + type
                + " - layout = " + layout );
        TypeLabel typelabel = new TypeLabel();
        typelabel.setTypeLabel( type );
        Iterator<GraphObject> iter = new DeepIterator( g, GraphObject.Kind.GNODE );

        boolean found = false;
        while ( iter.hasNext() ) {
            GraphObject obj = iter.next();
            if ( obj instanceof TypeLabel ) {
                if ( obj.getTextLabel().toLowerCase().equals( type.toLowerCase() ) ) {
                    found = true;
                }
            }
        }
        if ( found ) throw new CGIFSubtypeException( "Type label " + type + " is already declared.");

                if ( layout != null ) parseChargerLayout( layout, typelabel );
        g.insertObject( typelabel );
//        typelabel.setLayout( layout );
    }

    /**
     * Processes the gen spec link from cgif07.jj (as converted into CGIFParser.java)
     * @param g
     * @param subtype
     * @param supertype
     * @param layout CGX layout string, if present in a CGIF comment
     * @throws CGIFSubtypeException 
     */
    public void makeGenSpecLink( Graph g, String subtype, String supertype, String layout ) throws CGIFSubtypeException {
        // find graph objects associated with the subtype and supertype
        GraphObject subobj = null;
        GraphObject superobj = null;
        Iterator<GraphObject> iter = new DeepIterator(g, GraphObject.Kind.GNODE);
        
        while (iter.hasNext())
        {
            GraphObject obj = iter.next();
            if (obj instanceof TypeLabel) {
                if ( obj.getTextLabel().toLowerCase().equals( subtype.toLowerCase()) ) 
                    subobj = obj;
                if ( obj.getTextLabel().toLowerCase().equals( supertype.toLowerCase()) ) 
                    superobj = obj;
            }
        }
        if ( subobj == null ) throw new CGIFSubtypeException( "subtype \"" + subtype + "\" not found.");
        if ( superobj == null ) throw new CGIFSubtypeException( "supertype \"" + supertype + "\" not found.");
        
        GenSpecLink link = new GenSpecLink( subobj, superobj );
        g.insertObject( link );
    }

    /**
     * Processes the relation from cgif07.jj (as converted into CGIFParser.java)
     * @param g the graph in which this actor is to be inserted
     * @param name name of the actor
     * @param variables zero or more variables, to be associated with  concepts. Last one is considered output arc's target.
     * @param layout CGX layout string, if present in a CGIF comment
     * @return
     * @throws CGIFVariableException 
     */
    public Relation makeRelation( Graph g, String name, ArrayList<String> variables, String layout ) throws CGIFVariableException {
        System.out.println( "Parsed relation - name: " + name  );
        Relation relation = new Relation();
        relation.setTextLabel( name );
        
        if ( layout != null ) parseChargerLayout( layout, relation );
        g.insertObject( relation );
        int varnum = 0;
                // TODO: traverse all the variables, figure out their concepts, and insert edges.
        for ( String var : variables ) {
            String newvar = var;
            varnum++;
            if ( var.startsWith( "?")) {
                newvar = var.replaceFirst(  Pattern.quote("?"), "*");
            }
            GraphObject go = referents.getObjectByReferent( newvar );
            if ( go == null ) // Something is wrong; we have an unbound var without its bound counterpart
                throw new CGIFVariableException("Variable \"" + var + "\" not found.");
            else {
                Arrow arc;
                if ( varnum == variables.size() ) {     // last argument makes relation the fromObj
                     arc = new Arrow( relation, go );
                } else {    // obj is a fromObj
                     arc = new Arrow( go, relation );
                }
                g.insertObject( arc);
            }
        }
        return relation;
    }
    
    /**
     * Processes the actor from cgif07.jj (as converted into CGIFParser.java)
     * @param g the graph in which this actor is to be inserted
     * @param name name of the actor
     * @param inputvariables zero or more input variables, to be associated with input concepts
     * @param outputvariables zero or more output variables, to be associated with output concepts
     * @param layout CGX layout string, if present in a CGIF comment
     * @return
     * @throws CGIFVariableException 
     */
    public Actor makeActor( Graph g, String name, ArrayList<String> inputvariables,ArrayList<String> outputvariables, String layout ) throws CGIFVariableException {
        System.out.println( "Parsed actor - name: " + name  );
        Actor actor = new Actor();
        actor.setTextLabel( name );
        
        if ( layout != null ) parseChargerLayout( layout, actor );
        g.insertObject( actor );
        int varnum = 0;
               // TODO: traverse all the variables, figure out their concepts, and insert edges.
        for ( String var : inputvariables ) {
            String newvar = var;
            varnum++;
            if ( var.startsWith( "?" ) ) {
                newvar = var.replaceFirst( Pattern.quote( "?" ), "*" );
            }
            GraphObject go = referents.getObjectByReferent( newvar );
            if ( go == null ) // Something is wrong; we have an unbound var without its bound counterpart
            {
                throw new CGIFVariableException( "Variable \"" + var + "\" not found." );
            } else {
                Arrow arc;
                arc = new Arrow( go, actor );
                g.insertObject( arc );
            }
        }
        
        for ( String var : outputvariables ) {
            String newvar = var;
            varnum++;
            if ( var.startsWith( "?" ) ) {
                newvar = var.replaceFirst( Pattern.quote( "?" ), "*" );
            }
            GraphObject go = referents.getObjectByReferent( newvar );
            if ( go == null ) // Something is wrong; we have an unbound var without its bound counterpart
            {
                throw new CGIFVariableException( "Variable \"" + var + "\" not found." );
            } else {
                Arrow arc;
                arc = new Arrow( actor, go );
                g.insertObject( arc );
            }
        }
        return actor;
    }

//    public void makeGraph( String name, ArrayList<GraphObject> objects, String layout ) {
//        System.out.println( "Parsed relation - name: " + name  );
//    }
    
    String unquotify( String possiblyQuotedString ) {
        if ( ! possiblyQuotedString.startsWith( "\""))
            return possiblyQuotedString;
        if ( ! possiblyQuotedString.endsWith( "\"")) {      // Can't happen. Parser generated won't recognize such a string.
            JOptionPane.showMessageDialog( null, "Malformed string in CGIF, non-terminated quoted string:\n"
                + possiblyQuotedString);
            return null;
        }
        possiblyQuotedString = possiblyQuotedString.replaceFirst( Pattern.quote("\""), "");
        possiblyQuotedString = possiblyQuotedString.substring( 0, possiblyQuotedString.length() - 1 );
        
        possiblyQuotedString = possiblyQuotedString.replaceAll( Pattern.quote("\\\""), "\"");
        
        return possiblyQuotedString;
    }
    
}
