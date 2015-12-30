package com.totango.discoveryagent;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import com.totango.discoveryagent.model.Service;
import com.totango.discoveryagent.model.ServiceGroup;

import static com.totango.discoveryagent.ServiceRequest.request;

public class DiscoveryService {

  private static final Logger Logger =  LoggerFactory.getLogger(DiscoveryService.class);
  
  private final ConsulClient consulClient;

  private final Map<String, ServiceGroup> serviceGroupsMap = new ConcurrentHashMap<>();

  private final Map<String, Observable<List<Service>>> observableMap = new ConcurrentHashMap<>();

  private int retry;

  private Func1<Integer, Integer> delayFunc;
  
  public DiscoveryService(ConsulClient consulClient, int retry, Func1<Integer, Integer> delayFunc) {
    this.consulClient = consulClient;
    this.retry = retry;
    this.delayFunc = delayFunc;
  }

  public List<Service> getServices(String serviceName) throws IOException {
    ServiceGroup serviceGroup = serviceGroupsMap.get(serviceName);
    if (serviceGroup != null) {
      return serviceGroup.getServices();
    }
    
    ServiceRequest serviceRequest = request().forService(serviceName).build();
    return consulClient.discoverService(serviceRequest)
      .map(sGroup -> {
        serviceGroupsMap.put(serviceName, sGroup);
        return sGroup.getServices();
      })
      .orElse(Collections.emptyList());
  }

  public Subscription subscribe(String serviceName, Action1<? super List<Service>> onNext) {
    Observable<List<Service>> serviceObservable = createOrGetObservable(serviceName);
    return serviceObservable.subscribe(onNext);
  }
  
  public Subscription subscribe(String serviceName, Action1<? super List<Service>> onNext,
      Action1<Throwable> onError) {
    Observable<List<Service>> serviceObservable = createOrGetObservable(serviceName);
    return serviceObservable.subscribe(onNext, onError);
  }
  
  public Subscription subscribe(String serviceName, final Observer<? super List<Service>> observer) {
    Observable<List<Service>> serviceObservable = createOrGetObservable(serviceName);
    return serviceObservable.subscribe(observer);
  }
  
  public Subscription subscribe(String serviceName, Subscriber<? super List<Service>> subscriber) {
    Observable<List<Service>> serviceObservable = createOrGetObservable(serviceName);
    return serviceObservable.subscribe(subscriber);
  }
  
  private Observable<List<Service>> createOrGetObservable(String serviceName) {
    Observable<List<Service>> observable = observableMap.get(serviceName);
    if (observable == null) {
      observable = createServiceObservable(serviceName);
      observableMap.put(serviceName, observable);
      observable = observable.subscribeOn(Schedulers.newThread());
    }
    return observable;
  }

  private Observable<List<Service>> createServiceObservable(String serviceName) {
    //  Casting is needed because of a known bug in javac
    Observable<List<Service>> observable = Observable.create((OnSubscribe<List<Service>>)(subscriber) -> {

      ServiceGroup lastServiceGroup = null;
      
      while(true) {
        try {
          String serviceIndex = getServiceIndex(serviceName).orElse("0");
          ServiceRequest serviceRequest = request()
              .forService(serviceName)
              .lastUpdateIndex(serviceIndex).build();
          
          Optional<ServiceGroup> serviceGroupOpt = consulClient.discoverService(serviceRequest);
          
          serviceGroupOpt.ifPresent(sGroup -> {            
            if (lastServiceGroup == null || !lastServiceGroup.equals(sGroup)) {
              serviceGroupsMap.put(serviceName, sGroup);
              subscriber.onNext(sGroup.getServices());
            }
          });
        } catch (Throwable t) {
          subscriber.onError(t);
        }
      }
    });
    
    return observable.retryWhen(attempts -> {
      return attempts.zipWith(Observable.range(1, retry + 1), (notification, i) -> {
            Logger.warn("Failed to listen for \"{}\" service updates. reason: {}",
                serviceName, notification.getThrowable().toString());
            return i;
          })
          .map(delayFunc)
          .flatMap(delay -> {
            Logger.warn("Delay next call to discover \"{}\" service by {} second(s)", serviceName, delay);
            return Observable.timer(delay.intValue(), TimeUnit.SECONDS);
          });
    });
  }

  private Optional<String> getServiceIndex(String name) {
    ServiceGroup serviceGroup = serviceGroupsMap.get(name);
    if (serviceGroup == null) {
      return Optional.empty();
    } else {
      return serviceGroup.getIndex();
    }
  }
  
}
