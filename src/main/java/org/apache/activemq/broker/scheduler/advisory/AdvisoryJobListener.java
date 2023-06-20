/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
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

	/**
	 * Used to specify that a some operation should be performed on the Scheduled Message,
	 * the Message must have an assigned Id for this action to be taken.
	 */
	public static final String AMQ_SCHEDULER_ACTION = "AMQ_SCHEDULER_ACTION";

	/**
	 * Indicates that a browse of the Scheduled Messages is being requested.
	 */
	public static final String AMQ_SCHEDULER_ACTION_BROWSE = "BROWSE";
	/**
	 * Indicates that a Scheduled Message is to be remove from the Scheduler, the Id of
	 * the scheduled message must be set as a property in order for this action to have
	 * any effect.
	 */
	public static final String AMQ_SCHEDULER_ACTION_REMOVE = "REMOVE";
	/**
	 * Indicates that all scheduled Messages should be removed.
	 */
	public static final String AMQ_SCHEDULER_ACTION_REMOVEALL = "REMOVEALL";

	/**
	 * A property that holds the beginning of the time interval that the specified action should
	 * be applied within.  Maps to a long value that specified time in milliseconds since UTC.
	 */
	public static final String AMQ_SCHEDULER_ACTION_START_TIME = "ACTION_START_TIME";

	/**
	 * A property that holds the end of the time interval that the specified action should be
	 * applied within.  Maps to a long value that specified time in milliseconds since UTC.
	 */
	public static final String AMQ_SCHEDULER_ACTION_END_TIME = "ACTION_END_TIME";

	private JobListener delegateJobListener; 

	public AdvisoryJobListener(JobListener delegateJobListener) {
		this.delegateJobListener = delegateJobListener;
	}

	public void willScheduleJob(String id, ByteSequence job) throws Exception  {
	}

	public void didScheduleJob(String id, ByteSequence job) throws Exception {
	}

	public void willDispatchJob(String id, ByteSequence job) throws Exception {
	}

	// Actual dispatching of the job
	@Override
	public void scheduledJob(String id, ByteSequence job) {
		if(null == delegateJobListener) {
			return;
		}
		delegateJobListener.scheduledJob(id, job);
	}

	public void didDispatchJob(String id, ByteSequence job) throws Exception {
	}

	public void willRemoveJob(String id) throws Exception {
	}

	public void didRemoveJob(String id) throws Exception {
	}

	public void willRemoveRange(long start, long end) throws Exception {
	}

	public void didRemoveRange(long start, long end) throws Exception {
	}
}

