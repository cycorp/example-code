package com.cyc.core.examples.advanced;

/*
 * #%L
 * File: QueryAnswerJustifying.java
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

import com.cyc.query.ProofView;
import com.cyc.query.ProofViewNode;
import com.cyc.query.ProofViewSpecification;
import com.cyc.query.Query;
import com.cyc.session.SessionManager;
import com.cyc.session.exception.SessionCommunicationException;
import java.io.IOException;
import java.util.Iterator;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

/**
 * This example runs a simple query, and then computes and displays the justification for the first
 * answer that comes back from Cyc.
 */
public class QueryAnswerJustifying {

  public static void main(String[] args) {
    final String exampleName = QueryAnswerJustifying.class.getSimpleName();
    try (SessionManager sessionMgr = SessionManager.getInstance()) {
      System.out.println("Running " +  exampleName + "...");
      Query combinedQuery = Query.get("(and (isa ?X PubliclyHeldCorporation) (stockTickerSymbol ?X ?Y))", "InferencePSC");
      //In order to justify an answer, the inference itself needs to still be present on the Cyc server."
      combinedQuery.retainInference();

      displayAnswerJustification(combinedQuery);

      /* Closing isn't strictly necessary in this case, since we're about to exit the program,
       but it's still good form to clean up inferences as soon as you can.
       */
      combinedQuery.close();
    } catch (Exception ex) {
      ex.printStackTrace(System.err);
      System.exit(1);
    } finally {
      System.out.println("... " +  exampleName + " concluded.");
      System.exit(0);
    }
  }

  public static void displayAnswerJustification(final Query query)
          throws IOException, SessionCommunicationException {
    /* This is the place to set other parameters on the justification that would then be obeyed when 
    it is populated.  Populating the justification fills in all the structures, and makes sure they
    have both the detailed structure, and also the relevant natural language strings.
    */
    ProofViewSpecification proofViewSpec = ProofViewSpecification.get();
    /* Get the justification for a particular answer.  In this case, we get the very first answer
    that came back. */
    ProofView proofView = ProofView.getProofView(query.getAnswer(0), proofViewSpec);
    displayJustification(proofView);
  }

  /* The next two methods show how to traverse a justification.  In a real application, you'd probably
  want to do something more complicated than this with each of the nodes.  */
  public static void displayJustification(final ProofView proofView) 
          throws IOException {
    System.out.println("\n============= Justification ================");
    final Iterator<ProofViewNode> iter = proofView.toDepthFirstIterator();
    while (iter.hasNext()) {
      displayJustificationNode(iter.next());
    }
  }
  
  public static void displayJustificationNode(final ProofViewNode node) throws IOException {
    final StringBuilder renderer = new StringBuilder();
    // Indent according to node's depth:
    for (int i = 0; i < node.getDepth(); i++) {
      renderer.append(' ');
    }
    // Render initially hidden nodes with parens:
    final boolean expandInitially = node.isExpandInitially();
    if (!expandInitially) {
      renderer.append('(');
    }
    // Render the node's HTML, or the label:
    final String html = node.getHTML();
    if (html == null || html.isEmpty()) {
      final String label = node.getLabel();
      if (label != null) {
        renderer.append(label);
      }
    } else {
      // Render HTML as text:
      new TextExtractor(new Source(html)).appendTo(renderer);
    }
    if (!expandInitially) {
      renderer.append(')');
    }
    System.out.println(renderer);
  }


}
