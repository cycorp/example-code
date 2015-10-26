package com.cyc.core.examples.advanced;

/*
 * #%L
 * File: ConstructingQueries.java
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

import com.cyc.base.CycConnectionException;
import com.cyc.baseclient.nl.Paraphraser;
import com.cyc.kb.ArgPosition;
import com.cyc.kb.KBCollection;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.KBCollectionImpl;
import com.cyc.kb.config.KBAPIConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KBApiException;
import com.cyc.kb.exception.KBTypeException;
import com.cyc.km.modeling.task.CycBasedTask;
import com.cyc.km.query.construction.QuerySearch;
import com.cyc.query.Query;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.SessionCommunicationException;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;

public class ConstructingQueries {

  static QuerySearch querySearch;
  private static final Paraphraser queryParaphraser = QuerySearching.getQueryParaphraser();
  private static final String[] fragmentStrings = {"Natural gas is sold by",
    "sells natural gas", "The ticker symbol for", "is a publicly held corporation",
    "(isa ?X PubliclyHeldCorporation)", "(sellsProductType ?X NaturalGas)", "(stockTickerSymbol ?X ?Y)"};
  private static final int expectedFragmentCount = 3;
  static KBCollection termToReplace;
  //@todo get rid of need to use CycObjects for replacing stuff.

  public static void main(String[] args) throws KBTypeException, CreateException {
    final String exampleName = ConstructingQueries.class.getSimpleName();
    termToReplace = KBCollectionImpl.get("NaturalGas");
    try {
      System.out.println("Running " +  exampleName + "...");
      /*
//      if (CycSessionManager.getCurrentSession().getServerInfo().isOpenCyc()) {
        System.out.println("The \"Query Search and Construction\" feature is not available in OpenCyc.");
        System.out.println("... " +  exampleName + " concluded.");
        System.exit(0);
//      }
              */
      KBAPIConfiguration.setShouldTranscriptOperations(false);
      querySearch = QuerySearching.getQuerySearch();
      final Set<Query> fragments = getFragments();
      final Query combinedQuery = combineFragments(fragments);
      getAndDisplayCandidateReplacements(combinedQuery, termToReplace);
    } catch (Exception ex) {
      ex.printStackTrace(System.err);
      System.exit(1);
    } finally {
      System.out.println("... " +  exampleName + " concluded.");
      System.exit(0);
    }
  }

  public static Set<Query> getFragments() throws JAXBException, CycConnectionException {
    final Set<Query> fragments = new HashSet<Query>();
    for (final Query query : querySearch.getQueries()) {
      final String queryString = queryParaphraser.paraphrase(query).getString();
      for (final String fragmentString : fragmentStrings) {
        if (queryString.contains(fragmentString)) {
          System.out.println("Found " + query);
          fragments.add(query);
          break;
        }
      }
    }
    if (fragments.size() != expectedFragmentCount) {
      throw new RuntimeException(
              "Expected " + expectedFragmentCount + " query fragments, got " + fragments);
    }
    return fragments;
  }

  public static Query combineFragments(final Set<Query> fragments) 
          throws QueryConstructionException, SessionCommunicationException, OpenCycUnsupportedFeatureException {
    Query combinedQuery = null;
    for (final Query query : fragments) {
      if (combinedQuery == null) {
        combinedQuery = query;
      } else {
        combinedQuery = combinedQuery.merge(query);
      }
    }
    System.out.println("Combined query:\n " + combinedQuery.toString());
    return combinedQuery;
  }

  public static void getAndDisplayCandidateReplacements(final Query query,
          final Object term) throws KBApiException, IOException, OpenCycUnsupportedFeatureException {
    final Sentence querySentence = query.getQuerySentence();
    final ArgPosition argPosition = querySentence.getArgPositionsForTerm(term).iterator().next();
    final CycBasedTask task = querySearch.getTask();
    final List<Object> candidateReplacements
            = task.getCandidateReplacements(querySentence, argPosition);
    System.out.println("Found " + candidateReplacements.size() + " candidate replacements for "
            + termToReplace + ":");

    System.out.println("=========== Building Hierarchy ==============");

  }
}
