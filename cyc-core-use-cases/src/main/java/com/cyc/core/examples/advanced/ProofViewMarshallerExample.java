package com.cyc.core.examples.advanced;

/*
 * #%L
 * File: ProofViewMarshallerExample.java
 * Project: Core API Use Cases
 * %%
 * Copyright (C) 2013 - 2015 Cycorp, Inc.
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

import com.cyc.kb.Sentence;
import com.cyc.kb.client.Constants;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.km.query.answer.justification.ProofViewJustification;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryImpl;
import com.cyc.xml.query.ProofView;
import com.cyc.xml.query.ProofViewMarshaller;

/**
 * A bare-bones, self-contained example of running a query and returning proofview XML.
 * It is adapted from com.cyc.xml.query.ProofViewMarshallerTest test in the Query API.
 * @author nwinant
 */
public class ProofViewMarshallerExample {
  
  public void runExample() {
    QueryAnswer answer = null;
    try {
      // Set up the query. Here, we're just proving a fully-bound query sentence.
      final Sentence genlsEmuBird = new SentenceImpl("(genls Emu Bird)");
      final QueryImpl query = new QueryImpl(genlsEmuBird, Constants.inferencePSCMt());
      
      // Make sure to keep the inference around after it has concluded. Because we're doing this, 
      // we'll need to make sure that we clean up after it in the "finally" block.
      query.retainInference();
      
      // Get a proof view for the first (and in this case, only) answer.
      answer = query.getAnswer(0);
      final ProofViewJustification justification = new ProofViewJustification(answer);
      final ProofView proofView = justification.getProofView();
      
      // Marshall proof view to XML. Here we pass it to System.out, but we could use any 
      // OutputStream or Writer.
      new ProofViewMarshaller().marshal(proofView, System.out);
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    } finally {
      if (answer != null) {
        // It's important to close the inference. Otherwise, its problem store won't be destroyed,
        // and you'll have a memory leak.
        answer.getId().getInferenceIdentifier().close();
      }
    }
  }
  
  public static void main(String args[]) {
    try {
      ProofViewMarshallerExample example = new ProofViewMarshallerExample();
      example.runExample();
    } finally {
      System.exit(0);
    }
  }
  
}
