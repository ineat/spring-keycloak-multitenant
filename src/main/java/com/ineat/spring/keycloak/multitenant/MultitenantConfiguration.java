package com.ineat.spring.keycloak.multitenant;

import java.util.HashSet;
import java.util.Set;

import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.keycloak.adapters.springboot.KeycloakAutoConfiguration;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class MultitenantConfiguration extends KeycloakAutoConfiguration {
	private KeycloakSpringBootProperties m_keycloakProperties;

	@Autowired
	@Override
	public void setKeycloakSpringBootProperties(final KeycloakSpringBootProperties keycloakProperties) {
		m_keycloakProperties = keycloakProperties;
		super.setKeycloakSpringBootProperties(keycloakProperties);
		HeaderBasedConfigResolver.setAdapterConfig(keycloakProperties);	}

	@Bean
	@ConditionalOnClass(name = { "org.apache.catalina.startup.Tomcat" })
	@Override
	public TomcatContextCustomizer tomcatKeycloakContextCustomizer() {
		return new MultitenantTomcatContextCustomizer(m_keycloakProperties);
    }
    
    static class KeycloakBaseTomcatContextCustomizer {

        protected final KeycloakSpringBootProperties keycloakProperties;

        public KeycloakBaseTomcatContextCustomizer(KeycloakSpringBootProperties keycloakProperties) {
            this.keycloakProperties = keycloakProperties;
        }

        public void customize(Context context) {
            LoginConfig loginConfig = new LoginConfig();
            loginConfig.setAuthMethod("KEYCLOAK");
            context.setLoginConfig(loginConfig);

            Set<String> authRoles = new HashSet<>();
            for (KeycloakSpringBootProperties.SecurityConstraint constraint : keycloakProperties.getSecurityConstraints()) {
                for (String authRole : constraint.getAuthRoles()) {
                    if (!authRoles.contains(authRole)) {
                        context.addSecurityRole(authRole);
                        authRoles.add(authRole);
                    }
                }
            }

            for (KeycloakSpringBootProperties.SecurityConstraint constraint : keycloakProperties.getSecurityConstraints()) {
                SecurityConstraint tomcatConstraint = new SecurityConstraint();
                for (String authRole : constraint.getAuthRoles()) {
                    tomcatConstraint.addAuthRole(authRole);
                    if(authRole.equals("*") || authRole.equals("**")) {
                        // For some reasons embed tomcat don't set the auth constraint on true when wildcard is used
                        tomcatConstraint.setAuthConstraint(true);
                    }
                }

                for (KeycloakSpringBootProperties.SecurityCollection collection : constraint.getSecurityCollections()) {
                    SecurityCollection tomcatSecCollection = new SecurityCollection();

                    if (collection.getName() != null) {
                        tomcatSecCollection.setName(collection.getName());
                    }
                    if (collection.getDescription() != null) {
                        tomcatSecCollection.setDescription(collection.getDescription());
                    }

                    for (String pattern : collection.getPatterns()) {
                        tomcatSecCollection.addPattern(pattern);
                    }

                    for (String method : collection.getMethods()) {
                        tomcatSecCollection.addMethod(method);
                    }

                    for (String method : collection.getOmittedMethods()) {
                        tomcatSecCollection.addOmittedMethod(method);
                    }

                    tomcatConstraint.addCollection(tomcatSecCollection);
                }

                context.addConstraint(tomcatConstraint);
            }

            context.addParameter("keycloak.config.resolver", KeycloakSpringBootConfigResolver.class.getName());
        }
    }

	static class MultitenantTomcatContextCustomizer extends KeycloakBaseTomcatContextCustomizer implements TomcatContextCustomizer {
		public MultitenantTomcatContextCustomizer(final KeycloakSpringBootProperties keycloakProperties) {
			super(keycloakProperties);
		}

		@Override
		public void customize(final Context context) {
			super.customize(context);
			final String name = "keycloak.config.resolver";
			context.removeParameter(name);
			context.addParameter(name, HeaderBasedConfigResolver.class.getName());
		}
    }

}