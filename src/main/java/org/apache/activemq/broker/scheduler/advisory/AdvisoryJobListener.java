/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.broker.scheduler.advisory;

import org.apache.activemq.broker.scheduler.JobListener;

import org.apache.activemq.util.ByteSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an advisory JobListener interface.
 */
public class AdvisoryJobListener implements JobListener {
    private static final Logger LOG = LoggerFactory.getLogger(AdvisoryJobListener.class);

	private JobListener delegateJobListener; 

	public AdvisoryJobListener(JobListener delegateJobListener) {
		this.delegateJobListener = delegateJobListener;
	}

	@Override
	public void scheduledJob(String id, ByteSequence job) {
		if(null == delegateJobListener) {
			return;
		}
		delegateJobListener.scheduledJob(id, job);
	}
}
