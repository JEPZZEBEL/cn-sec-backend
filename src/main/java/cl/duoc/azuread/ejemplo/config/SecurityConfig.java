package cl.duoc.azuread.ejemplo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String RUT = "RUT";

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
		jwtAuthConverter.setJwtGrantedAuthoritiesConverter(new RolesClaimConverter());

		http.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth.requestMatchers("/mensaje-admin").hasRole("ADMIN123")
						.requestMatchers("/mensaje").authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

		return http.build();
	}

	@Bean
	JwtDecoder jwtDecoder() {

		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(
                "https://factura2025duoc.b2clogin.com/factura2025duoc.onmicrosoft.com/discovery/v2.0/keys?p=b2c_1_apptest1")
				.build();

		jwtDecoder.setJwtValidator(token -> {
			OAuth2TokenValidatorResult defaultResult = JwtValidators.createDefault().validate(token);
			if (!token.hasClaim("extension_" + RUT)) {
				System.out.println("ERROR: Falta el claim: " + RUT);
				return OAuth2TokenValidatorResult
						.failure(new OAuth2Error("invalid_token", "Falta el claim: " + RUT, null));
			}
			return defaultResult;
		});
		return jwtDecoder;
	}
}
