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
package org.apache.activemq.broker.scheduler;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.BrokerServiceAware;

import org.apache.activemq.broker.scheduler.memory.InMemoryJob;
import org.apache.activemq.broker.scheduler.memory.InMemoryJobFactory;

import org.apache.activemq.broker.scheduler.SchedulerBroker;

import org.apache.activemq.util.ByteSequence;
import org.apache.activemq.util.LongSequenceGenerator;

import org.apache.activemq.wireformat.WireFormat;
import org.apache.activemq.openwire.OpenWireFormat;

import org.apache.activemq.ScheduledMessage;

import org.apache.activemq.command.Message;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.MessageId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SchedulerUtils implements BrokerServiceAware {
	private static final Logger LOG = LoggerFactory.getLogger(SchedulerUtils.class);
	private static final LongSequenceGenerator longGenerator = new LongSequenceGenerator();

	private BrokerService brokerService;
	private WireFormat wireFormat;
	private JobFactory jobFactory;

	public SchedulerUtils(BrokerService brokerService) {
		this(brokerService, new InMemoryJobFactory(brokerService));
	}

	public SchedulerUtils(BrokerService brokerService, JobFactory jobFactory) {
		setBrokerService(brokerService);

		wireFormat = new OpenWireFormat();
		wireFormat.setVersion(brokerService.getStoreOpenWireVersion());

		this.jobFactory = jobFactory;
	}

	public BrokerService getBrokerService() {
		return brokerService;
	}   

	public void setBrokerService(BrokerService brokerService) {
		this.brokerService = brokerService;
	}   

	public Message toMessage(Job job) throws Exception {
		ByteSequence payload = new ByteSequence(job.getPayload());
		return toMessage(job.getJobId(), payload);
	}

	public Message createMessage() throws Exception {
		Message message = new ActiveMQMessage();
		return message;
	}

	public Message createMessage(String id) throws Exception {
		Message message = createMessage();
		message.setProperty(ScheduledMessage.AMQ_SCHEDULED_ID, id);
		return message;
	}

	public Message createMessage(long start, long end) throws Exception {
		Message message = createMessage();
		message.setProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION_START_TIME, start);
		message.setProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION_END_TIME, end);
		return message;
	}

	public ByteSequence toByteSequence(Message message) throws Exception {
		return wireFormat.marshal(message);
	}
	public Message toMessage(ByteSequence payload) throws Exception {
		Message message = (Message)wireFormat.unmarshal(payload);

		return message;
	}
	public Message copyProperties(Message toMessageJob, Message fromMessage) throws Exception {
		for (Map.Entry<String,Object> entry : fromMessage.getProperties().entrySet()) {
			toMessageJob.setProperty(entry.getKey(), entry.getValue());
		}
		return toMessageJob;
	}
	public Message toMessage(String id, ByteSequence payload) throws Exception {
		Message message = toMessage(payload);
		// TODO: check if message already has a job id and compare it to the actual job id. Should not really conflict.
		message.setProperty(ScheduledMessage.AMQ_SCHEDULED_ID, id);
		return message;
	}

	public InMemoryJob toJob(Message message) throws Exception {
        String id = (String)message.getProperty(ScheduledMessage.AMQ_SCHEDULED_ID);
		if(id == null) {
			id = new MessageId(message.getMessageId().getProducerId(), longGenerator.getNextSequenceId()).toString();
		}

        InMemoryJob job = jobFactory.createJob(id);

		long startTime = System.currentTimeMillis();
        // round startTime - so we can schedule more jobs at the same time
        startTime = ((startTime + 500) / 500) * 500;

        job.setStart(startTime);

		ByteSequence payload = wireFormat.marshal(message);
        job.setPayload(payload.getData());
		return job;
	}

	public InMemoryJob toJob(Message message, String cronEntry) throws Exception {
		InMemoryJob job = toJob(message);
        job.setCronEntry(cronEntry);
        job.setNextTime(CronParser.getNextScheduledTime(cronEntry, job.getStart()));
		return job;
	}

	public InMemoryJob toJob(Message message, long delay, long period, int repeat) throws Exception {
		InMemoryJob job = toJob(message);
		long executionTime = job.getStart();

        if (delay > 0) {
            executionTime += delay;
        } else {
            executionTime += period;
        }

        job.setDelay(delay);
        job.setPeriod(period);
        job.setRepeat(repeat);

		return job;
	}

}
