![alt text](https://github.com/orubel/logos/blob/master/beapi_logo_large.png)
# Beapi Spring Boot Starter
**( public version will be released under a [Reciprocal Public License]([https://en.wikipedia.org/wiki/Cross-cutting_concern](https://en.wikipedia.org/wiki/Reciprocal_Public_License)) )**

**Beapi abstracts all RULES for API endpoints** so they can be **shared/syncronized with all services** in a distributed API architecture **without requiring restarts of all servers** to do so. 
In current architectures, DATA for endpoints is bound to FUNCTIONALITY ( see [Cross Cutting Concern](https://en.wikipedia.org/wiki/Cross-cutting_concern) ) through things like 'annotations'; this makes it so that you have to duplicate this DATA everywhere (see OpenApi) as it is hardcoded into functionality.

The thing that changes from server to server is VERSION and FUNCTIONALITY... **NOT ENDPOINT RULES**. How you call the data (and how the data is returned) is related to VERSION; both the VERSION of the CODE and the API. In other words, you may want to call your API several different ways with each FUNCTIONAL VERSION OF THE CODE and **without having to release a several different versions of the code on a several different servers**. 

If the API Object remains the same, we merely have to parse it differently using different rules. Thus we merely need an abstracted set of rules for each different version and one common codebase (until the codebase changes).

By abstracting it into an externally loadable file, things like ROLES for endpoints can be easily adjusted without requiring a restart of the service. Plus using functionality like webhooks, one can synchronize all services from a MASTER server. This allows for changes to API endpoint DATA on a distributed API architecture without restarting services.

Additionally, this also allow for the introduction of addition patterns like automated batching and 'API Chaining(tm)'

**Springboot Version** - 2.6.2 (or greater)

**JVM** - 1.8 (or greater)

**Build (Gradle 7)** 
```
gradle --stop;gradle clean;gradle build --stacktrace  --refresh-dependencies
```
**Run Demo Application(Gradle 7)** 
- Change to demo-application and run the following from a shell:
```
cd demo-application
java -jar build/libs/demo-application-{appVersion}.jar
```

**Getting a Token and calling your api** - 

This will get you your BEARER token to use in your calls/app:
```
curl -v -H "Content-Type: application/json" -X POST -d '{"username":"admin","password":"@6m!nP@s5"}' http://localhost:8080/authenticate
```

Then call your api normally:
```
curl -v -H "Content-Type: application/json" -H "Authorization: Bearer {your_token_here}" --request GET "http://localhost:8080/v{appVersion}/user/show/5"
```


**Configuration Files** - https://github.com/orubel/spring-boot-starter-beapi-config (Note : Move these into your 'iostateDir' location as found in your demo-application/src/main/resources/beapi_api.yaml file)


**OLD Documentation (new documentation coming soon)** - [https://orubel.github.io/spring-boot-starter-beapi/](https://orubel.github.io/spring-boot-starter-beapi/)

## 0.5-PUBLIC-RELEASE (release Sept/2022)
 - API AUTOMATION
    - localized api caching (do first for benchmarking) 
    - automated versioning for urlmapping
    - automated batching
    - api chaining&reg; 
      - 'blankchain'
      - 'prechain'
      - 'postchain'
    - separated I/O State for sharing/synchronizing of all endpoint rules
    - built-in performance tracing (partial)
  - SECURITY
    - network groups
    - role checking
    - automated resolution for [API3:2019 Excessive Data Exposure](https://github.com/OWASP/API-Security/blob/master/2019/en/src/0xa3-excessive-data-exposure.md).
  - DEMO APPLICATION
    - JWT token creation and authentication 
   


 ## 0.6-PUBLIC-RELEASE (release Feb/2023)
  - API AUTOMATION
    - automated apidocs 
    - automated webhooks
    - rate limiting
    - automated CORS; uses whitelisted IP 'networkGrps' (see previous implementation)
 - BOOTSTRAPPING
    - automated I/O State generation
    - functional test scaffolding
    - automated controller scaffolding
 - REPORTING
    - stats reporting
 - 3RD PARTY TOOLS
    - properties file/service/endpoint for 3rd party/local oauth implementations 
 - UI
    - UI/UX tools (maybe)
      - build out demo application as an SDK(???) 


# Q&A
- **Why Not bind the endpoints to the 'MODEL'?**
    - API return data may be a mixture of two tables (or more), a file, a link to another api call, etc. By binding to your model, you are asking it to become the 'business logic','communication logic' as well as 'data handling'. This breaks rules of AOP, Separation of Control' and over complicates your build and testing. This also makes your entire environment slower and harder to scale.
- **Why require a cache?**
    - It is complex to build dynamic properties and properties that represent MULTIPLE MAPS in a SINGLE PROPERTIES FILE. Cache is hence the simpler solution. Also many developers do not understand proper caching techniques (with API's).
- **Why not just use @RequestMapping, @GetMapping, etc?**
    - The RequestMapping annotations create a HARD CODED 'rules' to functionality; you cannot update these 'rules' while the server is running and these cannot be synchronized across multiple servers. Updating endpoint RULES is often necessary but requires a restart because these bindings are hardcoded. 
    - By abstracting this data from the functionality, we are better able to make LIVE CHANGES TO ENDPOINT RULES (when functionality hasn't changed). So for example if we want to disable privileges or change how a certain ROLE accesses endpoints, we can do that on the fly without taking down servers.
- **Why can't 'API Chaining(R)' have more than ONE UNSAFE method in the chain?**
    - FIRST, You can only send ONE METHOD with a chain; you cannot send a PUT and POST method in the same call. But you can just default every other call to a SAFE call (ie GET) as long as client has AUTHORITY to the endpoint. SECOND, since it can only have one UNSAFE METHOD, you can only send ONE DATASET. We made it to be as simple as possible while encompassing the most commonly used calls thus simplifying the processing and the client call.
- **Isn't it BAD to send form data with a GET request? I thought you could only send URI encoded data??**
    - Per W3C guidelines : 'A client SHOULD NOT generate content in a GET request unless it is made directly to an origin server that has previously indicated, in or out of band, that such a request has a purpose and will be adequately supported'. API Chaining(tm) is that direct connection with purpose. It provides the necessary backend checks and limits what can be sent.
   

