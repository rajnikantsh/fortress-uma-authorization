# fortress-uma-authorization

Create 2 Module client and server

Server Structure:

1 - Registering the site

2 - Protecting the resource

3 - Authorizes RPT

4 - checking access to resource


Client Structure:

1 - Generates Authorization URL

2 - Authenticate User through oxauth

3 - Generates AAT

4 - Generates RPT

5 - logout functionality

6 - UI for Check Access Page

Work Flow:

Run both Applications in order Server and Client. 

1. On Server Application hit https://localhost:9099/oxdId this will generate oxdId to location specified in application.properties file used for further api calls.

2. On Client Application hit https://localhost:9999 this will redirect user for oxauth authentication. After authentication user is redirected to checkAccess Page where user will show appropriate response as he is authorized to resource.

3. The Protected Resource Exists on Server https://localhost:9099/test/resource. which is protected to access from client application. based on authorization by RPT.

Steps:

1. Create a Rest Resource Api.

2. Create a web application to call the resource.

3. Install oxd on your local system where Rest Api and Web Application exists.

4. Install Gluu CE server.

5. Register the site for Api with OP host pointing to the location to Gluu server by following instructions on https://gluu.org/docs/oxd/protocol/#register-site. The url to register is "/oxdId" in oxdproducer api.

6. If the site is successfully registered then it will return oxdId.

7. After this protect the resource by passing oxdId and Resource List.

8. then install the fortress server by following instructions on https://github.com/apache/directory-fortress-core/blob/master/README-QUICKSTART-SLAPD.md

9. then create roles and users in fortress corresponding to users in gluu ce server.

10. Then write a python script and add it under Manage Custom Scripts under UMA Authorization Policies. 

11. Now add this newly create generated uma authorization policy to scopes created when we protected the resource in step 7.

12. Then generate authorization url as shown on oxd protocol page https://gluu.org/docs/oxd/protocol/#get-authorization-url. This generated when hitting the home page for web api.

13. After authorization url successfully returned from oxd then need to use that url to authenticate using username and password credentials and then user is redirected to loginsuccess page.

14. After getting logged in through gluu ce server. Generate AAT token for user information and uma protection. 

15. Then user is redirected to checkAccess page where an RPT token and ticket is generated for logged in user then it is determined based on RPT and ticket whether user is authorize to access the protected resource and output is shown based on that.

