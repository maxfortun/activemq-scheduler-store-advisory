# activemq-scheduler-store-advisory


### Configuring broker
```xml
	<jobSchedulerStore>
		<bean xmlns="http://www.springframework.org/schema/beans" id="jobSchedulerStore" class="org.apache.activemq.broker.scheduler.advisory.AdvisoryJobSchedulerStore">
			<constructor-arg ref="broker" />
			<constructor-arg>
				<bean xmlns="http://www.springframework.org/schema/beans" id="jobSchedulerStoreDelegate" class="org.apache.activemq.store.kahadb.scheduler.JobSchedulerStoreImpl">
					<property name="directory" value="${activemq.data}/scheduler" />
				</bean>
			</constructor-arg>
		</bean>
	</jobSchedulerStore>
```

