# survey-service

This MicroService is for posting an survey and retrieving statistics. *question-service* MicroService must run to make
this MicroService fully functional as it sends request there for validation. However, this MicroService could also run
independently but posting survey might not be possible due to the mentioned validation. 

When this application runs it also creates data for easier testing.

Swagger UI URL: **http://localhost:8082/swagger-ui.html**