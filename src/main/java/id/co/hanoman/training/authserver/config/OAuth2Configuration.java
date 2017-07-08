package id.co.hanoman.training.authserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@EnableAuthorizationServer
public class OAuth2Configuration extends AuthorizationServerConfigurerAdapter{
	
	//ditambahkan sesudah training resources-server
	@Autowired
	@Qualifier("authenticationManagerBean")
	private AuthenticationManager authenticationManager;
	//end comment
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
			.withClient("hanoman")
				.secret("hanoman123")
				.authorizedGrantTypes("authorization_code", "refresh_token")
				.scopes("alamat")
				.authorities("Operator")
				.accessTokenValiditySeconds(180)
			//ditambahkan sesudah training resources-server	
			.and().withClient("hci")
				.secret("hci123")
				.authorizedGrantTypes("password")
				.scopes("alamat")
				.authorities("Operator")
				.accessTokenValiditySeconds(600)
			.and().withClient("hciimp")
				.secret("hciimp123")
				.authorizedGrantTypes("implicit")
				.scopes("alamat")
				.authorities("Operator")
				.redirectUris("http://localhost:7070/implicit/implicit-client.html")
				.accessTokenValiditySeconds(600)
			.autoApprove(true);
			//end comment
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.checkTokenAccess("hasAuthority('Operator')");
	}
	
	//ditambahkan sesudah training resources-server
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(authenticationManager);
	}
	//end comment
}
