![alt text](https://github.com/orubel/logos/blob/master/beapi_logo_large.png)
# Beapi Spring Boot Starter

### Current Stable Version : 0.6.0-STABLE

### Springboot Version : 2.6.2 (or greater)

### JVM : 17 (has previously been tested with as low as 1.8)
---

**License** - [Reciprocal Public License](https://en.wikipedia.org/wiki/Reciprocal_Public_License)

**Documentation** - [https://beapi-io.github.io/spring-boot-starter-beapi/](https://beapi-io.github.io/spring-boot-starter-beapi/)

**Demo Application** - [https://github.com/Beapi-io/beapi-java-demo](https://github.com/Beapi-io/beapi-java-demo)

**Configuration Files** - https://github.com/orubel/spring-boot-starter-beapi-config (Note : Move these into your 'iostateDir' location as found in your demo-application/src/main/resources/beapi_api.yaml file)

---

**Beapi abstracts all RULES for API endpoints** so they can be **shared/syncronized with all services** in a distributed API architecture **without requiring restarts of all servers** to do so.

In current architectures, DATA for endpoints is bound to FUNCTIONALITY ( see [Cross Cutting Concern](https://en.wikipedia.org/wiki/Cross-cutting_concern) ) through things like 'annotations'; this makes it so that you have to **duplicate this DATA everywhere**(see OpenApi) as said data is hardcoded into functionality via those annotations. And UNFORTUNATE. existing tools (like OpenAPI) [refuse to copy/include RBAC rules](https://www.flickr.com/photos/orubel/50695726007/in/dateposted-public/) making it so other services become insecure when using tools like OpenApi.

By abstracting it into an externally **reloadable file**, things like [RBAC](https://en.wikipedia.org/wiki/Role-based_access_control)/'endpoint ROLES' can be easily adjusted without requiring a restart of services. Plus using functionality like webhooks, one can synchronize all services from a MASTER server. This allows for changes to API endpoint DATA on a distributed API architecture without restarting services.

Additionally, this creates new patterns like [automated batching](https://beapi-io.github.io/spring-boot-starter-beapi/advanced.html#section-1) and '[Api Chaining&reg;](https://beapi-io.github.io/spring-boot-starter-beapi/advanced.html#section-3) '

---

## Functionality
- **Security**
  - Automated [Role Based Access Control (RBAC)](https://digitalguardian.com/blog/what-role-based-access-control-rbac-examples-benefits-and-more)
  - Full OWASP API Security Compliance
  - Automated RBAC+OWASP checks on request/response parameters per endpoint
  - Automated RBAC+OWASP checks on cache
- **Configuration Management**
  - Reloadable API RULES (ie connectors)
  - Automated synchronization of API RULES w/ services
- **Automation**
  - Automated Role-based Batching
  - API Chaining(R)
  - Autogenerated Role-based API Docs
  - Return type inference (Accept)
  - Connector Scaffolding (coming in 0.7.X)
  - Automated Role-based Webhooks (coming soon)

---

**Gradle Implementation**
```
repositories {
	mavenLocal()
	mavenCentral()
	maven {
		url 'https://s01.oss.sonatype.org/content/repositories/releases'
	}
}

...

dependencies {
    ...
    implementation 'io.beapi:spring-boot-starter-beapi:0.6.0-STABLE'
    ...
}
 ```

**Getting a Token and calling your api** - 

Using the [Java-demo implementation](https://github.com/Beapi-io/beapi-java-demo), this will get you your BEARER token to use in your calls/app:
```
curl -v -H "Content-Type: application/json" -X POST -d '{"username":"admin","password":"@6m!nP@s5"}' http://localhost:8080/authenticate
```

Then call your api normally:
```
curl -v -H "Content-Type: application/json" -H "Authorization: Bearer {your_token_here}" --request GET "http://localhost:8080/v{appVersion}/user/show/5"
```
---

# Q&A
- **Why Not bind the endpoints to the 'MODEL'(ie GraphQL)?**
    - First 'resource' in API spec (per Roy Fielding) is used in the sense of 'Uniform Resource Indicator' (ie URI). This points to a service or business logic which then calls data which may be a mixture of two tables (ie JOIN), a file, a link to another api, etc. By binding to your model, you are asking it to become the 'business logic','communication logic' as well as 'data handling' and not only breaks the API spec/standard but limits what your API can return. This breaks rules of AOP, Separation of Control', OWASP API security and over complicates your build and testing. This also makes your entire environment slower and harder to scale. So yeah... bad idea in general.
- **Why require a cache?**
    - Caching is actually listed as part of the API requirements in Roy Fieldings dissertation; You cannot name an professional API implementation that does not use a cache. Unfortunately, many developers do not understand proper caching techniques (with API's). So we took that as an opportunity to handle that for you. You're welcome.
- **Why not just use @RequestMapping, @GetMapping, etc?**
    - The RequestMapping annotations create HARD CODED 'rules' to functionality; you cannot update/synchronize these 'rules' across your shared servers while the server is running. This breaks the rules for configuration management when applying rules for the shared state across your entire architecture. We abstract the rules away from the business logic so that they CAN be updated and shared with all running services WITHOUT requiring restarts. 
    - By abstracting this data from the functionality, we are better able to make LIVE CHANGES TO ENDPOINT RULES **when functionality hasn't changed**. So for example if we want to disable privileges or change how a certain ROLE accesses endpoints, we can do that on the fly without taking down servers.
- **Why can't 'API Chaining(R)' have more than ONE UNSAFE method in the chain?**
    - FIRST, You can only send ONE METHOD with a chain; you cannot send a PUT and POST method in the same call. But you can just default every other call to a SAFE call (ie GET) as long as client has AUTHORITY to the endpoint. SECOND, since it can only have one UNSAFE METHOD, you can only send ONE DATASET. We made it to be as simple as possible while encompassing the most commonly used calls thus simplifying the processing and the client call.
- **Isn't it BAD to send form data with a GET request? I thought you could only send URI encoded data??**
    - Per W3C guidelines : 'A client SHOULD NOT generate content in a GET request unless it is made directly to an origin server that has previously indicated, in or out of band, that such a request has a purpose and will be adequately supported'. API Chaining(tm) is that direct connection with purpose. It provides the necessary backend checks and limits what can be sent.

   

