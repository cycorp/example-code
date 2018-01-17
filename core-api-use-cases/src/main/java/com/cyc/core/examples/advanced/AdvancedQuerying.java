package com.cyc.core.examples.advanced;

/*
 * #%L
 * File: AdvancedQuerying.java
 * Project: Cyc Core API Use Cases
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
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbObject;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswers;
import com.cyc.query.QueryListener;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.query.exception.QueryRuntimeException;
import com.cyc.session.SessionManager;
import com.cyc.session.exception.SessionCommunicationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cyc.Cyc.Constants.INFERENCE_PSC;

public class AdvancedQuerying {

  public static void main(String[] args) {
    final String exampleName = AdvancedQuerying.class.getSimpleName();
    try (SessionManager sessionMgr = SessionManager.getInstance()) {
      System.out.println("Running " + exampleName + "...");
      demonstrateIncrementalResultsQuery();
      try (Query q = Query.get("(and (genls HomoSapiens ?TYPE) (scientificName ?TYPE ?NAME))", "InferencePSC")) {
        if (q.getAnswerCount() > 25) {
          System.out.print("First 25 ");
        }
        System.out.println("Combined query answers:\n ");
        displayQueryBindings(q, 25);
      }
      demonstrateTermSubstitution();
    } catch (Exception ex) {
      System.out.println("Problem building or running demo query.");
      ex.printStackTrace(System.err);
      System.exit(1);
    } finally {
      System.out.println("... " + exampleName + " concluded.");
      System.exit(0);
    }
  }

  /**
   * Demonstrate how terms can be substituted in a Query before it is run.
   */
  public static void demonstrateTermSubstitution() {
    final Map<KbObject, Object> substitutions = new HashMap<>();
    final KbCollection theSpecies;
    try {
      theSpecies = KbCollection.findOrCreate("(TheFn BiologicalSpecies)");
    } catch (CreateException | KbTypeException ex) {
      throw new RuntimeException("Problem finding or creating indexical.", ex);
    }
    final List<String> sampleSpecies = Arrays.asList("PlainsZebra", "Ostrich", "HumpbackWhale");
    for (final String species : sampleSpecies) {
      try {
        final KbCollection kbSpecies = KbCollection.get(species);
        if (!kbSpecies.isInstanceOf(KbCollection.get("BiologicalSpecies"))) {
          throw new RuntimeException(kbSpecies + " is not known to be a species.");
        } else {
          final Sentence querySentence = Sentence.get("(and (genls (TheFn BiologicalSpecies) ?TYPE) (scientificName ?TYPE ?NAME))");
          try (Query indexicalQuery = querySentence.toQuery(INFERENCE_PSC)) {
            substitutions.put(theSpecies, kbSpecies);
            indexicalQuery.setSubstitutions(substitutions);
            System.out.println("\nResults for " + species + ": ");
            displayQueryBindings(indexicalQuery, 5);
          } catch (QueryConstructionException ex) {
            throw new RuntimeException(ex);
          }
        }
      } catch (CreateException | KbTypeException | RuntimeException ex) {
        System.out.println("Trouble testing " + species + ": " + ex.getLocalizedMessage());
      }
    }
  }

  /**
   * A simple method to traverse and display the bindings for a query.
   */
  public static void displayQueryBindings(final Query query, final Integer maxToDisplay) {
    final Collection<Variable> queryVars;
    try {
      queryVars = query.getQueryVariables();
    } catch (KbException ex) {
      throw new RuntimeException("Couldn't get query variables.", ex);
    }
    System.out.format("%-16s", "Variables:");
    queryVars
            .forEach(var -> System.out.format("%-24s", var));
    System.out.println();
    int i = 0;
    final QueryAnswers<?> answers;
    try {
      answers = query.getAnswers();
    } catch (SessionCommunicationException ex) {
      throw new RuntimeException("Couldn't get query answers.", ex);
    }
    for (final QueryAnswer answer : answers) {
      final Map<Variable, Object> bindings = answer.getBindings();
      System.out.format("%-16s", "Answer " + i + ":");
      queryVars
              .forEach(var -> System.out.format("%-24s", bindings.get(var)));
      System.out.println();
      if (++i >= maxToDisplay) {
        break;
      }
    }
  }

  /**
   * Sometimes you don't expect all the answers to a query to be returned quickly, and you want to
   * be able to do something with them as they come in. Who knows, you might even have some other
   * reason for handling the asynchronously. This example shows how to attach a listeners to a query
   * for all the major query events. Typically the most interesting ones are changes in the
   * inference status (i.e. whether it's running, suspended, etc.) and arrival of new answers.
   */
  private static void demonstrateIncrementalResultsQuery() {
    // A query that should get lots of results, not all at once:
    final Query query;
    try {
      query = Query.get("(#$and \n"
              + "(#$integerBetween 1 ?N 10000) \n"
              + "(#$isa ?N #$PrimeNumber))");
    } catch (QueryConstructionException ex) {
      throw new RuntimeException("Exception constructing query.", ex);
    }
    query.setMaxTime(30);
    query.setMaxAnswerCount(500);
    query.retainInference();
    // Add a listener that will handle important inference events:
    query.addListener(new QueryListener() {
      @Override
      public void notifyInferenceCreated(Query query) {
        System.out.println("Created " + query);
      }

      @Override
      public void notifyInferenceStatusChanged(InferenceStatus oldStatus,
                                               InferenceStatus newStatus, 
                                               InferenceSuspendReason suspendReason,
                                               Query query) {
        System.out.println("Inference status changed from " + oldStatus + " to " + newStatus);
        if (InferenceStatus.SUSPENDED.equals(newStatus)) {
          System.out.println("Suspend reason: " + suspendReason);
        }
      }

      @Override
      public void notifyInferenceAnswersAvailable(Query query, List<QueryAnswer> newAnswers) {
        int answerCount = query.getAnswerCount();
        System.out.println("New answers! Query now has " + answerCount + " answers.");
        try {//Do stuff with an answer:
          final QueryAnswer answer = newAnswers.get(newAnswers.size() - 1);
          System.out.println("Here is " + answer.getId());
          for (final Variable var : query.getQueryVariables()) {
            System.out.println(var + " -> " + answer.getBinding(var));
          }
        } catch (KbException ex) {

        }
        if (answerCount >= 1000) {
          System.out.println("Got enough answers. Terminating query.");
          query.stop(null);
        }
      }

      @Override
      public void notifyInferenceTerminated(Query query, Exception e) {
        System.out.println("Query terminated.");
      }
    });
    try {
      // Start the inference. This won't return until the inference terminates:
      query.performInference();
    } catch (QueryRuntimeException ex) {
      throw new RuntimeException("Exception performing inference.", ex);
    }
    query.close();
  }

}
