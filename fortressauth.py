# oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
# Copyright (c) 2016, Gluu


from org.jboss.seam import Component
from org.jboss.seam.security import Identity
from org.xdi.model.custom.script.type.auth import PersonAuthenticationType
from org.xdi.model.custom.script.type.uma import AuthorizationPolicyType
from org.xdi.util import StringHelper, ArrayHelper
from org.xdi.oxauth.service.uma.authorization import AuthorizationContext
from org.xdi.oxauth.service import UserService
from org.xdi.util import StringHelper
from java.io import File
from java.io import IOException
from java.net import URISyntaxException
from java.net import URL
from java.io import StringReader
from java.util import HashMap, ArrayList
from org.xdi.oxauth.model.util import Base64Util
from org.xdi.oxauth.model.util import Util
from org.xdi.oxauth.service.net import HttpService

from javax.ws.rs import WebApplicationException
from javax.xml.parsers import DocumentBuilder
from javax.xml.parsers import DocumentBuilderFactory
from javax.xml.parsers import ParserConfigurationException

from org.w3c.dom import Document
from org.w3c.dom import NodeList
from org.xml.sax import InputSource
from org.xml.sax import SAXException

import java

class AuthorizationPolicy(AuthorizationPolicyType):
    def __init__(self, currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis

    def init(self, configurationAttributes):
        print "UMA Fortress Authorization. Initialization"

	if configurationAttributes.containsKey("fortress_auth_url"):
            self.fortressAuthUrl = configurationAttributes.get("fortress_auth_url").getValue2()

	if configurationAttributes.containsKey("fortress_auth_username"):
            self.fortressAuthUserName = configurationAttributes.get("fortress_auth_username").getValue2()

	if configurationAttributes.containsKey("fortress_auth_password"):
            self.fortressAuthPassword = configurationAttributes.get("fortress_auth_password").getValue2()

        print "UMA Fortress Authorization. Initialized successfully"
        return True   

    def destroy(self, configurationAttributes):
        print "UMA Fortress Authorization. Destroy"
        print "UMA Fortress Authorization. Destroyed successfully"
        return True

    def getApiVersion(self):
        return 1

    def authorize(self, authorizationContext, configurationAttributes):
        print "UMA Fortress Authorization. Attempting to authorize client"
	fortress_authorized = False
        sn = authorizationContext.getUserClaimByLdapName("sn")
        user = authorizationContext.getGrant().getUser()
        user_name = authorizationContext.getUserClaimByLdapName("uid")
        fortress_authorized = self.authorizeFortress(user_name)
        if fortress_authorized:
            print "UMA Fortress Authorization. Authorizing client"
            return True
        else:
            print "UMA Fortress Authorization. Client isn't authorized"
            return False
        print "UMA Fortress Authorization. Authorizing client"
        return True

    def getFortressResponse(self, user_name):
	szResponse = ""
        requestData = "<FortRequest>"
  	requestData = requestData + "<contextId>HOME</contextId>"
  	requestData = requestData + "<entity xsi:type=\"user\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
  	requestData = requestData + "<userId>"+user_name+"</userId>"
        requestData = requestData + "</entity>"
  	requestData = requestData + "</FortRequest>"
	try:
            headersMap = HashMap()
            headersMap.put("Accept", "text/xml")
	    headersMap.put("Content-Type", "application/xml")
            httpService = Component.getInstance(HttpService)
	    httpclient = httpService.getHttpsClient()
            authData = httpService.encodeBase64(self.fortressAuthUserName + ":" + self.fortressAuthPassword)
            resultResponse = httpService.executePost(httpclient, self.fortressAuthUrl, authData, headersMap, requestData)
            http_response = resultResponse.getHttpResponse()
            response_bytes = httpService.getResponseContent(http_response)
            szResponse = httpService.convertEntityToString(response_bytes)
            httpService.consume(http_response)

	except IOException, ie:
	    print ie
	except WebApplicationException, we:
	    print we
	finally:
	    if resultResponse is not None:
	        resultResponse.closeConnection()

       	return szResponse

    def authorizeFortress(self, user_name):
        responseData = self.getFortressResponse( user_name)
        print responseData
	dbf = DocumentBuilderFactory.newInstance()
	fortressAuthenticated = False
    	db = None
	try:
	    db = dbf.newDocumentBuilder()
            print responseData
    	    isource = InputSource()
    	    isource.setCharacterStream(StringReader(responseData))
	    try:
	        doc = db.parse(isource)
	    	errorCode = doc.getElementsByTagName("errorCode")
		valueSet = doc.getElementsByTagName("valueSet")
	    	if(errorCode.item(0).getTextContent() == "0" and self.roleExists("GLUU_ADMIN", valueSet)):
	    	    return True
                else:
		    fortressAuthenticated = False
	    except Exception, ex:
	        print "Exception"

	except ParserConfigurationException, pce:
	    print pce
    	return fortressAuthenticated

    def toStringFromInputStream(self, input, charset):
        return toString(input, 4096, charset)

    def toString(self, ipt, bufSize):
        buf = StringBuilder()
        buffer = []
        n = 0
        try:
            n = ipt.read(buffer)
        except:
            print "Exception"
        while n != -1:
            if n == 0:
                return null

            buf.append(String(buffer, 0, n))
            try:
                n = ipt.read(buffer)
            except:
                print "Exception"
        try:
            ipt.close()
        except:
            print "Exception"
        return buf.toString()

    def toString(self, ipt, bufferSize, charset):
        avail = 0
        try:
            avail = ipt.available()
        except:
            print "Exception"
        if avail > bufferSize:
            bufferSize = avail

        reader = InputStreamReader(ipt, Charset.forName("utf-8"))
        return toString(reader, bufferSize)

    def roleExists(self, roleName, valueSet):
        roleValues = ArrayList()
        print roleName
    	length = valueSet.getLength()
    	for i in range(0, length):
    	    roleValues.add(valueSet.item(i).getTextContent())
    	print roleValues.contains(roleName)
    	if roleValues.contains(roleName):
    	    return True
    	return False

    def logout(self, configurationAttributes, requestParameters):
        return True
