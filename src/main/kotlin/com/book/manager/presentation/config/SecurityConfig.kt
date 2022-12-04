package com.book.manager.presentation.config

import com.book.manager.domain.enum.RoleType
import com.book.manager.presentation.handler.BookManagerAccessDeniedHandler
import com.book.manager.presentation.handler.BookManagerAuthenticationEntryPoint
import com.book.manager.presentation.handler.BookManagerAuthenticationFailureHandler
import com.book.manager.presentation.handler.BookManagerAuthenticationSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        http.authorizeHttpRequests { authorize ->
            authorize
                .requestMatchers("/greeter/**", "/csrf_token").permitAll()
                .requestMatchers("/admin/**").hasAuthority(RoleType.ADMIN.toString())
                .anyRequest().authenticated()
        }.formLogin { login ->
            login
                .loginProcessingUrl("/login").permitAll()
                .usernameParameter("email")
                .passwordParameter("pass")
                .successHandler(BookManagerAuthenticationSuccessHandler())
                .failureHandler(BookManagerAuthenticationFailureHandler())
        }.logout {
            it.logoutUrl("/logout")
            it.logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler())
            it.invalidateHttpSession(true)
            it.deleteCookies("SESSION")
        }.exceptionHandling { ex ->
            ex
                .authenticationEntryPoint(BookManagerAuthenticationEntryPoint())
                .accessDeniedHandler(BookManagerAccessDeniedHandler())
        }.csrf {
        }.cors { cors ->
            cors.configurationSource(corsConfigurationSource())
        }
        return http.build()
    }

    private fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL)
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL)
        corsConfiguration.addAllowedOrigin("http://localhost:8081")
        corsConfiguration.allowCredentials = true

        val corsConfigurationSource = UrlBasedCorsConfigurationSource()
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)

        return corsConfigurationSource
    }
}