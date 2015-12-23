package com.cyc.core.examples.basics;

import com.cyc.kb.BinaryPredicate;
import com.cyc.kb.BinaryPredicateFactory;
import com.cyc.kb.Context;
import com.cyc.kb.ContextFactory;
import com.cyc.kb.Fact;
import com.cyc.kb.FactFactory;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbCollectionFactory;
import com.cyc.kb.KbFactory;
import static com.cyc.kb.KbFactory.getSentence;
import static com.cyc.kb.KbFactory.getVariable;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbIndividualFactory;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.KbTerm;
import com.cyc.kb.KbTermFactory;
import com.cyc.kb.Sentence;
import com.cyc.kb.SentenceFactory;
import com.cyc.kb.Variable;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryFactory;
import static com.cyc.query.QueryFactory.getQuery;
import com.cyc.query.QueryListener;
import com.cyc.query.QueryResultSet;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.query.exception.QueryException;
import com.cyc.session.CycServerInfo;
import com.cyc.session.CycSession;
import com.cyc.session.CycSessionManager;
import com.cyc.session.SessionOptions;
import com.cyc.session.exception.SessionCommandException;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.spi.SessionManager;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/*
 * #%L
 * File: BasicWalkthrough.java
 * Project: Core API Use Cases
 * %%
 * Copyright (C) 1995 - 2014 Cycorp, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


/**
 * This class provides a simple example of working with the Cyc KB through the Cyc Core API. It 
 * includes new term creation, term retrieval, asserting and deleting facts about terms, and 
 * querying the Cyc KB. 
 * 
 * <p>The purpose of this class is to illustrate the basics of the Cyc Core API, so it is structured
 * to be read in linear order, from top to bottom, with methods that build upon the examples in
 * previous methods. Thus, while this file demonstrates best practices for Core API usage, it does 
 * not represent the ideal structure for an actual application class.
 * 
 * <p>All of the example code in this class should work with all Cyc server releases supported by 
 * the Cyc Core API.
 * 
 * @note There are multiple ways of interacting with Cyc (through and independent of the Core API).
 * The illustrations here are some of the more popular techniques.
 */
public class BasicWalkthrough {

  // Fields
  
  private Context peopleDataMt;
  private Context massMediaDataMt;
  private KbCollection actorInMovies;
  private KbCollection restrictedRating;
  private KbIndividual nicholson;
  private KbIndividual kingOfMarvinGardens;
  private BinaryPredicate movieActors;
  private BinaryPredicate movieActorsWithStarringRole;
  private BinaryPredicate movieAdvisoryRating;
  
  
  // Main
  
  public static void main(String[] args) {
    /*
     Wrapping the current SessionManager instance in a try-with-resources statement (Java 7 and 
     later) in the main method ensures that ALL CycSessions created by the SessionManager are 
     closed, along with all of their Cyc server connections, before the program ends:
     */
    try (SessionManager sessionMgr = CycSessionManager.getInstance()) {
      
      System.out.println("Running walkthrough...");
      BasicWalkthrough usage = new BasicWalkthrough();
      usage.runExample();
      
      System.out.println();
    } catch (KbException | QueryException | SessionException | RuntimeException kbe) {
      kbe.printStackTrace(System.err);
      System.exit(1);
    } catch (IOException ioe) {
      System.err.println("Error closing SessionManager");
      ioe.printStackTrace(System.err);
      System.exit(1);
    } finally {
      /*
       If we were running under Java 6, we could manually close the current SessionManager here 
       within the finally block, like so:
      
       CycSessionManager.getInstance().close();
       */
      
      System.out.println("... Walkthrough concluded.");
      System.exit(0);
    }
  }
  
  
  // Example code
  
  /**
   * Calls the actual example methods in linear order.
   * 
   * @throws SessionConfigurationException
   * @throws SessionCommunicationException
   * @throws SessionInitializationException
   * @throws SessionCommandException
   * @throws KbTypeException
   * @throws CreateException
   * @throws DeleteException
   * @throws QueryConstructionException
   * @throws KbException 
   */
  public void runExample() 
          throws SessionConfigurationException, SessionCommunicationException, 
          SessionInitializationException, SessionCommandException, KbTypeException, CreateException,
          DeleteException, QueryConstructionException, KbException {
    /* 
     Note that we call all of the example methods within a try-with-resources block, to ensure that
     the CycSession for the current thread is closed before the thread is terminated. This may seem
     a little redundant, as this is a single-threaded application and we're already closing the 
     SessionManager in the main() method (thereby closing ALL CycSessions). Although this isn't 
     strictly necessary for simple applications like this one, it is good practice for multi-
     threaded applications (e.g., servlets), so we're demonstrating it here.
    
     Typically, we don't need to directly request the current CycSession; if one does not exist, it 
     will be automatically created the first time that the Cyc APIs require it. However, wrapping it
     in a try-with-resources statement (Java 7 and later) ensures that the session will be cleaned 
     up at the end of the current execution thread.
     */
    try (CycSession session = CycSessionManager.getCurrentSession()) {
      
      configureCurrentSession();
      
      setupContexts();
      
      basicTermLookup();
      
      setupMarvinGardens();
      
      setupActorBackgroundKnowledge();
      
      setupJackNicholson();
      
      createQueryViaSentence();
      
      createQueryViaString();
      
      runQueryAsynchronously();
      
    } finally {
      /*
       If we were running under Java 6, we could have manually closed the current CycSession here 
       within the finally block, like so:
      
       CycSessionManager.getCurrentSession().close();
       */
    }
  }
  
  /**
   * Configure the current CycSession. There is no need to programmatically specify a Cyc server; 
   * when one is required, the CycSessionManager will check the System properties to determine how
   * to get access to Cyc. By default, in a graphical environment, it will fall back on
   * interactively prompting the user for connection information.
   * 
   * @throws SessionConfigurationException
   * @throws SessionCommunicationException
   * @throws SessionInitializationException
   * @throws SessionCommandException
   * @throws KbTypeException
   * @throws CreateException 
   */
  protected void configureCurrentSession() 
          throws SessionConfigurationException, SessionCommunicationException, 
          SessionInitializationException, SessionCommandException, KbTypeException, 
          CreateException {
    System.out.println();
    
    /* 
     Retrieve the current CycSession, creating one if necessary. In this case a CycSession should
     already exist, because we already called CycSessionManager#getCurrentSession() in the
     runExample() method. Calling it again here should return the same instance of CycSession.
     */
    CycSession session = CycSessionManager.getCurrentSession();
    
    /*
     Let's get some information about the Cyc server being used for this session:
     */
    CycServerInfo serverInfo = session.getServerInfo();
    System.out.println("Current Cyc server: " + serverInfo.getCycServer());
    System.out.println("Cyc server release type: " + serverInfo.getSystemReleaseType());
    System.out.println("Cyc server revision number:" + serverInfo.getCycRevisionString());
    
    /*
     Now let's set some options for the current session. Here we are defining them programmatically,
     but future versions of the Session API will allow them to be defined by configuration
     properties...
     */
    SessionOptions options = session.getOptions();
    
    /* 
     Transcript the Cyc operations issued by the API. These operations can be saved into an ASCII 
     text file and reloaded on a clean image to recreate a given state of the KB with such 
     operations.
     */
    options.setShouldTranscriptOperations(true);
    
    /* 
     Set "CycAdministrator" as the current Cyclist. Setting the cyclist is required for bookkeeping 
     information to be added for assertions and constants.
     */
    options.setCyclistName("CycAdministrator");
  }
  
  /**
   * Choose the Contexts we'll be using. "Contexts" are the objects which the KB API uses to 
   * represent CycL microtheories. We'll be referring to them often, so we'll assign them to 
   * instance fields.
   * 
   * <p>It's very important to ensure that the knowledge used by your application goes into the
   * appropriate microtheory; this can have an enormous impact upon the correctness and efficiency 
   * of inference.
   * 
   * @throws KbTypeException
   * @throws CreateException
   * @throws SessionConfigurationException
   * @throws SessionCommunicationException
   * @throws SessionInitializationException
   */
  protected void setupContexts() 
          throws KbTypeException, CreateException, SessionConfigurationException, 
          SessionCommunicationException, SessionInitializationException {
    /* 
     KbObjects are requested from factories, and there is KB factory for each KbObject type.
     Here, we retrieve Context objects.
    */
    peopleDataMt = ContextFactory.get("PeopleDataMt");
    massMediaDataMt = ContextFactory.get("MassMediaDataMt");
    
    /* 
     Instead of manually specifying a Context for every query or assertion, many methods will check
     for default assertion and query Contexts if no Context argument is directly provided.
     */
    CycSessionManager.getCurrentSession().getOptions().setDefaultContext(
            ContextFactory.getDefaultContext(massMediaDataMt, ContextFactory.INFERENCE_PSC));
  }
  
  /**
   * Perform some simple term lookup and creation. Most of the methods in the KB API and the Query 
   * API will accept both Strings and KB objects for their arguments.  But it's good practice to
   * create the KB object versions, and use them.  In this file, we'll alternate back and forth 
   * between using KB object and using Strings for these arguments.  Here's how you retrieve the KB
   * objects.
   * 
   * @throws KbTypeException
   * @throws CreateException 
   * @throws DeleteException 
   */
  protected void basicTermLookup()
          throws KbTypeException, CreateException, DeleteException {
    System.out.println();
    
    /* 
     KbObjects are requested from factories, and there is KB factory for each KbObject type.
     Here, we find or create an Individual term in the KB to represent the actor Jack Nicholson.
    */
    KbIndividual nicholsonIndividual = KbIndividualFactory.findOrCreate("JackNicholson");
    System.out.println("Jack Nicholson as an individual: "
            + nicholsonIndividual);
    
    /* 
     And here, we request JackNicholson as a KbTerm, which is a super-type of KbIndividual. Note 
     that we're now using the get method, which should only be used if you're sure the term is in 
     the KB. This code shows how to test whether a term is in the KB (in a form that can be cast to 
     the desired type) before actually retrieving it. In most real-world cases, you won't run this
     test before every call to get. 
     */
    KbTerm nicholsonTerm = null;
    if (KbTermFactory.existsAsType("JackNicholson")) {
      nicholsonTerm = KbTermFactory.get("JackNicholson");
    }
    System.out.println("Jack Nicholson as a term: " + nicholsonTerm);
    
    /*
     We can also confirm that Jack Nicholson isn't, say, a KbCollection.
     */
    System.out.println("Is Jack Nicholson a collection? "
            + KbCollectionFactory.existsAsType("JackNicholson"));
    
    /*
     The KbObject factories are intelligent enough to return an instance of the most specific type.
     Because JackNicholson is an Individual, and because KbIndividual is a subclass of KbTerm,
     either factory will return an instance of KbIndividual. And, because of caching, they will 
     return the *same* instance of KbIndividual:
     */
    System.out.println("Are the KbTerm and KbIndividual for #$JackNicholson the exact same object? "
            + (nicholsonIndividual == nicholsonTerm));
    
    /*
     Now, let's check whether the movie _King of Marvin Gardens_ exists in Cyc's KB. Here is some
     slightly different syntax for checking whether it can be expressed as a KbIndividual:
    */
    System.out.println("Does Cyc already know that _King of Marvin Gardens_ is an individual? "
            + KbIndividualFactory.existsAsType("TheKingOfMarvinGardens-TheMovie"));
    
    /*
     And here's a way to check whether the term exists at all:
     */
    System.out.println("Does _King of Marvin Gardens_ exist in Cyc's KB at all? "
            + KbFactory.existsInKb("TheKingOfMarvinGardens-TheMovie"));
    
    /*
     For sake of the example, let's ensure that _King of Marvin Gardens_ does NOT exist in Cyc's KB.
     Of course, it needs to exist in order for us to delete it, and we could check 
     KbFactory#existsInKb() (like we just did above) to determine whether it exists. But for sake of
     the example let's assume that it's in the KB, and add some code for handling the case where it 
     isn't:
     */
    try {
      KbTermFactory.get("TheKingOfMarvinGardens-TheMovie").delete();
      System.out.println("TheKingOfMarvinGardens-TheMovie: deleted!");
    } catch (KbObjectNotFoundException e) {
      System.out.println("Apparently TheKingOfMarvinGardens-TheMovie wasn't in the KB,"
              + " but we'll just carry on: " + " (" + e.getMessage() + ")");
    }
  }
  
  /**
   * Ensure that _King Of Marvin Gardens_ exists in Cyc's KB, and has enough supporting knowledge to
   * be useful. This is known as "term elaboration".
   * 
   * @throws CreateException
   * @throws KbTypeException 
   */
  protected void setupMarvinGardens()
          throws CreateException, KbTypeException {
    System.out.println();
    
    /* 
     Find or create an individual term in the KB to represent the movie  _King of Marvin Gardens_, 
     and simultaneously make it an instance of the DramaticMovies collection in the MassMediaDataMt 
     Context. Note that we could also have passed the String DramaticMovie and the context 
     MassMediaDataMt as parameters to the findOrCreate method. Also note that we assumed the
     collection DramaticMovie exists in the KB. If DramaticMovie were not in the KB, this code would
     throw an exception, and we would have to create it.
    */
    KbCollection dramaticMovie = KbCollectionFactory.get("DramaticMovie");
    kingOfMarvinGardens = KbIndividualFactory
            .findOrCreate("TheKingOfMarvinGardens-TheMovie", dramaticMovie, massMediaDataMt);
    System.out.println("We've created _King of Marvin Gardens_: " + kingOfMarvinGardens);
    
    /* 
     Now, let's add the fact that the movie "The King of Marvin Gardens" has a restricted rating. 
    */
    restrictedRating = KbCollectionFactory.get("RestrictedRating");
    movieAdvisoryRating = BinaryPredicateFactory.get("movieAdvisoryRating");
    Fact kingOfMarvinGardensRestrictedRating = FactFactory.findOrCreate(
            getSentence(movieAdvisoryRating, kingOfMarvinGardens, restrictedRating),
            massMediaDataMt);
    System.out.println("_King of Marvin Gardens_ is R-rated: " 
            + kingOfMarvinGardensRestrictedRating);
  }
  
  /**
   * Add general background knowledge about actors. Later, this will be leveraged to associated Jack
   * Nicholson with the movie _King of Marvin Gardens_.
   * 
   * @throws CreateException
   * @throws KbTypeException 
   */
  protected void setupActorBackgroundKnowledge()
          throws CreateException, KbTypeException {
    System.out.println();
    
    /*
     Since we will be talking about Jack Nicholson, we have to make sure Cyc knows about actors. 
     Most Cyc releases will already have this term, but just in case, here's how you go about
     creating it.  Don't worry if it's already in your Cyc KB.  These operations are idempotent, so
     running them in a Cyc that already has the terms won't do anything bad.  When in doubt, the
     following idiom is most useful. 
     */
    actorInMovies = KbCollectionFactory.findOrCreate("ActorInMovies");

    /*
     Let's also convey the fact that all movie actors are people. (Our apologies to the highly 
     trained animals who have sometimes carried movies on their shoulders). We assume the collection
     Person already exists and we only have to specify the name to retrieve it.
     */
    actorInMovies.addGeneralization("Person");
    
    /* 
     We need a way to relate actors to the movies they are in. Let's call the relation
     "movieActors". This particular predicate already exists in Cyc, but if it didn't, this is how
     you would create it. 
     */
    movieActors = BinaryPredicateFactory.findOrCreate("movieActors");
    System.out.println("Binary predicate associating actors with movies: " + movieActors);
    
    /* 
     It is always a good idea to put constraints on the arguments of predicates for semantic 
     validity. The following establishes in the KB (in the MassMediaDataMt context) that the first
     argument of movieActors must be an instance of the collection Movie-CW and the second argument
     must be an instance of the collection ActorInMovies. (Note that #$DramaticMovie is a spec of
     #$Movie-CW, so #$TheKingOfMarvinGardens-TheMovie will be a valid argument for arg1.)
     */
    System.out.println("Asserting constraints on #$movieActors,"
            + " these might take a little while to propagate...");
    movieActors.addArgIsa(1, "Movie-CW", "MassMediaDataMt");
    movieActors.addArgIsa(2, "ActorInMovies", "MassMediaDataMt");
    
    /* 
     Of course, some movie actors have a starring role. To add this more specific knowledge to the 
     KB, we need a more specific predicate; let's call it "movieActors-WithStarringRole".  Again, 
     this predicate already exists in most Cyc KBs, so the following will typically retrieve, rather
     than create, the predicate in most Cyc KBs. 
     */
    System.out.println("Asserting constraints on #$movieActors-WithStarringRole,"
            + " these might take a little while to propagate...");
    movieActorsWithStarringRole = BinaryPredicateFactory.findOrCreate("movieActors-WithStarringRole");
    movieActorsWithStarringRole.addArgIsa(1, "Movie-CW", "MassMediaDataMt");
    movieActorsWithStarringRole.addArgIsa(2, "ActorInMovies", "MassMediaDataMt");
    
    /*
     Finally, let's establish that movieActors is a generalization of 
     movieActorsWithStarringRole 
     */
    movieActorsWithStarringRole.addGeneralization("movieActors", "UniversalVocabularyMt");
    
    /*
     Now, let's view all of the specializations on #$movieActors.
    */
    System.out.println("Specializations of #$movieActors:");
    for (KbPredicate spec : movieActors.getSpecializations()) {
      System.out.println("  - " + spec);
    }
  }
  
  /**
   * Elaborate on Jack Nicholson's role as an actor, and his relation to the movie
   * _King Of Marvin Gardens_...
   * 
   * @throws CreateException
   * @throws KbTypeException
   * @throws DeleteException
   * @throws SessionConfigurationException
   * @throws SessionCommunicationException
   * @throws SessionInitializationException 
   */
  protected void setupJackNicholson() 
          throws CreateException, KbTypeException, DeleteException, SessionConfigurationException, 
          SessionCommunicationException, SessionInitializationException {
    System.out.println();
    
    /*
     We ensured earlier that #$JackNicholson was in the KB. We'll be referring to him a few times 
     more, though, so let's assign him to an instance field.
    */
    nicholson = KbIndividualFactory.get("JackNicholson");
    
    /*
     Let's convey the fact Jack Nicholson is an instance of the ActorInMovies collection in the 
     PeopleDataMt Context.
    */
    nicholson.instantiates(actorInMovies, peopleDataMt);
    
    /* 
     We can easily see what JackNicholson is known to be an instance of from the PeopleDataMt context. 
     */
    Collection<KbCollection> nicholsonTypes = nicholson.instanceOf("PeopleDataMt");
    System.out.println("Things that #$JackNicholson is known to be:");
    for (KbCollection nicholsonType : nicholsonTypes) {
      System.out.println("  - " + nicholsonType);
    }
    
    /* 
     Since JackNicholson is an instance of ActorInMovies (which is a specialization of Person) we 
     should be able to prove that JackNicholson is a person without ever having asserted it.
     */
    assert (nicholsonTypes.contains(KbCollectionFactory.get("Person"))) :
            "Jack Nicholson may be an ActorInMovies but he is not a Person";
    
    /*
     Similarly, Cyc knows that he is a human.
    */
    System.out.println("Does Cyc know that #$JackNicholson is a human? "
            + nicholson.isInstanceOf("HomoSapiens"));
    
    /* 
     Now, assert the fact, in the MassMediaMt context, that JackNicholson was one of the actors in 
     KingOfMarvinGardens.
     */
    Fact nicholsonActedInMarvinGardens = FactFactory.findOrCreate(
            getSentence(movieActors, kingOfMarvinGardens, nicholson), massMediaDataMt);
    
    /* 
     But wait! Jack Nicholson actually had a starring role in TheKingOfMarvinGardens-TheMovie, so
     let's add that more specific assertion. This method shows how to do it with a String containing
     a CycL sentence. 
     */
    Fact nicholsonStarredInMarvinGardens = FactFactory.findOrCreate(
            "(movieActors-WithStarringRole TheKingOfMarvinGardens-TheMovie JackNicholson)",
            "MassMediaDataMt");
    System.out.println("Jack Nicholson starred in _King of Marvin Gardens_: " 
            + nicholsonStarredInMarvinGardens);
    
    /*
     Now that we know he was a star, it's redundant to say that he acted in the 
     movie, so we can (but are not required to) delete the first fact, and we'll 
     still know that he acted in the movie.
     */
    nicholsonActedInMarvinGardens.delete();
  }
  
  /**
   * Construct a query from a Sentence of KbObjects (which could include other Sentences) and run 
   * it. 
   * 
   * <p>Here we will retrieve the results as a List of QueryAnswers, with each QueryAnswer 
   * containing a map of query variables and their bindings.
   * 
   * @throws KbTypeException
   * @throws CreateException
   * @throws QueryConstructionException
   * @throws SessionCommunicationException
   * @throws KbException 
   */
  public void createQueryViaSentence()
          throws KbTypeException, CreateException, QueryConstructionException, 
          SessionCommunicationException, KbException {
    System.out.println();
    
    Variable movieVar = KbFactory.getVariable("?MOVIE");
    Sentence querySentence = SentenceFactory.and(
            getSentence(movieActors, movieVar, nicholson),
            getSentence(movieAdvisoryRating, movieVar, restrictedRating));
    System.out.println("Query sentence: " + querySentence);
    
     /* 
     Now we can construct a query for this sentence. We will use InferencePSC, the all-encompassing
     context, as the context in which to execute the query. Note here we retrieve a handle to 
     InferencePSC from the ContextFactory class, but we could also have retrieved it using 
     Context.get("InferencePSC"). 
    
     Note that we are wrapping the Query in a try-with-resources statement; this ensures that it 
     will be cleaned up when we are done with it, without having to manually call Query#close().
    */
    try (Query query = getQuery(querySentence, ContextFactory.INFERENCE_PSC)) {
      
      /*
       Let's set a few parameters for efficiency using our knowledge of answer expectations. In 
       particular we will set a cap on the number of results retrieved and the amount of time spent
       in retrieval and we will make the results accessible through the Cyc browser. Note that these
       setters return the Query object, so they are chainable.
       */
      query.setMaxAnswerCount(10)
              .setMaxTime(2)
              .setBrowsable(true);
      
      /*
       Calling Query#getAnswers() will force an inference to run, if this has not already happened.
       */
      System.out.println("Retrieving query answers as a List of binding-sets...");
      List<QueryAnswer> answers = query.getAnswers();
      
      System.out.println("Status: " + query.getStatus());
      System.out.println("Is inference suspended? "
              + InferenceStatus.SUSPENDED.equals(query.getStatus()));
      System.out.println("Number of results: " + answers.size());
      
      for (QueryAnswer answer : answers) {
        /*
         If we know the type of the binding, we can use that as a generic.
         */
        KbIndividual binding = answer.<KbIndividual>getBinding(movieVar);
        System.out.println(" - " + binding);
      }
    } finally {
      /*
       We've wrapped the Query in a try-with-resources statement, so it will be automatically closed 
       at the end of the try block.
       
       Closing the query performs clean up operations. Not closing queries leaves objects in the Cyc
       server, and can cause memory leaks.  The finalize method on the query closes it, thereby 
       releasing resources on the Cyc server, but it is performed at the mercy of the garbage 
       collector.  To ensure that the resources are released promptly on the server, the query 
       should generally be closed as soon as you are done with it and its directly related objects
       (such as any QueryResultSets).
       
       This is especially important when a query has been run as "browsable" (which this query was),
       as the Cyc server will hold onto resources for that query even after the calling application
       has terminated.
       */
      System.out.println("Done with query!");
    }
  }
  
  /**
   * Construct a query from a String containing a CycL sentence and run it. Although it's generally
   * preferable to construct Queries via a Sentence of KbObjects (less error-prone), there are 
   * certainly times when you may want to read raw Strings (user input, prototyping, etc.)
   * 
   * <p>Here we will retrieve the query answers as a synchronous QueryResultSet, which is very
   * similar to a JDBC ResultSet.
   * 
   * @throws KbTypeException
   * @throws CreateException
   * @throws QueryConstructionException
   * @throws KbException 
   */
  public void createQueryViaString()
          throws KbTypeException, CreateException, QueryConstructionException, KbException {
    System.out.println();
    
    /* 
     Note that the #$ prefix for constant names is not required, and that the
     toString method on KBObjects yields a string that can be used in this kind
     of method.
     */
    try (Query query = QueryFactory.getQuery(
            "(and (movieActors ?MOVIE " + nicholson + ")" +
            "     (movieAdvisoryRating ?MOVIE RestrictedRating))",
            "InferencePSC")) {
      query.setMaxAnswerCount(10)
              .setMaxTime(2)
              .setBrowsable(true);
      
      /*
       Calling Query#getResultSet() will force an inference to run, if this has not already 
       happened.
       */
      System.out.println("Retrieving query answers as a synchronous QueryResultSet...");
      QueryResultSet results = query.getResultSet();
      
      System.out.println("Status: " + query.getStatus());
      System.out.println("Is inference suspended? "
              + InferenceStatus.SUSPENDED.equals(query.getStatus()));
      System.out.println("Number of results: " + results.getCurrentRowCount());
      while (results.next()) {
        KbIndividual binding = results.getKbObject("?MOVIE", KbIndividual.class);
        System.out.println(" - " + binding);
      }
    } finally {
      System.out.println("Done with query!");
    }
  }
  
  /**
   * Run a query asynchronously and receive answers via listeners.
   * 
   * @throws KbTypeException
   * @throws CreateException
   * @throws QueryConstructionException 
   */
  public void runQueryAsynchronously()
          throws KbTypeException, CreateException, QueryConstructionException {
    System.out.println();
    
    final Variable movieVar = getVariable("?MOVIE");
    Sentence querySentence = SentenceFactory.and(
            getSentence(movieActors, movieVar, nicholson),
            getSentence(movieAdvisoryRating, movieVar, restrictedRating));
    Query query = null;
    
    /*
     Note that we are not wrapping this query in a try-with-resource statement. We'll explain that
     below.
     */
    try {
      query = getQuery(querySentence, ContextFactory.INFERENCE_PSC);
      query.setMaxAnswerCount(10)
              .setMaxTime(2)
              .setBrowsable(true);
      query.addListener(new QueryListener() {
        @Override
        public void notifyInferenceCreated(Query query) {
          System.out.println("... Created inference for " + query);
        }
        @Override
        public void notifyInferenceStatusChanged(InferenceStatus oldStatus, InferenceStatus newStatus, InferenceSuspendReason suspendReason, Query query) {
          System.out.println("... Inference status changed from " + oldStatus + " to " + newStatus);
        }
        @Override
        public void notifyInferenceAnswersAvailable(Query query, List<QueryAnswer> newAnswers) {
          for (QueryAnswer answer : newAnswers) {
            System.out.println("New answer: " + answer.<KbIndividual>getBinding(movieVar));
          }
        }
        @Override
        public void notifyInferenceTerminated(Query query, Exception exception) {
          if (exception == null) {
            System.out.println("... Inference terminated!");
          } else {
            System.err.println("... Inference terminated with exception!");
            exception.printStackTrace(System.err);
          }
        }
      });
      System.out.println("Retrieving query answers asynchronously via listeners...");
      query.performInference();
      
      /*
       Wait a couple of seconds for the purpose of this example. This is an asynchronous query, so
       we want to give it a chance to return results. 
       */
      try {
        Thread.sleep(2000);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    } finally {
      /*
       We're closing this query manually to illustrate a point: because this query is asynchronous, 
       we need to be a little more careful about when (and how) we close it. For example, in a web 
       service, a client might repeatedly poll the server to pick up new results; the server would 
       need to be at least mildly intelligent about when to close the query.
       */
      if (query != null) {
        System.out.println("Closing query...");
        query.close();
      }
      System.out.println("Done with asynchronous query!");
    }
  }
  
}
