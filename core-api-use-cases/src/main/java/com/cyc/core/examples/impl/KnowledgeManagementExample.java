/*
 * Copyright 2018 Cycorp, Inc.
 *
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
 */

package com.cyc.core.examples.impl;

/*
 * #%L
 * File: KnowledgeManagementExample.java
 * Project: Cyc Core API Use Cases
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc.
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

import com.cyc.kb.Assertion;
import com.cyc.kb.Context;
import com.cyc.kb.client.services.AssertionServiceImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.session.SessionManager;
import java.io.IOException;
import java.util.List;

import static com.cyc.Cyc.Constants.UV_MT;

/**
 * This class provides an example of knowledge management (KM) functionality. Note that much 
 * KM-specific functionality is included not in the Core API, but in other APIs (such as the 
 * Knowledge Management API) which sit atop it. This is because, while knowledge management is a 
 * cross-cutting concern at the project level, many <em>applications</em> are domain-focused and
 * typically don't provide or require high-level general KM functionality.
 * 
 * @author nwinant
 */
public class KnowledgeManagementExample {

  public static void main(String[] args) {
    final String exampleName = KnowledgeManagementExample.class.getSimpleName();
    try (SessionManager sessionMgr = SessionManager.getInstance()) {
      System.out.println("Running " + exampleName + "...");
      KnowledgeManagementExample example = new KnowledgeManagementExample();
      example.viewMicrotheoryContents();
      System.out.println();
    } catch (KbException | RuntimeException kbe) {
      kbe.printStackTrace(System.err);
      System.exit(1);
    } catch (IOException ioe) {
      System.err.println("Error closing SessionManager");
      ioe.printStackTrace(System.err);
      System.exit(1);
    } finally {
      System.out.println("... " + exampleName + " concluded.");
      System.exit(0);
    }
  }
  
  /**
   * Find all of the assertions that are directly asserted within an Mt (and not those which are 
   * merely <em>visible</em> from the Mt). 
   * <p>
   * In practice, this is rarely done. There are cases where this method is useful, such as in
   * general-purpose knowledge editing applications for the Cyc KB, but it should be used with
   * caution. This method is currently not included in the official KB API for two reasons:
   * <ol>
   *   <li>In practice, it's rarely used: most applications never need to use it, and it would 
   *       in fact be the wrong approach.
   *       What is much more commonly useful is retrieving relevant answers to a specific question 
   *       (typically as KbTerms from a query) in which case what matters is an assertion's 
   *       <em>visibility</em> from an Mt.
   *       When applications directly interact with assertions, it is typically by retrieving the
   *       predicate extent for some relevant KbPredicate, or to create or delete specific 
   *       assertions whose Mt is either already known or easily retrievable.</li>
   *   <li>It's potentially very expensive. Retrieving the contents of a small, application-focused
   *       Mt is relatively quick, but retrieving the contents of a broad Mt like 
   *       {@code #$UniversalVocabularyMt} may take upwards of an hour or possibly even longer. Even
   *       some Mts like {@code #$CurrentWorldDataCollectorMt-NonHomocentric} may contain ~10,000 
   *       assertions.</li>
   * </ol>
   * 
   * @throws KbTypeException
   * @throws CreateException
   * @throws IllegalArgumentException if given an expensive Context when not allowed
   */
  protected void viewMicrotheoryContents() 
          throws KbTypeException, CreateException, IllegalArgumentException {
    final AssertionServiceImpl assertionSvc = new AssertionServiceImpl();
    final Context ctx = Context.get("CurrentWorldDataCollectorMt-NonHomocentric");
    
    final List<Assertion> assertions = assertionSvc.getAllAssertedInContext(ctx);
    System.out.println("Assertions in " + ctx + ": ");
    assertions
            .forEach(assertion -> System.out.println("- " + assertion));
    
    final int numAssertions = assertionSvc.getCountOfAllAssertedInContext(ctx);
    System.out.println("Number of assertions in " + ctx + ": " + numAssertions);
    
    try {
      final List<Assertion> wayTooManyAssertions = assertionSvc.getAllAssertedInContext(UV_MT);
      System.out.println("Assertions in " + UV_MT + ": ");
      wayTooManyAssertions
              .forEach(assertion -> System.out.println("- " + assertion));
    } catch (IllegalArgumentException ex) {
      System.out.println(
              "An IllegalArgumentException was thrown when we tried to retrieve all of the"
                      + " assertions in " + UV_MT + "."
                      + " We could attempt to force the issue, but it would be unwise to do so.");
    }
  }
  
}
