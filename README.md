# CachingSupplier
A package to control "perfect storm" over-loading of shared resources

## The problem - #1
Every time you restart server "A", every client detects that the server was bounced and re-initializes common datastructures
by invoking the services on the newly bounced server.  This "perfect storm" of service requests happens in a short time
window and overwhelms the server.  This can trigger a backlog of requests with exponential service times.  For some services,
a "perfect storm" of requests can also cause a backlog of upstream requests to other resources like upstream REST services
or databases.  In extreme cases, overwhelmed services can slow down entire systems and service pipelines.

## The problem - #2
Application "A" has always displayed a summary data view on it's main application window.  Our service provides a REST
endpoint to provide this summary data.  The DB query to produce this data is complex, but has always taken under 1 second.
The usage of application "A" has now grown into the triple digits.  There are now so many requests per minute to the service,
that database queries are taking 3-10 seconds to return results.  Response times from the endpoint are slow enough that users
are complaining about page load times.  In response, the application team has added a local cache of the summary data with
an async loader to update the data when it arrives.  The page load times are reasonable, but users are complaining of stale
data.  So in response, users are reloading the application page continuously, with the F5 key, until they see a data update.
Service response times are skyrocketing.  Users are mad.  The application and service developer teams are pointing fingers
at each other.  What a nightmare.  (Why did we decide to not use eventing and web sockets?)

# The proposal
**What if a service could execute just one upstream request or query and share the results with many client requests?**

The service could receive a first request, start processing it, collect all requests that are received before the 
first request completes, and return the same results to ***all*** waiting requests.
In addition, the result could have a "lifetime" in a cache and could be returned to additional requests until they are
deemed "stale".

# Assumptions

* **The service to be cached is providing results that are from a non-parameterized request or query.**
   
  The premise is that the cached results are shared by many requests.  Parameterization could make every request unique. 
  Sharing of results is still possible if the parameterization has a limited set of allowed values.  Each combination of 
  parametrized arguments would become a unique cached result set.

* **The cached result object must be "immutable".**

  With many requests sharing a single cached result, any modification to the result would change it for all requests.
  At best, changing results could be non-deterministic. At worst, the results could be corrupted.
  Within this package are utility classes for decorating result classes with immutable adapter classes. 

* **The use of the cache can produce request results that do ***NOT*** contain data updates that may have changed ***BEFORE*** a specific request was made.**

  Cached result objects can be "stale".  They can be from upstream queries that do not encompass changes recently made to the upstream system.
  This package has configuration settings to control the caching staleness time window, but it will often be a tradeoff between performance
  and staleness.  Even if the caching TTL configuration is set to the minimal value (0 = turns off caching), there are scenarios
  where upstream data changes are not captured until another request is made.  A later section of this document will highlight the issue and a few scnearios. 

* **This package (CachingSupplier) is a Tactical Solution, not a Strategic one.**

  Polling architectures and designs will always have scaling problems.  This package can supply a remedy for specific types of polling, but
  it is not a universal strategic solution.  To avoid the issues of polling for data, developers should investigate event and message based
  designs and solutions.

# The Implementation

The primary class in this package is the CachingSupplier class.  This class implements the Supplier interface:

```
...
package java.util.function;
...

@FunctionalInterface
public interface Supplier<T> {

/**
  * Gets a result.
  *
  * @return a result
 */
     T get();
 }
```

The design is for the CachingSupplier class to act as a caching Decorator around an existing Supplier.

Simplified usage:

```
// Creation is done once
CachingSupplier<String> cs = new CachingSupplier<>(() -> {
    String jsonResult;
    
    // do a lookup of a result object
    jsonResult = null;  // jsonResult = ...
    
    return jsonResult;
});

...

// Fetching data
String jsonResult = cs.get();
```

If you want to override the default configuration settings, then use this constructor:

```
public CachingSupplier(String supplierId, SupplierConfig config, Supplier<T> supplier)
```

It is highly recommended that developers use the convenience class: CachingSupplierManager.  This class tracks multiple CachingSupplier
instances by client created Ids.  The Ids can be used to query caching efficiency statistics.

```
CachingSupplierManager<String> manager = new CachingSupplierManager<>();
manager.registerSupplier("cachingsupplier1", new Supplier<>(() -> {
    String jsonResult;

    // do a lookup of a result object
    jsonResult = null;  // result = ...

    return jsonResult;
});

...

// Fetching data
String jsonResult = manager.get("cachingsupplier1");

// Stats
System.out.println("\n" + manager.getStatsJson("cachingsupplier1"));
```

Please see the unit tests for more coding examples.

........








