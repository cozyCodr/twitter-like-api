package com.bright.TwitterAnalog.service

import com.bright.TwitterAnalog.model.Role
import com.bright.TwitterAnalog.model.User
import com.bright.TwitterAnalog.repository.UserRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

import java.security.Key
import java.util.function.Function
import java.util.stream.Collectors;

@Service
class JwtService {

    private final UserRepository userRepository;

    JwtService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Value('${secret_key}')
    private String SECRET_KEY;

    // Extracts Username
    String decodeUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    def decodeUserId(String token){
        return extractClaim(token, Claims::getId);
    }


    // Extract claims from token
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Makes new token
    String  genToken(User user, String role){
        return generateToken(new HashMap<>(), user, role);
    }

    // Makes new token
    String generateToken(
            Map<String, Object> extraClaims,
            User user,
            String role
    ){

        if (Objects.equals(user.getPermissions()[0], role)){
                return getJwt(extraClaims, user);
        }
        else {
            throw new IllegalArgumentException("Specified Role does not exist")
        }
    }

    String extractRole(String token) {
        List<SimpleGrantedAuthority> rolesClaim = extractClaim(token, claims ->
                ((List<?>) claims.get("permissions")).stream()
                        .map(authority -> new SimpleGrantedAuthority(authority.toString()))
                        .collect(Collectors.toList())
        );
        if (!rolesClaim.isEmpty()) {
            return rolesClaim.get(0)
        } else {
            return null;
        }
    }

    boolean hasRole(String token, String requiredRole) {
        String userRole = extractRole(token)
        return userRole != null && (userRole == requiredRole)
    }

    private String getJwt(Map<String, Object> extraClaims, User user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .claim ("id", user.id)
                .claim("username", user.username)
                .claim("permissions",user.getPermissions())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // Validate token
    boolean isTokenValid(String token, UserDetails userDetails){
        final String username = decodeUsername(token);
        return (username == userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims (String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
