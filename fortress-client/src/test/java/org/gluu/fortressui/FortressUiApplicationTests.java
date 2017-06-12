package org.gluu.fortressui;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.gluu.fortressui.controller.OxdConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=FortressUiApplication.class)
public class FortressUiApplicationTests {
	
	@Value("${oxd.host}")
	private String oxdHost ;

	@Value("${oxd.port}")
	private int oxdPort;

	@Value("${oxd.token.file}")
	private String oxdTokenContainer ;
	
	
	@Autowired
	OxdConsumer oxdConsumer;
	
	@Test
	public void testAuthUrl(){
		String oxdId = oxdConsumer.getOxdToken();
		try {
			String url = oxdConsumer.getAuthenticationUrl(oxdId);
			assertNotNull(url);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
