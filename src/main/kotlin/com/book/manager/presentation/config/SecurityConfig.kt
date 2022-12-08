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

        http.authorizeHttpRequests {
            it.requestMatchers("/greeter/**", "/csrf_token").permitAll()
            it.requestMatchers("/admin/**").hasAuthority(RoleType.ADMIN.toString())
            it.anyRequest().authenticated()
        }.formLogin {
            it.loginProcessingUrl("/login").permitAll()
            it.usernameParameter("email")
            it.passwordParameter("pass")
            it.successHandler(BookManagerAuthenticationSuccessHandler())
            it.failureHandler(BookManagerAuthenticationFailureHandler())
        }.logout {
            it.logoutUrl("/logout")
            it.logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler())
            it.invalidateHttpSession(true)
            it.deleteCookies("SESSION")
        }.exceptionHandling {
            it.authenticationEntryPoint(BookManagerAuthenticationEntryPoint())
            it.accessDeniedHandler(BookManagerAccessDeniedHandler())
        }.csrf {
        }.cors {
            it.configurationSource(corsConfigurationSource())
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