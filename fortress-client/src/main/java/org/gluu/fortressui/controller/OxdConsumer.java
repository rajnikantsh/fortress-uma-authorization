package org.gluu.fortressui.controller;

import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xdi.oxd.client.CommandClient;
import org.xdi.oxd.common.Command;
import org.xdi.oxd.common.CommandResponse;
import org.xdi.oxd.common.CommandType;
import org.xdi.oxd.common.params.GetAuthorizationUrlParams;
import org.xdi.oxd.common.params.GetTokensByCodeParams;
import org.xdi.oxd.common.params.RpGetRptParams;
import org.xdi.oxd.common.response.GetAuthorizationUrlResponse;
import org.xdi.oxd.common.response.GetTokensByCodeResponse;
import org.xdi.oxd.common.response.RpGetRptResponse;

@Controller
@RequestMapping("/")
public class OxdConsumer {

	@Value("${oxd.host}")
	private String oxdHost ;

	@Value("${oxd.port}")
	private int oxdPort;

	@Value("${oxd.token.file}")
	private String oxdTokenContainer ;

	@GetMapping("/")
	private String index() {
		return "redirect:/check";
	}
	
	@GetMapping("/rpt")
	private ResponseEntity<?> getRpt(HttpServletRequest req) {
		System.out.println("rpt call");
		
		CommandClient client = null;
		HttpSession session = req.getSession(true);
		
		try {
			client = new CommandClient(oxdHost, oxdPort);
			String oxdId = "";
			RpGetRptParams params = new RpGetRptParams();
			if(session.getAttribute("oxdId") != null){
				oxdId = (String) session.getAttribute("oxdId");
			}else {
				oxdId = getOxdToken();
				session.setAttribute("oxdId", oxdId);
			}
			
			
			params.setOxdId(oxdId);
			String recivedAAt = (String)session.getAttribute("aat");
			params.setAat(recivedAAt);
			Command command = new Command(CommandType.RP_GET_RPT).setParamsObject(params);

			CommandResponse response = client.send(command);

			RpGetRptResponse rptResponse = response.dataAsResponse(RpGetRptResponse.class);
			
			JsonResponseRpt json = new JsonResponseRpt();
			json.setOxdId(oxdId);
			json.setRptToken(rptResponse.getRpt());
			
			return new ResponseEntity<>(json, HttpStatus.OK);

		} catch (IOException ioE) {
			ioE.printStackTrace();

		} finally {
			CommandClient.closeQuietly(client);
		}
		return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
	}
	@GetMapping("/all")	
	private  String all() {
		return "all";

	}

	@GetMapping("/logout")
	private ResponseEntity<?> logout(HttpServletRequest request) {
		HttpStatus status = HttpStatus.OK;
		String message = "user logged out";
		return new ResponseEntity<>(message, status);
	}

	@GetMapping("/loginSuccess")
	private void loginSuccess(HttpServletRequest request ,HttpServletResponse response) throws ServletException, IOException {
	
		HttpSession session = request.getSession(true);
		String code = "";
		String state = "";
		String oxdId = "";
		if(session.getAttribute("oxdId") != null){
			oxdId = (String) session.getAttribute("oxdId");
		}
		if(request.getParameterMap().containsKey("code")){
			code = request.getParameter("code");
		}
		
		if(request.getParameterMap().containsKey("state")){
			state = request.getParameter("state");
		}
		
		if(!(code.isEmpty()) && !(state.isEmpty())){
			CommandClient client = null;
			try {
			    client = new CommandClient(oxdHost, oxdPort);

			    final GetTokensByCodeParams commandParams = new GetTokensByCodeParams();
			    commandParams.setOxdId(oxdId);
			    commandParams.setCode(code);
			    commandParams.setState(state);
			    final Command command = new Command(CommandType.GET_TOKENS_BY_CODE).setParamsObject(commandParams);

			    final GetTokensByCodeResponse resp = client.send(command).dataAsResponse(GetTokensByCodeResponse.class);
			    String accessToken = resp.getAccessToken();
			    System.out.println("AAT :- "+ accessToken);
			    session.setAttribute("aat", accessToken);
			} finally {
			    CommandClient.closeQuietly(client);
			}

		}
		session.setAttribute("code", request.getParameter("code"));	
		request.getRequestDispatcher("/checkAccess").forward(request, response);
	
	}
	
	@GetMapping("/check")
	private ResponseEntity<?> checkAccessTestResourcePage(HttpServletRequest request,HttpServletResponse response) throws Exception{
		HttpSession session = request.getSession(true);
		String oxdId = getOxdToken();
		String sessionOxdId = (String)session.getAttribute("oxdId");
		if(!oxdId.equals(sessionOxdId)){
			session.removeAttribute("code");
			session.setAttribute("oxdId", getOxdToken());
		}
		
		if(session.getAttribute("code") != null){
			request.getRequestDispatcher("/checkAccess").forward(request, response);
		}
		else {
			String url = getAuthenticationUrl(oxdId);
			if(url != null)
			{
				System.out.println("url --> "+url);
				response.sendRedirect(url);
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@GetMapping("/checkAccess")
	private String checkAccess() {	
		return "checkaccess";
	}
	
	
	public String getAuthenticationUrl(String oxdId) throws IOException{
		CommandClient client = null;
		String url = "";
		try {
			client = new CommandClient(oxdHost, oxdPort);
			GetAuthorizationUrlParams commandParams = new GetAuthorizationUrlParams();
			commandParams.setOxdId(oxdId);

			Command command = new Command(CommandType.GET_AUTHORIZATION_URL).setParamsObject(commandParams);

			GetAuthorizationUrlResponse resp = client.send(command).dataAsResponse(GetAuthorizationUrlResponse.class);
			url = resp.getAuthorizationUrl();
		} catch (IOException e) {
			throw e;
		}
		finally {
			CommandClient.closeQuietly(client);
		}
		return url;
	}
	
	public String getOxdToken(){
		String oxdToken = "";	
		try{
			FileReader reader = new FileReader(oxdTokenContainer);
			char[] oxdTokenChar = new char[36]; 
			reader.read(oxdTokenChar);
			oxdToken = new String(oxdTokenChar);
			reader.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return oxdToken;

	}
	
	class JsonResponseRpt{
		String rptToken;
		String oxdId;
		public String getRptToken() {
			return rptToken;
		}
		public void setRptToken(String rptToken) {
			this.rptToken = rptToken;
		}
		public String getOxdId() {
			return oxdId;
		}
		public void setOxdId(String oxdId) {
			this.oxdId = oxdId;
		}
		
	}
}
