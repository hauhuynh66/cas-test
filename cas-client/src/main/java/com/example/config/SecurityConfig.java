package com.example.config;

import com.example.security.CustomAuthenticationSuccessHandler;
import com.example.security.CustomUserDetailService;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
@ComponentScan("com.example")
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private AuthenticationProvider authenticationProvider;
    private AuthenticationEntryPoint authenticationEntryPoint;
    private SingleSignOutFilter singleSignOutFilter;
    private LogoutFilter logoutFilter;
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;
    @Autowired
    public SecurityConfig(CasAuthenticationProvider casAuthenticationProvider, AuthenticationEntryPoint entryPoint,
                          LogoutFilter logoutFilter
            , SingleSignOutFilter singleSignOutFilter
    ) {
        this.authenticationProvider = casAuthenticationProvider;
        this.authenticationEntryPoint = entryPoint;

        this.logoutFilter = logoutFilter;
        this.singleSignOutFilter = singleSignOutFilter;

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .regexMatchers("/secured.*", "/login")
                .authenticated()
                .and()
                .authorizeRequests()
                .regexMatchers("/")
                .permitAll()
                .and()
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .logout().logoutSuccessUrl("/login?logout")
                .and()
                .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class)
                .addFilterBefore(logoutFilter, LogoutFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(Arrays.asList(authenticationProvider));
    }

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter(ServiceProperties sP) throws Exception {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setServiceProperties(sP);
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }
}
