package com.cyc.core.examples.advanced;



/*
 * #%L
 * File: QuerySearching.java
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

import com.cyc.baseclient.nl.ParaphraserFactory;
import com.cyc.baseclient.nl.ParaphraserFactory.ParaphrasableType;
import com.cyc.km.modeling.task.CycBasedTask;
import com.cyc.query.Query;
import com.cyc.kb.KbObject;
import com.cyc.kb.exception.KbException;
import com.cyc.km.query.construction.QuerySearch;
import com.cyc.nl.Paraphraser;
import com.cyc.nl.Span;
import com.cyc.session.CycSessionManager;
import com.cyc.session.spi.SessionManager;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;


/**
 * Examples for using the QuerySearch class.
 *
 */
public class QuerySearching {

  private static final String searchString = "What public corporations sell natural gas and what are their ticker symbols?";
  private static final String desiredTaskSummaryFragment = "Natural Gas";

  public static void main(String[] args) {
    final String exampleName = QuerySearching.class.getSimpleName();
    try (SessionManager sessionMgr = CycSessionManager.getInstance()) {
      System.out.println("Running " +  exampleName + "...");
/*
//      if (CycSessionManager.getCurrentSession().getServerInfo().isOpenCyc()) {
        System.out.println("The \"Query Search\" feature is not available in OpenCyc.");
        System.out.println("... " +  exampleName + " concluded.");
        System.exit(0);
//      }
*/           
      CycSessionManager.getCurrentSession().getOptions().setShouldTranscriptOperations(false);
      QuerySearch querySearch = getQuerySearch();
      getAndDisplayTerms(querySearch);
      getAndDisplayQueries(querySearch);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      System.exit(1);
    } finally {
      System.out.println("... " +  exampleName + " concluded.");
      System.exit(0);
    }
  }

  /**
   * Get a task with respect to which the query search will be performed.
   *
   * @return a task
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException
   */
  public static CycBasedTask getATask() throws OpenCycUnsupportedFeatureException {
    // First, get all known tasks:
    final Set<CycBasedTask> allTasks = new HashSet<>();
    try {
      allTasks.addAll(CycBasedTask.getAll());
      System.out.println("Got " + allTasks.size() + " tasks.");
    } catch (Exception ex) {
      System.err.println("Oops. Got exception finding set of Cyc-based tasks.");
      ex.printStackTrace(System.err);
    }
    // Then pick one you like:
    for (final CycBasedTask task : allTasks) {
      try {
        final String summary = task.getSummary();
        if (summary != null && summary.contains(desiredTaskSummaryFragment)) {
          return task;
        }
      } catch (Exception ex) {
        System.err.println("Oops. Got exception checking " + task);
        ex.printStackTrace(System.err);
      }
    }
    throw new RuntimeException("Failed to get a task mentioning '" + desiredTaskSummaryFragment + "'.");
  }

  /**
   * Print out the queries found by this search.
   *
   * @param querySearch
   * @throws Exception
   */
  public static void getAndDisplayQueries(final QuerySearch querySearch) throws KbException {
    final Collection<Query> queries = querySearch.getQueries();
    System.out.println("========================\nFound " + queries.size()
            + " queries:\n========================");
    Paraphraser paraphraser = getQueryParaphraser();
    for (final Query query : queries) {
      System.out.println(showLocations(querySearch.getSearchString(),
              querySearch.getQueryLocations(query)));
      System.out.println("CycL: "
              + query.getQuerySentence());
      System.out.println("Gloss: "
              + paraphraser.paraphrase(query).getString());
      System.out.println();
    }
  }

  /**
   * Print out the terms found by this search.
   *
   * @param querySearch
   * @throws Exception
   */
  public static void getAndDisplayTerms(final QuerySearch querySearch) {
    final Collection<KbObject> terms = querySearch.getTerms();
    System.out.println("========================\nFound " + terms.size()
            + " terms:\n========================");
    Paraphraser termParaphraser = getTermParaphraser();
    for (final KbObject term : terms) {
      System.out.println(showLocations(querySearch.getSearchString(),
              querySearch.getTermLocations(term)));
      System.out.println("CycL: " + term.toString());
      System.out.println("NL: "
              + termParaphraser.paraphrase(term).getString());
      System.out.println();
    }
  }

  /**
   * Show the substrings demarcated by locations in searchString.
   *
   * @param searchString
   * @param locations
   * @return searchString with locations highlighted.
   */
  public static String showLocations(String searchString,
          Collection<? extends Span> locations) {
    String result = searchString;
    for (final Span location : locations) {
      result = showLocation(result, location);
    }
    return result;
  }

  /**
   * Highlight location in searchString.
   *
   * @param searchString
   * @param location
   * @return searchString with location highlighted.
   */
  public static String showLocation(String searchString, Span location) {
    final int startIndex = location.getOffset();
    final int endIndex = location.getEnd() + 1;
    return searchString.substring(0, startIndex) + '|'
            + searchString.substring(startIndex, endIndex) + '|'
            + searchString.substring(endIndex);
  }

  /**
   * Get a paraphraser suitable for paraphrasing queries.
   * <p/>If the Cyc NL API is not available, the paraphrases will be extremely
   * primitive, typically just the CycL string.
   * @see Paraphraser#getInstance(com.cyc.baseclient.nl.Paraphraser.ParaphrasableType).
   *
   * @return the paraphraser
   */
  public static Paraphraser getQueryParaphraser() {
    // Get a generic paraphraser:
    final Paraphraser paraphraser = ParaphraserFactory.getInstance(ParaphrasableType.QUERY);
    // This could be set either way. Experimentation encouraged!
    //paraphraser.setBlanksForVars(true); //this doesn't exist in the general paraphraser interface.  Only in the actual QueryParaphraser interface.
    return paraphraser;
  }

  /**
   * Get a paraphraser suitable for paraphrasing terms.
   * <p/>If the Cyc NL API is not available, the paraphrases will be extremely
   * primitive, typically just the CycL string.
   * @see Paraphraser#getInstance(com.cyc.baseclient.nl.Paraphraser.ParaphrasableType).
   *
   * @return the paraphraser
   */
  public static Paraphraser getTermParaphraser() {
    final Paraphraser<KbObject> cycObjectParaphraser = ParaphraserFactory.getInstance(ParaphrasableType.KBOBJECT);
    // Set the force appropriately for terms:
    //cycObjectParaphraser.setForce(NLForce.None);
    /** Wrap our CycObject paraphraser in an KBObject paraphraser that will delegate
     * paraphrasing of the Cyc objects to it:
     * */
    return cycObjectParaphraser;
  }

  /**
   * Get a query search object initialized for a sample search string and task.
   *
   * @return the QuerySearch object for the sample task
   * @throws JAXBException
   */
  public static QuerySearch getQuerySearch() throws JAXBException, OpenCycUnsupportedFeatureException {
    final CycBasedTask task = getATask();
    final QuerySearch querySearch = new QuerySearch(searchString, task);
    return querySearch;
  }
}
