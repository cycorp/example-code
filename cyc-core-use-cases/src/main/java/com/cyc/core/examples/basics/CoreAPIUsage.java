package com.cyc.core.examples.basics;

/*
 * #%L
 * File: CoreAPIUsage.java
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

import com.cyc.kb.BinaryPredicate;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.KBCollection;
import com.cyc.kb.KBIndividual;
import com.cyc.kb.KBStatus;
import com.cyc.kb.KBTerm;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.BinaryPredicateImpl;
import com.cyc.kb.client.Constants;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.client.FactImpl;
import com.cyc.kb.client.KBCollectionImpl;
import com.cyc.kb.client.KBIndividualImpl;
import com.cyc.kb.client.KBTermImpl;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.client.VariableImpl;
import com.cyc.kb.config.KBAPIConfiguration;
import com.cyc.kb.config.KBAPIDefaultContext;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KBApiException;
import com.cyc.kb.exception.KBObjectNotFoundException;
import com.cyc.kb.exception.KBTypeException;
import com.cyc.query.Query;
import com.cyc.query.QueryFactory;
import com.cyc.query.QueryResultSet;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.CycSession;
import com.cyc.session.CycSessionManager;
import com.cyc.session.SessionApiException;
import com.cyc.session.SessionManager;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * CoreAPIUsage is designed to illustrate the exemplary idioms for
 * programmatically working with the Cyc KB through the Cyc Core API.
 * Illustrations include new term creation, term retrieval, asserting and
 * deleting facts about terms, and querying the Cyc KB.
 *
 * @note There are multiple ways of interacting with Cyc (through and
 * independent of the Core API). The illustrations here are the most popular
 * paradigms.
 *
 */
public class CoreAPIUsage {
  
  static BinaryPredicate movieActors;
  static KBCollection RestrictedRating;
  static BinaryPredicate movieAdvisoryRating;
  static KBCollection ActorInMovies;
  static Context PeopleDataMt;
  static Context MassMediaMt;

  private KBIndividual JackNicholson;
  private KBIndividual TheKingOfMarvinGardens_TheMovie;
    
  public static void main(String[] args) throws IOException {
    final String exampleName = CoreAPIUsage.class.getSimpleName();

    /*
     Wrapping the current SessionManager instance in a try-with-resources statement (Java 7 and 
     later) in the main method ensures that ALL CycSessions created by the SessionManager are 
     closed, along with all of their Cyc server connections, before the program ends:
     */
    try (SessionManager sessionMgr = CycSessionManager.getInstance()) {

      System.out.println("Running " + exampleName + "...");
      CoreAPIUsage usage = new CoreAPIUsage();

      //Here's where we run the example code...
      usage.runExample();

    } catch (KBApiException kbe) {
      kbe.printStackTrace(System.err);
      System.exit(1);
    } catch (QueryConstructionException qce) {
      qce.printStackTrace(System.err);
      System.exit(1);
    } catch (SessionApiException sae) {
      sae.printStackTrace(System.err);
      System.exit(1);
    } catch (Throwable ex) {
      ex.printStackTrace(System.err);
      System.exit(1);
    } finally {
      /*
       If we were running under Java 6, we could manually close the current SessionManager here 
       within the finally block, like so:
      
       CycSessionManager.getInstance().close();
       */

      System.out.println("... " + exampleName + " concluded.");
      System.exit(0);
    }
  }
  
  /**
   * Illustrates popular paradigms of interaction with Cyc through the Core API
   * using a movie theme. In this simple exercise, we will tell Cyc that the
   * movie actor Jack Nicholson played a leading role in the R-rated movie
   * <i>The King of Marvin Gardens</i>. Then we will query the Cyc KB to make
   * sure Cyc remembers the fact.
   */
  private void runExample() throws CreateException, KBTypeException, DeleteException, SessionApiException, KBApiException, QueryConstructionException {
 
    /* 
     Here we ensure that the CycSession for the current thread is closed before the thread is 
     terminated. This may seem a little redundant, as this is a single-threaded application and
     we're already closing the SessionManager in the main() method (thereby closing ALL
     CycSessions). Although this isn't necessary for simple applications like this one, it is good
     practice for multi-threaded applications (e.g., servlets), so we're demonstrating it here.
    
     Typically, we don't need to directly request the current CycSession; if one does not exist, it 
     will be automatically created the first time that the Cyc APIs require it. However, wrapping it
     in a try-with-resources statement (Java 7 and later) ensures that the session will be cleaned 
     up at the end of the current execution thread.
     */
    try (CycSession session = CycSessionManager.getCurrentSession()) {

      /* Connect to a Cyc server and set a few basic options. */
      setUpCyc();
      CoreAPIUsage.initializeStatics();

      setupJackNicholson();
      
      setupMarvinGardens();

      /* FOR FUTURE RELEASE (WITH NL API)
       Yet another method to retrieve things from the KB is by using their natural
       language representations
       NLFormat nlf = NLFormat.getInstance();
       List<Object> jacks = (List<Object>) nlf.parseObjects("Jack Nicholson");
       boolean foundJackNicholsonByNLString = false;
       for(Object NLJack : jacks) {
       if(Nicholson.equalsSemantically(NLJack)) 
       foundJackNicholsonByNLString = true;
       }
       assert (foundJackNicholsonByNLString) : "Could not retrieve Jack Nicholson by natural language parsing"; 
       */

      /* Now let's query the KB to make sure we we able to make the above 
       assertions correctly. To that end, let's try to retrieve all movies in which 
       Jack Nicholson has acted and which have a restricted rating and make sure 
       that TheKingOfMarvinGardens-TheMovie appears in the results. */
      runQueryAndTestAnswers(createQueryViaKBSentence());

      /* The second method for creating a new query has a simpler form */
      runQueryAndTestAnswers(createQueryViaString());
      /* Execute the method illustrating Cyc Core API usage through a movie theme */
    } finally {
      /*
       If we were running under Java 6, we could have manually closed the current CycSession here 
       within the finally block, like so:
      
       CycSessionManager.getCurrentSession().close();
       */
    }
  }
  
  /**
   * Connect to a Cyc server and set a few basic options.
   * 
   * @throws SessionApiException 
   */
  private void setUpCyc() throws SessionApiException {
    System.out.println("Setting up Cyc... ");

    /* 
    There is no need to specify a Cyc server. When one is required, the
    CycSessionManager will check the System properties to determine how to
    get access to Cyc. By default, in a graphical environment, it will fall 
    back on interactively prompting the user for connection information.
    */
    CycSession session = CycSessionManager.getCurrentSession();
    System.out.println("Acquired a cyc server... " + session.getServerInfo().getCycServer());

    /* Initialize the default assertion and query contexts to UniversalVocabularyMt and InferencePSC resp. */
    KBAPIConfiguration.setDefaultContext(new KBAPIDefaultContext(Constants.uvMt(), Constants.inferencePSCMt()));

    // setTranscriptOperations will Transcript the Cyc operations issued by the API
    // These operations can be saved into a ASCII text file and reloaded on a clean image to recreate 
    // a given state of the KB with such operations.
    KBAPIConfiguration.setShouldTranscriptOperations(true);

    // This will set "TheUser" as the current Cyclist.
    // Setting the cyclist is required for Bookkeeping information to be added for
    // assertions and constants.
    CycSessionManager.getCurrentSession().getOptions().setCyclistName("TheUser");
  }

  private static void initializeStatics() throws CreateException, KBTypeException, DeleteException {
    /* Most of the methods in the KB API and the Query API will accept both Strings and KB objects for their
     arguments.  But it's good practice to create the KB object versions, and use them.  In this file, we'll
     alternate back and forth between using KB object and using Strings for these arguments.  Here's how
     you create the KB objects.  */
    PeopleDataMt = ContextImpl.get("PeopleDataMt");
    MassMediaMt = ContextImpl.get("MassMediaDataMt");

    /* Since we will be talking about Jack Nicholson, we have to make 
     sure Cyc knows about actors. Most Cyc releases will already have this term, but just in case,
     here's how you go about creating it.  Don't worry if it's already in your Cyc KB.  
     These operations are idempotent, so running them in a Cyc that already has the terms won't do 
     anything bad.  When in doubt, the following idiom is most useful. */
    ActorInMovies = KBCollectionImpl.findOrCreate("ActorInMovies");    
    /*Let's also convey the fact that all movie actors are people. (Our apologies 
     to the highly trained animals who have sometimes carried movies on their 
     shoulders). We assume the collection Person already exists and we only have 
     to specify the name to retrieve it */
    ActorInMovies.addGeneralization("Person");

    /* We need a way to relate actors to the movies they are in.
     Let's call the relation "movieActors". This particular predicate already exists in Cyc, 
     but if it didn't, this is how you would create it. */
    movieActors = BinaryPredicateImpl.findOrCreate("movieActors");

    /* It is always a good idea to put constraints on the arguments of predicates
     for semantic validity. The following establishes in the KB that inside the 
     MassMediaDataMt context the first argument of movieActors must be an 
     instance of the collection Movie-CW and the second argument must be an
     instance of the collection ActorInMovies */
    movieActors.addArgIsa(1, "Movie-CW", "MassMediaDataMt");
    movieActors.addArgIsa(2, "ActorInMovies", "MassMediaDataMt");

    /* Not only did Jack Nicholson act in the movie, but he had a starring role. 
     Let's add this more specific knowledge to the KB. We need a more specific 
     predicate. Let's call it "movieActors-WithStarringRole".  Again, this predicate
     already exists in most Cyc KBs, so the following will typically retrieve, 
     rather than create, the predicate in most Cyc KBs. */
    BinaryPredicate movieActorsWithStarringRole = BinaryPredicateImpl.findOrCreate("movieActors-WithStarringRole");
    movieActorsWithStarringRole.addArgIsa(1, "Movie-CW", "MassMediaDataMt");
    movieActorsWithStarringRole.addArgIsa(2, "ActorInMovies", "MassMediaDataMt");

    /* Let's also establish that movieActors is a generalization of 
     movieActorsWithStarringRole */
    movieActorsWithStarringRole.addGeneralization("movieActors", "UniversalVocabularyMt");

    /* If we're going to set the rating for a movie, we'll need that vocabulary, too. */
    RestrictedRating = KBCollectionImpl.get("RestrictedRating");
    movieAdvisoryRating = BinaryPredicateImpl.get("movieAdvisoryRating");
  }

  /**
   * We need a handle to the representation of the actor Jack Nicholson in the KB. We are not sure 
   * if this term exists in the KB.
   * 
   * @throws CreateException
   * @throws KBTypeException 
   */
  public void setupJackNicholson() throws CreateException, KBTypeException {
    System.out.println("Setting up JackNicholson.");
    
    /* Find or create an individual term in the KB to represent the actor 
     Jack Nicholson. Simultaneously make it an instance of the ActorInMovies 
     collection in the PeopleDataMt Context*/
    JackNicholson = KBIndividualImpl.findOrCreate("JackNicholson",
            ActorInMovies, PeopleDataMt);

    /* There's also a get method, but it should only be used if you're sure the term is in the KB.  This
     code shows how to test whether a term is in the KB, and then actually gets it.  In most real-world
     cases, you won't runExample this test before every call to get. */
    if (KBIndividualImpl.getStatus("JackNicholson").equals(KBStatus.EXISTS_WITH_COMPATIBLE_TYPE)) {
      JackNicholson = KBIndividualImpl.get("JackNicholson");
    }

    /* We can easily see what JackNicholson is known to be instance of from the PeopleDataMt context. */
    Collection<KBCollection> nicholsonTypes = JackNicholson.instanceOf("PeopleDataMt");
    /* Since JackNicholson is an instance of ActorInMovies which is a specialization
     of Person, we should be able to prove that JackNicholson is a person without
     ever having asserted it*/
    assert (nicholsonTypes.contains(KBCollectionImpl.get("Person"))) :
            "Jack Nicholson may be an ActorInMovies but he is not a Person";

    /* Since every KBIndividual is a KBTerm we could also retrieve JackNicholson 
     as a KBTerm */
    KBTerm NicholsonTerm = KBTermImpl.get("JackNicholson");

    /* It is important to note that Nicholson and NicholsonTerm are different 
     Java objects but since they refer to the same instance in the KB, they are 
     semantically equal */
    assert (JackNicholson.equalsSemantically(NicholsonTerm)) :
            "Nicholson and NicholsonTerm refer to different things in the KB";
  }

  /**
   * Similar to Jack Nicholson, we need a handle to the representation of the movie "The King of 
   * Marvin Gardens" in the KB. Once again we are not sure if this term exists in the KB.
   * 
   * @throws CreateException
   * @throws KBTypeException
   * @throws DeleteException 
   */
  private void setupMarvinGardens() throws CreateException, KBTypeException, DeleteException {
    System.out.println("Setting up MarvinGardens.");
    /*
    If you would like to ensure that the specific constant name 
    "TheKingOfMarvinGardens-TheMovie" is not being used, then check for its 
    existance using the get method.
    */
    
    // We will ensure that TheKingOfMarvinGardens-TheMovie does not exist
    try {
      System.out.println("Attempting to delete TheKingOfMarvinGardens-TheMovie...");
      KBIndividualImpl.get("TheKingOfMarvinGardens-TheMovie").delete();
    } catch (KBObjectNotFoundException e) {
      // Expect KBObjectNotFoundException stack trace if "TheKingOfMarvinGardens-TheMovie" is not in the KB
      // Otherwise the concept will be deleted without exceptions. 
      System.out.println("... Apparently it was not in the KB: " + e.getMessage());
    }
    
    try {
      TheKingOfMarvinGardens_TheMovie = KBIndividualImpl.get("TheKingOfMarvinGardens-TheMovie");
    } catch (CreateException ce) {
      TheKingOfMarvinGardens_TheMovie = KBIndividualImpl.findOrCreate("TheKingOfMarvinGardens-TheMovie");
    }

    /* Let's also convey the fact in the MassMediaDataMt context that 
     KingOfMarvinGardens-TheMovie is a Dramatic movie. Note that we could also 
     have passed the collection DramaticMovie and the context MassMediaDataMt as
     parameters to the findOrCreate method as we did when creating the 
     KBIndividual JackNicholson. Also note that we assumed the collection
     DramaticMovie exists in the KB. If DramaticMovie were not in the KB, this code would throw an
     exception, and we would have to create it like we did ActorInMovies */
    TheKingOfMarvinGardens_TheMovie.instantiates("DramaticMovie", "MassMediaDataMt");

    /* It's a fact, in the MassMediaMt context, that JackNicholson was one of the actors in 
     KingOfMarvinGardens */
    Fact nicholsonInMarvinGardens
            = FactImpl.findOrCreate(new SentenceImpl(movieActors, TheKingOfMarvinGardens_TheMovie, JackNicholson), MassMediaMt);


    /* The following Fact asserts that Jack Nicholson had a starring role in 
     TheKingOfMarvinGardens-TheMovie. This method shows how to do it with a String 
     containing a CycL sentence. */
    FactImpl.findOrCreate("(movieActors-WithStarringRole TheKingOfMarvinGardens-TheMovie JackNicholson)", "MassMediaDataMt");

    /* Now that we know he was a star, it's redundant to say that he acted in the 
     movie, so we can (but are not required to) delete the first fact, and we'll 
     still know that he acted in the movie */
    nicholsonInMarvinGardens.delete();


    /* Finally we add the fact that the movie "The King of Marvin Gardens" has a 
     restricted rating. This uses yet another means of adding a new fact to the 
     KB */
    TheKingOfMarvinGardens_TheMovie.addArg2(movieAdvisoryRating, RestrictedRating, MassMediaMt);
  }

  /**
   * Construct a query from a sentence (which could be the result of logically connecting other 
   * sentences).
   * 
   * @return Query
   * @throws KBApiException
   * @throws QueryConstructionException 
   */
  private Query createQueryViaKBSentence() throws KBApiException, QueryConstructionException {
    Set<Sentence> movieSentences = new HashSet<Sentence>();
    Variable movieVar = new VariableImpl("?MOVIE");
    movieSentences.add(new SentenceImpl(movieActors, movieVar, JackNicholson));
    movieSentences.add(new SentenceImpl(movieAdvisoryRating, movieVar, RestrictedRating));
    Sentence querySentence = SentenceImpl.and(movieSentences);

    /* The above construction creates the Sentence:
    
     "(and (movieActors ?MOVIE JackNicholson) 
       (movieAdvisoryRating ?MOVIE RestrictedRating))". 
    
     Now we can construct a query for this sentence. We will use InferencePSC, the 
     all-encompassing context, as the context in which to execute the query. Note 
     here we retrieve a handle to InferencePSC from the Constants class (which 
     maintains handles to popular constants) but we could also have retrieved it
     using Context.get("InferencePSC"). */
    Query q = QueryFactory.getQuery(querySentence, Constants.inferencePSCMt());

    /* Let's set a few parameters for efficiency using our knowledge of answer
     expectations. In particular we will set a cap on the number of results 
     retrieved and the amount of time spent in retrieval and we will make the 
     results accessible through the Cyc browser.
     Note that these setters return the Query object, so they are chainable.
     */
    q.setMaxNumber(10).setMaxTime(2).setBrowsable(true);
    return q;
  }

  /**
   * Just as we can create a query using a Sentence object, we can also create one using a String 
   * containing a CycL sentence.
   * 
   * @return
   * @throws QueryConstructionException 
   */
  private Query createQueryViaString() throws QueryConstructionException {
    /* 
     Note that the #$ prefix for constant names is not required, and that the
     toString method on KBObjects yields a string that can be used in this kind
     of method.
     */
    Query q = QueryFactory.getQuery("(and (movieActors ?MOVIE " + JackNicholson + ")"
            + "(movieAdvisoryRating ?MOVIE RestrictedRating))");

    q.setMaxNumber(10).setMaxTime(2).setBrowsable(true);
    return q;
  }

  /**
   * Having constructed a query, let's execute it...
   * 
   * @param q
   * @throws KBApiException
   * @throws CycTimeOutException
   * @throws CycConnectionException 
   */
  private void runQueryAndTestAnswers(Query q) throws KBApiException {
    QueryResultSet results = q.getResultSet();
    /* Now let's verify that TheKingOfMarvinGardens-TheMovie is one of the results returned */
    boolean found = false;
    System.out.println("The query statement is : " + q.getQuerySentence());
    System.out.println("And the results are : ");
    while (results.next()) {
      System.out.println(results.getKBObject("?MOVIE", KBIndividual.class));
      if (results.getKBObject("?MOVIE", KBIndividual.class).equals(TheKingOfMarvinGardens_TheMovie)) {
        found = true;
      }
    }
    System.out.println();
    assert (found) : "The KB does not know about Jack Nicholson's R-rated movie The King of "
            + "Marvin Gardens";
    
    /* Closing the query performs clean up operations. Not closing queries leaves objects in the Cyc
     server, and can cause memory leaks.  The finalize method on the query closes it, thereby releasing
     resources on the Cyc server, but it is performed at the mercy of the garbage collector.  To ensure
     that the resources are released promptly on the server, the query should generally be closed as soon
     as you are done with it and its directly related objects (e.g. the KBInferenceResultSet).  */
    q.close();

  }
}
