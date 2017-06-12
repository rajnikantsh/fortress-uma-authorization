package fortress_demo;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.gluu.fortress.FortressDemoApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.oxd.client.CommandClient;
import org.xdi.oxd.common.Command;
import org.xdi.oxd.common.CommandType;
import org.xdi.oxd.common.params.RegisterSiteParams;
import org.xdi.oxd.common.response.RegisterSiteResponse;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=FortressDemoApplication.class)
public class FortressDemoApplicationTests {

	@Value("${oxd.host}")
	private String oxdHost ;

	@Value("${oxd.port}")
	private int oxdPort;

	@Value("${op.host}")
	private String opHost ;
	
	@SuppressWarnings("deprecation")
	@Test
	public void testCheckOxdId(){
		CommandClient client = null;

		try {
			client = new CommandClient(oxdHost, oxdPort);
			RegisterSiteParams commandParams = new RegisterSiteParams();
			commandParams.setOpHost(opHost);
			commandParams.setAuthorizationRedirectUri("https://rks.local:9999/loginSuccess");
			commandParams.setRedirectUris(Lists.newArrayList("https://rks.local:9999/logout"));
			commandParams.setPostLogoutRedirectUri("https://rks.local:9999/logout");
			commandParams.setClientLogoutUri(Lists.newArrayList("https://rks.local:9999/logout"));
			commandParams.setAcrValues(Lists.newArrayList("auth_ldap_server"));
			commandParams.setScope(Lists.newArrayList("openid","uma_protection", "uma_authorization"));
			commandParams.setGrantType(Lists.newArrayList(GrantType.AUTHORIZATION_CODE.getValue()));
			commandParams.setResponseTypes(Lists.newArrayList(ResponseType.CODE.getValue()));
			//commandParams.setTrustedClient(true);
			Command command = new Command(CommandType.REGISTER_SITE).setParamsObject(commandParams);
			System.out.println(command.getParams());
			RegisterSiteResponse site = client.send(command).dataAsResponse(RegisterSiteResponse.class);
			
			assertNotNull(site.getOxdId());

		} catch (IOException ioE) {

			ioE.printStackTrace();

		} finally {
			CommandClient.closeQuietly(client);
		}
	}
}
