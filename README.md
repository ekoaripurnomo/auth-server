# auth-server
Authentication Server Rest API<br />

Sebelumnya download terlebih dahulu repository berikut untuk mensuplai data authentication, sehingga web services wajib di jalankan terlebih dahulu guna mensuply database dari user sebelum menggunakan resources server yang sesungguhnya :<br />
	```link
	https://github.com/ekoaripurnomo/web-services.git
	```
	
*  Create Auth Server
*  open start.spring.io
*  Generate SpringBoot Project use dependency Security, Web, JDBC, MySQL, JPA, Thymleaf
*  extract hasil generate, import ke eclipse
* run gradle bootRun, ada error dengan database
* konfigurasi application.properties lalu set jdbc dan yang laiinya dbi
	```txt
	spring.datasource.url=jdbc:mysql://192.168.227.133:3306/latihan
	spring.datasource.username=root
	spring.datasource.password=123456
	spring.datasource.driver-class-name=com.mysql.jdbc.Driver
	server.port=10000
	spring.jpa.generate-ddl=true
	spring.jpa.show-sql=true
	spring.jpa.properties.hibernate.format_sql=true
	spring.jackson.serialization.indent_output=true
	spring.jpa.hibernate.ddl-auto=create

	project.base-dir=file:///D:/Data/Training/auth-server
	spring.thymeleaf.prefix=${project.base-dir}/src/main/resources/templates/
	spring.thymeleaf.cache=false
	spring.resources.static-locations=${project.base-dir}/src/main/resources/static/
	spring.resources.cache-period=0
	```
* run gradle bootRun sudah bisa, tetapi halaman login masih default security dependency dan security password masih di suplai dari console

* untuk mengarahkan login ke halaman yang diingin maka perlu di konfigurasi terlebih dahulu sbb:
	create package id.co.hanoman.training.authserver.config
	create file WebConfiguration.java yang di extend from WebMvcConfigurerAdapter di package 
	```java
	@Configuration
	public class WebConfiguration extends WebMvcConfigurerAdapter {

	}
	```
  add addViewController untuk halaman login di WebConfiguration.java
	```java
		@Override
		public void addViewControllers(ViewControllerRegistry registry){
			registry.addViewController("/login").setViewName("/login");
		}
	```
	sehingga code complete WebConfiguration.java menjadi :
	```java
	@Configuration
	public class WebConfiguration extends WebMvcConfigurerAdapter {
		@Override
		public void addViewControllers(ViewControllerRegistry registry){
			registry.addViewController("/login").setViewName("/login");
		}
	}
	```
	
* create SecurityConfiguration.java di package id.co.hanoman.training.authserver.config
	```java
	@Configuration
	public class SecurityConfiguration extends WebSecurityConfigurerAdapter{

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.authorizeRequests()
					.anyRequest().authenticated()
					.and()
				.formLogin()
					.loginPage("/login")
					.permitAll()
					.and()
				.logout();			
		}	
	}
	```
* run gradle bootRun error, karena file login.html belum ada tetapi sudah mau mencari file login.html di folder templates
* create login.html di folder templates
	```txt
	put css file on /static/css/
								bootstrap.min.css
								bootstrap-theme.min.css
								app.css
	put js file on /static/js/
								bootstrap.min.js
	```
	login.html :
	```html
	<!DOCTYPE html>
	<html>
	<head>
	<meta charset="UTF-8" />
	<title>Please Sign In</title>
	<link rel="stylesheet" href="css/bootstrap.min.css" />
	<link rel="stylesheet" href="css/bootstrap-theme.min.css" />
	<link rel="stylesheet" href="css/app.css" />
	</head>
	<body>

		<div class="container">

				<form class="form-signin" th:action="@{/login}" method="post">
					<div th:if="${param.error}" class="alert alert-error">Invalid username and password.</div>
			<div th:if="${param.logout}" class="alert alert-success">You have been logout.</div>
			
					<h2 class="form-signin-heading">Please sign in</h2>
					<label for="username" class="sr-only">Username</label>
					<input type="text" id="username" name="username" class="form-control" placeholder="Username" required="true" autofocus="true" />
					<label for="password" class="sr-only">Password</label>
					<input type="password" id="password" name="password" class="form-control" placeholder="Password" required="true" />
					<button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
				</form>

			</div> <!-- /container -->
		
		<script src="js/bootstrap.min.js" />
	</body>
	</html>
	```
	
* login sudah bisa dengan custom halaman login, tetapi masih menggunakan user password default generate console

* create login use user password from database add method configure(AuthenticationManagerBuilder auth)....
	```java
	@Autowired
	private DataSource dataSource;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource)
		.usersByUsernameQuery("SELECT username, password, "
			+ "enabled FROM users WHERE username=?")
		.authoritiesByUsernameQuery("SELECT u.username, a.authority "
				+ "FROM users u INNER JOIN authorities a ON u.id = a.id_user WHERE u.username=?");
	}
	```
	
* run gradle bootRun sudah bisa login dari database 

* add OAuth2 dependencies ke gradle
	compile('org.springframework.security.oauth:spring-security-oauth2')

* cretae OAuth2Configuration.java
* extends class dengan AuthorizationServerConfigurerAdapter
	```java
	@Configuration
	@EnableAuthorizationServer
	public class OAuth2Configuration extends AuthorizationServerConfigurerAdapter{

	}
	```
	
* add method configure(ClientDetailsServiceConfigurer clients) 
	```java
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
			.withClient("hanoman")
			.secret("hanoman123")
			.authorizedGrantTypes("authorization_code", "refresh_token")
			.scopes("alamat")
			.authorities("Operator")
			.accessTokenValiditySeconds(180);
	}
	```
* test gradle bootRun

* get authorization open : http://localhost:10000/oauth/authorize?client_id=hanoman&response_type=code&redirect_uri=http://example.com
* ketika muncul pilihan scope, klik approve and authorize
* after authorize will get code( pada url)  : http://example.com/?code=yXm3B5
* Get Token open RestAPI Console App
	Input :
	Target Request URI = http://localhost:10000/oauth/token
	Body Content Header Content Type = application/x-www-form-urlencoded
	Request Payload Request Parameter :
		code = 5JjRa1
		grant_type=authorization_code
		redirect_uri=http://example.com
	Authorization Header Click basic Auth :
		username=hanoman
		password=hanoman123
	
	Click POST
		
	Response, reponse body :
	```
	{
    "access_token": "80c883f4-b021-4693-ae1b-dcbd94c6355c",
    "token_type": "bearer",
    "refresh_token": "7b5fbe8f-da36-4efe-a468-3d529aab9297",
    "expires_in": 179,
    "scope": "alamat"
	}
	```
* Check token open RestAPI Console App
	Input :
	Target Request URI = http://localhost:10000/oauth/check_token
	Body Content Header Content Type = application/x-www-form-urlencoded
	Request Payload Request Parameter :
		token = b86736a4-a511-401d-880d-265a2b9a6f73
	Authorization Header Click basic Auth :
		username=hanoman
		password=hanoman123
	
	Click POST
		
	Response, reponse body :
	```
	{
    "timestamp": 1497852792781,
    "status": 403,
    "error": "Forbidden",
    "message": "Access is denied",
    "path": "/oauth/check_token"
	}
	```
*	Forbidden karena method configure(AuthorizationServerSecurityConfigurer security) belum dibuat, method ini berfungsi untuk check token
	buat dulu methode tsb :
	```java
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.checkTokenAccess("hasAuthority('Operator')");
	}
	```
	
* tes gradle bootRun again
	Result untuk check token hasil seperti dbi:
	```
	{
    "exp": 1497853322,
    "user_name": "eko",
    "authorities": ["Admin", "Operator"],
    "client_id": "hanoman",
    "scope": ["alamat"]
	}
	```
