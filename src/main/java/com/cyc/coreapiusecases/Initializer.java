package com.cyc.coreapiusecases;

/*
 * #%L
 * File: Initializer.java
 * Project: Cyc API Use Cases
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

import com.cyc.kb.Context;
import com.cyc.kb.config.KBAPIConfiguration;
import com.cyc.kb.config.KBAPIDefaultContext;

/** 
 * <P>Initializer is designed to provide methods for initializing/configuring 
 * the Core Cyc API so applications can start using the API. 
 *
 * <P>Copyright (c) 2014 Cycorp, Inc.  All rights reserved.
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>Use is subject to license terms.
 * 
 * @deprecated This functionality will be moved into {@link com.cyc.session.SessionOptions} by the 1.0.0-rc2 release of the Session API.
 */
@Deprecated
public class Initializer {
  
  /**
   * Configures the KBAPI to send operations to transcript.
   */
  public static void setTranscriptOperations(boolean shouldTranscriptOperations) {
    KBAPIConfiguration.setShouldTranscriptOperations(shouldTranscriptOperations);
  }
  
  /**
   * Configures the KBAPI by setting default Contexts for assertions and queries. 
   */ 
  public static void setDefaultContexts(Context defaultAssertionContext, Context defaultQueryContext) { 
    KBAPIConfiguration.setDefaultContext(new KBAPIDefaultContext(defaultAssertionContext, defaultQueryContext));
  }
}
