package ca.utoronto.lms.shared.security;

import ca.utoronto.lms.shared.feign.AuthFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired private AuthFeignClient authFeignClient;
    @Autowired private TokenUtils tokenUtils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader("Authorization");
        String username = tokenUtils.getUsername(authToken);

        if ((username != null)
                && (SecurityContextHolder.getContext().getAuthentication() == null)) {
            UserDetails userDetails = authFeignClient.getUser(username);
            if (tokenUtils.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        super.doFilter(request, response, chain);
    }
}
