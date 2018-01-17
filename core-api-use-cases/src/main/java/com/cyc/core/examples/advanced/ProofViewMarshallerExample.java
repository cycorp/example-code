package com.cyc.core.examples.advanced;

/*
 * #%L
 * File: ProofViewMarshallerExample.java
 * Project: Cyc Core API Use Cases
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
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.ProofViewGenerator;
import com.cyc.query.ProofViewSpecification;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.exception.ProofViewException;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.SessionManager;
import com.cyc.session.exception.SessionCommunicationException;
import java.io.IOException;
import java.io.OutputStream;

import static com.cyc.Cyc.Constants.INFERENCE_PSC;

/**
 * A bare-bones, self-contained example of running a query and returning proofview XML.
 *
 * @author nwinant
 */
public class ProofViewMarshallerExample {
  
  public static void main(String args[]) {
    final String exampleName = ProofViewMarshallerExample.class.getSimpleName();
    try (SessionManager sessionMgr = SessionManager.getInstance()) {
      System.out.println("Running " +  exampleName + "...");
      ProofViewMarshallerExample example = new ProofViewMarshallerExample();
      example.runExample();
    } catch (Exception ex) {
      ex.printStackTrace(System.err);
      System.exit(1);
    } finally {
      System.out.println("... " +  exampleName + " concluded.");
      System.exit(0);
    }
  }
  
  /**
   * Create and run a query, keeping the inference around for just long enough afterward that we can
   * generate and marshal a proofview to XML. In this example we're just proving a fully-bound 
   * sentence (note that we can construct a query directly from a Sentence object).
   * <p>
   * Incidentally, this is a useful technique to create proofviews for old answers (even for 
   * inferences that were run days or weeks ago): for a given answer, substitute the bindings back 
   * into the original query that generated it, and then run a new query from the fully-bound query
   * sentence. Cyc only needs to prove a single answer, and it can often do so in much less time 
   * than it originally took to find the answer. Just be aware that it may not be the <em>same</em>
   * proof that Cyc arrived at the first time.
   * <p>
   * Note that we're creating the query in a try-with-resources block: it's important to close the 
   * inference, otherwise its problem store won't be destroyed and you'll have a memory leak.
   * 
   * @throws KbTypeException
   * @throws CreateException 
   */
  public void runExample() throws KbTypeException, CreateException {
    try (Query query = Sentence.get("(genls Emu Bird)").toQuery(INFERENCE_PSC)) {
      // Make sure to keep the inference around after it has concluded. Because we're doing this, 
      // we need to make sure that we clean up after it, which we've done here with a 
      // try-with-resources block. It's important to close the inference, otherwise its problem 
      // store won't be destroyed and we'll have a memory leak in Cyc.
      query.retainInference();
      
      // Get a proof view generator for the first (and in this case, only) answer.
      final ProofViewGenerator proofGen 
              = ProofViewGenerator.get(query.getAnswer(0), ProofViewSpecification.get());
      
      // Marshall the proof view to XML; we can use any OutputStream or Writer.
      // This will cause the generator to create the proofview if it has not done so already.
      proofGen.getMarshaller().marshal(OUT);
    } catch (ProofViewException | QueryConstructionException | SessionCommunicationException ex) {
      ex.printStackTrace(System.err);
    }
  }
  
  private static final OutputStream OUT = new OutputStream() {
    @Override
    public void write(int b) throws IOException {
      System.out.write(b);
    }
  };
  
}
