# Time in Transit
## Introduction
  Time in Transit, TNT, is an api for obtaining number of days in transit, 
  pickup, delivery times, and an indication of whether or not UPS can 
  guarantee the delivery time for a shipment information including origin, 
  destination, and desired service. It expects a TimeInTransitRequest json 
  object in a request body and returns a TimeInTransitResponse in a response 
  body.  The TimeInTransitResponse would include a validationList in a case 
  of invalid shipping information.

## Run Procedure
- fork a copy of the repo in GitHub
- get a local copy of the project
```sh
git clone <forked repo url>
```
- build project
```sh
cd <project home>
mvn clean package
```
- update your information in src/main/resources/application.properties file

|Property Name|Description|
| :------: | :------: |
|```api.oauth.partner.client.id```| client id obtained in the onboarding process|
|```api.oauth.partner.secret```| client secret obtained in the onboarding process|
>    These are the properties in a section marked with "# UPS partner specific properties 
(change them!)" where you update with your client specific information like client id and secret.


- run com.ups.dap.TntApplication
```sh
java -jar tnt-x.x.x.jar             # ex. java -jar tnt-0.0.1-SNAPSHOT.jar
```
- check console output for application result


## Code Walk Through
There are 2 notable classes in this tutorial, namely com.ups.dap.app.AppConfig 
and com.ups.dap.app.TNTDemo.  The AppConfig class is a configuration class leveraging 
Spring injection to incorporate the property value from src/main/resources/application.properties 
file.  The TNTDemo is to illustrate how to use the TNT api.

```java
 String accessToken = Util.getAccessToken(appConfig, restTemplate);
```
> Get an access token via OAuth client_credentials grant type.

```java
 // Prepare TNT api access.
 final TntApi tntApi = initializeTntApi(restTemplate, appConfig.getTntBaseUrl(), accessToken);
```
> initializeTntApi function is to create a TNT api object with the base url and populated 
the HTTP Authorization header with the access token.
					
```java
	TimeInTransitRequest timeInTransitRequest = Util.createRequestFromJsonFile(entry.getKey(),
								entry.getValue().get(AppConfig.SCENARIO_PROPERTIES_JSON_FILE_NAME),
								TimeInTransitRequest.class,
								appConfig,
								Arrays.asList(new CreateRequestEnricher() {}));
```
> It reconstructs a TimeInTransitRequest object from a json file which includes 
origin/destination address and other required information.  In a typical application, 
a TimeInTransitRequest object would be created via a default constructor followed by 
a set of setter to populate the necessary attribute.

```java
	// Get a time in transit information for a particular shipment.
	TimeInTransitResponse timeInTransitResponse = tntApi.timeInTransit(appConfig.getTntVersion(),
								transId,
								appConfig.getTransactionSrc(),
								timeInTransitRequest);
```
> A TimeInTransitResponse will be returned from a backend server for a particular 
shipment specified in the TimeInTransitRequest object.  The TimeInTransitResponse 
would have a validation error if there is any as well as emsResponse, which 
includes a list of available UPS service and service detail, to a successful request.


### Data Schema 
- [Request Schema TimeInTransitRequest](../docs/TimeInTransitRequest.md)

- [Response Schema TimeInTransitResponse](../docs/TimeInTransitResponse.md)

### Sample Request/Response
- An international TimeInTransitRequest request for a shipment of "Non Document" 
package from Germany to United States
```json
{
  "originCountryCode": "DE",
  "originStateProvince": "",
  "originCityName": "",
  "originTownName": "",
  "originPostalCode": "10703",
  "destinationCountryCode": "US",
  "destinationStateProvince": "NH",
  "destinationCityName": "MANCHESTER",
  "destinationTownName": "",
  "destinationPostalCode": "03104",
  "weight": "10.5",
  "weightUnitOfMeasure": "LBS",
  "shipmentContentsValue": "10.5",
  "shipmentContentsCurrencyCode": "USD",
  "billType": "03",
  "shipDate": "2022-10-07",
  "shipTime": "",
  "residentialIndicator": "",
  "numberOfPackages": "1"
}
```


- An desmostic TimeInTransitRequest request
```json
{
  "originCountryCode": "US",
  "originPostalCode": "11023",
  "destinationCountryCode": "US",
  "destinationTownName": "",
  "destinationPostalCode": "03104",
  "weight": "10.5",
  "weightUnitOfMeasure": "LBS",
  "shipDate": "2022-10-07",
  "shipTime": "",
  "residentialIndicator": "",
  "numberOfPackages": "1"
}
```

- A successful TimeInTransitResponse response
```json
{
  "validationList": null,
  "destinationPickList": null,
  "originPickList": null,
  "emsResponse": {
    "shipDate": "2022-10-14",
    "shipTime": "13:0:36",
    "serviceLevel": "A",
    "billType": "02",
    "dutyType": null,
    "residentialIndicator": "02",
    "destinationCountryName": "UNITED STATES",
    "destinationCountryCode": "US",
    "destinationPostalCode": "03104",
    "destinationPostalCodeLow": "03104",
    "destinationPostalCodeHigh": "03104",
    "destinationStateProvince": "NH",
    "destinationCityName": "MANCHESTER",
    "originCountryName": "UNITED STATES",
    "originCountryCode": "US",
    "originPostalCode": "11023",
    "originPostalCodeLow": "11023",
    "originPostalCodeHigh": "11023",
    "originStateProvince": "NY",
    "originCityName": "GREAT NECK",
    "weight": "10.5",
    "weightUnitOfMeasure": "LBS",
    "shipmentContentsValue": "10.5",
    "shipmentContentsCurrencyCode": "USD",
    "guaranteeSuspended": false,
    "numberOfServices": 9,
    "services": [
      {
        "serviceLevel": "1DMS",
        "serviceLevelDescription": "UPS Next Day Air® Early",
        "shipDate": "2022-10-14",
        "deliveryDate": "2022-10-15",
        "commitTime": "09:00:00",
        "deliveryTime": "09:00:00",
        "deliveryDayOfWeek": "SAT",
        "nextDayPickupIndicator": "0",
        "saturdayPickupIndicator": "0",
        "saturdayDeliveryDate": null,
        "saturdayDeliveryTime": null,
        "serviceRemarksText": null,
        "guaranteeIndicator": "0",
        "totalTransitDays": 0,
        "businessTransitDays": 1,
        "restDaysCount": 0,
        "holidayCount": 0,
        "delayCount": 0,
        "pickupDate": "2022-10-14",
        "pickupTime": "19:00:00",
        "cstccutoffTime": "17:00:00",
        "poddate": null,
        "poddays": 0
      },
     ...
    }
  }
}
```

- A TimeInTransitResponse response with invalid shipDate
```json
{
  "validationList": {
    "invalidFieldList": [
      "ShipDate"
    ],
    "invalidFieldListCodes": [
      "1085"
    ],
    "destinationAmbiguous": false,
    "originAmbiguous": false
  },
  "destinationPickList": null,
  "originPickList": null,
  "emsResponse": null
}
```


### Related tutorial

|Name|Description|
| :------: | :------: |
|OAuth|[Get/refresh access token](http://localhost:8080/GitHub/link/placeHolder)|

### Glossary

|Term|Definition|
| :------: | :------: |
|billType | can be non-document, document, WWEF, Pallet.|
|transId| is a 32 character long unique identifier which is being used internally troubleshooting issue.|
|transactionSrc|is a maximum 512 charcter long string to identify an application sending a request; however, it is optional for partner application (suggested to use your company).|
|WWDT|World Wide Delivery Time is one of UPS api.|
|WWEF|World Wide Express Flight is a division of UPS for shipping.|

