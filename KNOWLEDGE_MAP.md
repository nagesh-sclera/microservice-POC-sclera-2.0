## POM: api-gateway
  - spring-boot-starter-parent
  - api-gateway
  - spring-cloud-dependencies
  - spring-cloud-starter-gateway
  - spring-cloud-starter-netflix-eureka-client
  - spring-boot-starter-test
  - spring-boot-maven-plugin
## CONFIG: application.yml (C:\Users\KNageshNayak\Desktop\Sclera-2.0_AMP\Sclera-2.0_AMP\api-gateway\src\main\resources\application.yml)
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false
      routes:
        - id: asset-service
          uri: lb://asset-service
          predicates:
            - Path=/api/devices/**

        - id: asset-onboard-service
          uri: lb://asset-onboard-service
          predicates:
            - Path=/api/onboard/**

        - id: asset-onboarding-service
          uri: lb://asset-onboarding-servic
## POM: asset-onboarding-ai-service
  - spring-boot-starter-parent
  - asset-onboarding-ai-service
  - spring-cloud-dependencies
  - spring-boot-starter-web
  - spring-boot-starter-webflux
  - spring-boot-starter-data-jpa
  - spring-boot-starter-validation
  - mysql-connector-j
  - lombok
  - spring-boot-starter-test
  - spring-cloud-starter-netflix-eureka-client
  - spring-cloud-starter-loadbalancer
  - spring-boot-maven-plugin
## SERVICE: AssetOnboardingServiceClient.java
  public List<AssetDTO> getUnmappedAssets(String vdmsId, int page, int size)
  public void createMapping(String deviceId, String assetId, Integer score, String vdmsId)
## SERVICE: AssetServiceClient.java
  public DeviceDTO getDevice(String deviceId, String vdmsId)
  public List<DeviceDTO> getDevicesWithAiEnabled(String vdmsId, int page, int size)
## CONTROLLER: AIController.java
  RequestMapping
  PostMapping
  PostMapping
  public ResponseEntity<MatchResultDTO> suggest(
            @Valid @RequestBody AISuggestionRequestDTO request)
  public ResponseEntity<List<MatchResultDTO>> batchMatch(@RequestParam String vdmsId)
## SERVICE: AIMatchingService.java
  public MatchResultDTO suggestMatches(String deviceId, String vdmsId)
  public List<MatchResultDTO> batchMatch(String vdmsId)
## CONFIG: application.yml (C:\Users\KNageshNayak\Desktop\Sclera-2.0_AMP\Sclera-2.0_AMP\asset-onboarding-ai-service\src\main\resources\application.yml)
server:
  port: 8084

spring:
  application:
    name: asset-onboarding-ai-service
  datasource:
    url: jdbc:mysql://localhost:3306/vdms
    username: root
    password: mypass123
    hikari:
      maximum-pool-size: 20
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

services:
  asset-service:
    base-url: lb://asset-service
  asset-onboarding-service:
    base-url: lb://asset-onboarding-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/e
## POM: asset-onboarding-service
  - spring-boot-starter-parent
  - asset-onboarding-service
  - spring-cloud-dependencies
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-webflux
  - spring-boot-starter-validation
  - mysql-connector-j
  - lombok
  - spring-boot-starter-test
  - spring-cloud-starter-netflix-eureka-client
  - spring-cloud-starter-loadbalancer
  - spring-boot-maven-plugin
## SERVICE: AssetServiceClient.java
  public void verifyDevice(String deviceId, String vdmsId)
  public void patchAssetMatchStatus(String deviceId, Integer matchStatus, String vdmsId, String username)
## CONTROLLER: AssetOnboardingController.java
  RequestMapping
  GetMapping
  GetMapping
  PostMapping
  DeleteMapping
  PostMapping
  DeleteMapping
  GetMapping
  GetMapping
  public ResponseEntity<List<AssetDTO>> list(
            @RequestParam String vdmsId,
            @RequestParam(required 
  public ResponseEntity<List<AssetDTO>> unmapped(
            @RequestParam String vdmsId,
            @RequestParam(defau
  public ResponseEntity<AssetDTO> upsert(@RequestBody AssetDTO dto)
  public ResponseEntity<Void> delete(@PathVariable String id)
  public ResponseEntity<AssetDeviceMapping> createMapping(
            @Valid @RequestBody MappingDTO dto,
            @Re
  public ResponseEntity<Void> deleteMapping(
            @PathVariable String mappingId,
            @RequestParam String 
  public ResponseEntity<List<AssetDeviceMapping>> getMappings(@RequestParam String deviceId)
  public ResponseEntity<List<AssetField>> getFields()
## SERVICE: AssetOnboardingService.java
  public List<AssetDTO> getPaginated(String vdmsId, String importType,
                                       String searc
  public List<AssetDTO> getUnmapped(String vdmsId, int page, int size)
  public AssetDTO upsert(AssetDTO dto)
  public void delete(String id)
  public AssetDeviceMapping createMapping(MappingDTO dto, String vdmsId)
  public void deleteMapping(String mappingId, String vdmsId)
  public List<AssetDeviceMapping> getMappingsByDevice(String deviceId)
  public List<AssetField> getAllFields()
## CONFIG: application.yml (C:\Users\KNageshNayak\Desktop\Sclera-2.0_AMP\Sclera-2.0_AMP\asset-onboarding-service\src\main\resources\application.yml)
server:
  port: 8083

spring:
  application:
    name: asset-onboarding-service
  datasource:
    url: jdbc:mysql://localhost:3306/vdms
    username: root
    password: mypass123
    hikari:
      maximum-pool-size: 30
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

services:
  asset-service:
    base-url: lb://asset-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

## POM: asset-onboard-service
  - spring-boot-starter-parent
  - asset-onboard-service
  - spring-cloud-dependencies
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-webflux
  - spring-boot-starter-validation
  - mysql-connector-j
  - lombok
  - spring-boot-starter-test
  - spring-cloud-starter-netflix-eureka-client
  - spring-cloud-starter-loadbalancer
  - spring-boot-maven-plugin
## SERVICE: AssetServiceClient.java
  public DeviceResponseDTO getDevice(String deviceId, String vdmsId)
  public void patchOnboardStatus(String deviceId, Integer onboardStatus)
## CONTROLLER: AssetOnboardController.java
  RequestMapping
  PostMapping
  PutMapping
  GetMapping
  GetMapping
  public ResponseEntity<Void> upsert(
            @RequestParam String vdmsId,
            @RequestBody UpsertOnboardReque
  public ResponseEntity<Void> updateData(
            @PathVariable String deviceId,
            @RequestParam String  vdm
  public ResponseEntity<DeviceOnboardStatus> getStatus(
            @PathVariable String deviceId,
            @RequestPar
  public ResponseEntity<Set<String>> getAssignees(@RequestParam String vdmsId)
## SERVICE: AssetOnboardService.java
  public void upsertOnboardAssets(String vdmsId, UpsertOnboardRequestDTO request)
  public void updateOnboardData(String vdmsId, String deviceId,
                                  DeviceOnboardStatusDTO d
  public DeviceOnboardStatus getOnboardStatus(String deviceId)
  public Set<String> getAssignees(String vdmsId)
## CONFIG: application.yml (C:\Users\KNageshNayak\Desktop\Sclera-2.0_AMP\Sclera-2.0_AMP\asset-onboard-service\src\main\resources\application.yml)
server:
  port: 8082

spring:
  application:
    name: asset-onboard-service
  datasource:
    url: jdbc:mysql://localhost:3306/vdms
    username: root
    password: mypass123
    hikari:
      maximum-pool-size: 30
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

services:
  asset-service:
    base-url: lb://asset-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

## POM: asset-service
  - spring-boot-starter-parent
  - asset-service
  - spring-cloud-dependencies
  - spring-cloud-starter-netflix-eureka-client
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-webflux
  - spring-boot-starter-validation
  - mysql-connector-j
  - lombok
  - spring-boot-starter-test
  - spring-boot-maven-plugin
## CONTROLLER: DeviceController.java
  RequestMapping
  GetMapping
  GetMapping
  PostMapping
  PutMapping
  DeleteMapping
  GetMapping
  public ResponseEntity<DeviceDTO> getById(
            @PathVariable String id,
            @RequestParam String vdmsId)
  public ResponseEntity<PageResponse<DeviceDTO>> filter(
            @RequestParam String  vdmsId,
            @RequestPar
  public ResponseEntity<DeviceDTO> create(
            @Valid @RequestBody DeviceDTO dto,
            @RequestParam String
  public ResponseEntity<DeviceDTO> update(
            @PathVariable String id,
            @Valid @RequestBody DeviceDTO 
  public ResponseEntity<Void> patchOnboardStatus(
            @PathVariable String id,
            @Valid @RequestBody Onb
  public ResponseEntity<Void> delete(@PathVariable String id)
## SERVICE: DeviceService.java
  public DeviceDTO getById(String id, String vdmsId)
  public PageResponse<DeviceDTO> filter(String vdmsId, String dockerId, String search,
                                   
  public DeviceDTO create(DeviceDTO dto, String username)
  public DeviceDTO update(String id, DeviceDTO dto, String username)
  public void patchOnboardStatus(String id, OnboardStatusPatchDTO patch)
  public void delete(String id)
## CONFIG: application.yml (C:\Users\KNageshNayak\Desktop\Sclera-2.0_AMP\Sclera-2.0_AMP\asset-service\src\main\resources\application.yml)
server:
  port: 8085

spring:
  application:
    name: asset-service
  datasource:
    url: jdbc:mysql://localhost:3306/vdms
    username: root
    password: mypass123
    hikari:
      maximum-pool-size: 50
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate.dialect: org.hibernate.dialect.MySQL8Dialect

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

logging:
  level:
    io
## POM: discovery-service
  - spring-boot-starter-parent
  - discovery-service
  - spring-cloud-dependencies
  - spring-cloud-starter-netflix-eureka-server
  - spring-boot-starter-test
  - spring-boot-maven-plugin
## CONFIG: application.yml (C:\Users\KNageshNayak\Desktop\Sclera-2.0_AMP\Sclera-2.0_AMP\discovery-service\src\main\resources\application.yml)
server:
  port: 8761

spring:
  application:
    name: discovery-service

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
  server:
    wait-time-in-ms-when-sync-empty: 0

logging:
  level:
    io.sclera: INFO
    com.netflix.eureka: WARN
    com.netflix.discovery: WARN

## POM: Sclera-2.0_AMP
  - sclera-amp