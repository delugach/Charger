package cgif.generate;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import cgif.generate.compare.NumericLabelComparator;
import cgif.generate.compare.ObjectIDComparator;
import charger.Global;
import charger.obj.Actor;
import charger.obj.Concept;
import charger.obj.Coref;
import charger.obj.GEdge;
import charger.obj.GNode;
import charger.obj.GenSpecLink;
import charger.obj.GraphObjectID;
import charger.obj.Graph;
import charger.obj.GraphObject;
import charger.obj.Relation;
import charger.obj.TypeLabel;
import charger.cgx.CGXGenerator;
import java.util.HashMap;
import java.util.regex.Pattern;

/*
 CharGer - Conceptual Graph Editor
 Copyright 1998-2019 by Harry S. Delugach.

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
 * This class can be used to convert conceptual graphs in the CharGer format
 * into the standard CGIF file format based on ISO/IEC 24707:2007(E).
 * * @author Harry S. Delugach ( delugach@uah.edu ) Copyright 1998-2019 

 */
public class CGIFWriter {

    /**
     * Used to generate unique label names for concepts in the graph that do not
     * contain constants.
     */
    private NameGenerator nameGenerator = new NameGenerator();

    /**
     * Table mapping GraphObject objectID's to defining/bound label names
     */
    private HashMap<GraphObjectID, String> nameTable = new HashMap<>();

    /**
     * Output stream to which to write the conceptual graphs
     */
    private PrintStream stream;

    /**
     * This string represents the indention applied to inner subcontexts
     */
    private String indent;

    /**
     * Flag specifying if comments should be written when generating CGIF
     */
    private boolean writeComments;

    /**
     * Creates a CGIFWriter that can write conceptual graphs in CGIF format to
     * the specified output stream using a default sub-context indention of four
     * whitespaces.
     *
     * @param out Output stream to which to write the conceptual graphs
     */
    public CGIFWriter( OutputStream out ) {
        this( out, "    " );
    }

    /**
     * Creates a CGIFWriter that can write conceptual graphs in CGIF format to
     * the specified output stream using the specified sub-context indention.
     *
     * @param out Output stream to which to write the conceptual graphs
     * @param indent Indention applied to each sub-context
     */
    public CGIFWriter( OutputStream out, String indent ) {
        // first, buffer the output stream for efficiency, then wrap it with
        // a PrintStream to make printing easier
        stream = new PrintStream( new BufferedOutputStream( out ) );
        this.indent = ( indent != null ) ? indent : "";
    }

    /**
     * Creates a CGIFWriter that can write conceptual graphs in CGIF format to
     * the specified output stream using the specified sub-context indention.
     *
     * @param out Output stream to which to write the conceptual graphs
     * @param indent Indention applied to each sub-context
     * @param writeComments Flag specifying if comments should be written
     */
    public CGIFWriter( OutputStream out, String indent, boolean writeComments ) {
        // first, buffer the output stream for efficiency, then wrap it with
        // a PrintStream to make printing easier
        stream = new PrintStream( new BufferedOutputStream( out ) );
        this.indent = ( indent != null ) ? indent : "";
        this.writeComments = writeComments;
    }

    /**
     * Writes the specified graph to the specified output stream using the
     * standard CGIF format without comments.
     *
     * @param out Output stream to which to write the graph
     * @param graph Graph to convert and write out
     */
    public static void write( OutputStream out, Graph graph ) {
        write( out, graph, false );
    }

    /**
     * Writes the specified graph to the specified output stream using the
     * standard CGIF format.
     *
     * @param out Output stream to which to write the graph
     * @param graph Graph to convert and write out
     * @param writeComments Flag specifying if comments should be written
     */
    public static void write( OutputStream out, Graph graph,
            boolean writeComments ) {
        // verify the inputs are not null
        if ( out != null && graph != null ) {
            new CGIFWriter( out, "    ", writeComments ).write( graph );
        }
    }

    /**
     * Writes the specified graph to a file with the specified filename using
     * the standard CGIF format without comments.
     *
     * @param filename Name of the file to which to write the graph
     * @param graph Graph to convert and write out
     * @throws FileNotFoundException, IOException
     */
    public static void writeToFile( String filename, Graph graph )
            throws FileNotFoundException, IOException {
        writeToFile( filename, graph, false );
    }

    /**
     * Writes the specified graph to a file with the specified filename using
     * the standard CGIF format.
     *
     * @param filename Name of the file to which to write the graph
     * @param graph Graph to convert and write out
     * @param writeComments Flag specifying if comments should be written
     * @throws FileNotFoundException, IOException
     */
    public static void writeToFile( String filename, Graph graph,
            boolean writeComments )
            throws FileNotFoundException, IOException {
        if ( filename != null ) {
            // create a new output stream to the specified file and write
            // the specified graph to it.  there is not need to use the more
            // efficient BufferedOutputStream b/c it is used internally.
            FileOutputStream out = new FileOutputStream( filename );
            write( out, graph, writeComments );
            out.close();
        }
    }

    /**
     * Converts the specified graph into a string representation of the standard
     * CGIF format without comments.
     *
     * @param graph Graph to convert to a string
     * @return Returns a string representation of the graph in CGIF format
     */
    public static String graphToString( Graph graph ) {
        return graphToString( graph, false );
    }

    /**
     * Converts the specified graph into a string representation of the standard
     * CGIF format.
     *
     * @param graph Graph to convert to a string
     * @param writeComments Flag specifying if comments should be written
     * @return Returns a string representation of the graph in CGIF format
     */
    public static String graphToString( Graph graph, boolean writeComments ) {
        // create a memory output stream, write the contents of the graph to
        // it, and then convert it to a string
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write( out, graph, writeComments );
        return out.toString();
    }

    /**
     * Returns the string representing the indention applied to the beginning of
     * each new subcontext.
     *
     * @return Returns the subcontext indention
     */
    public String getIndention() {
        return indent;
    }

    /**
     * Sets the string to be applied to the beginning of each new subcontext.
     *
     * @param indent Indention string to be applied to each subcontext
     */
    public void setIndention( String indent ) {
        this.indent = indent;
    }

    /**
     * Writes the specified graph to the output stream of this class using the
     * standard CGIF format.
     *
     * @param graph Graph to convert and write out
     */
    public void write( Graph graph ) {
        // verify the graph isn't null and that a stream has been created
        if ( graph != null && stream != null ) {
            nameGenerator.addNames( graph );
            write( graph, "" );
            stream.flush();
        }
    }

    /**
     * Writes the specified graph to the output stream of this class using the
     * standard CGIF format. The core CGIF format is given preference over the
     * extended CGIF format where possible.
     *
     * @param graph Graph to convert and write out
     * @param currIndent Indention for each line for the current context
     */
    protected void write( Graph graph, String currIndent ) {
        // put all the graph objects into their own lists based on type to
        // preclude multiple traversals of the entire list of objects when
        // searching for particular types of objects.  put concepts and
        // graphs (subcontexts) in the same list, but sort them by type by
        // putting the concepts at the beginning and the graphs at the end.
        List<Concept> concepts = new LinkedList<Concept>();
        List<Relation> relations = new LinkedList<Relation>();
        List<Actor> actors = new LinkedList<Actor>();
            // only needed to keep track of which types have already been emitted
        // since we only care about type labels when they're involved in gen spec links.
        List<GenSpecLink> genspeclinks = new LinkedList<GenSpecLink>();

        Iterator itr = graph.graphObjects();
        while ( itr.hasNext() ) {
            Object obj = itr.next();
            if ( obj instanceof Graph ) {
                concepts.add( (Concept)obj );
            } else if ( obj instanceof Concept ) {
                concepts.add( 0, (Concept)obj );
            } else if ( obj instanceof Relation ) {
                relations.add( (Relation)obj );
            } else if ( obj instanceof Actor ) {
                actors.add( (Actor)obj );
            } else if ( obj instanceof GenSpecLink ) {
                genspeclinks.add( (GenSpecLink)obj );
            }
        }

        // for each genspeclink, emit its types (unless already emitted)
        // and then generate the genspec link
        if ( Global.exportSubtypesAsRelations ) {
            writeGenSpecLinks( genspeclinks, currIndent );
        }
        
        // find all the concepts that have explicitly defined labels such
        // as [Number: *x] and register all the concepts that are linked to
        // them so we are guaranteed to have the same coreference names
        for ( Concept concept : concepts ) {
            String referent = concept.getReferent();
            // There are several cases to consider:
            // A simple * referent : needs to be given new name
            // An identifier such as *x1 : needs to be ensured unique (not already used)
            // A set referent 
            int index = referent.indexOf( '*' );
            if ( index >= 0 && index < referent.length() - 1 ) {    // we have a * referent non-null
                String corefName;
                if ( referent.contains( "{")) {   // we have a set referent
                    String trimmed = referent.trim().replaceAll( " ", "");
//                    int closingBracket = referent.indexOf( "}");
                    if ( trimmed.startsWith(  "{*}")) {
                                // take care of case where referent looks like {*}@3
                        corefName = "{*" + nameGenerator.generateName() + "}" + trimmed.substring( trimmed.indexOf( "}") + 1);
                        
                    } else {
                        corefName = referent;
                    }
                } else {
                     corefName = referent.substring( index + 1 ).trim();
                }
                if ( corefName.length() > 0 ) {
                    addCorefLinks( concept, corefName );
                }
            } else if ( referent.contains("*")) {
                if ( referent.startsWith( "{")) {
                    //  we have a set referent
                    String s = referent;
                }
            }
        }

        // write the concepts and graphs to the output stream
        writeConcepts( concepts, currIndent );

        // write the relations to the output stream
        writeLinkingNodes( relations, currIndent );

        // write the actors to the output stream
        writeLinkingNodes( actors, currIndent );
    }
    
    /**
     * Write the generalization-specialization links, with all the types on one line
     * and all the subtype relations on a second line, in accordance with Dr. Simon Polovina's requirements.
     * @param genspeclinks
     * @param currIndent 
     */
    protected void writeGenSpecLinks( List<GenSpecLink> genspeclinks, String currIndent ) {
        List<String> typelabels = new LinkedList<String>();
        StringBuilder typeLabelsCGIF = new StringBuilder( currIndent );
        StringBuilder subtypeRelationsCGIF = new StringBuilder( currIndent );
        for ( GenSpecLink genspeclink : genspeclinks ) {
            TypeLabel typelabelFrom = (TypeLabel)genspeclink.fromObj;
            if ( ! typelabels.contains( typelabelFrom.getTextLabel() ) ) {
                typeLabelsCGIF.append(  
                "[" + (writeComments ? generateComment( typelabelFrom) : ""
                        + "Type: " + typelabelFrom.getTypeLabel() + "] "));
//                stream.print( Global.LineSeparator + currIndent );
//                stream.print(  "[" + (writeComments ? generateComment( typelabelFrom) : "")
//                        + "Type: " + typelabelFrom.getTypeLabel() + "]");
                typelabels.add(  typelabelFrom.getTextLabel() );
            }
            TypeLabel typelabelTo = (TypeLabel)genspeclink.toObj;
            if ( ! typelabels.contains( typelabelTo.getTextLabel() ) ) {
                typeLabelsCGIF.append(  
                "[" + (writeComments ? generateComment( typelabelTo) : ""
                        + "Type: " + typelabelTo.getTypeLabel() + "] "));
//                stream.print( Global.LineSeparator + currIndent );
//                stream.print(  "[" + (writeComments ? generateComment( typelabelTo) : "")
//                        + "Type: " + typelabelTo.getTypeLabel() + "]");
                typelabels.add(  typelabelTo.getTextLabel() );

            }
            subtypeRelationsCGIF.append( 
                    "(subtype " + typelabelTo.getTextLabel() + " " + typelabelFrom.getTextLabel() + ") ");
//            stream.print( Global.LineSeparator + currIndent );
//            stream.print( "(subtype " + typelabelTo.getTextLabel() + " " + typelabelFrom.getTextLabel() + ")");
            stream.print( typeLabelsCGIF );
            stream.print( Global.LineSeparator );
            stream.print( subtypeRelationsCGIF );
            stream.print( Global.LineSeparator );
        }
    }

    /**
     * Writes the specified concepts to the output stream of this class using
     * the standard CGIF format. The core CGIF format is given preference over
     * the extended CGIF format where possible.
     *
     * @param concepts List of concepts to print
     * @param currIndent Indention for each line for the current context
     */
    protected void writeConcepts( List<Concept> concepts, String currIndent ) {
        // return if there are no concepts so extra spaces won't be printed 
        if ( concepts.isEmpty() ) {
            return;
        }

        // print the beginning indention
        stream.print( currIndent );

        // print all the concepts and subcontexts
        boolean first = true;
        for ( Concept concept : concepts ) {
            // place graphs (subcontexts) on a new line from the concepts.
            // also, if comments are printed, write concepts on new lines.
            if ( !first && ( writeComments || concept instanceof Graph ) ) {
                stream.print( Global.LineSeparator + currIndent );
            }
            first = false;

            // print the opening of the concept/graph
            stream.print( concept.isNegated() ? "~[" : "[" );
            
            if ( writeComments ) {
                stream.print( ' ' + generateComment( concept ) + ' ' );
            }



            // don't print the ':' at the beginning if the type label is empty
            String typeLabel = concept.getTypeLabel().trim();
            if ( typeLabel.length() > 0 ) {
//                 stream.print( "\"" + typeLabel + "\"" + ": " );
//               stream.print( typeLabel + ": " );
               stream.print( quotify( typeLabel ) + ": " );
            }

                    // TODO: Sometimes we want the original referent, but other times
                    // we want the generated name label (e.g., *x5). The nametable
                    // doesn't seem to include "regular" referents.
            String referent = concept.getReferent();

            // if the concept doesn't have a referent, create a defining label.
            // get the bound label if it is already bound to something else.
            // also check for the special cases of [Concept: *] or [Concept: {*}].  if the user
            // had one of these as the name, create a bound label.
            if ( nameGenerator.isGenericName( referent)) {
//            if ( referent.equals( "" ) || referent.trim().equals( "*") ) {
                // if a defining label hasn't been created yet for these
                // concepts linked together, create one and write it out
                String name = nameTable.get( concept.objectID );
                if ( name == null ) {
                    referent = nameGenerator.generateName();
                    if ( referent.trim().startsWith( "{")) {
                        referent = "{*" + referent + "}";
                        addCorefLinks( concept, referent );
                    } else {
                        addCorefLinks( concept, referent );
                        referent = '*' + referent;
                    }
                } else {
                    if ( name.startsWith( "{"))
                        referent = name;
                    else
                        referent = '?' + name;
                }
            } else {
                referent = quotifyWithPrefix( referent );
            }

            stream.print( referent );

            if ( concept instanceof Graph ) {
                stream.print( Global.LineSeparator );
                write( (Graph)concept, currIndent + indent );
            }


            stream.print( "] " );
        }
    }

    /**
     * Writes the specified nodes that link concepts (Actor or Relation) to the
     * output stream of this class using the standard CGIF format. The core CGIF
     * format is given preference over the extended CGIF format where possible.
     *
     * @param nodes List of Actors or Relations
     * @param currIndent Indention for each line for the current context
     */
    protected <T> void writeLinkingNodes( List<? extends GNode> nodes,
            String currIndent ) {
        // return if there are no nodes so extra spaces won't be printed 
        if ( nodes.isEmpty() ) {
            return;
        }

        // print the beginning indention
        stream.print( Global.LineSeparator + currIndent );

        // print each relation/actor
        boolean first = true;
        for ( GNode node : nodes ) {
            // if comments are printed, write linking nodes on new lines.
            if ( !first && writeComments ) {
                stream.print( Global.LineSeparator + currIndent );
            }
            first = false;

            // split the edges into input and output edges
            List<GEdge> inputEdges = new LinkedList<GEdge>();
            List<GEdge> outputEdges = new LinkedList<GEdge>();
            for ( Object obj : node.getEdges() ) {
                if ( obj instanceof GEdge ) {
                    GEdge edge = (GEdge)obj;
                    if ( edge.toObj == node ) {
                        inputEdges.add( edge );
                    } else {
                        outputEdges.add( edge );
                    }
                }
            }

            // sort the input edges
            sortInputEdges( inputEdges );

            if ( node instanceof Actor ) {
                stream.print( '<' );
            } else {
                stream.print( '(' );
            }

            if ( writeComments ) {
                stream.print( ' ' + generateComment( node ) + ' ' );
            }

            stream.print( node.getTypeLabel() );

            // print the input edges
            for ( GEdge edge : inputEdges ) {
                // get a reference to the input/output object
                GraphObject go = ( edge.toObj == node )
                        ? edge.fromObj : edge.toObj;
                if ( go != null ) {
                    // the linked object should have either a constant label
                    // or a coreference label
                    String label = nameTable.get( go.objectID );
                    if ( label != null ) {
//                        if ( ! label.startsWith( "{"))  // Do not add ? for set referents
                            label = '?' + label;
                    } else if ( go instanceof Concept ) {
                        label = ( (Concept)go ).getReferent();
                    }

//                    stream.print( ' ' + label );
                    stream.print( ' ' + quotifyWithPrefix( label ));
                }
            }

            // if this is an actor, print a separator between inputs/outputs
            if ( node instanceof Actor ) {
                stream.print( " |" );
            }

            // print the output edges
            for ( GEdge edge : outputEdges ) {
                // get a reference to the input/output object
                GraphObject go = ( edge.toObj == node )
                        ? edge.fromObj : edge.toObj;
                if ( go != null ) {
                    // the linked object should have either a registered label
                    // name or a constant label
                    String label = nameTable.get( go.objectID );
                    if ( label != null ) {
                        label = '?' + label;
                    } else if ( go instanceof Concept ) {
                        label = ( (Concept)go ).getReferent();
                    }

//                    stream.print( ' ' + label );
                    stream.print( ' ' + quotifyWithPrefix( label ));
                }
            }

            if ( node instanceof Actor ) {
                stream.print( '>' );
            } else {
                stream.print( ')' );
            }
        }
    }

    /**
     * Adds coreference name entries for the specified concept and all the
     * linked concept to the corefNameTable.
     *
     * @param concept Concept to add
     * @param corefName Name of the coreference link without any special CGIF
     * characters
     */
    protected void addCorefLinks( Concept concept, String corefName ) {
        // add the entry for this concept
        nameTable.put( concept.objectID, corefName );

        // add entries for all the other concepts linked to this concept.
        // the concepts are traversed in a breadth first manner by visiting
        // a node and adding its edges to the end of the 'queue' and
        // processing those edges later.
        ArrayList edges = new ArrayList( concept.getEdges() );
        for ( int i = 0; i < edges.size(); i++ ) {
            Object obj = edges.get( i );
            if ( obj instanceof Coref ) {
                Coref coref = (Coref)obj;

                // add the entries of the starting Concept if not added
                if ( coref.fromObj instanceof Concept ) {
                    GraphObjectID fromObjID = coref.fromObj.objectID;
                    if ( !nameTable.containsKey( fromObjID ) ) {
                        nameTable.put( fromObjID, corefName );
                        edges.addAll( ( (Concept)coref.fromObj ).getEdges() );
                    }
                }

                // add the entries of the ending Concept if not added
                if ( coref.toObj instanceof Concept ) {
                    GraphObjectID toObjID = coref.toObj.objectID;
                    if ( !nameTable.containsKey( toObjID ) ) {
                        nameTable.put( toObjID, corefName );
                        edges.addAll( ( (Concept)coref.toObj ).getEdges() );
                    }
                }
            }
        }
    }

    /**
     * This sorts the specified input edges. It first attempts to sort them by
     * assuming they all have numeric labels. If any don't have numeric labels,
     * then it sorts them by object ID's.
     *
     * @param edges Edges to be sorted
     */
    protected void sortInputEdges( List<GEdge> edges ) {
        try {
            Collections.sort( edges, new NumericLabelComparator() );
        } catch ( NumberFormatException e ) {

            Collections.sort( edges, new ObjectIDComparator() );
        }
    }

    /**
     * Returns a string representation of the comment for the specified object.
     * The comment is the created by converting the graph object to XML and
     * removing unncessary spacing and lines.
     *
     * @param go GraphObject of which to generate a comment
     * @return Returns the XML string for the object in CGIF comment format
     */
    protected String generateComment( GraphObject go ) {
        // generate the XML representation of the graph object 
//        String xml = CGXGenerator.GraphObjectXML( go, "" );
        String xml = CGXGenerator.layoutInfoXML( go, "" );

        // remove newline characters
        xml = xml.replaceAll( Global.LineSeparator, "" );

        // remove the indention at the beginning of the lines
        xml = xml.replaceAll( ">\\s*<", "> <" );

//        return "/* " + xml + " */";
        return " /*" + Global.CharGerCGIFCommentStart + " " + xml + "*/ ";
    }
    
//    protected boolean doesStringContainQuotableCharacters( String s ) {
//        if ( )
//    }
    /**
     * Delimit a string with double quotes, and escape (via substituting <code>&#92;u0022</code> for double quote) any double quotes in the string.
     * See <a href="https://www.fileformat.info/info/unicode/char/0022/index.htm">https://www.fileformat.info/info/unicode/char/0022/index.htm</a>
     * for a description of this character.
     * @param s
     * @return a quoted string with double quotes replaced by
     * <code>&#92;u0022</code>. All other characters are preserved.
     */
    protected static String quotify( String s ) {
        
        if ( needsQuote( s ) ) {
            return "\"" + s.replaceAll( Pattern.quote( "\"" ), "\\\\\\\"" ) + "\"";
        } else {
            return s;
        }
    }

    protected static String quotifyWithPrefix( String s ) {
        int startchar = 0;
        if ( s.startsWith( "*" ) || s.startsWith( "?" ) ) {
            startchar = 1;
        }
        String quotified = s.substring( 0, startchar ) + quotify( s.substring( startchar ) );
        return quotified;
    }
    

    /** Examines a string and if it contains all "normal" characters, don't quote it.
     * @param s
     * @return true if anything other than alphanumeric characters are present.
     */
    protected static boolean needsQuote( String s )  {
        return ! s.matches( "[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_]*");
    }
}
