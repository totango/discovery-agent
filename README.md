# discovery-agent

Reactive [Consul](http://www.consul.io) client written in java 8

##Setup
maven
```xml

  <dependency>
    <groupId>com.totango</groupId>
    <artifactId>discovery-agent</artifactId>
    <version>0.2.0</version>
  </dependency>

```
gradle
```
'com.totango:discovery-agent:0.2.0'

```

## ConsulClient
ConsulClient is a HTTP client for [Consul](http://www.consul.io). You can either choose to work directly with ConsulClient or you can use the ServiceDiscovery or RoundRobinLoadBalancer which are targeted to more specific use cases. We suggest you read the whole README to fully understand which class to use when.  

By default ConsulClient connects to Consul agent on localhost and port 8500.

```java

ConsulClientFactory consulClientFactory = new ConsulClientFactory();
ConsulClient consulClient = consulClientFactory.client();

```

You can also provide a different host and port and wait timeout for consul blocking calls.

```java

ConsulClientFactory consulClientFactory = new ConsulClientFactory();
ConsulClient consulClient = consulClientFactory
  .host(server.getHostName())
  .port(server.getPort())
  .waitTimeInSec(waitTimeInSec)
  .client();
	
```

Get a list of healthy services

```java

ServiceRequest serviceRequest = Service.request()
  .forService(serviceName)
  .build();
			
Optional<ServiceGroup> response = consulClient.discoverService(serviceRequest);
	
```

You can then use the response to make calls to the service.

```java

if (response.isPresent() && response.get().size() > 0) {
	
	List<Service> services = response.get().getServices();
		
	// choose one from the list
	Service service = services.get(0);
		
	String url = String.format("http://%s:%d", service.getAddress(), service.getServicePort());
	Request request = new Request.Builder().url(url).build();
	Response response = httpClient.newCall(request).execute();
	...
} else {
	logger.log("Failed to discover a healthy " + serviceName);
}

```

You can get a healthy service with name and tag.

```java

String tag = "master";
	
ServiceRequest serviceRequest = Service.request()
	.forService(serviceName)
	.withTag(tag)
	.build();
		
```

You can also listen for service updates using the index of the last results. The call uses the MAX timeout for this call as described in the [Consul api](https://www.consul.io/docs/agent/http.html) which is 5 min.

```java

String tag = "master";
	
ServiceRequest serviceRequest = Service.request()
	.forService(serviceName)
	.lastUpdateIndex(index)
	.build();
		
```

ConsulClient can also call the Consul key-value api.
 
 ```java

Optional<Value> result = consulClient.keyValue(key);
	
```

To listen for key-value updates you need to provide the index of the last results. The call uses the MAX timeout for this call as described in the [Consul api](https://www.consul.io/docs/agent/http.html) which is 5 min.

 ```java

Optional<Value> result = consulClient.keyValue(key, index);
	
```

## DiscoveryService
Listen for updates is very useful but requires some work. This is why we have the DiscoveryService class.
DiscoveryService is a class that helps you register for service updates.
When you create a DiscoveryService instance you need to provide a ConsulClient, number of retries if the calls to the Consul Agent fail and function which will provide the delay in milliseconds between the retries.

Here is an example of a DiscoveryService with 10 retries. The retry function is a 1 - 10 series.
 
```java

DiscoveryService discoveryService = new DiscoveryService(consulClient, 10, i -> i);

```

The retry in this example is the number of retry to the power of 3.

```java

DiscoveryService discoveryService = new DiscoveryService(consulClient, 10, i -> (int)Math.pow(i, 3), TimeUnit.MILLISECONDS);

```

To subscribe for updates you need to provide the service name and an Action to be performed when there is an update.

```java

Subscription subscribe = discoveryService.subscribe("web-server", services -> {
  // do something with the returned List<Service>
});

```

If you want to take an action on errors you can provide an additional Action.

```java

Subscription subscribe = discoveryService.subscribe("web-server", services -> {
     // do something with the List<Service>
	},
	throwable -> {
		// do something with the Throwable
	});
  
```

DiscoveryService works with RxJava so you can subscribe to update using Subscriber, Observer and Action. 

## RoundRobinLoadBalancer
One of the things you can do when you have multiple instances that provides the same service is to call all of the instances in a round robin and this way to spread the load between them.
For this functionality we have The RoundRobinLoadBalancer.
The RoundRobinLoadBalancer uses a DiscoveryService in order to always know the most up to date service list. So when it is asked for the next endpoint it will provide a healthy one. To tell the balancer to start listening for updates you need to call the init() method.
 
```java

RoundRobinLoadBalancer balancer = new RoundRobinLoadBalancer(discoveryService, serviceName);
balancer.init();

```

To use the LoadBalancer you need to call the withNextEndpoint() method with a function that gets host and port. withNextEndpoint() is a generic method that can return any kind of Object you need to return in your case.

```java
	
balancer.withNextEndpoint((host, port) -> {
	String url = String.format("http://%s:%d", host, port);
	Request request = new Request.Builder().url(url).build();
	Response response = httpClient.newCall(request).execute();
	...
		
	return result;
});

```



## License

```

   Copyright 2015 Totango, Inc

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

```
